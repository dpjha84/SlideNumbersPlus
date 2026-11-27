package com.dpjha.slidingnumbersplus.game

import kotlin.math.roundToInt
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
        val cells = shape.rows * shape.cols
        // Scramble depth grows within a city and a little more each tour.
        val depthFactor = 5.0 + step * 1.2 + tour.coerceAtMost(6) * 1.5
        return LevelSpec(
            level = level,
            tour = tour,
            cityIndex = cityIndex,
            city = cities[cityIndex],
            // later visits to a city start on a different sky
            mood = mood(cityIndex, step + globalCity / cities.size * 2),
            shape = shape,
            stepInCity = step,
            scrambleSteps = (cells * depthFactor).roundToInt(),
            seed = 0x5EED0000L + level * 7919L,
        )
    }

    /** One shared puzzle per UTC day. */
    fun daily(epochDay: Long): LevelSpec {
        val shapes = listOf(BoardShape(3, 3), BoardShape(4, 3), BoardShape(4, 4))
        val shape = shapes[(epochDay % shapes.size).toInt()]
        // the first tour has the fully painted skylines
        val cityIndex = (epochDay % ladder.size).toInt()
        return LevelSpec(
            level = 0, tour = 0, cityIndex = cityIndex, city = cities[cityIndex],
            mood = mood(cityIndex, (epochDay % 5).toInt()), shape = shape, stepInCity = 0,
            scrambleSteps = shape.rows * shape.cols * 7, seed = 0xDA11L * 100_000 + epochDay,
        )
    }

    /** Par is how many moves a tidy solve should take. Chain slides count as one move, so this is generous. */
    fun par(engine: PuzzleEngine, tour: Int): Int {
        val raw = engine.manhattan() * 1.2 + 3
        val tighter = raw * (1.0 - 0.04 * tour.coerceAtMost(5))
        return tighter.roundToInt().coerceAtLeast(3)
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
