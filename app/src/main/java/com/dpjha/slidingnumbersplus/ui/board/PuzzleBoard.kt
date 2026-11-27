package com.dpjha.slidingnumbersplus.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.dpjha.slidingnumbersplus.R
import com.dpjha.slidingnumbersplus.game.PuzzleEngine
import com.dpjha.slidingnumbersplus.game.Weather
import com.dpjha.slidingnumbersplus.ui.art.Palette
import com.dpjha.slidingnumbersplus.ui.art.SCENE_W
import com.dpjha.slidingnumbersplus.ui.art.drawStill
import com.dpjha.slidingnumbersplus.ui.art.drawWeather
import com.dpjha.slidingnumbersplus.ui.art.focusOf
import com.dpjha.slidingnumbersplus.ui.art.frameColors
import com.dpjha.slidingnumbersplus.ui.art.renderBitmap
import com.dpjha.slidingnumbersplus.ui.art.renderTile
import com.dpjha.slidingnumbersplus.ui.art.tileHue
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val SLIDE_NS = 45_000_000L

/** Short-lived effects the screen asks the board to play. Times are in frame nanoseconds. */
class BoardEffects {
    var hintTile = 0
    var hintAt = 0L
    var bumpTile = 0
    var bumpAt = 0L
    var waveAt = 0L
    var now = 0L

    fun hint(tile: Int) {
        hintTile = tile
        hintAt = now
    }

    fun bump(tile: Int) {
        bumpTile = tile
        bumpAt = now
    }

    fun wave() {
        waveAt = now
    }

    fun clear() {
        hintAt = 0
        bumpAt = 0
        waveAt = 0
    }
}

private class Drag(val cells: Set<Int>, val dx: Float, val dy: Float)

/** Where each tile is drawn right now, so it can glide to where the engine says it is. */
private class TileAnim(size: Int) {
    val x = FloatArray(size) { Float.NaN }
    val y = FloatArray(size) { Float.NaN }
    val fromX = FloatArray(size)
    val fromY = FloatArray(size)
    val toX = FloatArray(size)
    val toY = FloatArray(size)
    val start = LongArray(size)
    val snap = LongArray(size)
    val home = BooleanArray(size)
    var first = true
    var drag: Drag? = null
}

/** Size of one cell, so the whole board fits the space it is given. All values in dp. */
fun cellFor(maxW: Float, maxH: Float, rows: Int, cols: Int): Float {
    val gap = gapFor(cols)
    val w = (min(maxW, 400f) - 18 - gap * (cols + 1)) / cols
    val h = (maxH - 18 - gap * (rows + 1)) / rows
    return min(min(w, h), 104f).coerceAtLeast(24f)
}

private fun gapFor(cols: Int): Float = if (cols > 4) 5f else 7f

@Composable
fun PuzzleBoard(
    engine: PuzzleEngine,
    palette: Palette,
    skin: String,
    cellDp: Float,
    effects: BoardEffects,
    calm: Boolean,
    onPress: (Int) -> Unit,
    onPlaced: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val context = LocalContext.current
    val font = remember { ResourcesCompat.getFont(context, R.font.lilita_one) }
    val rows = engine.rows
    val cols = engine.cols
    val cell = (cellDp * density).roundToInt()
    val gap = (gapFor(cols) * density).roundToInt()
    val width = cell * cols + gap * (cols + 1)
    val height = cell * rows + gap * (rows + 1)

    val anim = remember(engine, cell) { TileAnim(rows * cols) }
    val sprites = remember(palette.id, skin, cell, rows, cols) { HashMap<Int, ImageBitmap>() }
    fun sprite(value: Int, ok: Boolean): ImageBitmap = sprites.getOrPut(if (ok) -value else value) {
        renderTile(value, ok, cell, density, skin, palette, tileHue(palette, value, rows, cols), font)
    }
    // a still crop of the city lives inside the board
    val scenery = remember(palette.id, width, height) {
        var h = 400f
        var w = h * width / height
        if (w > SCENE_W) {
            w = SCENE_W
            h = w * height / width
        }
        val horizon = 400f
        val x = (focusOf(palette.city) - w / 2).coerceIn(0f, SCENE_W - w)
        val y = max(0f, horizon + h * 0.2f - h)
        renderBitmap(width, height, height / h, -x, -y) { drawStill(palette, horizon, horizon + h * 0.2f) }
    }

    val frame = remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) withFrameNanos {
            effects.now = it
            frame.longValue = it
        }
    }
    val press by rememberUpdatedState(onPress)
    val placed by rememberUpdatedState(onPlaced)
    val (rim, mid, dark) = frameColors(palette)

    Box(
        modifier
            .shadow(14.dp, RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(0f to rim, 0.34f to mid, 1f to dark), RoundedCornerShape(26.dp))
            .padding(9.dp),
    ) {
        Canvas(
            Modifier
                .size((width / density).dp, (height / density).dp)
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, Color.Black.copy(alpha = 0.35f), RoundedCornerShape(19.dp))
                .semantics { contentDescription = "Puzzle board. Tap or swipe a tile in line with the gap." }
                .pointerInput(engine, cell) {
                    val step = (cell + gap).toFloat()
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val c = ((down.position.x - gap / 2f) / step).toInt()
                        val r = ((down.position.y - gap / 2f) / step).toInt()
                        if (r !in 0 until rows || c !in 0 until cols) return@awaitEachGesture
                        val index = r * cols + c
                        if (engine.tileAt(index) == PuzzleEngine.EMPTY) return@awaitEachGesture
                        val direction = engine.directionToGap(index)
                        val chain = engine.chainFrom(index).map { engine.tileAt(it) }.toSet()
                        var moved = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            val delta = change.position - down.position
                            if (!moved && delta.getDistance() > 6 * density) moved = true
                            if (!change.pressed) {
                                if (!moved) press(index)
                                break
                            }
                            if (moved && direction != null) {
                                val off = (delta.x * direction.dc + delta.y * direction.dr).coerceIn(0f, step)
                                if (off > step * 0.2f) {
                                    anim.drag = null
                                    press(index)
                                    break
                                }
                                anim.drag = Drag(chain, direction.dc * off, direction.dr * off)
                            }
                        }
                        anim.drag = null
                    }
                },
        ) {
            val now = frame.longValue
            val t = (now % 3_600_000_000_000L) / 1e9f
            drawRect(Color(0xFF1C1448))
            drawImage(scenery)
            if (!calm && palette.weather != Weather.CLEAR) {
                drawIntoUnits(size.width / density, size.height / density, density) { w, h ->
                    drawWeather(palette.weather, palette.night, w, h, t, 0.3f)
                }
            }
            val radius = CornerRadius(14 * density)
            val dash = PathEffect.dashPathEffect(floatArrayOf(7 * density, 5 * density))
            for (i in 0 until rows * cols) {
                drawRoundRect(
                    Color.White.copy(alpha = 0.2f),
                    Offset(gap + (i % cols) * (cell + gap).toFloat(), gap + (i / cols) * (cell + gap).toFloat()),
                    Size(cell.toFloat(), cell.toFloat()), radius, style = Stroke(1.5f * density, pathEffect = dash),
                )
            }
            val drag = anim.drag
            for (i in 0 until rows * cols) {
                val v = engine.tileAt(i)
                if (v == PuzzleEngine.EMPTY) continue
                val homeX = gap + (i % cols) * (cell + gap).toFloat()
                val homeY = gap + (i / cols) * (cell + gap).toFloat()
                val dragged = drag != null && v in drag.cells
                if (anim.x[v].isNaN() || dragged) {
                    anim.x[v] = if (dragged) homeX + drag!!.dx else homeX
                    anim.y[v] = if (dragged) homeY + drag!!.dy else homeY
                    anim.toX[v] = Float.NaN
                } else {
                    if (anim.toX[v] != homeX || anim.toY[v] != homeY) {
                        anim.fromX[v] = anim.x[v]
                        anim.fromY[v] = anim.y[v]
                        anim.toX[v] = homeX
                        anim.toY[v] = homeY
                        anim.start[v] = now
                    }
                    val u = if (calm) 1f else ((now - anim.start[v]).toFloat() / SLIDE_NS).coerceIn(0f, 1f)
                    anim.x[v] = anim.fromX[v] + (anim.toX[v] - anim.fromX[v]) * u
                    anim.y[v] = anim.fromY[v] + (anim.toY[v] - anim.fromY[v]) * u
                }
                val ok = engine.isInPlace(i)
                if (ok && !anim.home[v] && !anim.first) {
                    anim.snap[v] = now
                    placed(v)
                }
                anim.home[v] = ok

                var x = anim.x[v]
                var y = anim.y[v]
                var grow = 1f
                if (effects.bumpTile == v && effects.bumpAt > 0) {
                    val u = (now - effects.bumpAt) / 160e6f
                    if (u in 0f..1f) x += sin(u * PI.toFloat() * 3) * 4 * density * (1 - u)
                }
                if (effects.hintTile == v && effects.hintAt > 0) {
                    val u = (now - effects.hintAt) / 1_650e6f
                    if (u in 0f..1f) grow = 1f + abs(sin(u * PI.toFloat() * 3)) * 0.1f
                }
                if (effects.waveAt > 0) {
                    val goalRow = (v - 1) / cols
                    val goalCol = (v - 1) % cols
                    val u = (now - effects.waveAt - (goalRow + goalCol) * 65_000_000L) / 500e6f
                    if (u in 0f..1f) {
                        val lift = sin(u * PI.toFloat())
                        y -= lift * 14 * density
                        grow = 1f + lift * 0.08f
                    }
                }
                val side = (cell * grow).roundToInt()
                val left = (x - (side - cell) / 2f).roundToInt()
                val top = (y - (side - cell) / 2f).roundToInt()
                drawRoundRect(Color.Black.copy(alpha = 0.22f), Offset(left.toFloat(), top + 8 * density), Size(side.toFloat(), side - 6 * density), radius)
                if (ok) glowAround(left.toFloat(), top.toFloat(), side.toFloat(), Color.White, 0.22f)
                drawImage(sprite(v, ok), dstOffset = IntOffset(left, top), dstSize = IntSize(side, side))
                if (effects.hintTile == v && grow > 1f) glowAround(left.toFloat(), top.toFloat(), side.toFloat(), Color(0xFFFFE9A8), 0.9f)
                val since = (now - anim.snap[v]) / 400e6f
                if (anim.snap[v] > 0 && since in 0f..1f) {
                    val centre = Offset(left + side / 2f, top + side / 2f)
                    drawCircle(Color.White, side * (0.35f + since * 0.55f), centre, alpha = 0.8f * (1 - since), style = Stroke(3 * density))
                }
            }
            anim.first = false
            // inner shadow at the top of the well
            drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent), 0f, 16 * density), size = Size(size.width, 16 * density))
        }
    }
}

private fun DrawScope.glowAround(x: Float, y: Float, side: Float, color: Color, alpha: Float) {
    for (i in 1..3) {
        val grow = i * 1.8f * density
        drawRoundRect(
            color, Offset(x - grow, y - grow), Size(side + grow * 2, side + grow * 2),
            CornerRadius(14 * density + grow), alpha = alpha / i, style = Stroke(2 * density),
        )
    }
}

/** Runs [block] with one unit equal to one dp. */
private inline fun DrawScope.drawIntoUnits(widthDp: Float, heightDp: Float, scale: Float, block: DrawScope.(Float, Float) -> Unit) {
    drawContext.canvas.save()
    drawContext.canvas.scale(scale, scale)
    block(widthDp, heightDp)
    drawContext.canvas.restore()
}
