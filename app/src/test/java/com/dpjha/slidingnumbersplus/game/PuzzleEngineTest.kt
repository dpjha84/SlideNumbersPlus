package com.dpjha.slidingnumbersplus.game

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PuzzleEngineTest {
    @Test
    fun newBoardIsSolved() {
        val e = PuzzleEngine(3, 4)
        assertTrue(e.isSolved())
        assertEquals(0, e.manhattan())
        assertEquals(11, e.inPlaceCount())
    }

    @Test
    fun scramblesAreAlwaysSolvableAndNeverSolved() {
        for (shape in Levels.ladder) {
            repeat(60) { seed ->
                val e = PuzzleEngine(shape.rows, shape.cols)
                e.scramble(Random(seed), 4 + seed)
                assertTrue(PuzzleEngine.isSolvable(e.tiles(), shape.rows, shape.cols), "$shape seed $seed")
                assertFalse(e.isSolved(), "$shape seed $seed")
            }
        }
    }

    @Test
    fun singleSlideMovesOneTileIntoGap() {
        val e = PuzzleEngine(3, 3)
        val moves = e.tap(7) // tile 8, left of the gap at index 8
        assertEquals(listOf(TileMove(8, 7, 8)), moves)
        assertEquals(7, e.gapIndex())
    }

    @Test
    fun chainSlideMovesEveryTileBetweenTileAndGap() {
        val e = PuzzleEngine(4, 4)
        val moves = e.tap(12) // bottom row: tiles 13,14,15 then gap
        assertEquals(3, moves.size)
        assertEquals(listOf(15, 14, 13), moves.map { it.tile })
        assertEquals(12, e.gapIndex())
        assertEquals(listOf(0, 13, 14, 15), (12..15).map { e.tileAt(it) })
    }

    @Test
    fun tileNotInLineWithGapIsBlocked() {
        val e = PuzzleEngine(3, 3)
        assertTrue(e.tap(0).isEmpty())
        assertTrue(e.chainFrom(0).isEmpty())
        assertEquals(null, e.directionToGap(0))
    }

    @Test
    fun slideInWrongDirectionIsBlocked() {
        val e = PuzzleEngine(3, 3)
        assertTrue(e.slide(7, 0, -1).isEmpty())
        assertTrue(e.isSolved())
    }

    @Test
    fun movingATileAndBackRestoresBoard() {
        val e = PuzzleEngine(4, 5)
        e.scramble(Random(7), 40)
        val before = e.tiles()
        val gap = e.gapIndex()
        val neighbour = if (gap % 5 > 0) gap - 1 else gap + 1
        assertTrue(e.tap(neighbour).isNotEmpty())
        assertEquals(neighbour, e.gapIndex())
        assertTrue(e.tap(gap).isNotEmpty())
        assertEquals(before.toList(), e.tiles().toList())
    }

    @Test
    fun solvableRuleMatchesKnownBoards() {
        // 3x3 swapped last two tiles is unsolvable
        assertFalse(PuzzleEngine.isSolvable(intArrayOf(1, 2, 3, 4, 5, 6, 8, 7, 0), 3, 3))
        // 3x4 swapped two tiles is unsolvable, solved board is solvable
        assertTrue(PuzzleEngine.isSolvable(IntArray(12) { if (it == 11) 0 else it + 1 }, 3, 4))
        assertFalse(PuzzleEngine.isSolvable(intArrayOf(2, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0), 3, 4))
    }
}
