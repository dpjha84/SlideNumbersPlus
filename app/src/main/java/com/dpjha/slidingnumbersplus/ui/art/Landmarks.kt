package com.dpjha.slidingnumbersplus.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal fun Color.a(m: Float): Color = copy(alpha = (alpha * m).coerceIn(0f, 1f))

internal fun DrawScope.box(c: Color, x: Float, y: Float, w: Float, h: Float) = drawRect(c, Offset(x, y), Size(w, h))
internal fun DrawScope.dot(c: Color, x: Float, y: Float, r: Float) = drawCircle(c, r, Offset(x, y))
internal fun DrawScope.poly(c: Color, vararg p: Float) {
    val path = Path()
    path.moveTo(p[0], p[1])
    var i = 2
    while (i < p.size) {
        path.lineTo(p[i], p[i + 1])
        i += 2
    }
    path.close()
    drawPath(path, c)
}

private fun DrawScope.curve(c: Color, w: Float, x0: Float, y0: Float, cx: Float, cy: Float, x1: Float, y1: Float) {
    val path = Path()
    path.moveTo(x0, y0)
    path.quadraticTo(cx, cy, x1, y1)
    drawPath(path, c, style = Stroke(width = w))
}

/**
 * Draws the signature building of a city in skyline space (the caller applies the scene transform).
 * [col] is the main silhouette colour, [detail] is for gaps and arches, [accent] is for lit parts.
 */
internal fun DrawScope.drawLandmark(type: LandmarkType, col: Color, detail: Color, accent: Color, am: Float, night: Boolean) {
    val c = col.a(am)
    val d = detail.a(am)
    val ac = accent.a(am)
    when (type) {
        LandmarkType.BIG_BEN -> {
            val bx = 330f
            box(c, bx - 120, 640f, 120f, 30f); box(c, bx - 112, 626f, 104f, 16f)
            for (i in 0 until 6) poly(c, bx - 112 + i * 17, 626f, bx - 103 + i * 17, 600f, bx - 94 + i * 17, 626f)
            box(c, bx - 18, 400f, 36f, 260f); box(c, bx - 22, 392f, 44f, 12f)
            box(c, bx - 20, 330f, 40f, 64f); poly(c, bx - 22, 330f, bx, 262f, bx + 22, 330f); box(c, bx - 1, 238f, 2f, 26f)
            dot(if (night) Color(0xFFFFF3C7).a(am) else Color(0xFFF5EFE0).a(am), bx, 352f, 13f)
            val tx = 120f
            box(c, tx - 70, 560f, 22f, 110f); box(c, tx + 48, 560f, 22f, 110f)
            for (ox in listOf(-70f, 48f)) {
                poly(c, tx + ox - 2, 560f, tx + ox + 11, 520f, tx + ox + 24, 560f)
                box(c, tx + ox + 4, 500f, 14f, 22f); poly(c, tx + ox + 2, 500f, tx + ox + 11, 478f, tx + ox + 20, 500f)
            }
            box(c, tx - 48, 590f, 96f, 8f); box(c, tx - 48, 640f, 96f, 6f)
            curve(c, 3f, tx - 170, 640f, tx - 120, 590f, tx - 59, 560f)
            curve(c, 3f, tx + 59, 560f, tx + 120, 590f, tx + 170, 640f)
            curve(c, 3f, tx - 59, 560f, tx, 650f, tx + 59, 560f)
        }
        LandmarkType.EIFFEL -> {
            val cx = 270f
            poly(c, cx - 92, 700f, cx - 58, 560f, cx - 48, 560f, cx - 62, 700f)
            poly(c, cx + 92, 700f, cx + 58, 560f, cx + 48, 560f, cx + 62, 700f)
            curve(c, 10f, cx - 56, 690f, cx, 590f, cx + 56, 690f)
            box(c, cx - 60, 552f, 120f, 9f)
            poly(c, cx - 52, 552f, cx - 22, 440f, cx - 14, 440f, cx - 30, 552f)
            poly(c, cx + 52, 552f, cx + 22, 440f, cx + 14, 440f, cx + 30, 552f)
            curve(c, 7f, cx - 32, 548f, cx, 500f, cx + 32, 548f)
            box(c, cx - 26, 432f, 52f, 8f)
            poly(c, cx - 20, 432f, cx - 7, 330f, cx + 7, 330f, cx + 20, 432f)
            box(c, cx - 12, 352f, 24f, 5f)
            poly(c, cx - 4, 330f, cx, 270f, cx + 4, 330f); box(c, cx - 1, 252f, 2f, 22f)
        }
        LandmarkType.EMPIRE -> {
            val ex = 300f
            box(c, ex - 28, 520f, 56f, 200f); box(c, ex - 22, 480f, 44f, 42f); box(c, ex - 16, 450f, 32f, 32f)
            box(c, ex - 10, 426f, 20f, 26f); box(c, ex - 5, 408f, 10f, 20f); box(c, ex - 1, 352f, 2f, 58f)
            val wx = 190f
            poly(c, wx - 22, 720f, wx - 18, 400f, wx, 388f, wx + 18, 400f, wx + 22, 720f); box(c, wx - 0.8f, 300f, 1.6f, 92f)
            val kx = 410f
            box(c, kx - 20, 500f, 40f, 220f)
            poly(c, kx - 20, 500f, kx - 14, 470f, kx - 6, 452f, kx, 440f, kx + 6, 452f, kx + 14, 470f, kx + 20, 500f)
            box(c, kx - 1, 420f, 2f, 30f)
            if (night) {
                for (i in 0 until 7) box(ac, ex - 20 + (i % 2) * 22, 530f + i * 24, 6f, 8f)
                for (i in 0 until 6) box(ac, wx - 10 + (i % 2) * 14, 420f + i * 40, 5f, 7f)
            }
        }
        LandmarkType.TOKYO_TOWER -> {
            val fuji = d
            poly(fuji, -20f, 640f, 150f, 470f, 190f, 450f, 230f, 470f, 400f, 640f)
            poly(Color.White.a(if (night) 0.22f else 0.5f), 150f, 470f, 190f, 450f, 230f, 470f, 214f, 482f, 198f, 474f, 188f, 486f, 172f, 476f)
            val tx = 380f
            val red = if (night) Color(0xFFFF7A5C).a(am) else Color(0xFFFF5B4A).a(am)
            poly(red, tx - 64, 720f, tx - 12, 470f, tx - 8, 470f, tx - 52, 720f)
            poly(red, tx + 64, 720f, tx + 12, 470f, tx + 8, 470f, tx + 52, 720f)
            box(red, tx - 34, 600f, 68f, 9f); box(red, tx - 22, 520f, 44f, 8f); box(red, tx - 14, 470f, 28f, 6f)
            poly(red, tx - 10, 470f, tx - 3, 360f, tx + 3, 360f, tx + 10, 470f)
            poly(red, tx - 2, 360f, tx, 300f, tx + 2, 360f); box(red, tx - 1, 280f, 2f, 22f)
        }
        LandmarkType.OPERA -> {
            val cx = 270f
            box(c, cx - 130, 650f, 260f, 16f)
            val sails = listOf(floatArrayOf(-120f, 80f, 110f), floatArrayOf(-62f, 80f, 140f), floatArrayOf(-4f, 80f, 165f), floatArrayOf(60f, 70f, 105f))
            for (s in sails) poly(c, cx + s[0], 650f, cx + s[0] + s[1] * 0.3f, 650f - s[2], cx + s[0] + s[1], 650f)
            for (s in sails) poly(d, cx + s[0] + s[1] * 0.3f, 650f - s[2], cx + s[0] + s[1] * 0.36f, 650f - s[2] + 6, cx + s[0] + s[1] * 0.52f, 650f)
        }
        LandmarkType.BURJ -> {
            val cx = 280f
            box(c, cx - 70, 560f, 26f, 160f); box(c, cx + 44, 590f, 24f, 130f); box(c, cx - 110, 610f, 24f, 110f)
            box(c, cx - 22, 600f, 44f, 120f); box(c, cx - 17, 520f, 34f, 82f); box(c, cx - 13, 450f, 26f, 72f)
            box(c, cx - 9, 390f, 18f, 62f); box(c, cx - 5, 330f, 10f, 62f); box(c, cx - 1.5f, 230f, 3f, 102f)
            if (night) for (i in 0 until 8) box(ac, cx - 3, 340f + i * 30, 2f, 12f)
        }
        LandmarkType.COLOSSEUM -> {
            val cx = 270f
            box(c, cx - 140, 560f, 280f, 110f); box(c, cx - 150, 548f, 300f, 14f)
            for (row in 0 until 3) for (i in 0 until 14) {
                box(d, cx - 132 + i * 19f, 570f + row * 32f, 9f, 18f)
                if (night && (i + row) % 3 == 0) box(ac, cx - 132 + i * 19f, 570f + row * 32f, 9f, 18f)
            }
            poly(c, cx + 100, 548f, cx + 150, 548f, cx + 140, 520f, cx + 118, 520f)
        }
        LandmarkType.ARCH -> {
            val cx = 270f
            box(c, cx - 80, 500f, 160f, 170f); box(c, cx - 90, 490f, 180f, 14f)
            box(d, cx - 22, 560f, 44f, 110f); dot(d, cx, 560f, 22f)
            for (dx in listOf(-92f, 78f)) { box(c, cx + dx, 440f, 14f, 60f); dot(c, cx + dx + 7, 440f, 9f) }
            dot(c, cx, 490f, 34f); box(c, cx - 1, 440f, 2f, 20f)
            if (night) for (i in 0 until 3) { box(ac, cx - 62 + i * 8f, 520f, 4f, 10f); box(ac, cx + 36 + i * 8f, 520f, 4f, 10f) }
        }
        LandmarkType.MOUNTAIN -> {
            poly(c, -40f, 670f, 90f, 520f, 150f, 420f, 190f, 410f, 250f, 520f, 330f, 670f)
            poly(c.a(0.85f), 200f, 670f, 330f, 480f, 380f, 470f, 470f, 560f, 600f, 670f)
            box(ac, 168f, 386f, 3f, 26f); box(ac, 160f, 392f, 19f, 3f)
        }
        LandmarkType.PYRAMIDS -> {
            poly(c, 60f, 690f, 220f, 460f, 380f, 690f)
            poly(c.a(0.9f), 300f, 690f, 400f, 540f, 500f, 690f)
            poly(d, 220f, 460f, 380f, 690f, 220f, 690f)
            poly(c.a(0.8f), 10f, 690f, 70f, 600f, 130f, 690f)
        }
        LandmarkType.DOMES -> {
            val cx = 270f
            box(c, cx - 90, 560f, 180f, 110f); dot(c, cx, 560f, 70f); box(c, cx - 90, 560f, 180f, 20f)
            dot(c, cx - 105, 600f, 26f); dot(c, cx + 105, 600f, 26f); box(c, cx - 131, 600f, 52f, 70f); box(c, cx + 79, 600f, 52f, 70f)
            for (dx in listOf(-150f, 150f)) { box(c, cx + dx - 5, 380f, 10f, 290f); poly(c, cx + dx - 8, 380f, cx + dx, 340f, cx + dx + 8, 380f) }
            box(c, cx - 1, 470f, 2f, 22f)
        }
        LandmarkType.TRIO -> {
            val cx = 270f
            for (dx in listOf(-70f, 0f, 70f)) poly(c, cx + dx - 26, 700f, cx + dx - 20, 470f, cx + dx + 20, 470f, cx + dx + 26, 700f)
            box(c, cx - 112, 456f, 224f, 14f); poly(c, cx - 112, 456f, cx - 80, 440f, cx + 80, 440f, cx + 112, 456f)
            if (night) for (i in 0 until 5) { box(ac, cx - 88 + i * 4f, 520f + i * 30f, 3f, 8f); box(ac, cx + 48 + i * 4f, 520f + i * 30f, 3f, 8f) }
        }
        LandmarkType.BRIDGE -> {
            val cx = 270f
            box(c, cx - 120, 450f, 14f, 240f); box(c, cx + 106, 450f, 14f, 240f)
            box(c, cx - 120, 520f, 14f, 6f); box(c, cx + 106, 520f, 14f, 6f)
            box(c, -20f, 640f, 600f, 8f)
            curve(c, 3f, -20f, 620f, cx - 200, 560f, cx - 113, 454f)
            curve(c, 3f, cx - 113, 454f, cx, 600f, cx + 113, 454f)
            curve(c, 3f, cx + 113, 454f, cx + 200, 560f, 560f, 620f)
            for (i in 0 until 12) box(c.a(0.7f), cx - 100 + i * 17f, 520f + (kotlin.math.abs(i - 5.5f) * 6f), 1.5f, 120f - kotlin.math.abs(i - 5.5f) * 6f)
        }
        LandmarkType.SPIRES -> {
            val cx = 270f
            val tops = listOf(floatArrayOf(-110f, 380f, 18f), floatArrayOf(-60f, 330f, 22f), floatArrayOf(0f, 260f, 26f), floatArrayOf(60f, 340f, 22f), floatArrayOf(110f, 400f, 18f))
            for (s in tops) {
                box(c, cx + s[0] - s[2] / 2, s[1] + 40, s[2], 330f)
                poly(c, cx + s[0] - s[2] / 2 - 2, s[1] + 40, cx + s[0], s[1] - 20, cx + s[0] + s[2] / 2 + 2, s[1] + 40)
            }
            box(c, cx - 130, 620f, 260f, 50f)
        }
        LandmarkType.HOUSES -> {
            var x = 20f
            val hs = listOf(90f, 120f, 100f, 140f, 95f, 125f, 105f, 135f, 90f)
            for ((i, h) in hs.withIndex()) {
                val w = 50f
                box(if (i % 2 == 0) c else c.a(0.88f), x, 670f - h, w, h)
                poly(c, x - 2, 670f - h, x + w / 2, 670f - h - 24f, x + w + 2, 670f - h)
                if (night) box(ac, x + 18, 670f - h + 24f, 12f, 14f)
                x += w + 6f
            }
        }
        LandmarkType.CLUSTER -> {
            val cx = 270f
            val hs = listOf(floatArrayOf(-150f, 230f), floatArrayOf(-100f, 330f), floatArrayOf(-50f, 250f), floatArrayOf(0f, 420f), floatArrayOf(55f, 300f), floatArrayOf(110f, 360f), floatArrayOf(160f, 220f))
            for (s in hs) {
                box(c, cx + s[0] - 20, 670f - s[1], 40f, s[1]); box(c, cx + s[0] - 1, 670f - s[1] - 26, 2f, 26f)
                if (night) for (i in 0 until (s[1] / 40f).toInt()) box(ac, cx + s[0] - 14, 670f - s[1] + 10 + i * 40f, 28f, 3f)
            }
        }
        LandmarkType.PAGODA -> {
            val cx = 270f
            var y = 640f
            var w = 150f
            for (i in 0 until 5) {
                box(c, cx - w / 2 + 10, y - 50, w - 20, 50f)
                poly(c, cx - w / 2 - 16, y - 50, cx - w / 2 + 14, y - 66, cx + w / 2 - 14, y - 66, cx + w / 2 + 16, y - 50)
                y -= 64f; w -= 22f
            }
            poly(c, cx - 4, y - 4, cx, y - 60, cx + 4, y - 4)
        }
        LandmarkType.NTOWER -> {
            val cx = 270f
            poly(c.a(0.9f), -20f, 690f, 120f, 560f, 270f, 520f, 420f, 560f, 560f, 690f)
            box(c, cx - 6, 300f, 12f, 230f); dot(c, cx, 380f, 20f); box(c, cx - 14, 360f, 28f, 8f)
            box(c, cx - 1, 230f, 2f, 72f)
            if (night) dot(ac, cx, 380f, 9f)
        }
    }
}
