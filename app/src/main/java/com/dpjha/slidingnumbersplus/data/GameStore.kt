package com.dpjha.slidingnumbersplus.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dpjha.slidingnumbersplus.game.Player
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.File

private val Context.legacyProgress by preferencesDataStore(name = "game_progress")

/** Saves the player in SharedPreferences, and carries progress over from the older builds. */
class GameStore(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(): Player {
        val saved = prefs.all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap()
        if (saved.containsKey("v")) return Player.decode(saved)
        val migrated = runCatching { fromNativeBuild() }.getOrNull()
            ?: runCatching { fromMauiBuild() }.getOrNull()
            ?: Player()
        save(migrated)
        return migrated
    }

    fun save(player: Player) {
        val editor = prefs.edit()
        player.encode().forEach { (key, value) -> editor.putString(key, value) }
        editor.apply()
    }

    /** The 2.x native build kept a JSON string in DataStore. */
    private fun fromNativeBuild(): Player? {
        if (!File(app.filesDir, "datastore/game_progress.preferences_pb").exists()) return null
        val text = runBlocking { app.legacyProgress.data.first()[stringPreferencesKey("progress_json")] } ?: return null
        val json = JSONObject(text)
        return Player.fromLegacy(json.optInt("level", 1), json.optInt("maxLevel", 1), json.optBoolean("soundOn", true))
    }

    /** The first release (.NET MAUI) kept a JSON string under snp_v1 in a shared preferences file. */
    private fun fromMauiBuild(): Player? {
        val files = File(app.applicationInfo.dataDir, "shared_prefs").listFiles() ?: return null
        for (file in files) {
            val text = app.getSharedPreferences(file.nameWithoutExtension, Context.MODE_PRIVATE).all["snp_v1"] as? String ?: continue
            val json = JSONObject(text)
            return Player.fromLegacy(json.optInt("Level", 1), json.optInt("MaxLevel", 1), json.optBoolean("SoundOn", true))
        }
        return null
    }

    private companion object {
        const val FILE = "snp3"
    }
}
