package com.dpjha.slidingnumbersplus.ui

import com.dpjha.slidingnumbersplus.game.Levels
import com.dpjha.slidingnumbersplus.game.Look
import com.dpjha.slidingnumbersplus.ui.art.CityCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CityCatalogTest {
    @Test
    fun everyCityInTheLevelRosterHasArt() {
        for (name in Levels.cities) {
            assertEquals(name, CityCatalog.forName(name).name, "missing art for $name")
        }
    }

    @Test
    fun cityNamesAreUnique() {
        val names = CityCatalog.all.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun everyCityHasOpaquePalettesForEveryLook() {
        for (art in CityCatalog.all) for (look in Look.entries) {
            val p = art.palette(look)
            assertTrue(listOf(p.skyTop, p.skyMid, p.skyBottom, p.far, p.mid, p.near).all { it.alpha == 1f }, "${art.name} $look")
        }
    }
}
