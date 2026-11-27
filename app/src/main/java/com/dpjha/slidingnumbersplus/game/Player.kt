package com.dpjha.slidingnumbersplus.game

/** A board left in progress, so the player can resume it. */
data class SavedBoard(
    val level: Int,
    val tiles: List<Int>,
    val moves: Int,
    val history: List<Int>,
    val freeLeft: Int,
    val assisted: Boolean,
)

data class Stats(
    val wins: Int = 0,
    val threeStarWins: Int = 0,
    val noAssistWins: Int = 0,
    val totalMoves: Int = 0,
    val hintsUsed: Int = 0,
    val dailyWins: Int = 0,
    val bestStreak: Int = 0,
)

/** Everything that is saved about the player. Immutable: every change returns a new copy. */
data class Player(
    /** Level that Continue opens. */
    val level: Int = 1,
    /** Highest level that is unlocked. */
    val reached: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val coins: Int = 100,
    val gems: Int = 0,
    val xp: Int = 0,
    val hints: Int = 3,
    val undos: Int = 5,
    val frees: Int = 2,
    val freezes: Int = 0,
    val streak: Int = 0,
    /** UTC epoch day of the last daily win, or -1. */
    val lastDaily: Long = -1,
    val claimedAchievements: Set<String> = emptySet(),
    val skin: String = "glass",
    val ownedSkins: Set<String> = setOf("glass"),
    val stats: Stats = Stats(),
    val board: SavedBoard? = null,
    val musicOn: Boolean = true,
    val soundOn: Boolean = true,
    val hapticsOn: Boolean = true,
    val reviewAsked: Boolean = false,
) {
    val totalStars: Int get() = stars.values.sum()

    fun encode(): Map<String, String> = buildMap {
        put("v", "1")
        put("level", level.toString())
        put("reached", reached.toString())
        put("stars", stars.entries.joinToString(",") { "${it.key}:${it.value}" })
        put("coins", coins.toString())
        put("gems", gems.toString())
        put("xp", xp.toString())
        put("hints", hints.toString())
        put("undos", undos.toString())
        put("frees", frees.toString())
        put("freezes", freezes.toString())
        put("streak", streak.toString())
        put("lastDaily", lastDaily.toString())
        put("achievements", claimedAchievements.joinToString(","))
        put("skin", skin)
        put("skins", ownedSkins.joinToString(","))
        put(
            "stats",
            listOf(stats.wins, stats.threeStarWins, stats.noAssistWins, stats.totalMoves, stats.hintsUsed, stats.dailyWins, stats.bestStreak)
                .joinToString(","),
        )
        put(
            "board",
            board?.let {
                listOf(it.level, it.tiles.joinToString(" "), it.moves, it.history.joinToString(" "), it.freeLeft, if (it.assisted) 1 else 0)
                    .joinToString("|")
            } ?: "",
        )
        put("music", musicOn.toString())
        put("sound", soundOn.toString())
        put("haptics", hapticsOn.toString())
        put("reviewAsked", reviewAsked.toString())
    }

    companion object {
        /** Reads a saved map. Any missing or damaged value falls back to its default. */
        fun decode(map: Map<String, String>): Player {
            val d = Player()
            fun int(key: String, default: Int, min: Int = 0) = (map[key]?.toIntOrNull() ?: default).coerceAtLeast(min)
            fun set(key: String) = map[key]?.split(',')?.filter { it.isNotBlank() }?.toSet()
            val reached = int("reached", d.reached, 1)
            val stats = map["stats"]?.split(',')?.map { it.toIntOrNull() ?: 0 }.orEmpty()
            fun stat(i: Int) = stats.getOrElse(i) { 0 }
            val skins = (set("skins") ?: d.ownedSkins) + "glass"
            return Player(
                level = int("level", d.level, 1).coerceAtMost(reached),
                reached = reached,
                stars = map["stars"].orEmpty().split(',').mapNotNull {
                    val parts = it.split(':')
                    val level = parts.getOrNull(0)?.toIntOrNull()
                    val s = parts.getOrNull(1)?.toIntOrNull()
                    if (level != null && s != null) level to s.coerceIn(1, 3) else null
                }.toMap(),
                coins = int("coins", d.coins),
                gems = int("gems", d.gems),
                xp = int("xp", d.xp),
                hints = int("hints", d.hints),
                undos = int("undos", d.undos),
                frees = int("frees", d.frees),
                freezes = int("freezes", d.freezes),
                streak = int("streak", d.streak),
                lastDaily = map["lastDaily"]?.toLongOrNull() ?: -1,
                claimedAchievements = set("achievements") ?: emptySet(),
                skin = map["skin"]?.takeIf { it in skins } ?: "glass",
                ownedSkins = skins,
                stats = Stats(stat(0), stat(1), stat(2), stat(3), stat(4), stat(5), stat(6)),
                board = decodeBoard(map["board"].orEmpty()),
                musicOn = map["music"]?.toBooleanStrictOrNull() ?: true,
                soundOn = map["sound"]?.toBooleanStrictOrNull() ?: true,
                hapticsOn = map["haptics"]?.toBooleanStrictOrNull() ?: true,
                reviewAsked = map["reviewAsked"]?.toBooleanStrictOrNull() ?: false,
            )
        }

        private fun decodeBoard(text: String): SavedBoard? {
            val p = text.split('|')
            if (p.size != 6) return null
            fun ints(s: String): List<Int>? {
                val words = s.split(' ').filter { it.isNotBlank() }
                val numbers = words.mapNotNull { it.toIntOrNull() }
                return numbers.takeIf { it.size == words.size }
            }
            val level = p[0].toIntOrNull()?.takeIf { it >= 1 } ?: return null
            val shape = Levels.spec(level).shape
            val size = shape.rows * shape.cols
            val tiles = ints(p[1]) ?: return null
            if (tiles.size != size || tiles.sorted() != (0 until size).toList()) return null
            if (!PuzzleEngine.isSolvable(tiles.toIntArray(), shape.rows, shape.cols)) return null
            val history = ints(p[3])?.filter { it in 0 until size } ?: return null
            return SavedBoard(level, tiles, p[2].toIntOrNull() ?: return null, history, p[4].toIntOrNull() ?: 0, p[5] == "1")
        }

        /**
         * A player coming from an older build starts at the level they had reached.
         * Cleared levels get one star, so they can be replayed for more.
         */
        fun fromLegacy(level: Int, maxLevel: Int, soundOn: Boolean): Player {
            val reached = maxOf(level, maxLevel, 1).coerceAtMost(10_000)
            return Player(
                level = level.coerceIn(1, reached),
                reached = reached,
                stars = (1 until reached).associateWith { 1 },
                coins = 100 + (reached - 1).coerceAtMost(100) * 10,
                stats = Stats(wins = reached - 1),
                soundOn = soundOn,
                musicOn = soundOn,
            )
        }
    }
}
