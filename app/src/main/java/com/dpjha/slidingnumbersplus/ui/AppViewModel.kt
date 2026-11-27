package com.dpjha.slidingnumbersplus.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dpjha.slidingnumbersplus.audio.GameAudio
import com.dpjha.slidingnumbersplus.audio.Sfx
import com.dpjha.slidingnumbersplus.data.GameStore
import com.dpjha.slidingnumbersplus.game.Item
import com.dpjha.slidingnumbersplus.game.LevelSpec
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.Player
import com.dpjha.slidingnumbersplus.game.PuzzleEngine
import com.dpjha.slidingnumbersplus.game.Rules
import com.dpjha.slidingnumbersplus.game.SavedBoard
import com.dpjha.slidingnumbersplus.game.Session
import com.dpjha.slidingnumbersplus.game.Solver
import com.dpjha.slidingnumbersplus.game.Weather
import com.dpjha.slidingnumbersplus.game.WinResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { HOME, LEVELS, GAME, REWARDS, THEMES, SHOP, PASSPORT, STATS }

/** One board being played. [id] changes whenever the board is dealt afresh. */
class Attempt(val spec: LevelSpec, val session: Session, val daily: Boolean, val id: Int)

fun today(): Long = System.currentTimeMillis() / 86_400_000L

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = GameStore(application)
    val audio = GameAudio(application)

    var player by mutableStateOf(store.load())
        private set
    var screen by mutableStateOf(Screen.HOME)
        private set
    var attempt by mutableStateOf<Attempt?>(null)
        private set
    var result by mutableStateOf<WinResult?>(null)
        private set
    /** Bumped after every change to the board, so counters redraw. */
    var turn by mutableIntStateOf(0)
        private set
    /** True once the win card is up, after the tiles have taken their bow. */
    var winShown by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    /** The booster the player just asked for and has none of: an ad can refill it. */
    var emptyBooster by mutableStateOf<Item?>(null)
    var hintBusy by mutableStateOf(false)
        private set

    private var deals = 0
    /** The board a hint was last paid for, and the tile it named. */
    private var lastHint: Pair<List<Int>, Int>? = null

    init {
        audio.soundOn = player.soundOn
        audio.hapticsOn = player.hapticsOn
        audio.musicOn = player.musicOn
        applyScene()
    }

    /** The level whose city and sky are behind the menus. */
    val homeSpec: LevelSpec get() = Levels.spec(player.level)

    /** The city and sky on screen now. */
    val sceneSpec: LevelSpec get() = if (screen == Screen.GAME) attempt?.spec ?: homeSpec else homeSpec

    private fun applyScene() {
        val spec = sceneSpec
        audio.setScene(spec.cityIndex, spec.mood.weather == Weather.RAIN)
    }

    private fun update(next: Player) {
        player = next
        store.save(next)
    }

    fun click() = audio.play(Sfx.CLICK)

    fun open(target: Screen) {
        click()
        screen = target
        applyScene()
    }

    fun goHome() {
        click()
        result = null
        screen = Screen.HOME
        applyScene()
    }

    /** Shows another city on the home screen. Only unlocked levels can be picked. */
    fun selectLevel(level: Int) {
        click()
        update(player.copy(level = level.coerceIn(1, player.reached)))
        applyScene()
    }

    fun play(level: Int = player.level, fresh: Boolean = false) {
        if (level > player.reached) {
            message = "Finish level ${level - 1} to open this one"
            return
        }
        click()
        val spec = Levels.spec(level)
        val saved = player.board?.takeIf { !fresh && it.level == level }
        val session = if (saved != null) {
            val engine = PuzzleEngine(spec.shape.rows, spec.shape.cols, saved.tiles.toIntArray())
            val par = Levels.par(Levels.newEngine(spec), spec.tour)
            Session(engine, par, saved.moves, saved.history, saved.freeLeft, saved.assisted)
        } else {
            Session.start(spec)
        }
        val kept = if (saved == null && player.board?.level == level) null else player.board
        update(player.copy(level = level, board = kept))
        deal(Attempt(spec, session, false, ++deals))
    }

    fun playDaily() {
        click()
        val spec = Levels.daily(today())
        deal(Attempt(spec, Session.start(spec), true, ++deals))
    }

    private fun deal(next: Attempt) {
        attempt = next
        lastHint = null
        winShown = false
        result = null
        hintBusy = false
        turn++
        screen = Screen.GAME
        applyScene()
    }

    fun restart() {
        val a = attempt ?: return
        if (a.daily) playDaily() else play(a.spec.level, fresh = true)
    }

    /** Taps a board cell. Returns false when that tile cannot move. */
    fun press(index: Int): Boolean {
        val a = attempt ?: return false
        if (result != null || a.session.solved) return true
        if (a.session.tap(index).isEmpty()) {
            audio.play(Sfx.BLOCK)
            audio.buzz(14)
            return false
        }
        audio.play(Sfx.SLIDE)
        audio.buzz(8, 60)
        changed(a)
        return true
    }

    private fun changed(a: Attempt) {
        turn++
        if (a.session.solved) {
            finish(a)
        } else if (!a.daily) {
            val s = a.session
            update(player.copy(board = SavedBoard(a.spec.level, s.engine.tiles().toList(), s.moves, s.history, s.freeLeft, s.assisted)))
        }
    }

    private fun finish(a: Attempt) {
        val won = if (a.daily) Rules.winDaily(player, today(), a.session) else Rules.winLevel(player, a.spec.level, a.session)
        update(won.player)
        result = won
    }

    fun undo() {
        val a = attempt ?: return
        if (result != null) return
        if (!a.session.canUndo) {
            message = "Make a move first"
            return
        }
        if (player.undos <= 0) {
            emptyBooster = Item.UNDOS
            return
        }
        if (a.session.undo().isEmpty()) return
        update(player.copy(undos = player.undos - 1))
        audio.play(Sfx.SLIDE)
        changed(a)
    }

    /** Looks for the next tile to move and calls [show] with its number. */
    fun hint(show: (Int) -> Unit) {
        val a = attempt ?: return
        if (result != null || hintBusy) return
        val paid = lastHint
        if (paid != null && paid.first == a.session.engine.tiles().toList()) return show(paid.second)
        if (player.hints <= 0) {
            emptyBooster = Item.HINTS
            return
        }
        click()
        hintBusy = true
        val engine = a.session.engine
        val tiles = engine.tiles()
        viewModelScope.launch {
            val cell = withContext(Dispatchers.Default) { Solver.hint(engine.rows, engine.cols, tiles) }
            if (attempt === a) hintBusy = false
            // ignore the answer if the board moved on while we were thinking
            if (cell == null || attempt !== a || !engine.tiles().contentEquals(tiles)) return@launch
            a.session.markAssisted()
            update(player.copy(hints = player.hints - 1, stats = player.stats.copy(hintsUsed = player.stats.hintsUsed + 1)))
            val tile = engine.tileAt(cell)
            lastHint = tiles.toList() to tile
            show(tile)
        }
    }

    fun useFreeMoves() {
        val a = attempt ?: return
        if (result != null) return
        if (a.session.freeLeft > 0) {
            message = "${a.session.freeLeft} free moves still running"
            return
        }
        if (player.frees <= 0) {
            emptyBooster = Item.FREES
            return
        }
        click()
        a.session.grantFreeMoves(Rules.FREE_MOVES_PER_PACK)
        update(player.copy(frees = player.frees - 1))
        message = "Your next ${Rules.FREE_MOVES_PER_PACK} moves don't count"
        changed(a)
    }

    /** Pays out for a rewarded ad watched to the end. */
    fun refill(item: Item) {
        update(Rules.refill(player, item))
        audio.play(Sfx.COIN)
        message = "You got ${Rules.refillText(item)}"
    }

    fun next() {
        val a = attempt ?: return goHome()
        if (a.daily) goHome() else play(a.spec.level + 1, fresh = true)
    }

    fun buy(item: Item) {
        val next = Rules.buy(player, item)
        if (next == null) {
            message = if (item == Item.FREEZE && player.freezes >= Rules.MAX_FREEZES) "You can hold ${Rules.MAX_FREEZES} freezes" else "Not enough coins"
            return
        }
        audio.play(Sfx.COIN)
        update(next)
    }

    fun chooseSkin(id: String) {
        val next = Rules.chooseSkin(player, id)
        if (next == null) {
            message = "Not enough to unlock this theme yet"
            return
        }
        click()
        update(next)
    }

    fun claim(id: String) {
        audio.play(Sfx.COIN)
        update(Rules.claim(player, id))
    }

    fun addCoins(coins: Int) {
        audio.play(Sfx.COIN)
        update(player.copy(coins = player.coins + coins))
    }

    fun markReviewAsked() = update(player.copy(reviewAsked = true))

    /** Applies a save that came from the cloud. */
    fun mergeCloud(cloud: Player) {
        val merged = Rules.merge(player, cloud)
        // do not swap the board under the player's finger
        update(merged.copy(level = if (screen == Screen.GAME) player.level else merged.level.coerceAtMost(merged.reached)))
    }

    fun toggleMusic() {
        update(player.copy(musicOn = !player.musicOn))
        audio.musicOn = player.musicOn
        message = if (player.musicOn) "Music on" else "Music off"
    }

    fun toggleSound() {
        update(player.copy(soundOn = !player.soundOn))
        audio.soundOn = player.soundOn
        click()
        message = if (player.soundOn) "Sound on" else "Sound off"
    }

    fun toggleHaptics() {
        update(player.copy(hapticsOn = !player.hapticsOn))
        audio.hapticsOn = player.hapticsOn
        audio.buzz(30)
        message = if (player.hapticsOn) "Vibration on" else "Vibration off"
    }

    override fun onCleared() = audio.release()
}
