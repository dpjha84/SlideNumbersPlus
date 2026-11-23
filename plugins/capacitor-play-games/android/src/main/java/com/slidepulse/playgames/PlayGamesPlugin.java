package com.slidepulse.playgames;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;

@CapacitorPlugin(name = "PlayGames")
public class PlayGamesPlugin extends Plugin {

    @Override
    public void load() {
        PlayGamesSdk.initialize(getContext());
    }

    @PluginMethod
    public void signIn(PluginCall call) {
        PlayGames.getGamesSignInClient(getActivity())
            .signIn()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                    fetchPlayer(call);
                } else {
                    JSObject res = new JSObject();
                    res.put("playerId", "local-android");
                    res.put("displayName", "SlidePulsePlayer");
                    res.put("isSignedIn", false);
                    call.resolve(res);
                }
            });
    }

    @PluginMethod
    public void getPlayerProfile(PluginCall call) {
        PlayGames.getGamesSignInClient(getActivity())
            .isAuthenticated()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                    fetchPlayer(call);
                } else {
                    JSObject res = new JSObject();
                    res.put("playerId", "local-android");
                    res.put("displayName", "SlidePulsePlayer");
                    res.put("isSignedIn", false);
                    call.resolve(res);
                }
            });
    }

    private void fetchPlayer(PluginCall call) {
        PlayGames.getPlayersClient(getActivity())
            .getCurrentPlayer()
            .addOnSuccessListener(player -> {
                JSObject res = new JSObject();
                res.put("playerId", player.getPlayerId());
                res.put("displayName", player.getDisplayName());
                res.put("isSignedIn", true);
                call.resolve(res);
            })
            .addOnFailureListener(e -> {
                JSObject res = new JSObject();
                res.put("playerId", "local-android");
                res.put("displayName", "SlidePulsePlayer");
                res.put("isSignedIn", false);
                call.resolve(res);
            });
    }

    @PluginMethod
    public void submitScore(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        Integer score = call.getInt("score", 0);
        if (leaderboardId != null && !leaderboardId.isEmpty()) {
            PlayGames.getLeaderboardsClient(getActivity()).submitScore(leaderboardId, score);
        }
        call.resolve();
    }

    @PluginMethod
    public void showLeaderboard(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        if (leaderboardId != null && !leaderboardId.isEmpty()) {
            PlayGames.getLeaderboardsClient(getActivity())
                .getLeaderboardIntent(leaderboardId)
                .addOnSuccessListener(intent -> getActivity().startActivityForResult(intent, 9001));
        }
        call.resolve();
    }

    @PluginMethod
    public void unlockAchievement(PluginCall call) {
        String achievementId = call.getString("achievementId");
        if (achievementId != null && !achievementId.isEmpty()) {
            PlayGames.getAchievementsClient(getActivity()).unlock(achievementId);
        }
        call.resolve();
    }

    @PluginMethod
    public void showAchievements(PluginCall call) {
        PlayGames.getAchievementsClient(getActivity())
            .getAchievementsIntent()
            .addOnSuccessListener(intent -> getActivity().startActivityForResult(intent, 9002));
        call.resolve();
    }
}
