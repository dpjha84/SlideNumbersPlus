package com.dpjha.slidingnumbersplus.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpjha.slidingnumbersplus.R
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import kotlinx.coroutines.delay

/**
 * Shown once on a cold start, over the live city. It starts where the system splash ends (the board
 * in the middle of an indigo screen), then the sky clears and the name arrives. A tap skips it.
 */
@Composable
fun SplashScreen(calm: Boolean, onDone: () -> Unit) {
    val reveal = remember { Animatable(if (calm) 1f else 0f) }
    val leave = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        delay(700)
        leave.animateTo(0f, tween(250))
        onDone()
    }
    Box(
        Modifier.fillMaxSize()
            .graphicsLayer { alpha = leave.value }
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDone),
    ) {
        // solid at first, then thins out so the city shows through
        Box(
            Modifier.fillMaxSize().graphicsLayer { alpha = 1f - reveal.value * 0.45f }
                .background(Brush.verticalGradient(listOf(Color(0xFF2A1F73), Color(0xFF2A1F73), Color(0xFF45276F)))),
        )
        Image(
            painterResource(R.drawable.splash_icon), contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(128.dp),
        )
        Column(
            Modifier.fillMaxSize().safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Logo(
                Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp).fillMaxWidth().height(150.dp)
                    .graphicsLayer {
                        alpha = reveal.value
                        translationY = (1f - reveal.value) * -24.dp.toPx()
                    },
            )
            Spacer(Modifier.weight(1f))
            Text("Pixel Pocket", Modifier.padding(bottom = 32.dp), color = Cream.copy(alpha = 0.85f), fontSize = 15.sp)
        }
    }
}
