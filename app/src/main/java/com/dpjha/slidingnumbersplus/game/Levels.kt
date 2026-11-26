package com.dpjha.slidingnumbersplus.game

import kotlin.math.roundToInt
import kotlin.random.Random

data class BoardShape(val rows: Int, val cols: Int)

/** How a city is drawn. Later tours reuse the cities in new looks. */
enum class Look { NIGHT, SUNSET, DAY }

data class LevelSpec(
    val level: Int,
    val tour: Int,
    val cityIndex: Int,
    val city: String,
    val look: Look,
    val shape: BoardShape,
    val stepInCity: Int,
    val scrambleSteps: Int,
    val seed: Long,
)

/** Level rules: a new city every 5 levels, and the board grows one rung of the ladder per city. */
object Levels {
    val ladder: List<BoardShape> = listOf(
        BoardShape(3, 3), BoardShape(3, 4), BoardShape(4, 4), BoardShape(4, 5),
        BoardShape(5, 5), BoardShape(5, 6), BoardShape(6, 6),
    )

    val cities: List<String> = listOf(
        "London", "Paris", "New York", "Tokyo", "Sydney", "Dubai", "Rome",
        "Mumbai", "Rio de Janeiro", "Cairo", "Istanbul", "Singapore", "San Francisco", "Barcelona", "Amsterdam",
        "Hong Kong", "Cape Town", "Moscow", "Bangkok", "Prague", "Seoul",
    )

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
        val shape = ladder[rung]
        val cells = shape.rows * shape.cols
        // Scramble depth grows within a city and a little more each tour.
        val depthFactor = 5.0 + step * 1.2 + tour * 1.5
        return LevelSpec(
            level = level,
            tour = tour,
            cityIndex = globalCity % cities.size,
            city = cities[globalCity % cities.size],
            look = Look.entries[(globalCity / cities.size) % Look.entries.size],
            shape = shape,
            stepInCity = step,
            scrambleSteps = (cells * depthFactor).roundToInt(),
            seed = 0x5EED0000L + level * 7919L,
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
