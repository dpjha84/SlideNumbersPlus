package com.dpjha.slidingnumbersplus.ui.art

import androidx.compose.ui.graphics.Color
import com.dpjha.slidingnumbersplus.game.Look

enum class LandmarkType {
    BIG_BEN, EIFFEL, EMPIRE, TOKYO_TOWER, OPERA, BURJ, COLOSSEUM, ARCH, MOUNTAIN, PYRAMIDS,
    DOMES, TRIO, BRIDGE, SPIRES, HOUSES, CLUSTER, PAGODA, NTOWER,
}

/** Colours for one look of a city. far/mid/near are the skyline layers, back to front. */
data class Palette(
    val skyTop: Color, val skyMid: Color, val skyBottom: Color,
    val far: Color, val mid: Color, val near: Color,
)

data class CityArt(
    val name: String,
    val landmark: LandmarkType,
    val hue: Float,
    val day: Palette? = null,
    val night: Palette? = null,
) {
    fun palette(look: Look): Palette = when (look) {
        Look.NIGHT -> night ?: Palettes.night(hue)
        Look.DAY -> day ?: Palettes.day(hue)
        Look.SUNSET -> Palettes.sunset()
    }
}

private fun c(hex: Long) = Color(0xFF000000 or hex)
private fun pal(a: Long, b: Long, cc: Long, d: Long, e: Long, f: Long) = Palette(c(a), c(b), c(cc), c(d), c(e), c(f))

object Palettes {
    fun night(h: Float) = Palette(
        Color.hsv(h, 0.85f, 0.20f), Color.hsv((h + 15f) % 360f, 0.65f, 0.38f), Color.hsv((h + 40f) % 360f, 0.55f, 0.78f),
        Color.hsv(h, 0.55f, 0.38f), Color.hsv(h, 0.60f, 0.26f), Color.hsv(h, 0.70f, 0.13f),
    )

    fun day(h: Float) = Palette(
        Color.hsv(h, 0.45f, 0.88f), Color.hsv(h, 0.20f, 0.96f), Color.hsv(40f, 0.20f, 1f),
        Color.hsv(h, 0.20f, 0.78f), Color.hsv(h, 0.30f, 0.58f), Color.hsv(h, 0.45f, 0.38f),
    )

    fun sunset() = pal(0x2a1a5e, 0xc0507a, 0xffb870, 0x7a4684, 0x4f2a6b, 0x241240)
}

object CityCatalog {
    val all: List<CityArt> = listOf(
        CityArt("London", LandmarkType.BIG_BEN, 225f,
            day = pal(0x6fa8dc, 0xcfe3f2, 0xfbe7c6, 0xb7c9dc, 0x7f93ad, 0x3b4a63),
            night = pal(0x0b1030, 0x26275f, 0x8a5a8c, 0x2a2f5c, 0x1a1d42, 0x0b0d24)),
        CityArt("Paris", LandmarkType.EIFFEL, 300f,
            day = pal(0xf6a5a0, 0xfbd1b0, 0xfff0c9, 0xe2b7b8, 0xb98a9f, 0x6b4f72),
            night = pal(0x150f3a, 0x3a2060, 0xc4587a, 0x3b2358, 0x251440, 0x0f0a24)),
        CityArt("New York", LandmarkType.EMPIRE, 215f,
            day = pal(0x4a90d9, 0x9cc8ee, 0xf3e3cf, 0x9db6d0, 0x6b86a6, 0x2f4561),
            night = pal(0x050a22, 0x14265a, 0x3a5ea0, 0x1b2a58, 0x0f1a3c, 0x060b1f)),
        CityArt("Tokyo", LandmarkType.TOKYO_TOWER, 330f,
            day = pal(0xff9a8b, 0xffc9a8, 0xfff1d0, 0xe6b4b0, 0xb98aa0, 0x5b4570),
            night = pal(0x0d0630, 0x35124f, 0xe0457b, 0x32154f, 0x1d0b38, 0x0a0420)),
        CityArt("Sydney", LandmarkType.OPERA, 195f),
        CityArt("Dubai", LandmarkType.BURJ, 35f),
        CityArt("Rome", LandmarkType.COLOSSEUM, 15f),
        CityArt("Mumbai", LandmarkType.ARCH, 25f),
        CityArt("Rio de Janeiro", LandmarkType.MOUNTAIN, 150f),
        CityArt("Cairo", LandmarkType.PYRAMIDS, 42f),
        CityArt("Istanbul", LandmarkType.DOMES, 280f),
        CityArt("Singapore", LandmarkType.TRIO, 175f),
        CityArt("San Francisco", LandmarkType.BRIDGE, 8f),
        CityArt("Barcelona", LandmarkType.SPIRES, 48f),
        CityArt("Amsterdam", LandmarkType.HOUSES, 205f),
        CityArt("Hong Kong", LandmarkType.CLUSTER, 262f),
        CityArt("Cape Town", LandmarkType.MOUNTAIN, 165f),
        CityArt("Moscow", LandmarkType.DOMES, 350f),
        CityArt("Bangkok", LandmarkType.PAGODA, 52f),
        CityArt("Prague", LandmarkType.SPIRES, 320f),
        CityArt("Seoul", LandmarkType.NTOWER, 240f),
    )

    fun forName(name: String): CityArt = all.firstOrNull { it.name == name } ?: all.first()
}
