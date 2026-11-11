package com.duoc.rotompedia.ui.maps

import com.duoc.rotompedia.data.maps.HoennSection
import com.duoc.rotompedia.data.maps.MapLocation

/** Calibration for Ztash's CC0 recreation, NOT the old game bitmap.
 * Pixel anchors inspected in the 1507x990 original: Fallarbor (226,148),
 * Oldale (278,598), Littleroot (278,698), Dewford (176,848).
 * Approximate section selection; not a claim of surveyed geographical polygons.
 */
object HoennAtlasProjection {
    const val WIDTH = 1507f
    const val HEIGHT = 990f
    fun fromGrid(x: Float, y: Float) = MapPoint((48.5f + 51f * x) / WIDTH, (123f + 50f * y) / HEIGHT)
    fun bounds(section: HoennSection): MapBounds {
        val start = fromGrid(section.x.toFloat(), section.y.toFloat())
        val end = fromGrid((section.x + section.width).toFloat(), (section.y + section.height).toFloat())
        return MapBounds(start.x, start.y, end.x, end.y)
    }

    fun hits(viewport: MapViewport, touch: MapPoint, linked: List<Pair<HoennSection, MapLocation>>): List<MapLocation> =
        linked.filter { (section, _) -> viewport.hit(touch, bounds(section)) }
            .sortedBy { it.first.width * it.first.height }.map { it.second }.distinctBy { it.id }
}
