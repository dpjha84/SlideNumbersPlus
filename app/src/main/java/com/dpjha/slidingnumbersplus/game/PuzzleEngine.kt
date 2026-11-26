package com.dpjha.slidingnumbersplus.game

import kotlin.math.abs
import kotlin.random.Random

/** One tile that changed cell during a move. Indices are row * cols + col. */
data class TileMove(val tile: Int, val from: Int, val to: Int)

/** A step toward the gap: dr/dc are each -1, 0 or 1 and exactly one is non-zero. */
data class Direction(val dr: Int, val dc: Int)

/**
 * Rectangular sliding puzzle. Tiles are 1..(rows*cols-1); 0 is the gap.
 * The solved board is 1,2,3,...,0 read left to right, top to bottom.
 * Every scramble is a random walk of legal moves, so it is always solvable.
 */
class PuzzleEngine(val rows: Int, val cols: Int, initial: IntArray? = null) {
    val size: Int = rows * cols
    private val cells: IntArray

    init {
        require(rows in MIN_SIDE..MAX_SIDE && cols in MIN_SIDE..MAX_SIDE) { "Board side must be $MIN_SIDE..$MAX_SIDE" }
        cells = initial?.copyOf() ?: IntArray(size) { goalAt(it) }
        require(cells.size == size && cells.sorted() == (0 until size).toList()) { "Invalid tile set" }
    }

    fun tiles(): IntArray = cells.copyOf()
    fun tileAt(index: Int): Int = cells[index]
    fun gapIndex(): Int = cells.indexOf(EMPTY)
    fun goalAt(index: Int): Int = if (index == size - 1) EMPTY else index + 1
    fun isInPlace(index: Int): Boolean = cells[index] != EMPTY && cells[index] == goalAt(index)
    fun isSolved(): Boolean = (0 until size).all { cells[it] == goalAt(it) }
    fun indexOfTile(tile: Int): Int = cells.indexOf(tile)

    /** Direction a tap on [index] would slide it, or null when the tile is not in line with the gap. */
    fun directionToGap(index: Int): Direction? {
        if (index !in 0 until size || cells[index] == EMPTY) return null
        val gap = gapIndex()
        val r = index / cols
        val c = index % cols
        val gr = gap / cols
        val gc = gap % cols
        return when {
            r == gr && c != gc -> Direction(0, if (gc > c) 1 else -1)
            c == gc && r != gr -> Direction(if (gr > r) 1 else -1, 0)
            else -> null
        }
    }

    /** The tiles that would move if [index] slides toward the gap, nearest the gap first. Empty when blocked. */
    fun chainFrom(index: Int): List<Int> {
        val d = directionToGap(index) ?: return emptyList()
        val out = ArrayList<Int>()
        var r = index / cols
        var c = index % cols
        while (cells[r * cols + c] != EMPTY) {
            out.add(r * cols + c)
            r += d.dr
            c += d.dc
        }
        out.reverse()
        return out
    }

    /**
     * Slides [index] and every tile between it and the gap one step toward the gap.
     * [dr]/[dc] must match the direction to the gap, otherwise the move is blocked.
     */
    fun slide(index: Int, dr: Int, dc: Int): List<TileMove> {
        val d = directionToGap(index) ?: return emptyList()
        if (d.dr != dr || d.dc != dc) return emptyList()
        val chain = chainFrom(index) // gap-nearest first
        val moves = ArrayList<TileMove>(chain.size)
        var target = gapIndex()
        for (from in chain) {
            moves.add(TileMove(cells[from], from, target))
            cells[target] = cells[from]
            target = from
        }
        cells[target] = EMPTY
        return moves
    }

    fun tap(index: Int): List<TileMove> {
        val d = directionToGap(index) ?: return emptyList()
        return slide(index, d.dr, d.dc)
    }

    /** Random walk of [steps] legal single-tile moves from the solved board. Never ends solved. */
    fun scramble(random: Random, steps: Int) {
        for (i in 0 until size) cells[i] = goalAt(i)
        var previousGap = -1
        var done = 0
        while (done < steps || isSolved()) {
            val gap = gapIndex()
            val r = gap / cols
            val c = gap % cols
            val options = ArrayList<Int>(4)
            if (r > 0) options.add(gap - cols)
            if (r < rows - 1) options.add(gap + cols)
            if (c > 0) options.add(gap - 1)
            if (c < cols - 1) options.add(gap + 1)
            if (options.size > 1) options.remove(previousGap)
            val pick = options[random.nextInt(options.size)]
            cells[gap] = cells[pick]
            cells[pick] = EMPTY
            previousGap = gap
            done++
        }
    }

    /** Sum of distances every tile is from its home cell. A lower bound on single-tile moves. */
    fun manhattan(): Int {
        var sum = 0
        for (i in 0 until size) {
            val t = cells[i]
            if (t == EMPTY) continue
            val home = t - 1
            sum += abs(i / cols - home / cols) + abs(i % cols - home % cols)
        }
        return sum
    }

    fun inPlaceCount(): Int = (0 until size).count { isInPlace(it) }

    companion object {
        const val EMPTY = 0
        const val MIN_SIDE = 3
        const val MAX_SIDE = 6

        fun isSolvable(tiles: IntArray, rows: Int, cols: Int): Boolean {
            val numbered = tiles.filter { it != EMPTY }
            var inversions = 0
            for (i in numbered.indices) for (j in i + 1 until numbered.size) if (numbered[i] > numbered[j]) inversions++
            return if (cols % 2 == 1) {
                inversions % 2 == 0
            } else {
                val blankRowFromBottom = rows - tiles.indexOf(EMPTY) / cols
                (inversions + blankRowFromBottom) % 2 == 1
            }
        }
    }
}
