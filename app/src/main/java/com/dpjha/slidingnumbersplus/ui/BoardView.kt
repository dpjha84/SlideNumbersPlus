package com.dpjha.slidingnumbersplus.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Hue for a tile, based on where it belongs, so the solved board forms a smooth cyan to amber sweep. */
fun tileHue(tile: Int, rows: Int, cols: Int): Float {
    val home = tile - 1
    val span = (rows + cols - 2).coerceAtLeast(1)
    return 175f + 185f * ((home / cols) + (home % cols)) / span
}

@Composable
fun BoardView(state: BoardState, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(state.cols.toFloat() / state.rows.toFloat())
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x520E0A28))
            .onSizeChanged { size ->
                val gap = size.width * 0.018f
                val cell = (size.width - gap * (state.cols + 1)) / state.cols
                state.layout(cell, gap)
            }
            .pointerInput(state) {
                detectTapGestures { p -> state.tap(state.indexAt(p)) }
            }
            .pointerInput(state) {
                var total = Offset.Zero
                var last = Offset.Zero
                var lastTime = 0L
                var speed = 0f
                detectDragGestures(
                    onDragStart = { p ->
                        total = Offset.Zero
                        speed = 0f
                        lastTime = System.nanoTime()
                        state.dragStart(state.indexAt(p))
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        total += amount
                        val now = System.nanoTime()
                        val dt = (now - lastTime) / 1_000_000_000f
                        if (dt > 0f) speed = (amount.getDistance() / dt)
                        lastTime = now
                        state.dragMove(total)
                    },
                    onDragEnd = { state.dragEnd(speed) },
                    onDragCancel = { state.dragEnd(0f) },
                )
            },
    ) {
        if (state.laidOut) {
            val cellDp = with(density) { state.cellPx.toDp() }
            for (v in state.visuals) {
                TileView(state, v, cellDp)
            }
        }
    }
}

@Composable
private fun TileView(state: BoardState, v: TileVisual, cellDp: androidx.compose.ui.unit.Dp) {
    val hue = tileHue(v.tile, state.rows, state.cols)
    val scale by animateFloatAsState(if (v.lifted) 1.04f else 1f, tween(70), label = "lift")
    val glow by animateFloatAsState(if (v.inPlace) 1f else 0f, tween(90), label = "glow")
    val shape = RoundedCornerShape(cellDp * 0.2f)
    val top = Color.hsv(hue % 360f, 0.55f, 1f).copy(alpha = 0.78f + 0.17f * glow)
    val bottom = Color.hsv((hue + 30f) % 360f, 0.75f, 0.92f).copy(alpha = 0.62f + 0.2f * glow)
    val glowColor = Color.hsv(hue % 360f, 0.7f, 1f)
    Box(
        modifier = Modifier
            .offset { IntOffset((v.x.value + v.drag.x).roundToInt(), (v.y.value + v.drag.y).roundToInt()) }
            .size(cellDp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (v.lifted) 14.dp else (3f + 9f * glow).dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor,
            )
            .clip(shape)
            .background(Brush.linearGradient(listOf(top, bottom)), shape)
            .border(1.dp, Color.White.copy(alpha = 0.45f + 0.5f * glow), shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = v.tile.toString(),
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = with(LocalDensity.current) { (state.cellPx * 0.42f).toSp() },
                shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(0f, 2f), 6f),
            ),
        )
    }
}
