package com.dpjha.slidingnumbersplus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.ui.art.CityBackdrop
import com.dpjha.slidingnumbersplus.ui.art.CityCatalog

@Composable
fun GameScreen() {
    var level by rememberSaveable { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val spec = remember(level) { Levels.spec(level) }
    val built = remember(level) { BoardState.forLevel(level, scope) }
    val state = built.first
    val par = built.second
    LaunchedEffect(state) {
        state.setCallbacks(
            move = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
            blocked = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
            solved = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
        )
    }

    Box(Modifier.fillMaxSize()) {
        CityBackdrop(CityCatalog.forName(spec.city), spec.look)
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Pill("Level $level · ${spec.city}")
                Pill("Moves ${state.moves} / par $par")
            }
            Spacer(Modifier.height(140.dp))
            BoardView(state)
        }
        if (state.solved) {
            Box(Modifier.fillMaxSize().background(Color(0xAA0D0A24)), contentAlignment = Alignment.Center) {
                Column(
                    Modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xEE1B1448)).padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("${spec.city} cleared", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${"★".repeat(Levels.stars(state.moves, par))} · ${state.moves} moves", color = Color(0xFFFFC766), fontSize = 18.sp)
                    Button(onClick = { level += 1 }) { Text("Next level") }
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x7312103A))
            .padding(horizontal = 13.dp, vertical = 7.dp),
    )
}
