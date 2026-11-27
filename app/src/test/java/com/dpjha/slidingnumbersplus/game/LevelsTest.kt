package com.dpjha.slidingnumbersplus.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LevelsTest {
    @Test
    fun newCityEveryFiveLevels() {
        assertEquals("London", Levels.spec(1).city)
        assertEquals("London", Levels.spec(5).city)
        assertEquals("Paris", Levels.spec(6).city)
        assertEquals("Rome", Levels.spec(35).city)
    }

    @Test
    fun boardGrowsAlongTheLadder() {
        assertEquals(BoardShape(3, 3), Levels.spec(1).shape)
        assertEquals(BoardShape(4, 3), Levels.spec(6).shape)
        assertEquals(BoardShape(4, 4), Levels.spec(11).shape)
        assertEquals(BoardShape(6, 6), Levels.spec(35).shape)
    }

    @Test
    fun secondTourRestartsLadderWithNewCities() {
        val s = Levels.spec(36)
        assertEquals(1, s.tour)
        assertEquals(BoardShape(3, 3), s.shape)
        assertEquals("Mumbai", s.city)
    }

    @Test
    fun everyLevelOfThreeToursBuildsSolvableBoardWithPositivePar() {
        for (level in 1..105) {
            val spec = Levels.spec(level)
            val engine = Levels.newEngine(spec)
            assertTrue(PuzzleEngine.isSolvable(engine.tiles(), spec.shape.rows, spec.shape.cols), "level $level")
            assertTrue(Levels.par(spec) > spec.best, "level $level")
        }
    }

    @Test
    fun sameLevelAlwaysGivesSameBoard() {
        val a = Levels.newEngine(Levels.spec(42)).tiles().toList()
        val b = Levels.newEngine(Levels.spec(42)).tiles().toList()
        assertEquals(a, b)
    }

    @Test
    fun difficultyNeverDropsWithinACity() {
        for (level in 1..245) {
            if (level % Levels.LEVELS_PER_CITY == 0) continue
            assertTrue(Levels.spec(level + 1).best >= Levels.spec(level).best, "after level $level")
        }
    }

    /** The table's best-move numbers only make sense if these exact boards are dealt. */
    @Test
    fun dealtBoardsMatchTheOnesThatWereSolved() {
        fun hash(specs: List<LevelSpec>) = specs.fold(1125899906842597L) { acc, spec ->
            Levels.newEngine(spec).tiles().fold(acc) { x, tile -> x * 31 + tile }
        }
        assertEquals(LEVEL_HASH, hash((1..245).map { Levels.spec(it) }))
        // daily() walks shapes in turn, so list each shape's days together, as the table does
        val days = (0 until 3).flatMap { shape -> (0 until 40).map { Levels.daily(it * 3L + shape) } }
        assertEquals(DAILY_HASH, hash(days))
    }

    @Test
    fun threeByThreeBestMovesAreExact() {
        val distance = distancesFromSolved()
        val specs = (1..245).map { Levels.spec(it) } + (0 until 40).map { Levels.daily(it * 3L) }
        for (spec in specs.filter { it.shape == BoardShape(3, 3) }) {
            val key = Levels.newEngine(spec).tiles().joinToString("")
            assertEquals(distance[key], spec.best, "level ${spec.level} seed ${spec.seed}")
        }
    }

    /** Breadth-first search over every 3 x 3 position, one tap (chain slides included) per move. */
    private fun distancesFromSolved(): Map<String, Int> {
        val start = PuzzleEngine(3, 3).tiles()
        val seen = hashMapOf(start.joinToString("") to 0)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val tiles = queue.removeFirst()
            val next = seen.getValue(tiles.joinToString("")) + 1
            for (cell in 0 until 9) {
                val engine = PuzzleEngine(3, 3, tiles)
                if (engine.tap(cell).isEmpty()) continue
                val moved = engine.tiles()
                val key = moved.joinToString("")
                if (key !in seen) {
                    seen[key] = next
                    queue.add(moved)
                }
            }
        }
        return seen
    }

    @Test
    fun starsFollowPar() {
        assertEquals(3, Levels.stars(10, 10))
        assertEquals(2, Levels.stars(15, 10))
        assertEquals(1, Levels.stars(16, 10))
    }

    private companion object {
        const val LEVEL_HASH = -7269066373528903024L
        const val DAILY_HASH = -4660079257420608671L
    }
}
