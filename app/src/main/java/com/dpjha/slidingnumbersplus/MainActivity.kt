package com.dpjha.slidingnumbersplus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpjha.slidingnumbersplus.services.Ads
import com.dpjha.slidingnumbersplus.services.BannerAd
import com.dpjha.slidingnumbersplus.services.PlayGamesService
import com.dpjha.slidingnumbersplus.services.askForReview
import com.dpjha.slidingnumbersplus.ui.AppViewModel
import com.dpjha.slidingnumbersplus.ui.Screen
import com.dpjha.slidingnumbersplus.ui.art.Backdrop
import com.dpjha.slidingnumbersplus.ui.art.Palette
import com.dpjha.slidingnumbersplus.ui.screens.Celebration
import com.dpjha.slidingnumbersplus.ui.screens.GameScreen
import com.dpjha.slidingnumbersplus.ui.screens.HomeActions
import com.dpjha.slidingnumbersplus.ui.screens.HomeScreen
import com.dpjha.slidingnumbersplus.ui.screens.LevelsScreen
import com.dpjha.slidingnumbersplus.ui.screens.PassportScreen
import com.dpjha.slidingnumbersplus.ui.screens.RewardsScreen
import com.dpjha.slidingnumbersplus.ui.screens.ShopScreen
import com.dpjha.slidingnumbersplus.ui.screens.StatsScreen
import com.dpjha.slidingnumbersplus.ui.screens.ThemesScreen
import com.dpjha.slidingnumbersplus.ui.theme.Cream
import com.dpjha.slidingnumbersplus.ui.theme.Ink
import com.dpjha.slidingnumbersplus.ui.theme.SlidingNumbersTheme
import com.dpjha.slidingnumbersplus.game.Rules
import kotlinx.coroutines.delay

private const val INSTAGRAM = "https://www.instagram.com/pixelpocket.apps/"

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()
    private lateinit var ads: Ads

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(AndroidColor.TRANSPARENT), SystemBarStyle.dark(AndroidColor.TRANSPARENT))
        ads = Ads(this)
        ads.start()
        val calm = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        setContent {
            SlidingNumbersTheme {
                App(calm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.audio.resume()
        PlayGamesService.sync(this, { vm.player }, vm::mergeCloud)
    }

    override fun onPause() {
        vm.audio.pause()
        super.onPause()
    }

    override fun onStop() {
        PlayGamesService.sync(this, { vm.player }, vm::mergeCloud)
        super.onStop()
    }

    @Composable
    private fun App(calm: Boolean) {
        val screen = vm.screen
        val spec = vm.sceneSpec
        BackHandler(enabled = screen != Screen.HOME) { vm.goHome() }

        val result = vm.result
        LaunchedEffect(result) {
            if (result == null) return@LaunchedEffect
            PlayGamesService.report(this@MainActivity, result.player)
            // ask for a rating once, after a good moment well into the game
            if (result.stars == 3 && result.player.reached >= 9 && !result.player.reviewAsked) {
                delay(4_000)
                vm.markReviewAsked()
                askForReview(this@MainActivity)
            }
        }

        Box(Modifier.fillMaxSize().background(Ink)) {
            // hold the city still behind the menus that cover it
            val covered = screen != Screen.HOME && screen != Screen.GAME
            Backdrop(Palette(spec.cityIndex, spec.mood), playing = screen == Screen.GAME, calm = calm || covered, celebrate = vm.result != null)
            if (screen != Screen.HOME && screen != Screen.GAME) Box(Modifier.fillMaxSize().background(Color(0xE0120A30)))
            val won = screen == Screen.GAME && vm.result != null
            if (won && vm.winShown) {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.15f to Color.Transparent, 0.65f to Color(0xC810082C))))
            }
            Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                AnimatedContent(
                    targetState = screen,
                    modifier = Modifier.widthIn(max = 440.dp).fillMaxHeight().padding(horizontal = 16.dp, vertical = 8.dp),
                    transitionSpec = { (fadeIn(tween(220)) + scaleIn(tween(220), 1.04f)) togetherWith fadeOut(tween(150)) },
                    label = "screen",
                ) { target ->
                    when (target) {
                        Screen.HOME -> HomeScreen(vm, calm, homeActions())
                        Screen.LEVELS -> LevelsScreen(vm)
                        Screen.GAME -> vm.attempt?.let { attempt -> GameScreen(vm, attempt, calm, ::nextLevel) }
                        Screen.REWARDS -> RewardsScreen(vm)
                        Screen.THEMES -> ThemesScreen(vm)
                        Screen.SHOP -> ShopScreen(vm, if (ads.rewardedReady) ({ ads.showRewarded { vm.addCoins(Rules.AD_COINS) } }) else null)
                        Screen.PASSPORT -> PassportScreen(vm)
                        Screen.STATS -> StatsScreen(vm, if (ads.privacyChoicesNeeded) ({ ads.showPrivacyChoices() }) else null)
                    }
                }
                Message()
            }
            // space above keeps taps on the boosters away from the ad
            if (ads.hasBanner) BannerAd(ads.ready, Modifier.padding(top = 6.dp))
            }
            if (won && !calm) Celebration(vm.audio)
        }
    }

    @Composable
    private fun androidx.compose.foundation.layout.BoxScope.Message() {
        val message = vm.message ?: return
        LaunchedEffect(message) {
            delay(2200)
            vm.message = null
        }
        Text(
            message,
            Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
                .clip(RoundedCornerShape(14.dp)).background(Cream).padding(horizontal = 14.dp, vertical = 9.dp),
            color = Ink, fontSize = 13.sp, textAlign = TextAlign.Center,
        )
    }

    /** A short ad now and then between levels, never in the first city. */
    private fun nextLevel() {
        val wins = vm.player.stats.wins
        if (vm.attempt?.daily != true && vm.player.reached > 6 && wins % 4 == 0) ads.showInterstitial { vm.next() } else vm.next()
    }

    private fun homeActions() = HomeActions(
        share = {
            val text = "I'm on level ${vm.player.level} of Sliding Numbers: City Puzzle. Come travel with me: ${storeLink()}"
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Share"))
        },
        follow = { view(INSTAGRAM) },
        rate = {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: ActivityNotFoundException) {
                view(storeLink())
            }
        },
        playGames = if (PlayGamesService.configured(this)) {
            { PlayGamesService.open(this) { vm.message = "Play Games is not available right now" } }
        } else {
            null
        },
    )

    private fun storeLink() = "https://play.google.com/store/apps/details?id=com.dpjha.slidingnumbersplus"

    private fun view(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            vm.message = "No app can open this link"
        }
    }
}
