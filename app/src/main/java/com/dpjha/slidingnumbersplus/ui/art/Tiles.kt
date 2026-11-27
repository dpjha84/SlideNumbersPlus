package com.dpjha.slidingnumbersplus.ui.art

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.dpjha.slidingnumbersplus.game.TimeOfDay
import com.dpjha.slidingnumbersplus.game.Weather
import kotlin.math.roundToInt
import kotlin.random.Random

/** The wooden, icy or leafy rim around the board follows the season. */
fun frameColors(p: Palette): List<Color> = when (p.weather) {
    Weather.CLEAR -> when (p.time) {
        TimeOfDay.SUNRISE -> listOf(Color(0xFFFFE3D0), Color(0xFFE39A8A), Color(0xFF9A5668))
        TimeOfDay.DAY -> listOf(Color(0xFFFFEAB0), Color(0xFFE0A444), Color(0xFF8A5420))
        TimeOfDay.SUNSET -> listOf(Color(0xFFFFD2A0), Color(0xFFE07A50), Color(0xFF7A3440))
        TimeOfDay.NIGHT -> listOf(Color(0xFFC9D4FF), Color(0xFF6A76B8), Color(0xFF2C3466))
    }
    Weather.RAIN -> listOf(Color(0xFFD6E6EE), Color(0xFF6F93A8), Color(0xFF33495C))
    Weather.SNOW -> listOf(Color(0xFFFFFFFF), Color(0xFFBCD6F0), Color(0xFF5F7FA8))
    Weather.AUTUMN -> listOf(Color(0xFFFFD08A), Color(0xFFC4662C), Color(0xFF6A2C18))
    Weather.SPRING -> listOf(Color(0xFFFFE4EE), Color(0xFFEE8FB4), Color(0xFF93466A))
}

/** Tile hue runs from the first value to the second across the solved board. */
private fun hues(p: Palette): Pair<Float, Float> = when (p.weather) {
    Weather.CLEAR -> when (p.time) {
        TimeOfDay.SUNRISE -> 285f to 390f
        TimeOfDay.DAY -> 170f to 265f
        TimeOfDay.SUNSET -> 320f to 405f
        TimeOfDay.NIGHT -> 200f to 300f
    }
    Weather.RAIN -> 185f to 250f
    Weather.SNOW -> 190f to 235f
    Weather.AUTUMN -> 352f to 405f
    Weather.SPRING -> 300f to 360f
}

fun tileHue(p: Palette, value: Int, rows: Int, cols: Int): Float {
    val (from, to) = hues(p)
    val r = (value - 1) / cols
    val c = (value - 1) % cols
    val u = r.toFloat() / (rows - 1) * 0.65f + c.toFloat() / (cols - 1) * 0.35f
    return (from + (to - from) * u) % 360f
}

/**
 * Paints one tile into a bitmap of side [cell]; the lower [lip] pixels are its raised edge.
 * [ok] marks a tile that sits in its home cell.
 */
fun renderTile(
    value: Int,
    ok: Boolean,
    cell: Int,
    density: Float,
    skin: String,
    p: Palette,
    hue: Float,
    font: Typeface?,
): ImageBitmap {
    val image = ImageBitmap(cell, cell)
    CanvasDrawScope().draw(Density(density), LayoutDirection.Ltr, Canvas(image), Size(cell.toFloat(), cell.toFloat())) {
        tileFace(value, ok, cell.toFloat(), skin, p, hue, font)
    }
    return image
}

private fun hsl(h: Float, s: Float, l: Float, a: Float = 1f) = Color.hsl(((h % 360f) + 360f) % 360f, s, l, a)

private fun DrawScope.tileFace(value: Int, ok: Boolean, s: Float, skin: String, p: Palette, hue: Float, font: Typeface?) {
    val lip = 5f * density
    val radius = CornerRadius(s * 0.18f)
    val face = Size(s, s - lip)
    val rnd = Random(value * 101)

    val top: Color
    val bottom: Color
    val edge: Color
    var text = Color.White
    var textShadow = hsl(hue, 0.7f, 0.2f, 0.85f)
    when (skin) {
        "neon" -> {
            top = if (ok) hsl(hue, 0.9f, 0.5f) else Color(0xE6161233)
            bottom = if (ok) hsl(hue, 0.9f, 0.36f) else Color(0xE60B0920)
            edge = hsl(hue, 0.9f, 0.28f)
            textShadow = hsl(hue, 1f, 0.6f)
        }
        "wood" -> {
            top = if (ok) Color(0xFFE8B26A) else Color(0xFFB97F47)
            bottom = if (ok) Color(0xFFC98A44) else Color(0xFF8C5A2E)
            edge = Color(0xFF5C3717)
            textShadow = Color(0xCC3A2008)
        }
        "marble" -> {
            top = if (ok) Color(0xFFFFF3D6) else Color(0xFFF4F5FA)
            bottom = if (ok) Color(0xFFF0D595) else Color(0xFFC6CADA)
            edge = Color(0xFF8A8FA6)
            text = Color(0xFF2B2548)
            textShadow = Color(0x66FFFFFF)
        }
        "brick" -> {
            top = if (ok) Color(0xFFE8784F) else Color(0xFFB4523A)
            bottom = if (ok) Color(0xFFC9522F) else Color(0xFF8E3A2A)
            edge = Color(0xFF5E2318)
            textShadow = Color(0xCC40120A)
        }
        "stone" -> {
            top = if (ok) Color(0xFFB9C7A8) else Color(0xFF9AA0AE)
            bottom = if (ok) Color(0xFF8DA078) else Color(0xFF6B7182)
            edge = Color(0xFF454A58)
            textShadow = Color(0xCC20232D)
        }
        else -> { // glass
            top = if (ok) hsl(hue, 1f, 0.76f, 0.95f) else hsl(hue, 0.9f, 0.64f, 0.70f)
            bottom = if (ok) hsl(hue, 0.94f, 0.54f, 0.93f) else hsl(hue, 0.8f, 0.44f, 0.60f)
            edge = if (ok) hsl(hue, 0.8f, 0.38f) else hsl(hue, 0.66f, 0.28f, 0.75f)
        }
    }

    // raised edge, then the face
    drawRoundRect(edge, Offset(0f, lip), face, radius)
    drawRoundRect(Brush.verticalGradient(listOf(top, bottom), 0f, face.height), Offset.Zero, face, radius)

    val clip = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(0f, 0f, s, face.height, radius)) }
    drawContext.canvas.save()
    drawContext.canvas.clipPath(clip)
    when (skin) {
        "wood" -> for (i in 0 until 6) {
            val y = face.height * (i + rnd.nextFloat()) / 6f
            val grain = Path().apply {
                moveTo(0f, y)
                cubicTo(s * 0.3f, y - s * 0.06f * rnd.nextFloat(), s * 0.65f, y + s * 0.08f * rnd.nextFloat(), s, y)
            }
            drawPath(grain, Color(0xFF5C3717), alpha = 0.28f, style = Stroke(density * (1f + rnd.nextFloat())))
        }
        "marble" -> for (i in 0 until 3) {
            var x = rnd.nextFloat() * s
            var y = 0f
            val vein = Path().apply { moveTo(x, y) }
            while (y < face.height) {
                x += (rnd.nextFloat() - 0.4f) * s * 0.3f
                y += s * (0.12f + rnd.nextFloat() * 0.12f)
                vein.lineTo(x, y)
            }
            drawPath(vein, Color(0xFF8B90AA), alpha = 0.35f, style = Stroke(density * 0.9f))
        }
        "brick" -> {
            for (i in 1..2) {
                val y = face.height * i / 3f
                drawLine(Color(0xFFF3D9C4), Offset(0f, y), Offset(s, y), density * 1.5f, alpha = 0.35f)
            }
            for (i in 0 until 3) {
                val x = if (i % 2 == 0) s * 0.5f else s * 0.2f
                drawLine(Color(0xFFF3D9C4), Offset(x, face.height * i / 3f), Offset(x, face.height * (i + 1) / 3f), density * 1.5f, alpha = 0.35f)
            }
        }
        "stone" -> for (i in 0 until 60) {
            drawCircle(
                if (rnd.nextBoolean()) Color.White else Color.Black,
                density * (0.5f + rnd.nextFloat() * 1.6f),
                Offset(rnd.nextFloat() * s, rnd.nextFloat() * face.height),
                alpha = 0.18f,
            )
        }
    }
    drawContext.canvas.restore()

    // gloss
    if (skin == "glass" || skin == "marble") {
        drawRoundRect(
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.05f)), 0f, face.height * 0.45f),
            Offset(s * 0.07f, face.height * 0.05f), Size(s * 0.86f, face.height * 0.38f), CornerRadius(s * 0.13f),
        )
    }
    val line = density * 1.5f
    val border = when {
        skin == "neon" -> hsl(hue, 1f, 0.66f)
        ok -> Color.White
        p.night -> hsl(hue, 1f, 0.82f, 0.6f)
        else -> Color.White.copy(alpha = 0.38f)
    }
    val stroke = if (skin == "neon" || ok) line * 1.7f else line
    drawRoundRect(border, Offset(stroke / 2, stroke / 2), Size(s - stroke, face.height - stroke), radius, style = Stroke(stroke))

    // season touches
    when (p.weather) {
        Weather.SNOW -> {
            val cap = Path().apply {
                moveTo(s * 0.06f, s * 0.13f)
                quadraticTo(s * 0.08f, 0f, s * 0.24f, 0f)
                lineTo(s * 0.76f, 0f)
                quadraticTo(s * 0.92f, 0f, s * 0.94f, s * 0.13f)
                quadraticTo(s * 0.78f, s * 0.22f, s * 0.62f, s * 0.14f)
                quadraticTo(s * 0.44f, s * 0.23f, s * 0.3f, s * 0.13f)
                quadraticTo(s * 0.18f, s * 0.2f, s * 0.06f, s * 0.13f)
                close()
            }
            drawPath(cap, Color(0xFFF4F9FF))
        }
        Weather.RAIN -> for ((x, y, r) in listOf(Triple(0.18f, 0.22f, 0.032f), Triple(0.8f, 0.38f, 0.024f), Triple(0.3f, 0.74f, 0.026f), Triple(0.68f, 0.8f, 0.018f))) {
            drawCircle(Color.White, s * r * 1.7f, Offset(s * x, face.height * y), alpha = 0.3f)
            drawCircle(Color.White, s * r * 0.7f, Offset(s * x - s * 0.01f, face.height * y - s * 0.01f), alpha = 0.9f)
        }
        Weather.AUTUMN, Weather.SPRING -> if (value % 3 == 0) {
            val c = if (p.weather == Weather.AUTUMN) Color(0xFFE8782A) else Color(0xFFFF9CC4)
            val at = Offset(s * 0.2f, face.height * 0.83f)
            rotate(-24f, at) { drawOval(c, Offset(at.x - s * 0.13f, at.y - s * 0.07f), Size(s * 0.26f, s * 0.14f)) }
        }
        Weather.CLEAR -> Unit
    }

    if (ok) {
        drawCircle(Color.White, s * 0.075f, Offset(s * 0.86f, face.height * 0.16f), alpha = 0.35f)
        drawCircle(Color.White, s * 0.04f, Offset(s * 0.86f, face.height * 0.16f))
    }

    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = font
            textAlign = Paint.Align.CENTER
            textSize = s * 0.5f
        }
        val y = face.height / 2f - (paint.descent() + paint.ascent()) / 2f
        paint.color = textShadow.toArgb()
        if (skin == "neon") paint.setShadowLayer(s * 0.14f, 0f, 0f, textShadow.toArgb())
        canvas.nativeCanvas.drawText(value.toString(), s / 2f, y + (2f * density).roundToInt(), paint)
        paint.clearShadowLayer()
        paint.color = text.toArgb()
        canvas.nativeCanvas.drawText(value.toString(), s / 2f, y, paint)
    }
}
