package com.dpjha.slidingnumbersplus.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.dpjha.slidingnumbersplus.R
import com.dpjha.slidingnumbersplus.game.Item
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.Reward
import com.dpjha.slidingnumbersplus.game.Rules
import com.dpjha.slidingnumbersplus.ui.AppViewModel
import com.dpjha.slidingnumbersplus.ui.Card
import com.dpjha.slidingnumbersplus.ui.Chip
import com.dpjha.slidingnumbersplus.ui.Pill
import com.dpjha.slidingnumbersplus.ui.ScreenHeader
import com.dpjha.slidingnumbersplus.ui.Small
import com.dpjha.slidingnumbersplus.ui.Title
import com.dpjha.slidingnumbersplus.ui.Token
import com.dpjha.slidingnumbersplus.ui.art.Palette
import com.dpjha.slidingnumbersplus.ui.art.renderTile
import com.dpjha.slidingnumbersplus.ui.art.tileHue
import com.dpjha.slidingnumbersplus.ui.count
import com.dpjha.slidingnumbersplus.ui.press
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import com.dpjha.slidingnumbersplus.ui.theme.Display
import com.dpjha.slidingnumbersplus.ui.theme.Gold
import com.dpjha.slidingnumbersplus.ui.theme.Ink
import com.dpjha.slidingnumbersplus.ui.theme.Teal
import com.dpjha.slidingnumbersplus.ui.today
import kotlin.math.roundToInt

@Composable
private fun Page(vm: AppViewModel, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title, subtitle, vm::goHome)
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            Chip(Token.COIN, count(vm.player.coins))
            Chip(Token.GEM, count(vm.player.gems))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

private fun describe(r: Reward): String = listOfNotNull(
    if (r.coins > 0) "${r.coins} coins" else null,
    if (r.gems > 0) "${r.gems} ${if (r.gems == 1) "gem" else "gems"}" else null,
    if (r.xp > 0) "${r.xp} XP" else null,
).joinToString(", ")

@Composable
private fun Bar(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f))) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(8.dp).clip(CircleShape).background(Gold))
    }
}

@Composable
fun RewardsScreen(vm: AppViewModel) {
    val player = vm.player
    val rank = Rules.rank(player.xp)
    val from = Rules.xpForRank(rank)
    val to = Rules.xpForRank(rank + 1)
    Page(vm, "Rewards", "Achievements and streak chests") {
        Card {
            Title("Traveller rank $rank", 19)
            Small("${player.xp - from} of ${to - from} XP to the next rank")
            Bar((player.xp - from).toFloat() / (to - from), Modifier.padding(top = 8.dp))
        }
        Card {
            val streak = Rules.liveStreak(player, today())
            Title("Daily streak: $streak", 19)
            for (day in Rules.chestDays) {
                val chest = Rules.chestFor(day)!!
                Small("Day $day chest: ${chest.coins} coins, ${chest.gems} gems", Modifier.padding(top = 4.dp), color = if (streak >= day) Gold else Cream.copy(alpha = 0.8f))
            }
        }
        val claimable = Rules.claimable(player).map { it.id }.toSet()
        val ordered = Rules.achievements.sortedBy { if (it.id in claimable) 0 else if (it.id in player.claimedAchievements) 2 else 1 }
        for (a in ordered) {
            val claimed = a.id in player.claimedAchievements
            val value = a.progress(player).coerceAtMost(a.target)
            Card {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Title(a.title, 17)
                        Small("${a.detail}. ${describe(a.reward)}")
                        if (!claimed) Bar(value.toFloat() / a.target, Modifier.padding(top = 6.dp))
                    }
                    when {
                        claimed -> Icon(Icons.Rounded.CheckCircle, "Claimed", Modifier.size(28.dp), tint = Teal)
                        a.id in claimable -> Pill("Claim", Gold, Color(0xFF4A2400)) { vm.claim(a.id) }
                        else -> Small("$value/${a.target}")
                    }
                }
            }
        }
    }
}

@Composable
fun ThemesScreen(vm: AppViewModel) {
    val player = vm.player
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val font = remember { ResourcesCompat.getFont(context, R.font.lilita_one) }
    val spec = vm.homeSpec
    val palette = Palette(spec.cityIndex, spec.mood)
    Page(vm, "Themes", "Tile styles") {
        for (skin in Rules.skins) {
            val owned = skin.id in player.ownedSkins
            val selected = player.skin == skin.id
            val side = (56 * density).roundToInt()
            val tiles = remember(skin.id, palette.id) {
                (1..3).map { renderTile(it, it == 3, side, density, skin.id, palette, tileHue(palette, it, 3, 3), font) }
            }
            Card {
                // The price gets its own row with the name, so long prices are never cut off.
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) { Title(skin.title, 18) }
                        val price = if (skin.gems > 0) "${skin.gems} gems" else "${skin.coins} coins"
                        Pill(
                            if (selected) "In use" else if (owned) "Use" else price,
                            if (owned) Teal else Gold,
                            if (owned) Color(0xFF05332D) else Color(0xFF4A2400),
                            enabled = !selected,
                        ) { vm.chooseSkin(skin.id) }
                    }
                    Canvas(Modifier.size(180.dp, 56.dp)) {
                        tiles.forEachIndexed { i, image ->
                            drawImage(image, dstOffset = androidx.compose.ui.unit.IntOffset(i * (side + (6 * density).roundToInt()), 0), dstSize = IntSize(side, side))
                        }
                    }
                }
            }
        }
    }
}

/** [watchAd] is null when rewarded ads are not available. */
@Composable
fun ShopScreen(vm: AppViewModel, watchAd: (() -> Unit)?) {
    val player = vm.player
    Page(vm, "Shop", "Spend coins on boosters") {
        for (item in Item.values()) {
            val have = when (item) {
                Item.HINTS -> player.hints
                Item.UNDOS -> player.undos
                Item.FREES -> player.frees
                Item.FREEZE -> player.freezes
            }
            Card {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Title(item.title, 18)
                        Small("${item.detail}. You have $have")
                    }
                    Pill("${item.coins} coins", Gold, Color(0xFF4A2400), enabled = player.coins >= item.coins) { vm.buy(item) }
                }
            }
        }
        if (watchAd != null) {
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Title("Free coins", 18)
                        Small("Watch a short ad for ${Rules.AD_COINS} coins")
                    }
                    Pill("Watch", onClick = watchAd)
                }
            }
        }
    }
}

@Composable
fun PassportScreen(vm: AppViewModel) {
    val player = vm.player
    val finished = ((player.reached - 1) / Levels.LEVELS_PER_CITY).coerceAtMost(Levels.cities.size)
    Page(vm, "Passport", "$finished of ${Levels.cities.size} cities stamped") {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Cream).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Levels.cities.withIndex().chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for ((i, city) in row) {
                        val got = i < finished
                        val ink = if (got) Color(0xFFB8324F) else Ink.copy(alpha = 0.3f)
                        Box(
                            Modifier.size(92.dp).rotate(if (got) -12f + (i * 7) % 24 else 0f).border(2.dp, ink, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$city\n${if (got) "★" else "…"}", textAlign = TextAlign.Center,
                                style = TextStyle(fontFamily = Display, fontSize = 13.sp, lineHeight = 16.sp, color = ink),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsScreen(vm: AppViewModel, adPrivacy: (() -> Unit)?) {
    val player = vm.player
    val s = player.stats
    Page(vm, "Stats", "Your journey so far") {
        Card {
            val lines = listOf(
                "Highest level" to player.reached, "Stars" to player.totalStars, "Puzzles solved" to s.wins,
                "3-star solves" to s.threeStarWins, "Solved without boosters" to s.noAssistWins, "Moves made" to s.totalMoves,
                "Hints used" to s.hintsUsed, "Daily puzzles solved" to s.dailyWins, "Best daily streak" to s.bestStreak,
            )
            for ((label, value) in lines) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 14.sp)
                    Text(count(value), style = TextStyle(fontFamily = Display, fontSize = 17.sp, color = Gold))
                }
            }
        }
        Card {
            Title("Settings", 18)
            for ((label, on, toggle) in listOf(
                Triple("Music", player.musicOn, vm::toggleMusic),
                Triple("Sound effects", player.soundOn, vm::toggleSound),
                Triple("Vibration", player.hapticsOn, vm::toggleHaptics),
            )) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 14.sp)
                    Pill(if (on) "On" else "Off", if (on) Teal else Color.White.copy(alpha = 0.2f), if (on) Color(0xFF05332D) else Cream) { toggle() }
                }
            }
            if (adPrivacy != null) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ad privacy choices", fontSize = 14.sp)
                    Pill("Review", onClick = adPrivacy)
                }
            }
        }
    }
}
