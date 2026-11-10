package com.duoc.rotompedia.ui.maps

import kotlin.math.max
import kotlin.math.min

data class MapPoint(val x: Float, val y: Float) {
    init { require(x.isFinite() && y.isFinite()) }
}

/** A verified area may supply these bounds later. No factual coordinates are authored here. */
data class MapBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    init { require(listOf(left, top, right, bottom).all { it.isFinite() && it in 0f..1f } && left < right && top < bottom) }
    fun contains(point: MapPoint) = point.x in left..right && point.y in top..bottom
}

/** Pixel-only fit/zoom/pan transform shared by image, pins and inverse hit-testing. */
class MapViewport private constructor(val viewportWidth: Float, val viewportHeight: Float,
    val sourceWidth: Float, val sourceHeight: Float, val zoom: Float, val panX: Float, val panY: Float) {
    private val fit = min(viewportWidth / sourceWidth, viewportHeight / sourceHeight)
    val renderedWidth = sourceWidth * fit * zoom
    val renderedHeight = sourceHeight * fit * zoom
    fun toScreen(point: MapPoint): MapPoint {
        require(point.x in 0f..1f && point.y in 0f..1f)
        return MapPoint(viewportWidth / 2 + panX + (point.x - 0.5f) * renderedWidth,
            viewportHeight / 2 + panY + (point.y - 0.5f) * renderedHeight)
    }

    fun toMap(point: MapPoint): MapPoint? {
        val x = (point.x - viewportWidth / 2 - panX) / renderedWidth + 0.5f
        val y = (point.y - viewportHeight / 2 - panY) / renderedHeight + 0.5f
        return if (x in 0f..1f && y in 0f..1f) MapPoint(x, y) else null
    }
    fun hit(screen: MapPoint, bounds: MapBounds) = screen.x in 0f..viewportWidth && screen.y in 0f..viewportHeight &&
        toMap(screen)?.let(bounds::contains) == true
    fun panBy(dx: Float, dy: Float) = create(viewportWidth, viewportHeight, sourceWidth, sourceHeight, zoom, panX + dx, panY + dy)

    /** Bring an audited map section into view without assuming GPS/cartographic coordinates.
     * Edge sections are clamped to the map, so they may not land exactly at screen center. */
    fun focusOn(bounds: MapBounds, preferredZoom: Float = 3f): MapViewport {
        require(preferredZoom.isFinite() && preferredZoom >= 1f)
        val next = zoom.coerceAtLeast(preferredZoom).coerceAtMost(5f)
        val atZoom = create(viewportWidth, viewportHeight, sourceWidth, sourceHeight, next)
        val midX = (bounds.left + bounds.right) / 2f
        val midY = (bounds.top + bounds.bottom) / 2f
        return create(viewportWidth, viewportHeight, sourceWidth, sourceHeight, next,
            (0.5f - midX) * atZoom.renderedWidth, (0.5f - midY) * atZoom.renderedHeight)
    }

    fun zoomAt(factor: Float, focus: MapPoint): MapViewport {
        require(factor.isFinite() && factor > 0)
        val next = (zoom * factor).coerceIn(1f, 5f)
        val ratio = next / zoom
        return create(viewportWidth, viewportHeight, sourceWidth, sourceHeight, next,
            (focus.x - viewportWidth / 2) * (1 - ratio) + panX * ratio,
            (focus.y - viewportHeight / 2) * (1 - ratio) + panY * ratio)
    }

    companion object {
        fun create(viewportWidth: Float, viewportHeight: Float, sourceWidth: Float, sourceHeight: Float,
            zoom: Float = 1f, panX: Float = 0f, panY: Float = 0f): MapViewport {
            require(listOf(viewportWidth, viewportHeight, sourceWidth, sourceHeight).all { it.isFinite() && it > 0 })
            require(zoom.isFinite() && panX.isFinite() && panY.isFinite())
            val z = zoom.coerceIn(1f, 5f)
            val fit = min(viewportWidth / sourceWidth, viewportHeight / sourceHeight)
            require(fit.isFinite() && fit > 0 && (sourceWidth * fit * z).isFinite() && (sourceHeight * fit * z).isFinite())
            val maxX = max(0f, (sourceWidth * fit * z - viewportWidth) / 2)
            val maxY = max(0f, (sourceHeight * fit * z - viewportHeight) / 2)
            return MapViewport(viewportWidth, viewportHeight, sourceWidth, sourceHeight, z,
                panX.coerceIn(-maxX, maxX), panY.coerceIn(-maxY, maxY))
        }
    }
}
