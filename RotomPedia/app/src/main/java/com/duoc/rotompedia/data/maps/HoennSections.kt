package com.duoc.rotompedia.data.maps

import com.duoc.rotompedia.ui.maps.MapBounds
import com.duoc.rotompedia.ui.maps.MapPoint
import com.duoc.rotompedia.ui.maps.MapViewport
import kotlin.math.abs

/**
 * Region-map SECTION grid, not geographic coordinates. Derived from the Emerald game's
 * region-map sections in pret/pokeemerald, and shown on OUR OWN schematic grid, not over
 * the original unsourced PNG. Game map sections may overlap (caves within routes).
 * Source: https://github.com/pret/pokeemerald/blob/0d3100185e0b13faabfc589fc402dd46f83c1d6a/src/data/region_map/region_map_sections.json
 * The coordinates describe the in-game region-map interface only. No item or encounter
 * availability is implied by a cartographic section.
 */
data class HoennSection(val locationSlug: String, val x: Int, val y: Int, val width: Int, val height: Int) {
    init { require(x in 0..27 && y in 0..14 && width > 0 && height > 0 && x + width <= 28 && y + height <= 15) }
    val bounds: MapBounds get() = MapBounds(x / 28f, y / 15f, (x + width) / 28f, (y + height) / 15f)
    val center: MapPoint get() = MapPoint((x + width / 2f) / 28f, (y + height / 2f) / 15f)
}

object HoennSections {
    const val COLUMNS = 28
    const val ROWS = 15
    // No claims that all sections correspond to a route with wild encounters.
    val all: List<HoennSection> = listOf(
        HoennSection("littleroot-town", 4, 11, 1, 1),
        HoennSection("oldale-town", 4, 9, 1, 1),
        HoennSection("dewford-town", 2, 14, 1, 1),
        HoennSection("lavaridge-town", 5, 3, 1, 1),
        HoennSection("fallarbor-town", 3, 0, 1, 1),
        HoennSection("verdanturf-town", 4, 6, 1, 1),
        HoennSection("pacifidlog-town", 17, 10, 1, 1),
        HoennSection("petalburg-city", 1, 9, 1, 1),
        HoennSection("slateport-city", 8, 10, 1, 2),
        HoennSection("mauville-city", 8, 6, 2, 1),
        HoennSection("rustboro-city", 0, 5, 1, 2),
        HoennSection("fortree-city", 12, 0, 1, 1),
        HoennSection("lilycove-city", 18, 3, 2, 1),
        HoennSection("mossdeep-city", 24, 5, 2, 1),
        HoennSection("sootopolis-city", 21, 7, 1, 1),
        HoennSection("ever-grande-city", 27, 8, 1, 2),
        HoennSection("hoenn-route-101", 4, 10, 1, 1),
        HoennSection("hoenn-route-102", 2, 9, 2, 1),
        HoennSection("hoenn-route-103", 4, 8, 4, 1),
        HoennSection("hoenn-route-104", 0, 7, 1, 3),
        HoennSection("hoenn-route-105", 0, 10, 1, 3),
        HoennSection("hoenn-route-106", 0, 13, 2, 1),
        HoennSection("hoenn-route-107", 3, 14, 3, 1),
        HoennSection("hoenn-route-108", 6, 14, 2, 1),
        HoennSection("hoenn-route-109", 8, 12, 1, 3),
        HoennSection("hoenn-route-110", 8, 7, 1, 3),
        HoennSection("hoenn-route-111", 8, 0, 1, 6),
        HoennSection("hoenn-route-112", 6, 3, 2, 1),
        HoennSection("hoenn-route-113", 4, 0, 4, 1),
        HoennSection("hoenn-route-114", 1, 0, 2, 3),
        HoennSection("hoenn-route-115", 0, 2, 1, 3),
        HoennSection("hoenn-route-116", 1, 5, 4, 1),
        HoennSection("hoenn-route-117", 5, 6, 3, 1),
        HoennSection("hoenn-route-118", 10, 6, 2, 1),
        HoennSection("hoenn-route-119", 11, 0, 1, 6),
        HoennSection("hoenn-route-120", 13, 0, 1, 4),
        HoennSection("hoenn-route-121", 14, 3, 4, 1),
        HoennSection("hoenn-route-122", 16, 4, 1, 2),
        HoennSection("hoenn-route-123", 12, 6, 5, 1),
        HoennSection("hoenn-route-124", 20, 3, 4, 3),
        HoennSection("hoenn-route-125", 24, 3, 2, 2),
        HoennSection("hoenn-route-126", 20, 6, 3, 3),
        HoennSection("hoenn-route-127", 23, 6, 3, 3),
        HoennSection("hoenn-route-128", 23, 9, 4, 1),
        HoennSection("hoenn-route-129", 24, 10, 2, 1),
        HoennSection("hoenn-route-130", 21, 10, 3, 1),
        HoennSection("hoenn-route-131", 18, 10, 3, 1),
        HoennSection("hoenn-route-132", 15, 10, 2, 1),
        HoennSection("hoenn-route-133", 12, 10, 3, 1),
        HoennSection("hoenn-route-134", 9, 10, 3, 1),
        HoennSection("granite-cave", 1, 13, 1, 1),
        HoennSection("mt-chimney", 6, 2, 1, 1),
        HoennSection("hoenn-safari-zone", 16, 2, 1, 1),
        HoennSection("petalburg-woods", 0, 8, 1, 1),
        HoennSection("rusturf-tunnel", 2, 5, 1, 1),
        HoennSection("abandoned-ship", 6, 14, 1, 1),
        HoennSection("new-mauville", 8, 7, 1, 1),
        HoennSection("meteor-falls", 0, 3, 1, 1),
        HoennSection("mt-pyre", 16, 4, 1, 1),
        HoennSection("shoal-cave", 24, 4, 1, 1),
        HoennSection("seafloor-cavern", 24, 9, 1, 1),
        HoennSection("hoenn-victory-road", 27, 9, 1, 1),
        HoennSection("cave-of-origin", 21, 7, 1, 1),
        HoennSection("southern-island", 12, 14, 1, 1),
        HoennSection("fiery-path", 6, 3, 1, 1),
        HoennSection("jagged-pass", 6, 3, 1, 1),
        HoennSection("sealed-chamber", 11, 10, 1, 1),
        HoennSection("scorched-slab", 13, 0, 1, 1)
    )

    /** Only positions that match *actual* Emerald pilot locations can be rendered or selected. */
    fun linked(locations: List<MapLocation>): List<Pair<HoennSection, MapLocation>> {
        val bySlug = locations.associateBy(MapLocation::slug)
        return all.mapNotNull { section -> bySlug[section.locationSlug]?.let { section to it } }
    }

    /** If areas overlap, return every audited candidate and let the user decide.
     * The old hit() remains as a compatibility helper for tests and callers. */
    fun hits(viewport: MapViewport, touch: MapPoint, linked: List<Pair<HoennSection, MapLocation>>): List<MapLocation> {
        val map = viewport.toMap(touch) ?: return emptyList()
        return linked.asSequence().filter { (section, _) -> section.bounds.contains(map) }
            .sortedWith(compareBy<Pair<HoennSection, MapLocation>> { it.first.width * it.first.height }
                .thenBy { abs(it.first.center.x - map.x) + abs(it.first.center.y - map.y) })
            .map { it.second }.distinctBy { it.id }.toList()
    }

    fun hit(viewport: MapViewport, touch: MapPoint, linked: List<Pair<HoennSection, MapLocation>>): MapLocation? =
        hits(viewport, touch, linked).firstOrNull()
}
