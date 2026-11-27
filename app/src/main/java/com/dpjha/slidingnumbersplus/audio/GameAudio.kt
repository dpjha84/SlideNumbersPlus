package com.dpjha.slidingnumbersplus.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import com.dpjha.slidingnumbersplus.R

enum class Sfx(val res: Int, val volume: Float) {
    SLIDE(R.raw.sfx_slide, 0.14f),
    PLACE(R.raw.sfx_place, 0.12f),
    BLOCK(R.raw.sfx_block, 0.13f),
    CLICK(R.raw.sfx_click, 0.12f),
    STAR1(R.raw.sfx_star1, 0.15f),
    STAR2(R.raw.sfx_star2, 0.15f),
    STAR3(R.raw.sfx_star3, 0.15f),
    WIN(R.raw.sfx_win, 0.17f),
    STAMP(R.raw.sfx_stamp, 0.17f),
    COIN(R.raw.sfx_coin, 0.12f),
    POP(R.raw.sfx_pop, 0.12f),
}

private const val MUSIC_VOLUME = 0.23f
private const val RAIN_VOLUME = 0.04f

/** Music, ambience, effects and vibration. All volumes are deliberately gentle. */
class GameAudio(context: Context) {
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val sounds = Sfx.values().associateWith { pool.load(app, it.res, 1) }
    @Suppress("DEPRECATION")
    private val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private var music: Loop? = null
    private var rain: Loop? = null
    private var wantedMusic = 0
    private var wantedRain = false
    private var foreground = false
    private var duckUntil = 0L

    private val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    /** Another app was already playing when we were about to start. */
    private var othersPlaying = false
    /** Set when the player switches music on by hand: they want ours, whatever else is playing. */
    private var insisted = false

    var soundOn = true
    var musicOn = true
        set(value) {
            if (value && !field) insisted = true
            field = value
            refresh()
        }
    /** Music and rain belong to the puzzle screen; every other screen is quiet. */
    var inPuzzle = false
        set(value) {
            field = value
            refresh()
        }
    var hapticsOn = true

    /** One looping track with a volume fade. */
    private inner class Loop(val res: Int, private val target: Float) {
        private val player: MediaPlayer? = MediaPlayer.create(app, res)?.apply {
            isLooping = true
            setVolume(0f, 0f)
        }
        private var level = 0f
        private var goal = 0f
        private var released = false
        private val step = object : Runnable {
            override fun run() {
                if (released) return
                level = if (level < goal) minOf(goal, level + target / 42f) else maxOf(goal, level - target / 14f)
                player?.setVolume(level, level)
                if (level != goal) handler.postDelayed(this, 60)
                else if (goal == 0f) release()
            }
        }

        val isPlaying: Boolean get() = !released && player?.isPlaying == true

        fun play() {
            player?.start()
            fadeTo(target)
        }

        fun pause() {
            if (!released) player?.pause()
        }

        fun fadeTo(volume: Float) {
            goal = volume
            handler.removeCallbacks(step)
            handler.post(step)
        }

        fun duck(down: Boolean) = fadeTo(if (down) target * 0.3f else target)

        fun stop() = fadeTo(0f)

        fun release() {
            if (released) return
            released = true
            handler.removeCallbacks(step)
            player?.release()
        }
    }

    private fun trackFor(city: Int): Int = when (city) {
        0 -> R.raw.music_london
        1 -> R.raw.music_paris
        3 -> R.raw.music_tokyo
        else -> intArrayOf(R.raw.music_london, R.raw.music_paris, R.raw.music_tokyo)[
            // New York shares the waltz; the rest rotate.
            if (city == 2) 2 else city % 3,
        ]
    }

    /** Picks the music for a city and whether rain falls over it. */
    fun setScene(city: Int, raining: Boolean) {
        wantedMusic = trackFor(city)
        wantedRain = raining
        refresh()
    }

    private fun refresh() {
        if (!foreground) return
        // only meaningful while we are silent ourselves, or we would hear our own track
        if (music?.isPlaying != true && rain?.isPlaying != true) othersPlaying = audioManager?.isMusicActive == true
        val allowed = musicOn && inPuzzle && (insisted || !othersPlaying)
        val wanted = if (allowed) wantedMusic else 0
        if (music?.res != wanted) {
            music?.stop()
            music = if (wanted != 0) Loop(wanted, MUSIC_VOLUME).also { it.play() } else null
        } else {
            music?.play()
        }
        val wantRain = allowed && wantedRain
        if (!wantRain) {
            rain?.stop()
            rain = null
        } else {
            (rain ?: Loop(R.raw.rain, RAIN_VOLUME).also { rain = it }).play()
        }
    }

    fun resume() {
        foreground = true
        pool.autoResume()
        refresh()
    }

    fun pause() {
        foreground = false
        pool.autoPause()
        music?.pause()
        rain?.pause()
    }

    fun release() {
        pause()
        music?.release()
        rain?.release()
        pool.release()
    }

    /** Lowers the music for a moment so a fanfare can be heard. */
    fun duck(millis: Long) {
        val loop = music ?: return
        loop.duck(true)
        duckUntil = System.currentTimeMillis() + millis
        handler.postDelayed({ if (System.currentTimeMillis() >= duckUntil && music === loop) loop.duck(false) }, millis + 20)
    }

    fun play(sfx: Sfx, rate: Float = 1f) {
        if (!soundOn) return
        val id = sounds[sfx] ?: return
        pool.play(id, sfx.volume, sfx.volume, 1, 0, rate)
    }

    fun buzz(millis: Long, strength: Int = 90) {
        if (!hapticsOn) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(millis, strength))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(millis)
        }
    }
}
