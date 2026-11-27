package com.dpjha.slidingnumbersplus.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RulesTest {
    /** A session one tap away from solved. */
    private fun nearlySolved(): Session {
        val engine = PuzzleEngine(3, 3, intArrayOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        return Session(engine, par = 3).also { it.tap(8) }
    }

    @Test
    fun sessionCountsMovesAndUndoTakesThemBack() {
        val s = Session(PuzzleEngine(3, 3, intArrayOf(1, 2, 3, 4, 5, 6, 0, 7, 8)), par = 3)
        s.tap(7)
        assertEquals(1, s.moves)
        s.undo()
        assertEquals(0, s.moves)
        assertEquals(6, s.engine.gapIndex())
        assertTrue(s.assisted)
    }

    @Test
    fun freeMovesDoNotCount() {
        val s = Session(PuzzleEngine(3, 3, intArrayOf(1, 2, 3, 4, 5, 6, 0, 7, 8)), par = 3)
        s.grantFreeMoves(1)
        s.tap(7)
        assertEquals(0, s.moves)
        s.tap(8)
        assertEquals(1, s.moves)
        assertTrue(s.solved)
    }

    @Test
    fun winningUnlocksNextLevelAndPaysOnceInFull() {
        val first = Rules.winLevel(Player(), 1, nearlySolved())
        assertEquals(2, first.player.reached)
        assertEquals(2, first.player.level)
        assertEquals(100 + 100, first.player.coins)
        val replay = Rules.winLevel(first.player, 1, nearlySolved())
        assertEquals(10, replay.reward.coins)
        assertEquals(2, replay.player.reached)
    }

    @Test
    fun streakGrowsOpensChestsAndBreaks() {
        var p = Player()
        for (day in 100L..102L) p = Rules.winDaily(p, day, nearlySolved()).player
        assertEquals(3, p.streak)
        assertEquals(2, p.gems)
        assertEquals(3, Rules.liveStreak(p, 103))
        assertEquals(0, Rules.liveStreak(p, 104))
        assertEquals(1, Rules.winDaily(p, 104, nearlySolved()).player.streak)
    }

    @Test
    fun freezeCoversOneMissedDay() {
        val p = Player(streak = 4, lastDaily = 10, freezes = 1)
        val after = Rules.winDaily(p, 12, nearlySolved()).player
        assertEquals(5, after.streak)
        assertEquals(0, after.freezes)
    }

    @Test
    fun dailyPaysOnlyOncePerDay() {
        val once = Rules.winDaily(Player(), 5, nearlySolved()).player
        assertEquals(once.coins, Rules.winDaily(once, 5, nearlySolved()).player.coins)
    }

    @Test
    fun shopAndSkinsNeedFunds() {
        assertNull(Rules.buy(Player(coins = 10), Item.HINTS))
        assertEquals(6, Rules.buy(Player(coins = 500), Item.HINTS)!!.hints)
        assertNull(Rules.chooseSkin(Player(), "brick"))
        assertEquals("neon", Rules.chooseSkin(Player(coins = 600), "neon")!!.skin)
    }

    @Test
    fun achievementsPayOnce() {
        val won = Rules.winLevel(Player(), 1, nearlySolved()).player
        val claimed = Rules.claim(won, "first_win")
        assertEquals(won.coins + 30, claimed.coins)
        assertEquals(claimed, Rules.claim(claimed, "first_win"))
    }

    @Test
    fun playerSurvivesSaveAndLoad() {
        val board = SavedBoard(1, listOf(1, 2, 3, 4, 5, 6, 0, 7, 8), 4, listOf(7, 8), 2, true)
        val p = Player(level = 1, reached = 12, stars = mapOf(1 to 3, 2 to 2), streak = 5, lastDaily = 20000, board = board,
            skin = "neon", ownedSkins = setOf("glass", "neon"), stats = Stats(3, 1, 2, 50, 1, 2, 5), musicOn = false)
        assertEquals(p, Player.decode(p.encode()))
    }

    @Test
    fun damagedSaveFallsBackToDefaults() {
        val p = Player.decode(mapOf("coins" to "abc", "board" to "1|1 2|x", "reached" to "-4"))
        assertEquals(100, p.coins)
        assertEquals(1, p.reached)
        assertNull(p.board)
    }

    @Test
    fun oldPlayersKeepTheirLevel() {
        val p = Player.fromLegacy(level = 14, maxLevel = 17, soundOn = true)
        assertEquals(14, p.level)
        assertEquals(17, p.reached)
        assertEquals(16, p.totalStars)
    }

    @Test
    fun cloudMergeKeepsBestProgressAndLocalLooks() {
        val local = Player(reached = 3, stars = mapOf(1 to 3, 2 to 1), skin = "glass", musicOn = false)
        val cloud = Player(reached = 9, stars = mapOf(2 to 3), skin = "neon", ownedSkins = setOf("glass", "neon"))
        val merged = Rules.merge(local, cloud)
        assertEquals(9, merged.reached)
        assertEquals(mapOf(1 to 3, 2 to 3), merged.stars)
        assertEquals("glass", merged.skin)
        assertEquals(false, merged.musicOn)
    }

    @Test
    fun hintsLeadToASolution() {
        for (level in listOf(1, 3, 5, 6, 8)) {
            val spec = Levels.spec(level)
            val engine = Levels.newEngine(spec)
            var steps = 0
            while (!engine.isSolved() && steps < 200) {
                val cell = Solver.hint(engine.rows, engine.cols, engine.tiles())
                assertNotNull(cell)
                assertTrue(engine.tap(cell).isNotEmpty(), "level $level")
                steps++
            }
            assertTrue(engine.isSolved(), "level $level not solved after $steps hints")
        }
    }

    @Test
    fun dailyPuzzleIsTheSameForEveryoneThatDay() {
        val a = Levels.newEngine(Levels.daily(20_800)).tiles().toList()
        assertEquals(a, Levels.newEngine(Levels.daily(20_800)).tiles().toList())
    }
}
