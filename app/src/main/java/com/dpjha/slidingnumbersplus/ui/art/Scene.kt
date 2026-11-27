package com.dpjha.slidingnumbersplus.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.dpjha.slidingnumbersplus.game.Mood
import com.dpjha.slidingnumbersplus.game.TimeOfDay
import com.dpjha.slidingnumbersplus.game.Weather
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** Scene space is 400 units wide; [horizon] is where the water starts. */
const val SCENE_W = 400f

private fun hex(v: Long) = Color(0xFF000000 or v)

/** Colours for one city under one sky. */
class Palette(val city: Int, val mood: Mood) {
    val time = mood.time
    val weather = mood.weather
    val night = time == TimeOfDay.NIGHT
    val sky: List<Color>
    val far: Color
    val mid: Color
    val near: Color
    val lm: Color
    val rim: Color
    val water: List<Color>
    val lamp: Color
    val windows: Float
    val stars: Int
    val tree: List<Color>
    val sun: Boolean
    val moon: Boolean

    init {
        var s: List<Color>
        var f: Color
        var m: Color
        var w: List<Color>
        var win: Float
        var st: Int
        var su = true
        var mo = false
        when (time) {
            TimeOfDay.SUNRISE -> {
                s = listOf(hex(0x7f9fd6), hex(0xc3b6e0), hex(0xf6c6c0), hex(0xffe3b8)); f = hex(0xc2a9c4); m = hex(0x9c82a8)
                near = hex(0x5d4d74); rim = hex(0xfff0dc); w = listOf(hex(0xb9a3c2), hex(0x4b4a73)); win = 0.05f; st = 0; lamp = hex(0xf6d9a6)
            }
            TimeOfDay.DAY -> {
                s = listOf(hex(0x4f9fe6), hex(0x7fc0f0), hex(0xb4defa), hex(0xe8f6ff)); f = hex(0xa9c9e4); m = hex(0x86a9c9)
                near = hex(0x4f7596); rim = hex(0xffffff); w = listOf(hex(0x8cc2e4), hex(0x316489)); win = 0f; st = 0; lamp = hex(0xd5e6f2)
            }
            TimeOfDay.SUNSET -> {
                s = listOf(hex(0x4a4a8c), hex(0x9a649a), hex(0xe98a7c), hex(0xffc98a)); f = hex(0xa96f86); m = hex(0x7c5078)
                near = hex(0x402a52); rim = hex(0xffd9a8); w = listOf(hex(0xa96a7c), hex(0x2a2046)); win = 0.14f; st = 12; lamp = hex(0xffdc92)
            }
            TimeOfDay.NIGHT -> {
                s = listOf(hex(0x0f1736), hex(0x22305a), hex(0x45567f), hex(0x8b8fab)); f = hex(0x404a74); m = hex(0x2b3458)
                near = hex(0x161c38); rim = hex(0xb8c6e8); w = listOf(hex(0x303c64), hex(0x0d1229)); win = 0.24f; st = 90; lamp = hex(0xffdc92)
                su = false; mo = true
            }
        }
        lm = when (time) {
            TimeOfDay.SUNRISE -> hex(0x7a5f86); TimeOfDay.DAY -> hex(0x5d6f92)
            TimeOfDay.SUNSET -> hex(0x553565); TimeOfDay.NIGHT -> hex(0x4b5788)
        }
        val grey: Pair<Color, Float>? = when (weather) {
            Weather.RAIN -> (if (night) hex(0x222a3d) else hex(0x8d9aae)) to 0.58f
            Weather.SNOW -> (if (night) hex(0x48546f) else hex(0xcdd9e8)) to 0.5f
            Weather.AUTUMN -> hex(0xd9a06a) to 0.14f
            Weather.SPRING -> hex(0xffd3e4) to 0.14f
            Weather.CLEAR -> null
        }
        if (grey != null) {
            val (g, a) = grey
            s = s.map { lerp(it, g, a) }
            f = lerp(f, g, a * 0.7f)
            m = lerp(m, g, a * 0.4f)
            w = w.map { lerp(it, g, a * 0.6f) }
        }
        if (weather == Weather.RAIN || weather == Weather.SNOW) {
            su = false; mo = false; st = 0; win = max(win, 0.09f)
        }
        var t = when (weather) {
            Weather.CLEAR -> if (night || time == TimeOfDay.SUNSET) listOf(near, near) else listOf(hex(0x4f8a64), hex(0x3d7258))
            Weather.RAIN -> listOf(hex(0x3d5a55), hex(0x33484f))
            Weather.SNOW -> listOf(hex(0xe8f0fa), hex(0xbccade))
            Weather.AUTUMN -> listOf(hex(0xd9772e), hex(0xb5432c))
            Weather.SPRING -> listOf(hex(0xffb3d1), hex(0xff90bb))
        }
        if (night && weather != Weather.CLEAR) t = t.map { lerp(it, hex(0x161c38), 0.45f) }
        sky = s; far = f; mid = m; water = w; windows = win; stars = st; tree = t; sun = su; moon = mo
    }

    val id: String get() = "$city/$time/$weather"
}

private fun DrawScope.rect(c: Color, x: Float, y: Float, w: Float, h: Float, a: Float = 1f) =
    drawRect(c, Offset(x, y), Size(w, h), alpha = a)

private fun DrawScope.shape(c: Color, vararg p: Float) {
    val path = Path()
    path.moveTo(p[0], p[1])
    var i = 2
    while (i < p.size) {
        path.lineTo(p[i], p[i + 1]); i += 2
    }
    path.close()
    drawPath(path, c)
}

private fun DrawScope.disc(c: Color, x: Float, y: Float, r: Float, a: Float = 1f) = drawCircle(c, r, Offset(x, y), alpha = a)

private fun DrawScope.glow(x: Float, y: Float, r: Float, c: Color, a: Float) =
    drawCircle(Brush.radialGradient(listOf(c.copy(alpha = a), c.copy(alpha = 0f)), Offset(x, y), r), r, Offset(x, y))

private fun DrawScope.oval(c: Color, cx: Float, cy: Float, rx: Float, ry: Float, a: Float = 1f) =
    drawOval(c, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2), alpha = a)

private fun DrawScope.arch(c: Color, cx: Float, cy: Float, r: Float) =
    drawArc(c, 180f, 180f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2))

private fun DrawScope.quad(c: Color, width: Float, x0: Float, y0: Float, cx: Float, cy: Float, x1: Float, y1: Float) {
    val path = Path()
    path.moveTo(x0, y0)
    path.quadraticTo(cx, cy, x1, y1)
    drawPath(path, c, style = Stroke(width))
}

private fun DrawScope.skyline(p: Palette, r: Random, base: Float, lo: Float, hi: Float, color: Color, lit: Boolean) {
    var x = -30f
    while (x < SCENE_W + 30f) {
        val w = 14f + r.nextFloat() * 30f
        val h = lo + r.nextFloat() * (hi - lo)
        val y = base - h
        rect(color, x, y, w, h + 4f)
        val t = r.nextFloat()
        if (t < 0.18f) shape(color, x, y, x + w / 2, y - 10f - r.nextFloat() * 10f, x + w, y)
        else if (t < 0.32f) {
            rect(color, x + w * 0.25f, y - 7f, w * 0.5f, 7f); rect(color, x + w * 0.48f, y - 18f, 1.2f, 12f)
        } else if (t < 0.42f) arch(color, x + w / 2, y, w * 0.32f)
        rect(p.rim, x, y, 1f, h, 0.25f)
        if (p.weather == Weather.SNOW && t >= 0.42f) rect(hex(0xf2f7ff), x - 0.5f, y - 2f, w + 1f, 3f)
        if (lit) {
            var i = x + 3f
            while (i < x + w - 3f) {
                var j = y + 6f
                while (j < base - 4f) {
                    if (p.windows > 0f) {
                        if (r.nextFloat() < p.windows) rect(if (r.nextFloat() < 0.8f) p.lamp else hex(0xcdeeff), i, j, 2.4f, 3.6f, 0.4f + r.nextFloat() * 0.5f)
                    } else rect(Color.White, i, j, 2.4f, 3.6f, 0.22f)
                    j += 8f
                }
                i += 6f
            }
        }
        x += w + r.nextFloat() * 2f
    }
}

private fun DrawScope.london(p: Palette, b: Float) {
    val c = p.lm
    rect(c, 178f, b - 48, 112f, 48f)
    for (i in 0 until 8) { val x = 180f + i * 14; shape(c, x, b - 48, x + 4, b - 70, x + 8, b - 48) }
    rect(c, 276f, b - 168, 30f, 168f); rect(c, 272f, b - 176, 38f, 9f); rect(c, 275f, b - 210, 32f, 35f)
    shape(c, 273f, b - 210, 291f, b - 262, 309f, b - 210); rect(c, 290f, b - 280, 2f, 20f)
    for (x in listOf(36f, 112f)) {
        rect(c, x, b - 100, 24f, 100f); shape(c, x - 3, b - 100, x + 12, b - 136, x + 27, b - 100)
        for (d in listOf(-3f, 22f)) shape(c, x + d, b - 100, x + d + 3, b - 118, x + d + 6, b - 100)
    }
    rect(c, 60f, b - 80, 52f, 5f); rect(c, -20f, b - 30, 200f, 7f)
    quad(c, 2.4f, -20f, b - 30, 20f, b - 50, 40f, b - 96)
    quad(c, 2.4f, 60f, b - 96, 86f, b - 34, 112f, b - 96)
    quad(c, 2.4f, 134f, b - 96, 150f, b - 52, 182f, b - 30)
    if (p.night) glow(291f, b - 192, 30f, hex(0xffeeb4), 0.75f)
    disc(hex(0xfff3c9), 291f, b - 192, 12f)
    drawLine(hex(0x3a2c60), Offset(291f, b - 201), Offset(291f, b - 192), 1.5f)
    drawLine(hex(0x3a2c60), Offset(291f, b - 192), Offset(297f, b - 188), 1.5f)
    var x = 186f
    while (x < 272f) { rect(p.lamp, x, b - 36, 3f, 10f); rect(p.lamp, x, b - 18, 3f, 9f); x += 7f }
    var y = b - 160
    while (y < b - 20) { rect(p.lamp, 283f, y, 3f, 7f); rect(p.lamp, 296f, y, 3f, 7f); y += 13f }
}

private fun DrawScope.paris(p: Palette, b: Float) {
    val x = 262f
    val c = p.lm
    if (p.windows > 0.1f) glow(x, b - 120, 150f, hex(0xf0c68c), 0.28f)
    val tower = Path().apply {
        moveTo(x - 82, b); quadraticTo(x - 38, b - 60, x - 30, b - 108); lineTo(x - 14, b - 186); lineTo(x - 5, b - 262)
        lineTo(x + 5, b - 262); lineTo(x + 14, b - 186); lineTo(x + 30, b - 108); quadraticTo(x + 38, b - 60, x + 82, b)
        lineTo(x + 54, b); quadraticTo(x, b - 92, x - 54, b); close()
    }
    drawPath(tower, c)
    rect(c, x - 48, b - 72, 96f, 8f); rect(c, x - 33, b - 114, 66f, 7f); rect(c, x - 18, b - 189, 36f, 6f)
    rect(c, x - 8, b - 266, 16f, 6f); rect(c, x - 1, b - 298, 2f, 34f)
    fun half(y: Float) = when {
        y < 72 -> 78 - y * 0.62f
        y < 114 -> 34 - (y - 72) * 0.18f
        y < 186 -> 26 - (y - 114) * 0.17f
        else -> 13 - (y - 186) * 0.11f
    }
    var y = 74f
    while (y < 258f) {
        val a = half(y); val n = half(y + 11)
        drawLine(p.rim, Offset(x - a, b - y), Offset(x + n, b - y - 11), 0.8f, alpha = 0.5f)
        drawLine(p.rim, Offset(x + a, b - y), Offset(x - n, b - y - 11), 0.8f, alpha = 0.5f)
        y += 11f
    }
    if (p.windows > 0.1f) for ((h, w) in listOf(72f to 48f, 114f to 33f, 189f to 18f, 266f to 8f)) {
        var i = -w + 3
        while (i < w) { disc(p.lamp, x + i, b - h, 1.4f); i += 7f }
    }
    rect(c, 52f, b - 52, 84f, 52f); arch(c, 94f, b - 62, 28f); rect(c, 74f, b - 64, 40f, 14f); rect(c, 92f, b - 106, 4f, 20f)
    rect(c, -20f, b - 18, 170f, 18f)
    var i = -8f
    while (i < 140f) { arch(p.water[0], i + 12, b, 13f); i += 34f }
    var w = 58f
    while (w < 132f) { rect(p.lamp, w, b - 40, 3f, 11f); w += 9f }
}

private fun DrawScope.tokyo(p: Palette, b: Float) {
    shape(lerp(p.far, p.sky[0], 0.35f), -30f, b - 30, 92f, b - 170, 118f, b - 182, 146f, b - 170, 280f, b - 30)
    shape(if (p.night) hex(0xcfd6ee) else Color.White, 92f, b - 170, 118f, b - 182, 146f, b - 170, 160f, b - 155, 144f, b - 160,
        132f, b - 148, 118f, b - 162, 102f, b - 146, 92f, b - 157, 78f, b - 154)
    skyline(p, Random(77), b, 20f, 60f, p.mid, true)
    val x = 296f
    val red = if (p.night) hex(0xd9776c) else hex(0xd9604e)
    if (p.windows > 0.1f) glow(x, b - 130, 130f, hex(0xe67869), 0.3f)
    val tower = Path().apply {
        moveTo(x - 56, b); quadraticTo(x - 16, b - 100, x - 5, b - 232); lineTo(x + 5, b - 232); quadraticTo(x + 16, b - 100, x + 56, b)
        lineTo(x + 40, b); quadraticTo(x, b - 80, x - 40, b); close()
    }
    drawPath(tower, red)
    rect(red, x - 30, b - 86, 60f, 10f); rect(red, x - 16, b - 158, 32f, 8f); rect(red, x - 1.2f, b - 280, 2.4f, 50f)
    rect(hex(0xfff2e0), x - 30, b - 83, 60f, 3f); rect(hex(0xfff2e0), x - 16, b - 156, 32f, 3f)
    for (i in 0 until 5) {
        val y = b - 24 - i * 26
        val w = 40f - i * 5
        rect(p.near, 70 - w * 0.5f, y - 12, w, 20f)
        shape(p.near, 70 - w - 8, y - 10, 70 - w + 4, y - 15, 70f, y - 27, 70 + w - 4, y - 15, 70 + w + 8, y - 10)
        if (p.weather == Weather.SNOW) shape(hex(0xf2f7ff), 70 - w + 4, y - 15, 70f, y - 27, 70 + w - 4, y - 15, 70f, y - 21)
    }
    rect(p.near, 69f, b - 168, 2f, 26f)
    for (i in 0 until 5) rect(p.lamp, 63f, b - 31 - i * 26, 14f, 7f)
}

private fun DrawScope.newYork(p: Palette, b: Float) {
    val c = p.lm
    for ((x, w, h) in listOf(Triple(120f, 30f, 120f), Triple(154f, 24f, 160f), Triple(250f, 34f, 138f), Triple(290f, 26f, 104f))) rect(c, x, b - h, w, h)
    rect(c, 190f, b - 190, 52f, 190f); rect(c, 197f, b - 222, 38f, 34f); rect(c, 205f, b - 246, 22f, 26f); rect(c, 214f, b - 296, 4f, 52f)
    shape(c, 150f, b - 160, 166f, b - 196, 182f, b - 160)
    rect(c, 296f, b - 96, 10f, 96f); rect(c, 366f, b - 96, 10f, 96f); rect(c, 270f, b - 34, 140f, 6f)
    quad(c, 2f, 262f, b - 34, 288f, b - 50, 301f, b - 96); quad(c, 2f, 301f, b - 96, 336f, b - 28, 371f, b - 96)
    quad(c, 2f, 371f, b - 96, 386f, b - 50, 414f, b - 34)
    val green = lerp(c, hex(0x4fb39a), 0.7f)
    rect(green, 48f, b - 44, 30f, 44f); shape(green, 52f, b - 44, 58f, b - 108, 68f, b - 108, 76f, b - 44); disc(green, 63f, b - 117, 8f)
    for (i in -3..3) shape(green, 61f + i * 2, b - 122, 63f + i * 5.5f, b - 136, 65f + i * 2, b - 122)
    shape(green, 66f, b - 102, 80f, b - 146, 85f, b - 144, 72f, b - 98)
    disc(hex(0xffcf5a), 83f, b - 150, 5f)
    rect(c, 40f, b - 26, 46f, 26f)
    var y = b - 184
    while (y < b - 8) {
        var x = 195f
        while (x < 238f) { rect(p.lamp, x, y, 3f, 5f); x += 9f }
        y += 11f
    }
}

private fun DrawScope.sydney(p: Palette, b: Float) {
    val c = p.lm
    quad(c, 7f, 196f, b - 26, 312f, b - 178, 430f, b - 26); quad(c, 3f, 196f, b - 14, 312f, b - 142, 430f, b - 14)
    rect(c, 180f, b - 48, 240f, 6f)
    var x = 216f
    while (x < 420f) {
        val u = (x - 196) / 234
        drawLine(c, Offset(x, b - 26 - 304 * u * (1 - u)), Offset(x, b - 45), 1.2f)
        x += 14f
    }
    rect(c, 192f, b - 70, 14f, 70f); rect(c, 406f, b - 70, 14f, 70f); rect(c, 24f, b - 22, 190f, 22f)
    val top = if (p.night) hex(0xdfe5fa) else Color.White
    fun sail(sx: Float, h: Float, w: Float) {
        val path = Path().apply {
            moveTo(sx, b - 22); quadraticTo(sx + w * 0.15f, b - h * 0.8f, sx + w * 0.8f, b - h)
            quadraticTo(sx + w * 0.85f, b - h * 0.45f, sx + w, b - 22); close()
        }
        drawPath(path, Brush.linearGradient(listOf(top, lerp(p.sky[2], Color.White, 0.35f)), Offset(sx, b - h), Offset(sx + w, b - 20)))
    }
    sail(34f, 92f, 52f); sail(66f, 118f, 60f); sail(104f, 98f, 52f); sail(142f, 72f, 44f); sail(174f, 52f, 32f)
}

private fun DrawScope.dubai(p: Palette, b: Float) {
    val c = p.lm
    for ((w, h) in listOf(46f to 80f, 36f to 150f, 27f to 205f, 18f to 250f, 10f to 292f, 4f to 330f)) rect(c, 222 - w / 2, b - h, w, h)
    rect(c, 221f, b - 372, 2f, 46f)
    for ((x, w, h) in listOf(Triple(150f, 22f, 130f), Triple(176f, 18f, 98f), Triple(262f, 22f, 150f), Triple(292f, 18f, 112f))) {
        rect(c, x, b - h, w, h); shape(c, x, b - h, x + w, b - h - 18, x + w, b - h)
    }
    rect(c, 62f, b - 150, 5f, 150f)
    val sail = Path().apply { moveTo(66f, b - 146); quadraticTo(128f, b - 80, 108f, b - 8); lineTo(66f, b - 8); close() }
    drawPath(sail, if (p.night) hex(0xcfd8f4) else Color.White)
    val dune = lerp(hex(0xe2ad6a), p.near, if (p.night) 0.7f else if (p.time == TimeOfDay.SUNSET) 0.35f else 0.08f)
    val sand = Path().apply {
        moveTo(-30f, b); quadraticTo(70f, b - 40, 170f, b - 6); quadraticTo(290f, b - 34, 430f, b - 4); lineTo(430f, b + 2); lineTo(-30f, b + 2); close()
    }
    drawPath(sand, dune)
    var y = b - 280
    while (y < b - 14) { rect(p.lamp, 221f, y, 2f, 5f); y += 12f }
}

private fun DrawScope.rome(p: Palette, b: Float) {
    val c = p.lm
    rect(c, 258f, b - 88, 96f, 88f); rect(c, 276f, b - 112, 60f, 26f); arch(c, 306f, b - 112, 34f)
    rect(c, 300f, b - 166, 12f, 22f); rect(c, 304.5f, b - 180, 3f, 16f)
    val stone = lerp(c, hex(0xe8b27a), if (p.night) 0.25f else 0.55f)
    shape(stone, 52f, b, 52f, b - 118, 168f, b - 118, 176f, b - 98, 196f, b - 92, 210f, b - 72, 236f, b - 68, 236f, b)
    val hole = if (p.windows > 0.1f) p.lamp else p.water[1]
    for ((h, right) in listOf(14f to 228f, 48f to 228f, 82f to 166f)) {
        var x = 62f
        while (x < right - 10) {
            rect(hole, x, b - h - 14, 11f, 14f); arch(hole, x + 5.5f, b - h - 14, 5.5f); x += 19f
        }
    }
    for ((x, h) in listOf(22f to 126f, 378f to 142f, 252f to 96f)) {
        rect(p.near, x - 2, b - h, 4f, h)
        val leaf = lerp(p.near, hex(0x3f7a52), if (p.night) 0.15f else 0.6f)
        oval(leaf, x, b - h, 30f, 11f); oval(leaf, x - 4, b - h - 8, 19f, 9f)
    }
}

/** Cities after the first seven use the simpler landmark set. */
private val laterCities = listOf(
    LandmarkType.ARCH, LandmarkType.MOUNTAIN, LandmarkType.PYRAMIDS, LandmarkType.DOMES, LandmarkType.TRIO,
    LandmarkType.BRIDGE, LandmarkType.SPIRES, LandmarkType.HOUSES, LandmarkType.CLUSTER, LandmarkType.MOUNTAIN,
    LandmarkType.DOMES, LandmarkType.PAGODA, LandmarkType.SPIRES, LandmarkType.NTOWER,
)

/** Where the eye should rest when the scene is cropped, for example inside the board. */
fun focusOf(city: Int): Float = floatArrayOf(240f, 245f, 200f, 260f, 180f, 200f, 200f).getOrElse(city) { 220f }

private fun DrawScope.landmark(p: Palette, b: Float) {
    when (p.city) {
        0 -> london(p, b)
        1 -> paris(p, b)
        2 -> newYork(p, b)
        3 -> tokyo(p, b)
        4 -> sydney(p, b)
        5 -> dubai(p, b)
        6 -> rome(p, b)
        else -> {
            val type = laterCities[(p.city - 7) % laterCities.size]
            // The older drawings stand on a base line of 700 and are centred on 270.
            translate(200f - 270f * 0.8f, b - 700f * 0.8f) {
                scale(0.8f, 0.8f, Offset.Zero) {
                    drawLandmark(type, p.lm, p.mid, p.lamp, 1f, p.night)
                }
            }
        }
    }
}

private fun DrawScope.tree(p: Palette, tx: Float, ty: Float, s: Float, seed: Int) {
    val dir = if (tx < 0) 1f else -1f
    val trunk = Path().apply { moveTo(tx, ty + 28); quadraticTo(tx + 10 * dir, ty - 20, tx + 34 * dir, ty - 44) }
    drawPath(trunk, hex(0x4a3340), style = Stroke(5 * s, cap = StrokeCap.Round))
    val r = Random(seed)
    for (i in 0 until 190) {
        val a = r.nextFloat() * 6.28f
        val d = kotlin.math.sqrt(r.nextFloat()) * 44 * s
        disc(p.tree[if (r.nextFloat() < 0.55f) 0 else 1], tx + 28 * dir + cos(a) * d, ty - 48 + sin(a) * d * 0.72f,
            2 + r.nextFloat() * 3.4f, 0.7f + r.nextFloat() * 0.3f)
    }
}

/** Everything that does not move: sky, sun or moon, skylines, landmark, waterfront and water. */
fun DrawScope.drawStill(p: Palette, horizon: Float, total: Float) {
    val hz = horizon
    drawRect(
        Brush.verticalGradient(0f to p.sky[0], 0.42f to p.sky[1], 0.75f to p.sky[2], 1f to p.sky[3], startY = 0f, endY = hz),
        Offset(-600f, 0f), Size(SCENE_W + 1200f, hz),
    )
    if (p.moon) {
        val mx = if (p.city == 3) 330f else 70f
        val my = hz * 0.36f
        glow(mx, my, 90f, hex(0xfff0d7), 0.4f)
        disc(hex(0xfff4d8), mx, my, 24f)
        for ((dx, dy, r) in listOf(Triple(-8f, -5f, 6f), Triple(7f, 6f, 4f), Triple(3f, -10f, 3f))) disc(hex(0xcdb4aa), mx + dx, my + dy, r, 0.35f)
    }
    if (p.sun) {
        val (x, y, r) = when (p.time) {
            TimeOfDay.SUNRISE -> Triple(318f, hz - 92f, 34f)
            TimeOfDay.DAY -> Triple(70f, hz - 330f, 32f)
            else -> Triple(98f, hz - 104f, 46f)
        }
        val inner = when (p.time) { TimeOfDay.SUNRISE -> hex(0xfffbe8); TimeOfDay.DAY -> Color.White; else -> hex(0xfff1c4) }
        val outer = when (p.time) { TimeOfDay.SUNRISE -> hex(0xffd9a0); TimeOfDay.DAY -> hex(0xfff1b0); else -> hex(0xff9a5c) }
        glow(x, y, r * 4.2f, outer, 0.75f)
        drawCircle(Brush.verticalGradient(listOf(inner, outer), y - r, y + r), r, Offset(x, y))
    }
    fun haze(a: Float) = drawRect(
        Brush.verticalGradient(listOf(p.sky[3].copy(alpha = 0f), p.sky[3]), hz - 110, hz),
        Offset(-600f, hz - 110), Size(SCENE_W + 1200f, 110f), alpha = a,
    )
    val r = Random(11 + p.city * 13)
    skyline(p, r, hz, 50f, 135f, p.far, false)
    haze(if (p.time == TimeOfDay.SUNRISE) 0.8f else 0.55f)
    if (p.city != 3) {
        skyline(p, r, hz, 28f, 92f, p.mid, true)
        haze(0.25f)
    }
    landmark(p, hz)
    val rt = Random(31 + p.city)
    var x = -20f
    while (x < SCENE_W + 20f) {
        val rad = 5f + rt.nextFloat() * 9f
        disc(p.tree[if (rt.nextFloat() < 0.5f) 0 else 1], x, hz - 3 - rt.nextFloat() * 5f, rad)
        x += 9f
    }
    if (p.weather == Weather.SPRING || p.weather == Weather.AUTUMN || p.weather == Weather.SNOW) {
        tree(p, -6f, hz - 24, 1.5f, 3)
        tree(p, SCENE_W + 4, hz - 6, 1.25f, 413)
    }
    rect(if (p.weather == Weather.SNOW) hex(0xe8f0fa) else p.near, -100f, hz - 2, SCENE_W + 1200f, 4f)
    drawRect(
        Brush.verticalGradient(0f to p.water[0], 0.45f to p.water[1], 1f to lerp(p.water[1], hex(0x10142c), 0.5f), startY = hz + 2, endY = max(total, hz + 60)),
        Offset(-600f, hz + 2), Size(SCENE_W + 1200f, max(total - hz, 1f)),
    )
}

/** Renders [block] in scene units into a bitmap. [offsetX] and [offsetY] shift the scene (in scene units). */
fun renderBitmap(width: Int, height: Int, unit: Float, offsetX: Float = 0f, offsetY: Float = 0f, block: DrawScope.() -> Unit): ImageBitmap {
    val image = ImageBitmap(max(width, 1), max(height, 1))
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(width.toFloat(), height.toFloat())) {
        scale(unit, unit, Offset.Zero) {
            translate(offsetX, offsetY) { block() }
        }
    }
    return image
}

/** A wobbling mirror of the skyline in the water. Works in screen pixels. */
fun DrawScope.drawReflection(still: ImageBitmap, horizonPx: Int, depthPx: Int, t: Float) {
    val step = max(4, (3 * density).roundToInt())
    val depth = minOf(depthPx, horizonPx, still.height - horizonPx)
    var y = 0
    while (y + step <= depth) {
        val d = y.toFloat() / depth
        val off = sin(t * 0.9f + y * 0.07f / density) * (0.6f + d * 4f) * density
        drawImage(
            still,
            srcOffset = IntOffset(0, horizonPx - y - step), srcSize = IntSize(still.width, step),
            dstOffset = IntOffset(off.roundToInt(), horizonPx + y), dstSize = IntSize(still.width, step),
            alpha = 0.3f * (1f - d),
        )
        y += step
    }
}

private fun frac(v: Float, m: Float) = ((v % m) + m) % m

/** Moving parts of the scene, in scene units. */
fun DrawScope.drawLive(p: Palette, hz: Float, total: Float, t: Float, celebrate: Boolean) {
    val r = Random(5 + p.city)
    for (i in 0 until p.stars) {
        val x = r.nextFloat() * SCENE_W
        val y = r.nextFloat() * hz * 0.6f
        val rad = 0.4f + r.nextFloat() * 1.1f
        disc(Color.White, x, y, rad, 0.35f + 0.65f * abs(sin(t * 0.45f + i * 1.7f)))
    }
    if (celebrate && (p.night || p.time == TimeOfDay.SUNSET)) {
        val w = Random(99)
        for (i in 0 until 60) rect(hex(0xffe6a3), w.nextFloat() * SCENE_W, hz - 8 - w.nextFloat() * 70, 2.4f, 3.6f, 0.5f + 0.5f * abs(sin(t * 3 + i)))
    }
    val cloud = when {
        p.weather == Weather.RAIN -> if (p.night) hex(0x39435c) else hex(0x7c889d)
        p.weather == Weather.SNOW -> if (p.night) hex(0x6a7694) else hex(0xeef4fc)
        p.time == TimeOfDay.DAY -> Color.White
        p.night -> hex(0x8f9bd0)
        p.time == TimeOfDay.SUNRISE -> hex(0xfff0ea)
        else -> hex(0xffd0bc)
    }
    val many = if (p.weather == Weather.RAIN || p.weather == Weather.SNOW) 7 else 4
    val alpha = if (p.night) 0.12f else if (p.weather == Weather.RAIN) 0.6f else 0.55f
    for (i in 0 until many) {
        val x = frac(t * (2 + i % 3) + i * 137f, SCENE_W + 240) - 120
        val y = hz * (0.13f + (i * 0.097f) % 0.4f)
        val s = 0.7f + (i % 3) * 0.28f
        val puff = Path()
        for ((a, b, rr) in listOf(Triple(0f, 0f, 17f), Triple(19f, -7f, 21f), Triple(40f, 1f, 15f), Triple(18f, 5f, 18f))) {
            puff.addOval(androidx.compose.ui.geometry.Rect(Offset(x + a * s, y + b * s), rr * s))
        }
        drawPath(puff, cloud, alpha = alpha)
    }
    if ((p.time == TimeOfDay.DAY || p.time == TimeOfDay.SUNRISE) && p.weather != Weather.RAIN && p.weather != Weather.SNOW) {
        for (i in 0 until 4) {
            val x = frac(t * 14 + i * 17 + (i % 2) * 9, SCENE_W + 300) - 60
            val y = hz * 0.4f + (i % 3) * 11 + sin(t * 0.7f + i) * 6
            val f = sin(t * 5 + i) * 3
            quad(hex(0x3d4a6a), 1.4f, x - 6, y - f, x - 2, y, x, y + 1)
            quad(hex(0x3d4a6a), 1.4f, x, y + 1, x + 2, y, x + 6, y - f)
        }
    }
    if (p.city == 1 && p.night && p.weather == Weather.CLEAR) {
        val a = sin(t * 0.18f) * 0.6f
        val x = 262f
        val y = hz - 270
        val beam = Path().apply {
            moveTo(x, y); lineTo(x + sin(a - 0.09f) * 260, y - cos(a - 0.09f) * 260); lineTo(x + sin(a + 0.09f) * 260, y - cos(a + 0.09f) * 260); close()
        }
        drawPath(beam, hex(0xfff0c8), alpha = 0.13f)
    }
    val bx = frac(t * 5, SCENE_W + 120) - 60
    val by = hz + 34
    shape(p.near, bx, by, bx + 42, by, bx + 36, by + 7, bx + 6, by + 7)
    rect(if (p.time == TimeOfDay.DAY) Color.White else p.near, bx + 10, by - 5, 22f, 5f)
    for (i in 0 until 4) rect(hex(0xffd98a), bx + 13 + i * 5, by - 4, 2.5f, 2.5f)
    for (i in 0 until 26) {
        val y = hz + 8 + ((i * 37) % 190)
        val x = frac(i * 89 + sin(t + i) * 12, SCENE_W)
        val a = max(0f, sin(t * 0.7f + i * 1.9f)) * if (p.night) 0.28f else 0.55f
        rect(p.rim, x, y, 10f + i % 7, 1f, a)
    }
    drawWeather(p.weather, p.night, SCENE_W, total, t, 1f)
}

/** Rain, snow, leaves or petals over an area of [width] by [height] units. */
fun DrawScope.drawWeather(weather: Weather, night: Boolean, width: Float, height: Float, t: Float, density: Float) {
    if (height <= 0f) return
    when (weather) {
        Weather.CLEAR -> Unit
        Weather.RAIN -> {
            val c = if (night) hex(0xc4d6ff) else hex(0xeef6ff)
            for (i in 0 until (90 * density).toInt()) {
                val y = frac(t * (260 + i % 7 * 26) + i * 131, height)
                val x = frac(i * 67.3f, width + 60) - y * 0.05f
                drawLine(c, Offset(x, y), Offset(x - 0.8f, y + 13), 0.9f, alpha = 0.45f)
            }
        }
        Weather.SNOW -> for (i in 0 until (100 * density).toInt()) {
            val y = frac(t * (14 + i % 6 * 4) + i * 89, height)
            val x = frac(i * 73.7f, width + 40) + sin(t * 0.6f + i) * 14 - 20
            disc(Color.White, x, y, 1 + (i % 4) * 0.6f, 0.5f + (i % 5) * 0.12f)
        }
        Weather.AUTUMN, Weather.SPRING -> {
            val autumn = weather == Weather.AUTUMN
            val cs = if (autumn) listOf(hex(0xe8872f), hex(0xc9482f), hex(0xf0b03a)) else listOf(hex(0xffc4dd), hex(0xffa6ca), Color.White)
            val size = if (autumn) 5f else 3.2f
            for (i in 0 until ((if (autumn) 20 else 30) * density).toInt()) {
                val y = frac(t * (12 + i % 5 * 4) + i * 97, height)
                val x = frac(i * 71 + sin(t * 0.8f + i) * 34 + t * 7, width + 30) - 15
                rotate((t * 40 + i * 57) % (2 * PI.toFloat() * 57.3f), Offset(x, y)) {
                    oval(cs[i % 3], x, y, size, size * 0.55f, 0.92f)
                }
            }
        }
    }
}
