package com.dpjha.slidingnumbersplus.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.State
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import com.dpjha.slidingnumbersplus.game.Look
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private const val W = 540f
private const val H = 960f
private const val WATER_Y = 395f // where the water starts in scene space

private class Star(val x: Float, val y: Float, val r: Float, val alpha: Float, val phase: Float, val twinkle: Boolean)
private class CloudData(val x: Float, val y: Float, val scale: Float, val speed: Int, val phase: Float)
private class Win(val x: Float, val y: Float, val color: Color, val alpha: Float, val blink: Boolean, val phase: Float)
private class Bld(val x: Float, val w: Float, val h: Float)
private class Layer(val y0: Float, val blds: List<Bld>, val wins: List<Win>, val hMax: Float)

private class SceneData(val art: CityArt, val look: Look) {
    val pal = art.palette(look)
    val night = look == Look.NIGHT
    val dusk = look == Look.SUNSET
    val stars: List<Star>
    val clouds: List<CloudData>
    val far: Layer
    val mid: Layer
    val near: Layer

    init {
        val r = Random(42)
        stars = if (look == Look.DAY) emptyList() else List(70) {
            val dim = if (night) 1f else 0.4f
            Star(r.nextFloat() * W, r.nextFloat() * (if (night) 420f else 220f), 0.5f + r.nextFloat() * 1.1f,
                (0.3f + r.nextFloat() * 0.6f) * dim, r.nextFloat(), r.nextFloat() < 0.5f)
        }
        clouds = listOf(
            CloudData(60f, 140f, 1f, 1, 0f), CloudData(300f, 90f, 0.8f, 2, 0.3f), CloudData(430f, 300f, 0.9f, 1, 0.65f),
        )
        far = layer(art.name.hashCode() + 1, 640f, 30f, 90f, 18f, 36f, windows = false)
        mid = layer(art.name.hashCode() + 2, 700f, 40f, 120f, 18f, 40f, windows = look != Look.DAY)
        near = layer(art.name.hashCode() + 3, 654f, 8f, 30f, 22f, 50f, windows = false)
    }

    private fun layer(seed: Int, y0: Float, hMin: Float, hMax: Float, wMin: Float, wMax: Float, windows: Boolean): Layer {
        val r = Random(seed)
        val blds = ArrayList<Bld>()
        val wins = ArrayList<Win>()
        var x = -40f
        val density = if (night) 0.55f else 0.28f
        val warm = listOf(Color(0xFFFFD98A), Color(0xFFFFE9B0), Color(0xFFFFC86B), Color(0xFFFFF3D0))
        while (x < 580f) {
            val w = wMin + r.nextFloat() * (wMax - wMin)
            val h = hMin + r.nextFloat() * (hMax - hMin)
            blds.add(Bld(x, w, h))
            if (windows) {
                val cols = max(1, (w / 7f).toInt())
                val rows = max(1, (h / 9f).toInt())
                for (i in 0 until cols) for (j in 0 until rows) {
                    if (r.nextFloat() < density) {
                        wins.add(Win(x + 3f + i * 7f, y0 - h + 4f + j * 9f, warm[r.nextInt(warm.size)],
                            0.55f + r.nextFloat() * 0.4f, r.nextFloat() < 0.12f, r.nextFloat()))
                    }
                }
            }
            x += w + r.nextFloat() * 3f
        }
        return Layer(y0, blds, wins, hMax)
    }
}

/** Full-screen animated city scene: layered skyline with haze and parallax, plus twinkling stars, clouds, birds, boats and blinking windows. */
@Composable
fun CityBackdrop(art: CityArt, look: Look, modifier: Modifier = Modifier) {
    val data = remember(art, look) { SceneData(art, look) }
    val transition = rememberInfiniteTransition(label = "scene")
    val clock: State<Float> = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing), RepeatMode.Restart),
        label = "clock",
    )
    Canvas(modifier.fillMaxSize()) {
        val scale = max(size.width / W, size.height / H)
        val extraX = (size.width / scale - W) / 2f
        val viewH = size.height / scale
        withTransform({
            scale(scale, scale, Offset.Zero)
            translate(extraX, 0f)
        }) {
            drawScene(data, clock.value, viewH)
        }
    }
}

private fun wave(p: Float, cycles: Int, offset: Float = 0f): Float = sin(2f * PI.toFloat() * (p * cycles + offset))
private fun frac(v: Float): Float = v - kotlin.math.floor(v)

private fun DrawScope.drawScene(d: SceneData, p: Float, viewH: Float) {
    val pal = d.pal
    // sky, extended sideways so any crop stays filled
    drawRect(
        Brush.verticalGradient(0f to pal.skyTop, 0.55f to pal.skyMid, 1f to pal.skyBottom, startY = 0f, endY = H),
        Offset(-400f, 0f), Size(W + 800f, viewH),
    )
    // sun or moon with a soft glow
    val body = when (d.look) {
        Look.NIGHT -> Triple(Offset(90f, 150f), 30f, Color(0xFFFFF6D8))
        Look.SUNSET -> Triple(Offset(150f, 330f), 70f, Color(0xFFFFD08A))
        Look.DAY -> Triple(Offset(430f, 190f), 46f, Color(0xFFFFF4CF))
    }
    drawCircle(
        Brush.radialGradient(listOf(body.third.copy(alpha = 0.4f), Color.Transparent), center = body.first, radius = body.second * 3.2f),
        body.second * 3.2f, body.first,
    )
    drawCircle(body.third, body.second, body.first)
    // stars
    for (s in d.stars) {
        val a = if (s.twinkle) s.alpha * (0.6f + 0.4f * wave(p, 25, s.phase)) else s.alpha
        drawCircle(Color.White.copy(alpha = a.coerceIn(0f, 1f)), s.r, Offset(s.x, s.y))
    }
    // clouds
    val cloudColor = when (d.look) { Look.DAY -> Color.White; Look.SUNSET -> Color(0xFFFFD0C0); Look.NIGHT -> Color(0xFF6A60A8) }
    val cloudAlpha = if (d.look == Look.DAY) 0.75f else 0.35f
    for (c in d.clouds) {
        val x = frac(c.x / 700f + p * c.speed + c.phase) * 700f - 130f
        val col = cloudColor.copy(alpha = cloudAlpha)
        drawOval(col, Offset(x - 50f * c.scale, c.y - 12f * c.scale), Size(100f * c.scale, 24f * c.scale))
        drawOval(col, Offset(x + 24f * c.scale - 28f * c.scale, c.y - 9f * c.scale - 12f * c.scale), Size(56f * c.scale, 24f * c.scale))
    }
    // birds by day and at dusk
    if (!d.night) {
        val ink = if (d.look == Look.DAY) Color(0xFF3A3550) else Color(0xFF2A1A40)
        for (i in 0 until 3) {
            val x = frac(p * 4f + i * 0.33f) * 680f - 40f
            val y = 220f + i * 34f - frac(p * 4f + i * 0.33f) * 40f
            val path = Path()
            path.moveTo(x, y)
            path.quadraticTo(x + 3.5f, y - 8f, x + 7f, y)
            path.quadraticTo(x + 10.5f, y - 8f, x + 14f, y)
            drawPath(path, ink, style = Stroke(width = 1.8f))
        }
    }
    // skyline group, moved to the scene's frame
    withTransform({
        translate(54f, -190f)
        scale(0.9f, 0.9f, Offset.Zero)
    }) {
        drawSkyline(d, p, 1f)
        if (d.art.landmark == LandmarkType.EIFFEL && d.night) {
            val sweep = 13f * wave(p, 10)
            withTransform({ rotate(sweep, Offset(270f, 270f)) }) {
                poly(Color(0xFFFFE9A8).copy(alpha = 0.14f), 270f, 270f, 190f, -300f, 350f, -300f)
            }
        }
    }
    // ground, water reflection and fade
    drawRect(pal.near, Offset(-400f, WATER_Y), Size(W + 800f, viewH - WATER_Y))
    clipRect(-400f, WATER_Y, W + 400f, WATER_Y + 300f) {
        withTransform({
            translate(0f, 2f * WATER_Y)
            scale(1f, -1f, Offset.Zero)
            translate(54f, -190f)
            scale(0.9f, 0.9f, Offset.Zero)
        }) {
            drawSkyline(d, p, if (d.night) 0.28f else 0.18f)
        }
    }
    drawRect(
        Brush.verticalGradient(
            0f to pal.mid.copy(alpha = 0.55f), 0.35f to pal.near.copy(alpha = 0.85f), 1f to pal.near,
            startY = WATER_Y, endY = max(viewH, WATER_Y + 200f),
        ),
        Offset(-400f, WATER_Y), Size(W + 800f, viewH - WATER_Y),
    )
    // boats
    if (d.look != Look.DAY || true) {
        val boatLight = listOf(Color(0xFFFFD98A), Color(0xFF9FE8FF))
        for (i in 0 until 2) {
            val x = frac(p * (2f + i) + i * 0.3f) * 660f - 60f
            val y = WATER_Y + 36f + i * 34f
            drawRoundRect(Color(0xFF0A0618).copy(alpha = 0.8f), Offset(x, y), Size(26f, 4f), androidx.compose.ui.geometry.CornerRadius(2f, 2f))
            drawCircle(boatLight[i], 2.4f, Offset(x + 6f, y - 1f))
            drawRect(boatLight[i].copy(alpha = 0.22f), Offset(x + 2f, y + 5f), Size(26f, 3f))
        }
    }
    drawRect(pal.near.copy(alpha = if (d.night) 0.18f else 0.05f), Offset(-400f, 0f), Size(W + 800f, viewH))
}

private fun lit(c: Color, night: Boolean, amt: Float): Color {
    if (!night) return c
    return Color(c.red + (1f - c.red) * amt, c.green + (1f - c.green) * amt, c.blue + (1f - c.blue) * amt, c.alpha)
}

private fun DrawScope.drawLayer(l: Layer, col: Color, haze: Color, hazeAlpha: Float, dx: Float, p: Float, am: Float, night: Boolean) {
    withTransform({ translate(dx, 0f) }) {
        drawRect(col.a(am), Offset(-60f, l.y0 - l.hMax * 0.6f), Size(W + 120f, H))
        for (b in l.blds) drawRect(col.a(am), Offset(b.x, l.y0 - b.h), Size(b.w, b.h + 200f))
        for (w in l.wins) {
            val a = if (w.blink) w.alpha * (0.1f + 0.9f * abs(wave(p, 20, w.phase))) else w.alpha
            drawRect(w.color.a(a * am), Offset(w.x, w.y), Size(3.2f, 4.2f))
        }
        if (hazeAlpha > 0f) {
            val top = l.y0 - l.hMax - 10f
            drawRect(
                Brush.verticalGradient(listOf(Color.Transparent, haze.copy(alpha = hazeAlpha * am)), startY = top, endY = l.y0 + 50f),
                Offset(-60f, top), Size(W + 120f, l.y0 + 50f - top),
            )
        }
    }
}

private fun DrawScope.drawSkyline(d: SceneData, p: Float, am: Float) {
    val pal = d.pal
    // layers sway at different speeds: farther layers move less, which gives depth
    val farDx = 5f * wave(p, 3)
    val midDx = 9f * wave(p, 3)
    val nearDx = 15f * wave(p, 3)
    drawLayer(d.far, pal.far, pal.skyBottom, 0.75f, farDx, p, am, d.night)
    drawLayer(d.mid, pal.mid, pal.skyBottom, 0.4f, midDx, p, am, d.night)
    val landmarkColor = if (d.art.landmark == LandmarkType.EIFFEL && d.night) Color(0xFFE0A63C) else lit(pal.near, d.night, 0.34f)
    drawLandmark(d.art.landmark, landmarkColor, pal.mid, Color(0xFFFFD98A), am, d.night)
    drawLayer(d.near, pal.near, pal.near, 0f, nearDx, p, am, d.night)
}
