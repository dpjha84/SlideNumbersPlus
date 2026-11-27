package com.dpjha.slidingnumbersplus.game

/**
 * One attempt at a board: moves, undo history and the "free moves" booster.
 * [history] holds the gap position before each move, so undo is simply tapping that cell.
 */
class Session(
    val engine: PuzzleEngine,
    val par: Int,
    moves: Int = 0,
    history: List<Int> = emptyList(),
    freeLeft: Int = 0,
    assisted: Boolean = false,
) {
    var moves: Int = moves
        private set
    var freeLeft: Int = freeLeft
        private set
    /** True once a hint, undo or free moves were used. */
    var assisted: Boolean = assisted
        private set
    private val past = ArrayList(history)

    val history: List<Int> get() = past.toList()
    val canUndo: Boolean get() = past.isNotEmpty() && !engine.isSolved()
    val solved: Boolean get() = engine.isSolved()
    val stars: Int get() = Levels.stars(moves, par)

    /** Slides the tile at [index] (and any between it and the gap). Empty when blocked. */
    fun tap(index: Int): List<TileMove> {
        if (solved) return emptyList()
        val gap = engine.gapIndex()
        val moved = engine.tap(index)
        if (moved.isEmpty()) return moved
        past.add(gap)
        if (past.size > MAX_HISTORY) past.removeAt(0)
        if (freeLeft > 0) freeLeft-- else moves++
        return moved
    }

    /** Takes the last move back, and takes it off the counter. */
    fun undo(): List<TileMove> {
        if (!canUndo) return emptyList()
        val moved = engine.tap(past.removeAt(past.size - 1))
        if (moved.isNotEmpty()) {
            moves = (moves - 1).coerceAtLeast(0)
            assisted = true
        }
        return moved
    }

    fun grantFreeMoves(count: Int) {
        freeLeft += count
        assisted = true
    }

    fun markAssisted() {
        assisted = true
    }

    companion object {
        const val MAX_HISTORY = 400

        fun start(spec: LevelSpec): Session {
            val engine = Levels.newEngine(spec)
            return Session(engine, Levels.par(engine, spec.tour))
        }
    }
}
