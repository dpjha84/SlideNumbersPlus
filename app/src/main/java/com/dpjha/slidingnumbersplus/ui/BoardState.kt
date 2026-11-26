package com.dpjha.slidingnumbersplus.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.PuzzleEngine
import com.dpjha.slidingnumbersplus.game.TileMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Visual state of one tile. Position is animated separately from the game logic. */
class TileVisual(val tile: Int) {
    val x = Animatable(0f)
    val y = Animatable(0f)
    var drag by mutableStateOf(Offset.Zero)
    var lifted by mutableStateOf(false)
    var inPlace by mutableStateOf(false)
}

/**
 * Holds the engine and the tile visuals. The engine updates the instant a move is made;
 * the visuals chase it with a very stiff critically damped spring, so quick taps never wait.
 */
class BoardState(val engine: PuzzleEngine, private val scope: CoroutineScope) {
    val rows = engine.rows
    val cols = engine.cols
    val visuals: List<TileVisual> = (1 until engine.size).map { TileVisual(it) }
    var moves by mutableIntStateOf(0)
        private set
    var solved by mutableStateOf(false)
        private set
    var laidOut by mutableStateOf(false)
        private set
    var cellPx = 0f
        private set
    var gapPx = 0f
        private set

    private val settle = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
    private var onMove: () -> Unit = {}
    private var onBlocked: () -> Unit = {}
    private var onSolved: () -> Unit = {}

    fun setCallbacks(move: () -> Unit, blocked: () -> Unit, solved: () -> Unit) {
        onMove = move
        onBlocked = blocked
        onSolved = solved
    }

    val step: Float get() = cellPx + gapPx

    fun cellOrigin(index: Int): Offset = Offset(gapPx + (index % cols) * step, gapPx + (index / cols) * step)

    fun indexAt(p: Offset): Int {
        val c = ((p.x - gapPx / 2) / step).toInt().coerceIn(0, cols - 1)
        val r = ((p.y - gapPx / 2) / step).toInt().coerceIn(0, rows - 1)
        return r * cols + c
    }

    fun layout(cell: Float, gap: Float) {
        cellPx = cell
        gapPx = gap
        refreshInPlace()
        scope.launch {
            for (v in visuals) {
                val o = cellOrigin(engine.indexOfTile(v.tile))
                v.x.snapTo(o.x)
                v.y.snapTo(o.y)
            }
            laidOut = true
        }
    }

    private fun visualOf(tile: Int) = visuals[tile - 1]

    private fun refreshInPlace() {
        for (v in visuals) v.inPlace = engine.isInPlace(engine.indexOfTile(v.tile))
    }

    /** Applies a finished move: animates each moved tile from where it is drawn now to its new cell. */
    private fun applyMoves(list: List<TileMove>) {
        for (m in list) {
            val v = visualOf(m.tile)
            val to = cellOrigin(m.to)
            val dragged = v.drag
            v.drag = Offset.Zero
            scope.launch {
                v.x.snapTo(v.x.value + dragged.x)
                v.y.snapTo(v.y.value + dragged.y)
                launch { v.x.animateTo(to.x, settle) }
                launch { v.y.animateTo(to.y, settle) }
            }
        }
        moves++
        refreshInPlace()
        onMove()
        if (engine.isSolved()) {
            solved = true
            onSolved()
        }
    }

    fun tap(index: Int) {
        if (solved) return
        val list = engine.tap(index)
        if (list.isEmpty()) bump(index) else applyMoves(list)
    }

    /** Short nudge against the stop for a tile that cannot move. */
    fun bump(index: Int) {
        val t = engine.tileAt(index)
        if (t == PuzzleEngine.EMPTY) return
        val v = visualOf(t)
        val sign = if (t % 2 == 0) 1f else -1f
        scope.launch {
            v.drag = Offset(sign * cellPx * 0.07f, 0f)
            kotlinx.coroutines.delay(45)
            v.drag = Offset.Zero
        }
        onBlocked()
    }

    // ---- drag ----
    private var dragIndex = -1
    private var dragChain: List<Int> = emptyList()
    private var dragDir = Offset.Zero
    private var dragDone = false

    fun dragStart(index: Int) {
        if (solved) return
        dragIndex = index
        dragDone = false
        dragChain = engine.chainFrom(index)
        val d = engine.directionToGap(index)
        dragDir = if (d == null) Offset.Zero else Offset(d.dc.toFloat(), d.dr.toFloat())
        val t = engine.tileAt(index)
        if (t != PuzzleEngine.EMPTY) visualOf(t).lifted = true
    }

    /** [total] is the finger movement since the drag began. A swipe commits as soon as it passes 20% of a cell. */
    fun dragMove(total: Offset) {
        if (dragIndex < 0 || dragDone || solved) return
        if (dragChain.isEmpty()) {
            val v = visualOf(engine.tileAt(dragIndex).takeIf { it != PuzzleEngine.EMPTY } ?: return)
            v.drag = Offset((total.x * 0.2f).coerceIn(-cellPx * 0.12f, cellPx * 0.12f), (total.y * 0.2f).coerceIn(-cellPx * 0.12f, cellPx * 0.12f))
            return
        }
        val along = (total.x * dragDir.x + total.y * dragDir.y).coerceIn(0f, step)
        val offset = Offset(dragDir.x * along, dragDir.y * along)
        for (idx in dragChain) visualOf(engine.tileAt(idx)).drag = offset
        if (along > step * 0.2f) {
            dragDone = true
            commitDrag()
        }
    }

    private fun commitDrag() {
        val d = engine.directionToGap(dragIndex) ?: return
        clearLift()
        val list = engine.slide(dragIndex, d.dr, d.dc)
        applyMoves(list)
    }

    fun dragEnd(velocityAlong: Float) {
        if (dragIndex < 0) return
        if (!dragDone && dragChain.isNotEmpty()) {
            val along = dragChain.firstOrNull()?.let { visualOf(engine.tileAt(it)).drag }?.let { it.x * dragDir.x + it.y * dragDir.y } ?: 0f
            if (velocityAlong > 300f && along > 2f) {
                dragDone = true
                commitDrag()
            } else {
                springBack()
            }
        } else if (!dragDone) {
            springBack()
        }
        clearLift()
        dragIndex = -1
        dragChain = emptyList()
    }

    private fun clearLift() {
        for (v in visuals) v.lifted = false
    }

    private fun springBack() {
        for (v in visuals) {
            val d = v.drag
            if (d == Offset.Zero) continue
            v.drag = Offset.Zero
            scope.launch {
                v.x.snapTo(v.x.value + d.x)
                v.y.snapTo(v.y.value + d.y)
                launch { v.x.animateTo(cellOrigin(engine.indexOfTile(v.tile)).x, settle) }
                launch { v.y.animateTo(cellOrigin(engine.indexOfTile(v.tile)).y, settle) }
            }
        }
    }

    companion object {
        fun forLevel(level: Int, scope: CoroutineScope): Pair<BoardState, Int> {
            val spec = Levels.spec(level)
            val engine = Levels.newEngine(spec)
            return BoardState(engine, scope) to Levels.par(engine, spec.tour)
        }
    }
}
