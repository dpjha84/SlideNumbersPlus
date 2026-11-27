package com.dpjha.slidingnumbersplus.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Seconds since this composable appeared, updated at most every [stepMillis]. Frozen when not [running]. */
@Composable
fun rememberSceneClock(stepMillis: Int, running: Boolean): () -> Float {
    val time = remember { mutableFloatStateOf(3f) }
    LaunchedEffect(stepMillis, running) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val passed = now - last
            if (passed >= stepMillis * 1_000_000L - 2_000_000L) {
                time.floatValue += min(passed / 1e9f, 0.25f)
                last = now
            }
        }
    }
    return { time.floatValue }
}

/** The living city behind every screen. */
@Composable
fun Backdrop(palette: Palette, playing: Boolean, calm: Boolean, celebrate: Boolean, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val unit = min(w.toFloat(), h * 0.62f) / SCENE_W
        val offsetX = (w - SCENE_W * unit) / 2f / unit
        val total = h / unit
        val horizon = max(total * 0.545f, min(total * 0.72f, 390f)).roundToInt().toFloat()
        val still = remember(palette.id, w, h) { renderBitmap(w, h, unit, offsetX) { drawStill(palette, horizon, total) } }
        val clock = rememberSceneClock(if (playing && !celebrate) 110 else 33, !calm)
        Canvas(Modifier.fillMaxSize()) {
            val t = clock()
            drawImage(still)
            val hz = (horizon * unit).roundToInt()
            drawReflection(still, hz, (230 * unit).roundToInt(), t)
            scale(unit, unit, Offset.Zero) {
                translate(offsetX, 0f) { drawLive(palette, horizon, total, t, celebrate) }
            }
            // quieten the scene behind the board
            if (playing && !celebrate) drawRect(Color(0xFF0C0E22), alpha = if (palette.night) 0.34f else 0.16f)
        }
    }
}
