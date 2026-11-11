package com.duoc.rotompedia

import com.duoc.rotompedia.data.maps.*
import com.duoc.rotompedia.ui.maps.*
import org.junit.Assert.*
import org.junit.Test

class HoennAtlasProjectionTest {
    @Test fun `town centres match inspected pixel anchors in CC0 image`() {
        val fallarbor = HoennAtlasProjection.fromGrid(3.5f, .5f)
        val oldale = HoennAtlasProjection.fromGrid(4.5f, 9.5f)
        assertEquals(227f, fallarbor.x * HoennAtlasProjection.WIDTH, 2f)
        assertEquals(148f, fallarbor.y * HoennAtlasProjection.HEIGHT, 1f)
        assertEquals(278f, oldale.x * HoennAtlasProjection.WIDTH, 2f)
        assertEquals(598f, oldale.y * HoennAtlasProjection.HEIGHT, 1f)
        HoennSections.all.forEach { HoennAtlasProjection.bounds(it) } // all finite and inside the image
    }

    @Test fun `route 102 selection follows the same fit zoom and pan as art`() {
        val section = HoennSections.all.first { it.locationSlug == "hoenn-route-102" }
        val location = MapLocation(450, section.locationSlug, mapOf("es" to "Ruta 102"))
        val bounds = HoennAtlasProjection.bounds(section)
        for ((w, h) in listOf(320f to 280f, 700f to 220f)) {
            val view = MapViewport.create(w, h, HoennAtlasProjection.WIDTH, HoennAtlasProjection.HEIGHT).focusOn(bounds)
            val centre = MapPoint((bounds.left + bounds.right) / 2, (bounds.top + bounds.bottom) / 2)
            assertEquals(listOf(location), HoennAtlasProjection.hits(view, view.toScreen(centre), listOf(section to location)))
            assertTrue(HoennAtlasProjection.hits(view, MapPoint(-1f, -1f), listOf(section to location)).isEmpty())
            val inverse = view.toMap(view.toScreen(centre))!!
            assertEquals(centre.x, inverse.x, .00001f)
            assertEquals(centre.y, inverse.y, .00001f)
        }
    }

    @Test fun `route 112 and its caves remain explicit separate candidates`() {
        val linked = HoennSections.all.mapIndexed { i, s -> s to MapLocation(i + 1, s.locationSlug, emptyMap()) }
        val section = linked.first { it.first.locationSlug == "fiery-path" }.first
        val view = MapViewport.create(600f, 400f, HoennAtlasProjection.WIDTH, HoennAtlasProjection.HEIGHT)
        val tap = view.toScreen(HoennAtlasProjection.fromGrid(section.x + .5f, section.y + .5f))
        val slugs = HoennAtlasProjection.hits(view, tap, linked).map { it.slug }
        assertTrue(slugs.containsAll(listOf("fiery-path", "jagged-pass", "hoenn-route-112")))
    }
}
