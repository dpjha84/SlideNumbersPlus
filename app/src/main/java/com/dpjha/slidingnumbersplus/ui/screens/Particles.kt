package com.dpjha.slidingnumbersplus.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import com.dpjha.slidingnumbersplus.audio.GameAudio
import com.dpjha.slidingnumbersplus.audio.Sfx
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Positions are fractions of the screen; speeds are dp per second. */
private class Spark(
    var x: Float, var y: Float, var vx: Float, var vy: Float, val gravity: Float,
    val color: Color, val size: Float, val life: Float, val paper: Boolean, val turn: Float,
) {
    var age = 0f
    var px = Float.NaN
    var py = Float.NaN
}

/** Fireworks and confetti over the finished board. */
@Composable
fun Celebration(audio: GameAudio) {
    val sparks = remember { ArrayList<Spark>() }
    var frame by remember { mutableLongStateOf(0L) }
    val size = remember { floatArrayOf(1f, 1f, 1f) }

    fun firework() {
        val w = size[0]
        val h = size[1]
        val x = w * (0.15f + Random.nextFloat() * 0.7f)
        val y = h * (0.08f + Random.nextFloat() * 0.22f)
        val color = listOf(Color(0xFFFFD36A), Color(0xFFFF7BB0), Color(0xFF6FF2E0), Color(0xFFB9A0FF)).random()
        fun burst(c: Color, n: Int, speed: Float, gravity: Float, thick: Float, life: Float) {
            repeat(n) {
                val a = Random.nextFloat() * 2 * PI.toFloat()
                val v = speed * (0.35f + Random.nextFloat() * 0.65f) * size[2]
                sparks.add(Spark(x, y, cos(a) * v, sin(a) * v, gravity * size[2], c, thick * (0.6f + Random.nextFloat() * 0.8f) * size[2], life * (0.7f + Random.nextFloat() * 0.6f), false, 0f))
            }
        }
        burst(color, 46, 190f, 110f, 2.6f, 1.2f)
        burst(Color.White, 14, 90f, 90f, 1.8f, 0.7f)
        audio.play(Sfx.POP, 0.9f + Random.nextFloat() * 0.25f)
    }

    fun confetti() {
        val colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFC54D), Color(0xFF43E6D2), Color(0xFF9B7BFF), Color(0xFFFF9AD0), Color(0xFFFFF4DC))
        repeat(110) { i ->
            val left = i % 2 == 1
            sparks.add(
                Spark(
                    if (left) 0f else size[0], size[1] * 0.62f,
                    (if (left) 1 else -1) * (60 + Random.nextFloat() * 300) * size[2], (-250 - Random.nextFloat() * 430) * size[2],
                    520f * size[2], colors[i % colors.size], (4 + Random.nextFloat() * 4) * size[2], 2.4f, true, Random.nextFloat() * 6,
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(520)
        confetti()
        firework()
        delay(380)
        repeat(9) {
            firework()
            delay(420)
        }
    }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) withFrameNanos { now ->
            val dt = minOf((now - last) / 1e9f, 0.05f)
            last = now
            val iterator = sparks.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.age += dt
                if (p.age >= p.life) {
                    iterator.remove()
                    continue
                }
                p.px = p.x
                p.py = p.y
                p.vy += p.gravity * dt
                if (p.paper) {
                    p.vx *= 0.985f
                    p.vy = minOf(p.vy, 170f * size[2])
                } else {
                    p.vx *= 0.97f
                    p.vy *= 0.98f
                }
                p.x += p.vx * dt
                p.y += p.vy * dt
            }
            frame = now
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        size[0] = this.size.width
        size[1] = this.size.height
        size[2] = density
        if (frame < 0) return@Canvas
        for (p in sparks) {
            val alpha = ((1 - p.age / p.life) * 1.8f).coerceIn(0f, 1f)
            if (p.paper) {
                val flip = abs(cos(p.age * 8 + p.turn))
                rotate((p.turn + p.age * 6) * 57.3f, Offset(p.x, p.y)) {
                    drawRect(p.color, Offset(p.x - p.size / 2, p.y - p.size * 0.3f * flip), Size(p.size, p.size * 0.6f * flip + 1), alpha = alpha)
                }
            } else if (!p.px.isNaN()) {
                drawLine(p.color, Offset(p.x - p.vx * 0.025f, p.y - p.vy * 0.025f), Offset(p.x, p.y), p.size, StrokeCap.Round, alpha = alpha)
            }
        }
    }
}
