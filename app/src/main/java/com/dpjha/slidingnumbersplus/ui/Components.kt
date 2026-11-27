package com.dpjha.slidingnumbersplus.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import com.dpjha.slidingnumbersplus.ui.theme.Display
import com.dpjha.slidingnumbersplus.ui.theme.GoldDark
import com.dpjha.slidingnumbersplus.ui.theme.Line
import com.dpjha.slidingnumbersplus.ui.theme.Panel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.util.Locale

fun count(n: Int): String = String.format(Locale.US, "%,d", n)

val titleShadow = androidx.compose.ui.graphics.Shadow(Color(0xB0000000), Offset(0f, 3f), 12f)

@Composable
fun Title(text: String, size: Int = 24, modifier: Modifier = Modifier, color: Color = Cream) {
    Text(
        text, modifier, color = color, textAlign = TextAlign.Center,
        style = TextStyle(fontFamily = Display, fontSize = size.sp, shadow = titleShadow),
    )
}

@Composable
fun Small(text: String, modifier: Modifier = Modifier, size: Int = 12, color: Color = Cream.copy(alpha = 0.85f), align: TextAlign? = null) {
    Text(text, modifier, color = color, fontSize = size.sp, textAlign = align, style = TextStyle(fontWeight = FontWeight.Bold, shadow = titleShadow))
}

/** A tap target that sinks a little while pressed. */
@Composable
fun Modifier.press(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val down by source.collectIsPressedAsState()
    return this.scale(if (down) 0.94f else 1f).clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

fun Modifier.panel(radius: Dp = 22.dp, color: Color = Panel): Modifier =
    this.clip(RoundedCornerShape(radius)).background(color).border(1.dp, Line, RoundedCornerShape(radius))

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().panel().padding(horizontal = 14.dp, vertical = 12.dp), content = content)
}

@Composable
fun RoundButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, dim: Boolean = false, onClick: () -> Unit) {
    Box(modifier.press(onClick = onClick).size(40.dp).panel(14.dp), contentAlignment = Alignment.Center) {
        Icon(icon, label, Modifier.size(22.dp), tint = if (dim) Cream.copy(alpha = 0.4f) else Cream)
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String, onBack: () -> Unit, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RoundButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Title(title)
            if (subtitle.isNotEmpty()) Small(subtitle)
        }
        Row(Modifier.width(40.dp), content = trailing)
    }
}

enum class Token { COIN, GEM, STAR }

private fun DrawScope.starPath(r: Float, centre: Offset): Path = Path().apply {
    for (i in 0 until 10) {
        val a = -PI / 2 + i * PI / 5
        val d = if (i % 2 == 0) r else r * 0.48f
        val x = centre.x + (cos(a) * d).toFloat()
        val y = centre.y + (sin(a) * d).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
fun TokenIcon(token: Token, size: Dp = 22.dp, modifier: Modifier = Modifier, dim: Boolean = false) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2
        val gold = Brush.verticalGradient(
            if (dim) listOf(Color(0xFF8C86A0), Color(0xFF4C4766)) else listOf(Color(0xFFFFF0A8), Color(0xFFFF9D1E)),
        )
        val edge = if (dim) Color(0xFF38334F) else Color(0xFFA85F0C)
        when (token) {
            Token.COIN -> {
                drawCircle(gold, r * 0.94f)
                drawCircle(edge, r * 0.94f, style = Stroke(r * 0.12f))
                drawCircle(Color(0xFFB86A10), r * 0.52f, style = Stroke(r * 0.18f))
            }
            Token.GEM -> {
                val s = r * 2
                val gem = Path().apply {
                    moveTo(s * 0.25f, s * 0.12f); lineTo(s * 0.75f, s * 0.12f); lineTo(s * 0.96f, s * 0.42f)
                    lineTo(s * 0.5f, s * 0.92f); lineTo(s * 0.04f, s * 0.42f); close()
                }
                drawPath(gem, Brush.linearGradient(listOf(Color(0xFFB6FFF4), Color(0xFF2A8FFF))))
                drawPath(gem, Color.White, style = Stroke(r * 0.1f))
            }
            Token.STAR -> {
                val path = starPath(r, Offset(r, r * 1.05f))
                drawPath(path, gold)
                drawPath(path, edge, style = Stroke(r * 0.1f))
            }
        }
    }
}

@Composable
fun Chip(token: Token, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.height(36.dp).panel(99.dp).padding(start = 6.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TokenIcon(token)
        Text(text, style = TextStyle(fontFamily = Display, fontSize = 16.sp, color = Cream), maxLines = 1)
    }
}

/** The big golden button, with an occasional glint. */
@Composable
fun GoldButton(title: String, subtitle: String? = null, modifier: Modifier = Modifier, calm: Boolean = false, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val down by source.collectIsPressedAsState()
    val glint = rememberInfiniteTransition(label = "glint")
    val u by glint.animateFloat(
        -0.2f, 1.2f,
        infiniteRepeatable(keyframes { durationMillis = 7000; -0.2f at 0; -0.2f at 4500 using LinearEasing; 1.2f at 6300 }, RepeatMode.Restart),
        label = "glintX",
    )
    val shape = RoundedCornerShape(24.dp)
    Box(modifier.fillMaxWidth()) {
        Box(Modifier.matchParentSize().offset(y = 6.dp).shadow(10.dp, shape).background(GoldDark, shape))
        Column(
            Modifier
                .offset { IntOffset(0, if (down) 5.dp.roundToPx() else 0) }
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.verticalGradient(0f to Color(0xFFFFE07A), 0.55f to Color(0xFFFFB52E), 1f to Color(0xFFF7941E)))
                .drawWithContent {
                    drawContent()
                    if (!calm) {
                        val x = u * size.width
                        val w = 44.dp.toPx()
                        val streak = Path().apply {
                            moveTo(x + w * 0.4f, 0f); lineTo(x + w * 1.4f, 0f); lineTo(x + w, size.height); lineTo(x, size.height); close()
                        }
                        drawPath(streak, Color.White, alpha = 0.5f)
                    }
                }
                .clickable(source, indication = null, role = Role.Button, onClick = onClick)
                .padding(top = 12.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = TextStyle(fontFamily = Display, fontSize = 28.sp, color = Color(0xFF4A2400)))
            if (subtitle != null) Text(subtitle, color = Color(0xC04A2400), fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

/** A smaller solid button. */
@Composable
fun Pill(text: String, color: Color = Color(0xFF43E6D2), ink: Color = Color(0xFF05332D), enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.press(enabled, onClick).clip(RoundedCornerShape(14.dp))
            .background(if (enabled) color else Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TextStyle(fontFamily = Display, fontSize = 16.sp, color = if (enabled) ink else Cream.copy(alpha = 0.6f)), maxLines = 1)
    }
}

@Composable
fun TextLink(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text,
        modifier.press(onClick = onClick).padding(horizontal = 8.dp, vertical = 6.dp),
        style = TextStyle(fontSize = 13.sp, color = Cream, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, shadow = titleShadow),
    )
}
