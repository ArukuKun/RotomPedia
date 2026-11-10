package com.duoc.rotompedia

import com.duoc.rotompedia.data.maps.HoennSections
import com.duoc.rotompedia.data.maps.MapLocation
import com.duoc.rotompedia.ui.maps.MapPoint
import com.duoc.rotompedia.ui.maps.MapViewport
import org.junit.Assert.*
import org.junit.Test

/** Synthetic tap positions over section rectangles from pinned Emerald region-map source. */
class HoennSectionsTest {
    @Test fun `pinned section rectangles are bounded and stable`() {
        assertEquals(68, HoennSections.all.size)
        assertEquals(HoennSections.all.size, HoennSections.all.map { it.locationSlug }.distinct().size)
        assertTrue(HoennSections.all.all { it.bounds.left >= 0f && it.bounds.right <= 1f })
        val littleroot = HoennSections.all.single { it.locationSlug == "littleroot-town" }
        assertEquals(4, littleroot.x)
        assertEquals(11, littleroot.y)
        val route134 = HoennSections.all.single { it.locationSlug == "hoenn-route-134" }
        assertEquals(9, route134.x)
        assertEquals(3, route134.width)
        assertEquals(15, HoennSections.ROWS)
        val fieryPath = HoennSections.all.single { it.locationSlug == "fiery-path" }
        assertEquals(1, fieryPath.width)
    }

    @Test fun `only matching PokeAPI location slugs are clickable`() {
        val route = MapLocation(449, "hoenn-route-101", emptyMap())
        val city = MapLocation(567, "littleroot-town", emptyMap())
        val linked = HoennSections.linked(listOf(route, city, MapLocation(9999,"imaginary-city",emptyMap())))
        assertEquals(2, linked.size)
        val screen = MapViewport.create(560f, 360f, 28f, 15f, 2f)
        val routeCenter = HoennSections.all.single { it.locationSlug == route.slug }.center
        assertEquals(449, HoennSections.hit(screen, screen.toScreen(routeCenter), linked)?.id)
        assertNull(HoennSections.hit(screen, screen.toScreen(MapPoint(0.9f, .1f)), linked))
        assertNull(HoennSections.hit(screen, MapPoint(-20f, -20f), linked))
    }

    @Test fun `overlapping large route and tiny cave chooses the specific location`() {
        val route = MapLocation(475, "hoenn-route-126", emptyMap())
        val cave = MapLocation(443, "cave-of-origin", emptyMap())
        val linked = HoennSections.linked(listOf(route, cave))
        val viewport = MapViewport.create(560f, 360f, 28f, 15f)
        val point = HoennSections.all.single { it.locationSlug == "cave-of-origin" }.center
        assertEquals(cave.id, HoennSections.hit(viewport, viewport.toScreen(point), linked)?.id)
        assertEquals(listOf(cave.id, route.id),
            HoennSections.hits(viewport, viewport.toScreen(point), linked).map { it.id })
    }
}
