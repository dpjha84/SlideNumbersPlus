package com.slidepulse.playgames;

import android.content.Intent;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.Player;
import com.google.android.gms.tasks.Task;

@CapacitorPlugin(name = "PlayGames")
public class PlayGamesPlugin extends Plugin {

    @Override
    public void load() {
        try {
            PlayGamesSdk.initialize(getContext());
        } catch (Throwable ignored) {
            // Non-fatal when Play Services are unavailable on a test emulator
        }
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        try {
            PlayGamesSdk.initialize(getContext());
        } catch (Throwable ignored) {
            // Safe no-op
        }
        call.resolve();
    }

    @PluginMethod
    public void signIn(PluginCall call) {
        boolean silent = call.getBoolean("silent", true);
        if (getActivity() == null) {
            resolveSignedOut(call, "unavailable");
            return;
        }
        var client = PlayGames.getGamesSignInClient(getActivity());
        Task<AuthenticationResult> task = silent ? client.isAuthenticated() : client.signIn();
        task.addOnCompleteListener(authTask -> {
            if (!authTask.isSuccessful() || authTask.getResult() == null || !authTask.getResult().isAuthenticated()) {
                resolveSignedOut(call, silent ? "not_authenticated" : "cancelled");
                return;
            }
            PlayGames.getPlayersClient(getActivity())
                .getCurrentPlayer()
                .addOnSuccessListener(player -> {
                    JSObject res = new JSObject();
                    res.put("signedIn", true);
                    res.put("player", toPlayerJson(player));
                    call.resolve(res);
                })
                .addOnFailureListener(e -> resolveSignedOut(call, "unavailable"));
        });
    }

    @PluginMethod
    public void isSignedIn(PluginCall call) {
        if (getActivity() == null) {
            JSObject res = new JSObject();
            res.put("signedIn", false);
            call.resolve(res);
            return;
        }
        PlayGames.getGamesSignInClient(getActivity())
            .isAuthenticated()
            .addOnCompleteListener(task -> {
                boolean ok = task.isSuccessful()
                    && task.getResult() != null
                    && task.getResult().isAuthenticated();
                JSObject res = new JSObject();
                res.put("signedIn", ok);
                call.resolve(res);
            });
    }

    @PluginMethod
    public void getPlayer(PluginCall call) {
        if (getActivity() == null) {
            call.reject("Activity unavailable");
            return;
        }
        PlayGames.getPlayersClient(getActivity())
            .getCurrentPlayer()
            .addOnSuccessListener(player -> call.resolve(toPlayerJson(player)))
            .addOnFailureListener(e -> call.reject("Not signed in", e));
    }

    @PluginMethod
    public void unlockAchievement(PluginCall call) {
        String id = call.getString("id");
        if (getActivity() != null && id != null && !id.isEmpty()) {
            PlayGames.getAchievementsClient(getActivity()).unlock(id);
        }
        call.resolve();
    }

    @PluginMethod
    public void incrementAchievement(PluginCall call) {
        String id = call.getString("id");
        Integer steps = call.getInt("steps", 1);
        if (getActivity() != null && id != null && !id.isEmpty() && steps != null && steps > 0) {
            PlayGames.getAchievementsClient(getActivity()).increment(id, steps);
        }
        call.resolve();
    }

    @PluginMethod
    public void showAchievements(PluginCall call) {
        if (getActivity() == null) {
            call.reject("Activity unavailable");
            return;
        }
        PlayGames.getAchievementsClient(getActivity())
            .getAchievementsIntent()
            .addOnSuccessListener(intent -> startActivityForResult(call, intent, "onUiClosed"))
            .addOnFailureListener(e -> call.reject("Failed to open Play Games Achievements", e));
    }

    @PluginMethod
    public void submitScore(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        Integer score = call.getInt("score", 0);
        if (getActivity() != null && leaderboardId != null && !leaderboardId.isEmpty() && score != null) {
            PlayGames.getLeaderboardsClient(getActivity()).submitScore(leaderboardId, score);
        }
        call.resolve();
    }

    @PluginMethod
    public void showLeaderboard(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        if (getActivity() == null || leaderboardId == null || leaderboardId.isEmpty()) {
            call.reject("Invalid leaderboardId");
            return;
        }
        PlayGames.getLeaderboardsClient(getActivity())
            .getLeaderboardIntent(leaderboardId)
            .addOnSuccessListener(intent -> startActivityForResult(call, intent, "onUiClosed"))
            .addOnFailureListener(e -> call.reject("Failed to open Play Games Leaderboard", e));
    }

    @PluginMethod
    public void showAllLeaderboards(PluginCall call) {
        if (getActivity() == null) {
            call.reject("Activity unavailable");
            return;
        }
        PlayGames.getLeaderboardsClient(getActivity())
            .getAllLeaderboardsIntent()
            .addOnSuccessListener(intent -> startActivityForResult(call, intent, "onUiClosed"))
            .addOnFailureListener(e -> call.reject("Failed to open Play Games Leaderboards", e));
    }

    @ActivityCallback
    private void onUiClosed(PluginCall call, ActivityResult result) {
        if (call != null) {
            call.resolve();
        }
    }

    private static JSObject toPlayerJson(Player player) {
        JSObject obj = new JSObject();
        obj.put("playerId", player.getPlayerId());
        obj.put("displayName", player.getDisplayName());
        if (player.getIconImageUri() != null) {
            obj.put("avatarUrl", player.getIconImageUri().toString());
        }
        return obj;
    }

    private static void resolveSignedOut(PluginCall call, String reason) {
        JSObject res = new JSObject();
        res.put("signedIn", false);
        res.put("reason", reason);
        call.resolve(res);
    }
}
