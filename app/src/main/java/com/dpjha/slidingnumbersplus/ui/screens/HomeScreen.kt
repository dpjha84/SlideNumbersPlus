package com.dpjha.slidingnumbersplus.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.dpjha.slidingnumbersplus.BuildConfig
import com.dpjha.slidingnumbersplus.R
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.Rules
import com.dpjha.slidingnumbersplus.ui.AppViewModel
import com.dpjha.slidingnumbersplus.ui.Card
import com.dpjha.slidingnumbersplus.ui.Chip
import com.dpjha.slidingnumbersplus.ui.GoldButton
import com.dpjha.slidingnumbersplus.ui.Pill
import com.dpjha.slidingnumbersplus.ui.RoundButton
import com.dpjha.slidingnumbersplus.ui.Screen
import com.dpjha.slidingnumbersplus.ui.Small
import com.dpjha.slidingnumbersplus.ui.TextLink
import com.dpjha.slidingnumbersplus.ui.Title
import com.dpjha.slidingnumbersplus.ui.Token
import com.dpjha.slidingnumbersplus.ui.count
import com.dpjha.slidingnumbersplus.ui.panel
import com.dpjha.slidingnumbersplus.ui.press
import com.dpjha.slidingnumbersplus.ui.theme.Coral
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import com.dpjha.slidingnumbersplus.ui.theme.Display
import com.dpjha.slidingnumbersplus.ui.theme.Gold
import com.dpjha.slidingnumbersplus.ui.theme.Ink
import com.dpjha.slidingnumbersplus.ui.theme.Teal
import com.dpjha.slidingnumbersplus.ui.theme.Violet
import com.dpjha.slidingnumbersplus.ui.today
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** What the home screen can ask the activity to do. */
class HomeActions(
    val share: () -> Unit,
    val follow: () -> Unit,
    val rate: () -> Unit,
    /** Null when Play Games is not set up. */
    val playGames: (() -> Unit)?,
)

@Composable
fun HomeScreen(vm: AppViewModel, calm: Boolean, actions: HomeActions) {
    val player = vm.player
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val short = maxHeight < 690.dp
        val screenHeight = maxHeight
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = screenHeight)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Chip(Token.COIN, count(player.coins))
                Chip(Token.GEM, count(player.gems))
                Chip(Token.STAR, count(player.totalStars))
                Spacer(Modifier.weight(1f))
                RoundButton(if (player.musicOn) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff, "Music on or off", dim = !player.musicOn, onClick = vm::toggleMusic)
                RoundButton(if (player.soundOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff, "Sound effects on or off", dim = !player.soundOn, onClick = vm::toggleSound)
            }
            Logo(Modifier.fillMaxWidth().height(if (short) 104.dp else 140.dp))
            // lets the city show between the title and the controls
            Spacer(Modifier.height((screenHeight - if (short) 560.dp else 640.dp).coerceAtLeast(12.dp)))
            Route(vm)
            val spec = Levels.spec(player.level)
            GoldButton(
                if (player.board?.level == player.level) "Resume" else if (player.reached == 1) "Play" else "Continue",
                "Level ${player.level}, ${spec.city}",
                Modifier.padding(top = 8.dp), calm,
            ) { vm.play() }
            DailyCard(vm, Modifier.padding(top = 15.dp))
            val claimable = Rules.claimable(player).size
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NavButton(Icons.Rounded.GridView, "All levels", Gold) { vm.open(Screen.LEVELS) }
                    NavButton(Icons.Rounded.CardGiftcard, "Rewards", Violet, claimable) { vm.open(Screen.REWARDS) }
                    NavButton(Icons.Rounded.Palette, "Themes", Color(0xFFFF9AD0)) { vm.open(Screen.THEMES) }
                    NavButton(Icons.Rounded.ShoppingBag, "Shop", Teal) { vm.open(Screen.SHOP) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NavButton(Icons.Rounded.MenuBook, "Passport", Coral) { vm.open(Screen.PASSPORT) }
                    NavButton(Icons.Rounded.BarChart, "Stats", Violet) { vm.open(Screen.STATS) }
                    NavButton(Icons.Rounded.Share, "Share", Color(0xFF6CC6FF)) { vm.click(); actions.share() }
                    val games = actions.playGames
                    if (games != null) NavButton(Icons.Rounded.SportsEsports, "Play Games", Color(0xFF43E6A0)) { vm.click(); games() }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                TextLink("Follow us") { actions.follow() }
                TextLink("Rate us") { actions.rate() }
                Small("Pixel Pocket, v${BuildConfig.VERSION_NAME}", Modifier.padding(start = 8.dp))
            }
            Small(
                "Instrument samples: tonejs-instruments, CC BY 3.0",
                Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp), size = 10, color = Cream.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun Logo(modifier: Modifier) {
    val context = LocalContext.current
    val font = remember { ResourcesCompat.getFont(context, R.font.lilita_one) }
    Canvas(modifier.semantics { contentDescription = "Sliding Numbers" }) {
        val u = minOf(size.width / 350f, size.height / 140f)
        val left = (size.width - 350 * u) / 2
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                typeface = font
                textAlign = android.graphics.Paint.Align.CENTER
                strokeJoin = android.graphics.Paint.Join.ROUND
            }
            fun word(text: String, x: Float, y: Float, sizeUnits: Float, color: Color, bottom: Color = color) {
                paint.textSize = sizeUnits * u
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 10 * u
                paint.shader = null
                paint.color = 0xFF3A1C66.toInt()
                paint.setShadowLayer(14 * u, 0f, 6 * u, 0x8C140832.toInt())
                canvas.nativeCanvas.drawText(text, left + x * u, y * u, paint)
                paint.clearShadowLayer()
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = color.toArgb()
                paint.shader = android.graphics.LinearGradient(
                    0f, (y - 48) * u, 0f, y * u, color.toArgb(), bottom.toArgb(), android.graphics.Shader.TileMode.CLAMP,
                )
                canvas.nativeCanvas.drawText(text, left + x * u, y * u, paint)
            }
            word("Sliding", 175f, 60f, 66f, Cream)
            word("Numbers", 175f, 126f, 66f, Color(0xFFFFF0A8), Color(0xFFFF9D1E))
        }
    }
}

/** The previous, current and next stops of the tour. */
@Composable
private fun Route(vm: AppViewModel) {
    val current = vm.player.level
    val rung = (current - 1) / Levels.LEVELS_PER_CITY
    val rungs = if (rung == 0) listOf(0, 1, 2) else listOf(rung - 1, rung, rung + 1)
    Box(Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
        Canvas(Modifier.fillMaxWidth().height(42.dp)) {
            drawLine(
                Cream.copy(alpha = 0.55f), Offset(46.dp.toPx(), center.y), Offset(size.width - 46.dp.toPx(), center.y),
                3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 6.dp.toPx())),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (r in rungs) {
                val first = r * Levels.LEVELS_PER_CITY + 1
                val here = r == rung
                val open = first <= vm.player.reached
                val level = if (here) current else minOf(first + Levels.LEVELS_PER_CITY - 1, vm.player.reached).coerceAtLeast(first)
                Column(
                    Modifier.width(92.dp).press {
                        if (open) vm.selectLevel(level) else vm.message = "Finish level ${first - 1} to reach ${Levels.spec(first).city}"
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(42.dp).scale(if (here) 1.12f else 1f).clip(CircleShape)
                            .background(if (here) Brush.radialGradient(listOf(Color(0xFFFFE9A8), Color(0xFFFFAB2E))) else Brush.linearGradient(listOf(Color(0xFF2C2060), Color(0xFF2C2060))))
                            .border(3.dp, if (here) Color.White else Cream.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$level", style = TextStyle(fontFamily = Display, fontSize = 17.sp, color = if (here) Color(0xFF4A2500) else Cream))
                    }
                    Small(Levels.spec(first).city, Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyCard(vm: AppViewModel, modifier: Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000 - System.currentTimeMillis() % 1000)
            now = System.currentTimeMillis()
        }
    }
    val day = today()
    val done = Rules.dailyDone(vm.player, day)
    val streak = Rules.liveStreak(vm.player, day)
    val left = ((day + 1) * 86_400_000L - now).coerceAtLeast(0) / 1000
    fun utc(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(now))
    Card(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.width(48.dp).clip(RoundedCornerShape(12.dp)).background(Cream), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().background(Coral), contentAlignment = Alignment.Center) {
                    Text(utc("MMM"), style = TextStyle(fontFamily = Display, fontSize = 12.sp, color = Color.White))
                }
                Text(utc("d"), style = TextStyle(fontFamily = Display, fontSize = 24.sp, color = Ink))
            }
            Column(Modifier.weight(1f)) {
                Title("Daily puzzle", 19)
                val time = String.format(Locale.US, "%02d:%02d:%02d", left / 3600, left / 60 % 60, left % 60)
                Small(if (done) "Done. Next in $time" else "Resets in $time (UTC)")
            }
            Pill(if (done) "Replay" else "Play") { vm.playDaily() }
        }
        val goal = Rules.chestDays.firstOrNull { it > streak } ?: Rules.chestDays.last()
        Row(Modifier.padding(top = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Small(if (streak == 0) "Start a streak" else "$streak day streak", Modifier.padding(end = 4.dp), size = 11)
            val segments = minOf(goal, 14)
            for (i in 0 until segments) {
                val filled = i < streak * segments / goal
                Box(
                    Modifier.weight(1f).height(8.dp).clip(CircleShape)
                        .background(if (filled) Brush.horizontalGradient(listOf(Color(0xFFFF8A3D), Gold)) else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.14f)))),
                )
            }
            Icon(Icons.Rounded.CardGiftcard, "Chest at $goal days", Modifier.padding(start = 3.dp).size(20.dp), tint = Gold)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavButton(icon: ImageVector, label: String, tint: Color, badge: Int = 0, onClick: () -> Unit) {
    Box(Modifier.weight(1f)) {
        Column(
            Modifier.fillMaxWidth().press(onClick = onClick).panel(18.dp).padding(top = 9.dp, bottom = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, Modifier.size(26.dp), tint = tint)
            Text(label, fontSize = 11.sp, maxLines = 1)
        }
        if (badge > 0) {
            Box(Modifier.align(Alignment.TopEnd).size(20.dp).clip(CircleShape).background(Coral), contentAlignment = Alignment.Center) {
                Text("$badge", style = TextStyle(fontFamily = Display, fontSize = 12.sp, color = Color.White))
            }
        }
    }
}
