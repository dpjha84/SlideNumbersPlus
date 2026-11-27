package com.dpjha.slidingnumbersplus.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.Mood
import com.dpjha.slidingnumbersplus.game.TimeOfDay
import com.dpjha.slidingnumbersplus.game.Weather
import com.dpjha.slidingnumbersplus.ui.AppViewModel
import com.dpjha.slidingnumbersplus.ui.RoundButton
import com.dpjha.slidingnumbersplus.ui.ScreenHeader
import com.dpjha.slidingnumbersplus.ui.Small
import com.dpjha.slidingnumbersplus.ui.Title
import com.dpjha.slidingnumbersplus.ui.art.Palette
import com.dpjha.slidingnumbersplus.ui.art.SCENE_W
import com.dpjha.slidingnumbersplus.ui.art.drawStill
import com.dpjha.slidingnumbersplus.ui.art.renderBitmap
import com.dpjha.slidingnumbersplus.ui.press
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import com.dpjha.slidingnumbersplus.ui.theme.Display
import com.dpjha.slidingnumbersplus.ui.theme.Gold
import com.dpjha.slidingnumbersplus.ui.theme.Ink

/** Each card on the levels page wears its own sky. */
private val cardMoods = listOf(
    Mood(TimeOfDay.DAY, Weather.CLEAR), Mood(TimeOfDay.SUNSET, Weather.CLEAR), Mood(TimeOfDay.DAY, Weather.AUTUMN),
    Mood(TimeOfDay.DAY, Weather.SPRING), Mood(TimeOfDay.SUNRISE, Weather.CLEAR), Mood(TimeOfDay.SUNSET, Weather.CLEAR),
    Mood(TimeOfDay.DAY, Weather.CLEAR),
)

@Composable
fun LevelsScreen(vm: AppViewModel) {
    val player = vm.player
    val perTour = Levels.levelsPerTour
    val lastTour = (player.reached - 1) / perTour
    var tour by remember { mutableIntStateOf((player.level - 1) / perTour) }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = ((player.level - 1) % perTour) / Levels.LEVELS_PER_CITY)
    val stars = (tour * perTour + 1..(tour + 1) * perTour).sumOf { player.stars[it] ?: 0 }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("World Tour ${tour + 1}", "$stars of ${perTour * 3} stars", vm::goHome)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            RoundButton(Icons.Rounded.ChevronLeft, "Previous tour", dim = tour == 0) { if (tour > 0) { vm.click(); tour-- } }
            Small(if (lastTour == 0) "Finish all 35 levels to open Tour 2" else "Tour ${tour + 1} of ${lastTour + 1} unlocked")
            RoundButton(Icons.AutoMirrored.Rounded.ArrowForward, "Next tour", dim = tour >= lastTour) { if (tour < lastTour) { vm.click(); tour++ } }
        }
        LazyColumn(Modifier.weight(1f), state = list, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(Levels.ladder.size) { rung -> CityCard(vm, tour * perTour + rung * Levels.LEVELS_PER_CITY + 1, rung) }
        }
    }
}

@Composable
private fun CityCard(vm: AppViewModel, first: Int, rung: Int) {
    val player = vm.player
    val spec = Levels.spec(first)
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier.fillMaxWidth().height(224.dp).clip(shape).background(Ink).border(2.dp, Cream.copy(alpha = 0.35f), shape),
    ) {
        Postcard(Palette(spec.cityIndex, cardMoods[rung % cardMoods.size]), Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color(0xA0140C32))))
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Title(spec.city, 19)
                Small("${spec.shape.cols} × ${spec.shape.rows}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (level in first until first + Levels.LEVELS_PER_CITY) {
                    val locked = level > player.reached
                    val now = level == player.reached
                    val ink = if (now) Color(0xFF4A2400) else if (locked) Cream.copy(alpha = 0.7f) else Cream
                    Column(
                        Modifier.weight(1f).height(58.dp).press { vm.play(level) }.clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    now -> Brush.verticalGradient(listOf(Color(0xFFFFE07A), Color(0xFFF7941E)))
                                    locked -> Brush.verticalGradient(listOf(Color(0xA0140C32), Color(0xA0140C32)))
                                    else -> Brush.verticalGradient(listOf(Color(0xFF8A6CF0), Color(0xFF5236B8)))
                                },
                            )
                            .semantics { contentDescription = "Level $level${if (locked) ", locked" else ""}" },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("$level", style = TextStyle(fontFamily = Display, fontSize = 20.sp, color = ink))
                        if (locked) Icon(Icons.Rounded.Lock, null, Modifier.height(13.dp), tint = ink)
                        else Text("★".repeat(player.stars[level] ?: 0), fontSize = 11.sp, color = if (now) ink else Gold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Postcard(palette: Palette, modifier: Modifier) {
    BoxWithConstraints(modifier) {
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val image = remember(palette.id, w, h) {
            val unit = w / SCENE_W
            val horizon = 330f
            renderBitmap(w, h, unit, 0f, h * 0.82f / unit - horizon) { drawStill(palette, horizon, horizon + h * 0.2f / unit) }
        }
        Canvas(Modifier.fillMaxSize()) { drawImage(image) }
    }
}
