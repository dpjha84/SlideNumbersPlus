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
        assertEquals(BoardShape(3, 4), Levels.spec(6).shape)
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
            assertTrue(Levels.par(engine, spec.tour) >= 3, "level $level")
        }
    }

    @Test
    fun sameLevelAlwaysGivesSameBoard() {
        val a = Levels.newEngine(Levels.spec(42)).tiles().toList()
        val b = Levels.newEngine(Levels.spec(42)).tiles().toList()
        assertEquals(a, b)
    }

    @Test
    fun starsFollowPar() {
        assertEquals(3, Levels.stars(10, 10))
        assertEquals(2, Levels.stars(15, 10))
        assertEquals(1, Levels.stars(16, 10))
    }
}
