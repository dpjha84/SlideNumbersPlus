package com.dpjha.slidingnumbersplus.game

import kotlin.random.Random

data class BoardShape(val rows: Int, val cols: Int)

enum class TimeOfDay { SUNRISE, DAY, SUNSET, NIGHT }

enum class Weather { CLEAR, RAIN, SNOW, AUTUMN, SPRING }

/** The sky a level is played under. */
data class Mood(val time: TimeOfDay, val weather: Weather)

data class LevelSpec(
    val level: Int,
    val tour: Int,
    val cityIndex: Int,
    val city: String,
    val mood: Mood,
    val shape: BoardShape,
    val stepInCity: Int,
    val scrambleSteps: Int,
    val seed: Long,
    /** Fewest moves a solver found for this board. Targets are set from it. */
    val best: Int,
)

/** Level rules: a new city every 5 levels, and the board grows one rung of the ladder per city. */
object Levels {
    /** Taller than wide, so boards use a phone screen well. */
    val ladder: List<BoardShape> = listOf(
        BoardShape(3, 3), BoardShape(4, 3), BoardShape(4, 4), BoardShape(5, 4),
        BoardShape(5, 5), BoardShape(6, 5), BoardShape(6, 6),
    )

    val cities: List<String> = listOf(
        "London", "Paris", "New York", "Tokyo", "Sydney", "Dubai", "Rome",
        "Mumbai", "Rio de Janeiro", "Cairo", "Istanbul", "Singapore", "San Francisco", "Barcelona", "Amsterdam",
        "Hong Kong", "Cape Town", "Moscow", "Bangkok", "Prague", "Seoul",
    )

    private fun m(t: TimeOfDay, w: Weather) = Mood(t, w)

    private val SR = TimeOfDay.SUNRISE
    private val D = TimeOfDay.DAY
    private val SS = TimeOfDay.SUNSET
    private val N = TimeOfDay.NIGHT
    private val CL = Weather.CLEAR
    private val RA = Weather.RAIN
    private val SN = Weather.SNOW
    private val AU = Weather.AUTUMN
    private val SP = Weather.SPRING

    /** The five skies a city's levels walk through. Cities beyond the first seven share these patterns. */
    private val moods: List<List<Mood>> = listOf(
        listOf(m(SR, CL), m(D, CL), m(D, RA), m(SS, AU), m(N, SN)),
        listOf(m(SR, CL), m(D, SP), m(D, AU), m(SS, RA), m(N, CL)),
        listOf(m(SR, CL), m(D, AU), m(D, SN), m(SS, CL), m(N, RA)),
        listOf(m(SR, CL), m(D, SP), m(D, SN), m(SS, AU), m(N, CL)),
        listOf(m(SR, CL), m(D, CL), m(D, SP), m(SS, RA), m(N, CL)),
        listOf(m(SR, CL), m(D, CL), m(SS, CL), m(D, RA), m(N, CL)),
        listOf(m(SR, SP), m(D, CL), m(D, AU), m(SS, CL), m(N, SN)),
    )

    fun mood(cityIndex: Int, step: Int): Mood = moods[cityIndex % moods.size][step % LEVELS_PER_CITY]

    const val LEVELS_PER_CITY = 5
    val levelsPerTour: Int get() = ladder.size * LEVELS_PER_CITY

    fun spec(level: Int): LevelSpec {
        require(level >= 1) { "Levels start at 1" }
        val zero = level - 1
        val tour = zero / levelsPerTour
        val inTour = zero % levelsPerTour
        val rung = inTour / LEVELS_PER_CITY
        val step = inTour % LEVELS_PER_CITY
        val globalCity = tour * ladder.size + rung
        val cityIndex = globalCity % cities.size
        val shape = ladder[rung]
        // Tours past the end of the table replay its last tour's boards under new cities and skies.
        val tableTours = BoardTable.levels.size / 3 / levelsPerTour
        val entry = (tour.coerceAtMost(tableTours - 1) * levelsPerTour + inTour) * 3
        return LevelSpec(
            level = level,
            tour = tour,
            cityIndex = cityIndex,
            city = cities[cityIndex],
            // later visits to a city start on a different sky
            mood = mood(cityIndex, step + globalCity / cities.size * 2),
            shape = shape,
            stepInCity = step,
            scrambleSteps = BoardTable.levels[entry + 1],
            seed = BoardTable.LEVEL_SEED + BoardTable.levels[entry],
            best = BoardTable.levels[entry + 2],
        )
    }

    /** One shared puzzle per UTC day. */
    fun daily(epochDay: Long): LevelSpec {
        val shapes = listOf(BoardShape(3, 3), BoardShape(4, 3), BoardShape(4, 4))
        val shapeIndex = (epochDay % shapes.size).toInt()
        val shape = shapes[shapeIndex]
        val pick = (epochDay / shapes.size % BoardTable.DAILY_PER_SHAPE).toInt()
        val entry = (shapeIndex * BoardTable.DAILY_PER_SHAPE + pick) * 3
        // the first tour has the fully painted skylines
        val cityIndex = (epochDay % ladder.size).toInt()
        return LevelSpec(
            level = 0, tour = 0, cityIndex = cityIndex, city = cities[cityIndex],
            mood = mood(cityIndex, (epochDay % 5).toInt()), shape = shape, stepInCity = 0,
            scrambleSteps = BoardTable.daily[entry + 1],
            seed = BoardTable.DAILY_SEED + BoardTable.daily[entry],
            best = BoardTable.daily[entry + 2],
        )
    }

    /**
     * The three-star target: about 2.5 times the best known solution plus two moves of grace,
     * tightening in later tours to 2 times. Two stars reach one and a half times this.
     * Set from play testing: people take well over twice the shortest route.
     */
    fun par(spec: LevelSpec): Int {
        val percent = (250 - 10 * spec.tour).coerceAtLeast(200)
        return (spec.best * percent + 99) / 100 + 2
    }

    fun newEngine(spec: LevelSpec): PuzzleEngine {
        val engine = PuzzleEngine(spec.shape.rows, spec.shape.cols)
        engine.scramble(Random(spec.seed), spec.scrambleSteps)
        return engine
    }

    fun stars(moves: Int, par: Int): Int = when {
        moves <= par -> 3
        moves <= par * 3 / 2 -> 2
        else -> 1
    }
}
