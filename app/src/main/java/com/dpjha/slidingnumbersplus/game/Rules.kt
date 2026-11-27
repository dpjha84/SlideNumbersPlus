package com.dpjha.slidingnumbersplus.game

data class Reward(val coins: Int = 0, val gems: Int = 0, val xp: Int = 0)

data class Achievement(
    val id: String,
    val title: String,
    val detail: String,
    val target: Int,
    val reward: Reward,
    val progress: (Player) -> Int,
)

enum class Item(val title: String, val detail: String, val coins: Int, val amount: Int) {
    HINTS("3 hints", "Shows the next best tile", 120, 3),
    UNDOS("5 undos", "Take a move back", 80, 5),
    FREES("2 free-move packs", "Each pack: 5 moves that don't count", 150, 2),
    FREEZE("Streak freeze", "Saves your streak if you miss one day", 200, 1),
}

data class Skin(val id: String, val title: String, val coins: Int, val gems: Int)

data class WinResult(
    val player: Player,
    val stars: Int,
    val reward: Reward,
    val firstClear: Boolean,
    /** Streak chest opened by this win, if any. */
    val chest: Reward? = null,
)

object Rules {
    const val FREE_MOVES_PER_PACK = 5

    /** What one rewarded ad gives when a booster runs out. */
    fun refill(player: Player, item: Item): Player = when (item) {
        Item.HINTS -> player.copy(hints = player.hints + 3)
        Item.UNDOS -> player.copy(undos = player.undos + 5)
        Item.FREES -> player.copy(frees = player.frees + 1)
        Item.FREEZE -> player
    }

    fun refillText(item: Item): String = when (item) {
        Item.HINTS -> "3 hints"
        Item.UNDOS -> "5 undos"
        Item.FREES -> "1 free-move pack"
        Item.FREEZE -> ""
    }
    const val AD_COINS = 60
    const val MAX_FREEZES = 2
    val chestDays = listOf(3, 7, 14, 30)

    val skins = listOf(
        Skin("glass", "Glass", 0, 0),
        Skin("neon", "Neon", 600, 0),
        Skin("wood", "Wood", 900, 0),
        Skin("marble", "Marble", 1500, 0),
        Skin("brick", "Brick", 0, 15),
        Skin("stone", "Stone", 0, 25),
    )

    val achievements: List<Achievement> = listOf(
        Achievement("first_win", "First arrival", "Solve your first puzzle", 1, Reward(30, 0, 30)) { it.stats.wins },
        Achievement("wins_10", "Frequent flyer", "Solve 10 puzzles", 10, Reward(60, 1, 60)) { it.stats.wins },
        Achievement("wins_50", "Globetrotter", "Solve 50 puzzles", 50, Reward(200, 5, 150)) { it.stats.wins },
        Achievement("three_star_1", "Perfect landing", "Earn 3 stars", 1, Reward(40, 0, 40)) { it.stats.threeStarWins },
        Achievement("three_star_25", "Star cartographer", "Earn 3 stars 25 times", 25, Reward(150, 4, 120)) { it.stats.threeStarWins },
        Achievement("pure_5", "Local knowledge", "Solve 5 puzzles without boosters", 5, Reward(80, 2, 80)) { it.stats.noAssistWins },
        Achievement("london", "London clear", "Finish all London levels", 5, Reward(80, 2, 80)) { (it.reached - 1).coerceAtMost(5) },
        Achievement("paris", "Paris clear", "Finish all Paris levels", 10, Reward(100, 2, 100)) { (it.reached - 1).coerceAtMost(10) },
        Achievement("new_york", "New York clear", "Finish all New York levels", 15, Reward(120, 3, 120)) { (it.reached - 1).coerceAtMost(15) },
        Achievement("tour_1", "World tourer", "Finish World Tour 1", 35, Reward(500, 10, 300)) { (it.reached - 1).coerceAtMost(35) },
        Achievement("streak_3", "Three in a row", "3 day daily streak", 3, Reward(60, 1, 60)) { it.stats.bestStreak },
        Achievement("streak_7", "Week away", "7 day daily streak", 7, Reward(200, 5, 150)) { it.stats.bestStreak },
        Achievement("streak_30", "Resident", "30 day daily streak", 30, Reward(600, 15, 500)) { it.stats.bestStreak },
    )

    /** XP needed in total to reach rank [n]: 0, 100, 300, 600, 1000... */
    fun xpForRank(n: Int): Int = 50 * n * (n - 1)

    fun rank(xp: Int): Int {
        var r = 1
        while (xp >= xpForRank(r + 1)) r++
        return r
    }

    private fun Player.add(r: Reward) = copy(coins = coins + r.coins, gems = gems + r.gems, xp = xp + r.xp)

    private fun Stats.plusWin(session: Session, stars: Int) = copy(
        wins = wins + 1,
        threeStarWins = threeStarWins + if (stars == 3) 1 else 0,
        noAssistWins = noAssistWins + if (session.assisted) 0 else 1,
        totalMoves = totalMoves + session.moves,
    )

    fun winLevel(player: Player, level: Int, session: Session): WinResult {
        val stars = session.stars
        val first = level !in player.stars
        val best = maxOf(stars, player.stars[level] ?: 0)
        val reward = if (first) Reward(coins = 40 + stars * 20, xp = stars * 20) else Reward(coins = 10, xp = 5)
        val next = player.add(reward).copy(
            stars = player.stars + (level to best),
            reached = maxOf(player.reached, level + 1),
            level = level + 1,
            board = null,
            stats = player.stats.plusWin(session, stars),
        )
        return WinResult(next, stars, reward, first)
    }

    /** The streak as it stands on [today]: zero once more days were missed than freezes cover. */
    fun liveStreak(player: Player, today: Long): Int {
        if (player.lastDaily < 0) return 0
        val missed = today - player.lastDaily - 1
        return if (missed <= 0 || missed <= player.freezes) player.streak else 0
    }

    fun dailyDone(player: Player, today: Long): Boolean = player.lastDaily >= today

    fun dailyCoins(streakBefore: Int): Int = 100 + streakBefore.coerceIn(0, 6) * 20

    fun chestFor(day: Int): Reward? = when (day) {
        3 -> Reward(coins = 100, gems = 2)
        7 -> Reward(coins = 250, gems = 5)
        14 -> Reward(coins = 400, gems = 8)
        30 -> Reward(coins = 1000, gems = 20)
        else -> null
    }

    fun winDaily(player: Player, today: Long, session: Session): WinResult {
        val stars = session.stars
        if (dailyDone(player, today)) {
            return WinResult(player.copy(stats = player.stats.plusWin(session, stars)), stars, Reward(), false)
        }
        val alive = liveStreak(player, today)
        val missed = if (alive > 0) (today - player.lastDaily - 1).coerceAtLeast(0).toInt() else 0
        val streak = alive + 1
        val reward = Reward(coins = dailyCoins(alive), xp = 50)
        val chest = chestFor(streak)
        var next = player.add(reward).copy(
            streak = streak,
            lastDaily = today,
            freezes = player.freezes - missed,
            stats = player.stats.plusWin(session, stars).copy(
                dailyWins = player.stats.dailyWins + 1,
                bestStreak = maxOf(player.stats.bestStreak, streak),
            ),
        )
        if (chest != null) next = next.add(chest)
        return WinResult(next, stars, reward, true, chest)
    }

    fun claimable(player: Player): List<Achievement> =
        achievements.filter { it.id !in player.claimedAchievements && it.progress(player) >= it.target }

    fun claim(player: Player, id: String): Player {
        val a = claimable(player).firstOrNull { it.id == id } ?: return player
        return player.add(a.reward).copy(claimedAchievements = player.claimedAchievements + id)
    }

    /** Returns null when the player cannot buy it. */
    fun buy(player: Player, item: Item): Player? {
        if (player.coins < item.coins) return null
        if (item == Item.FREEZE && player.freezes >= MAX_FREEZES) return null
        val paid = player.copy(coins = player.coins - item.coins)
        return when (item) {
            Item.HINTS -> paid.copy(hints = paid.hints + item.amount)
            Item.UNDOS -> paid.copy(undos = paid.undos + item.amount)
            Item.FREES -> paid.copy(frees = paid.frees + item.amount)
            Item.FREEZE -> paid.copy(freezes = paid.freezes + item.amount)
        }
    }

    /** Selects an owned skin, or buys and selects it. Returns null when the player cannot afford it. */
    fun chooseSkin(player: Player, id: String): Player? {
        val skin = skins.firstOrNull { it.id == id } ?: return null
        if (id in player.ownedSkins) return player.copy(skin = id)
        if (player.coins < skin.coins || player.gems < skin.gems) return null
        return player.copy(
            coins = player.coins - skin.coins,
            gems = player.gems - skin.gems,
            ownedSkins = player.ownedSkins + id,
            skin = id,
        )
    }

    /** Keeps the better of two saves for cloud sync; looks and sound settings stay as on this phone. */
    fun merge(local: Player, cloud: Player): Player {
        val ahead = if (compareValuesBy(cloud, local, { it.reached }, { it.totalStars }, { it.xp }) > 0) cloud else local
        val stars = (local.stars.keys + cloud.stars.keys).associateWith { maxOf(local.stars[it] ?: 0, cloud.stars[it] ?: 0) }
        val daily = if (cloud.lastDaily > local.lastDaily) cloud else local
        return ahead.copy(
            stars = stars,
            reached = maxOf(local.reached, cloud.reached),
            streak = daily.streak,
            lastDaily = daily.lastDaily,
            ownedSkins = local.ownedSkins + cloud.ownedSkins,
            claimedAchievements = local.claimedAchievements + cloud.claimedAchievements,
            skin = local.skin,
            musicOn = local.musicOn,
            soundOn = local.soundOn,
            hapticsOn = local.hapticsOn,
        )
    }
}
