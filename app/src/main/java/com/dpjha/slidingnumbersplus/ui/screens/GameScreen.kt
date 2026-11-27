package com.dpjha.slidingnumbersplus.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpjha.slidingnumbersplus.audio.Sfx
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.WinResult
import com.dpjha.slidingnumbersplus.ui.AppViewModel
import com.dpjha.slidingnumbersplus.ui.Attempt
import com.dpjha.slidingnumbersplus.ui.Chip
import com.dpjha.slidingnumbersplus.ui.GoldButton
import com.dpjha.slidingnumbersplus.ui.RoundButton
import com.dpjha.slidingnumbersplus.ui.Small
import com.dpjha.slidingnumbersplus.ui.TextLink
import com.dpjha.slidingnumbersplus.ui.Title
import com.dpjha.slidingnumbersplus.ui.Token
import com.dpjha.slidingnumbersplus.ui.TokenIcon
import com.dpjha.slidingnumbersplus.ui.art.Palette
import com.dpjha.slidingnumbersplus.ui.board.BoardEffects
import com.dpjha.slidingnumbersplus.ui.board.PuzzleBoard
import com.dpjha.slidingnumbersplus.ui.board.cellFor
import com.dpjha.slidingnumbersplus.ui.count
import com.dpjha.slidingnumbersplus.ui.panel
import com.dpjha.slidingnumbersplus.ui.press
import com.dpjha.slidingnumbersplus.ui.theme.Coral
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import com.dpjha.slidingnumbersplus.ui.theme.Display
import com.dpjha.slidingnumbersplus.ui.theme.Ink
import com.dpjha.slidingnumbersplus.ui.theme.Teal
import kotlinx.coroutines.delay

@Composable
fun GameScreen(vm: AppViewModel, attempt: Attempt, calm: Boolean, onNext: () -> Unit) {
    val player = vm.player
    val session = attempt.session
    val spec = attempt.spec
    val effects = remember(attempt.id) { BoardEffects() }
    val result = vm.result
    val showWin = vm.winShown && result != null

    // reading vm.turn redraws the counters after each move
    val turn = vm.turn
    val moves = session.moves
    val free = session.freeLeft

    LaunchedEffect(result != null, attempt.id) {
        if (result == null) return@LaunchedEffect
        delay(90)
        vm.audio.duck(3000)
        vm.audio.play(Sfx.WIN)
        vm.audio.buzz(90, 140)
        effects.wave()
        delay(1400)
        vm.winShown = true
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().alpha(if (showWin) 0f else 1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoundButton(Icons.AutoMirrored.Rounded.ArrowBack, "Home", onClick = vm::goHome)
                Spacer(Modifier.size(40.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.panel(16.dp).padding(horizontal = 14.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Title(spec.city)
                        Small((if (attempt.daily) "Daily puzzle" else "Level ${spec.level}") + ", ${spec.shape.cols} × ${spec.shape.rows}")
                    }
                }
                RoundButton(if (player.musicOn) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff, "Music on or off", dim = !player.musicOn, onClick = vm::toggleMusic)
                RoundButton(Icons.Rounded.Refresh, "Restart level") { vm.restart() }
            }
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Meter(if (free > 0) "Free moves: $free" else "Moves", "$moves")
                val left = Levels.stars(moves, session.par)
                Row {
                    for (i in 0 until 3) TokenIcon(Token.STAR, if (i < left) 28.dp else 23.dp, dim = i >= left)
                }
                Meter("3 stars within", "${session.par}")
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.BottomCenter) {
                val cell = cellFor(maxWidth.value, maxHeight.value, spec.shape.rows, spec.shape.cols)
                PuzzleBoard(
                    session.engine, Palette(spec.cityIndex, spec.mood), player.skin, cell, effects, calm, turn,
                    onPress = { index ->
                        val value = session.engine.tileAt(index)
                        if (!vm.press(index)) effects.bump(value)
                    },
                    onPlaced = { value ->
                        if (!session.solved) vm.audio.play(Sfx.PLACE, 1f + (value % 8) * 0.06f)
                    },
                )
            }
            Row(Modifier.widthIn(max = 380.dp).fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Power(Icons.AutoMirrored.Rounded.Undo, "Undo", player.undos, Teal, Modifier.weight(1f)) { vm.undo() }
                Power(Icons.Rounded.Lightbulb, if (vm.hintBusy) "Thinking" else "Hint", player.hints, Color(0xFFFFC54D), Modifier.weight(1f)) {
                    vm.hint { effects.hint(it) }
                }
                Power(Icons.Rounded.Bolt, "Free moves", player.frees, Color(0xFFFF9AD0), Modifier.weight(1f)) { vm.useFreeMoves() }
            }
        }
        if (result != null && showWin) WinPanel(vm, attempt, result, calm, onNext)
    }
}

@Composable
private fun Meter(label: String, value: String) {
    Column(Modifier.widthIn(min = 92.dp).panel(16.dp).padding(horizontal = 10.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Cream.copy(alpha = 0.75f))
        Text(value, style = TextStyle(fontFamily = Display, fontSize = 24.sp, color = Cream))
    }
}

@Composable
private fun Power(icon: ImageVector, label: String, left: Int, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier) {
        Column(
            Modifier.fillMaxWidth().press(onClick = onClick).panel(20.dp).padding(top = 9.dp, bottom = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, Modifier.size(28.dp), tint = tint)
            Text(label, fontSize = 12.sp, maxLines = 1)
        }
        Box(
            Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-6).dp).size(24.dp).clip(CircleShape).background(Coral).border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (left > 99) "99" else if (left <= 0) "Ad" else "$left", style = TextStyle(fontFamily = Display, fontSize = 12.sp, color = Color.White))
        }
    }
}

@Composable
private fun WinPanel(vm: AppViewModel, attempt: Attempt, result: WinResult, calm: Boolean, onNext: () -> Unit) {
    val spec = attempt.spec
    val stars = result.stars
    val appear = remember { Animatable(0f) }
    var shown by remember { mutableIntStateOf(0) }
    var stamped by remember { mutableStateOf(false) }
    val coins = remember { Animatable((result.player.coins - result.reward.coins - (result.chest?.coins ?: 0)).toFloat()) }

    LaunchedEffect(Unit) { appear.animateTo(1f, tween(260)) }
    LaunchedEffect(Unit) {
        delay(250)
        for (i in 0 until 3) {
            shown = i + 1
            if (i < stars) {
                vm.audio.play(listOf(Sfx.STAR1, Sfx.STAR2, Sfx.STAR3)[i])
                vm.audio.buzz(20)
            }
            delay(270)
        }
        delay(150)
        stamped = true
        delay(270)
        vm.audio.play(Sfx.STAMP)
        vm.audio.buzz(45, 160)
        delay(200)
        if (result.reward.coins > 0) vm.audio.play(Sfx.COIN)
        coins.animateTo(result.player.coins.toFloat(), tween(800))
    }

    Box(
        Modifier.fillMaxSize().alpha(appear.value)
            // swallow taps meant for the board underneath
            .clickable(remember { MutableInteractionSource() }, indication = null) {},
    ) {
        if (!calm) Rays(Modifier.fillMaxSize())
        Column(Modifier.align(Alignment.BottomCenter).widthIn(max = 440.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.height(104.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 0 until 3) SlamStar(i < shown, i >= stars, if (i == 1) 100.dp else 78.dp)
            }
            Title(listOf("Solved!", "Nicely done!", "Brilliant!")[stars - 1], 46, Modifier.padding(top = 6.dp))
            val m = attempt.session.moves
            Small("${spec.city} in $m ${if (m == 1) "move" else "moves"}", size = 14)

            val done = if (attempt.daily) 0 else spec.stepInCity + 1
            Row(
                Modifier.padding(vertical = 14.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Cream).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(if (attempt.daily) "Daily puzzle" else "${spec.city} passport", style = TextStyle(fontFamily = Display, fontSize = 19.sp, color = Ink))
                    val line = when {
                        attempt.daily && result.chest != null -> "Streak chest: +${result.chest.coins} coins, +${result.chest.gems} gems"
                        attempt.daily -> "${result.player.streak} day streak"
                        done == Levels.LEVELS_PER_CITY -> "City complete"
                        else -> "${Levels.LEVELS_PER_CITY - done} more to finish this city"
                    }
                    Text(line, fontSize = 12.sp, color = Ink.copy(alpha = 0.75f))
                    if (!attempt.daily) Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (i in 0 until Levels.LEVELS_PER_CITY) {
                            Box(Modifier.size(18.dp, 8.dp).clip(CircleShape).background(if (i < done) Color(0xFFF7941E) else Color(0xFFD9CDB0)))
                        }
                    }
                }
                Stamp(spec.city, if (attempt.daily) "daily" else "$done of 5", stamped)
            }
            Row(Modifier.padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Chip(Token.COIN, count(coins.value.toInt()))
                if (result.reward.xp > 0) Chip(Token.STAR, "+${result.reward.xp} XP")
            }
            GoldButton(if (attempt.daily) "Done" else "Next level", calm = calm, onClick = onNext)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextLink("Home") { vm.goHome() }
                if (!attempt.daily && stars < 3) TextLink("Try for 3 stars") { vm.play(spec.level, fresh = true) }
            }
        }
    }
}

@Composable
private fun SlamStar(visible: Boolean, dim: Boolean, size: Dp) {
    val u = remember { Animatable(0f) }
    LaunchedEffect(visible) { if (visible) u.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 500f)) }
    TokenIcon(
        Token.STAR, size,
        Modifier.alpha(u.value.coerceIn(0f, 1f)).scale(2.8f - 1.8f * u.value).rotate(-40f * (1 - u.value)),
        dim = dim,
    )
}

@Composable
private fun Stamp(city: String, line: String, visible: Boolean) {
    val u = remember { Animatable(0f) }
    LaunchedEffect(visible) { if (visible) u.animateTo(1f, tween(300)) }
    val red = Color(0xFFB8324F)
    Box(
        Modifier.size(82.dp).alpha(u.value * 0.95f).scale(2.6f - 1.6f * u.value).rotate(-14f)
            .border(2.dp, red, CircleShape).padding(4.dp).border(1.dp, red, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("$city\n★\n$line", textAlign = TextAlign.Center, style = TextStyle(fontFamily = Display, fontSize = 12.sp, lineHeight = 14.sp, color = red))
    }
}

@Composable
private fun Rays(modifier: Modifier) {
    val spin = rememberInfiniteTransition(label = "rays")
    val angle by spin.animateFloat(0f, 360f, infiniteRepeatable(tween(22_000, easing = LinearEasing), RepeatMode.Restart), label = "angle")
    Canvas(modifier) {
        val centre = Offset(size.width / 2, size.height * 0.23f)
        val r = 320.dp.toPx()
        val light = Brush.radialGradient(listOf(Color(0x40FFE696), Color(0x00FFE696)), centre, r)
        for (i in 0 until 15) {
            drawArc(light, angle + i * 24f, 9f, true, Offset(centre.x - r, centre.y - r), Size(r * 2, r * 2))
        }
    }
}
