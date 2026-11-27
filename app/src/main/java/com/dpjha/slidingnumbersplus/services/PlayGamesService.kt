package com.dpjha.slidingnumbersplus.services

import android.app.Activity
import android.content.Context
import com.dpjha.slidingnumbersplus.R
import com.dpjha.slidingnumbersplus.game.Player
import com.dpjha.slidingnumbersplus.game.Rules
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange

private const val SAVE_NAME = "progress"

private val achievementIds = mapOf(
    "first_win" to R.string.gpgs_achievement_first_win,
    "wins_10" to R.string.gpgs_achievement_wins_10,
    "wins_50" to R.string.gpgs_achievement_wins_50,
    "three_star_1" to R.string.gpgs_achievement_three_star_1,
    "three_star_25" to R.string.gpgs_achievement_three_star_25,
    "pure_5" to R.string.gpgs_achievement_pure_5,
    "london" to R.string.gpgs_achievement_london,
    "paris" to R.string.gpgs_achievement_paris,
    "new_york" to R.string.gpgs_achievement_new_york,
    "tour_1" to R.string.gpgs_achievement_tour_1,
    "streak_3" to R.string.gpgs_achievement_streak_3,
    "streak_7" to R.string.gpgs_achievement_streak_7,
    "streak_30" to R.string.gpgs_achievement_streak_30,
)

/**
 * Leaderboard, achievements and cloud save. Everything is optional: with no ids in
 * res/values/play_games_ids.xml this does nothing at all.
 */
object PlayGamesService {
    private var ready = false

    fun configured(context: Context): Boolean = context.getString(R.string.games_app_id).isNotBlank()

    fun start(context: Context) {
        if (!configured(context)) return
        PlayGamesSdk.initialize(context)
        ready = true
    }

    private fun whenSignedIn(activity: Activity, block: () -> Unit) {
        if (!ready) return
        PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) block()
        }
    }

    /** Opens the leaderboard, or the achievements list, signing in first if needed. */
    fun open(activity: Activity, onUnavailable: () -> Unit) {
        if (!ready) return onUnavailable()
        val client = PlayGames.getGamesSignInClient(activity)
        client.signIn().addOnCompleteListener { task ->
            if (!task.isSuccessful || !task.result.isAuthenticated) return@addOnCompleteListener onUnavailable()
            val leaderboard = activity.getString(R.string.gpgs_leaderboard_stars)
            val intent = if (leaderboard.isNotBlank()) {
                PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(leaderboard)
            } else {
                PlayGames.getAchievementsClient(activity).achievementsIntent
            }
            intent.addOnSuccessListener { @Suppress("DEPRECATION") activity.startActivityForResult(it, 9001) }
                .addOnFailureListener { onUnavailable() }
        }
    }

    /** Sends total stars to the leaderboard and unlocks earned achievements. */
    fun report(activity: Activity, player: Player) = whenSignedIn(activity) {
        val leaderboard = activity.getString(R.string.gpgs_leaderboard_stars)
        if (leaderboard.isNotBlank()) PlayGames.getLeaderboardsClient(activity).submitScore(leaderboard, player.totalStars.toLong())
        val client = PlayGames.getAchievementsClient(activity)
        for (achievement in Rules.achievements) {
            if (achievement.progress(player) < achievement.target) continue
            val res = achievementIds[achievement.id] ?: continue
            val id = activity.getString(res)
            if (id.isNotBlank()) client.unlock(id)
        }
    }

    /**
     * Reads the cloud save, hands it to [merge] and writes back whatever [current] then returns.
     */
    fun sync(activity: Activity, current: () -> Player, merge: (Player) -> Unit) = whenSignedIn(activity) {
        val client = PlayGames.getSnapshotsClient(activity)
        client.open(SAVE_NAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).addOnSuccessListener { result ->
            val snapshot = result.data ?: return@addOnSuccessListener
            runCatching {
                val text = snapshot.snapshotContents.readFully().toString(Charsets.UTF_8)
                val map = text.lines().filter { '=' in it }.associate { it.substringBefore('=') to it.substringAfter('=') }
                if (map.containsKey("v")) merge(Player.decode(map))
                val out = current().encode().entries.joinToString("\n") { "${it.key}=${it.value}" }
                snapshot.snapshotContents.writeBytes(out.toByteArray(Charsets.UTF_8))
                client.commitAndClose(snapshot, SnapshotMetadataChange.EMPTY_CHANGE)
            }.onFailure { client.discardAndClose(snapshot) }
        }
    }
}
