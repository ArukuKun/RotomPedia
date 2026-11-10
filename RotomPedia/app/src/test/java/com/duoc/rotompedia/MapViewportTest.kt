package com.duoc.rotompedia

import com.duoc.rotompedia.ui.maps.*
import org.junit.Assert.*
import org.junit.Test

/** Synthetic rectangles only: none of these coordinates represents a Pokémon location. */
class MapViewportTest {
    @Test fun `fit letterboxes image and refuses taps in the margin`() {
        val view = MapViewport.create(300f, 500f, 600f, 200f)
        assertEquals(300f, view.renderedWidth, 0.001f)
        assertEquals(100f, view.renderedHeight, 0.001f)
        assertEquals(MapPoint(0f, 200f), view.toScreen(MapPoint(0f, 0f)))
        assertNull(view.toMap(MapPoint(150f, 100f)))
        assertEquals(MapPoint(0.5f, 0.5f), view.toMap(MapPoint(150f, 250f)))
    }

    @Test fun `pan clamps each axis and zoom reset centers instead of letting map float out of view`() {
        val view = MapViewport.create(300f, 500f, 600f, 200f, 2f).panBy(10000f, -10000f)
        assertEquals(150f, view.panX, 0.001f)
        assertEquals(0f, view.panY, 0.001f) // image still shorter than viewport
        val reset = view.zoomAt(0.01f, MapPoint(10f, 10f))
        assertEquals(1f, reset.zoom, 0.001f)
        assertEquals(0f, reset.panX, 0.001f)
        assertEquals(0f, reset.panY, 0.001f)
    }

    @Test fun `inverse transform and hit areas follow the image through zoom and pan`() {
        val fixture = MapBounds(0.2f, 0.3f, 0.4f, 0.5f)
        val anchor = MapPoint(0.3f, 0.4f)
        val view = MapViewport.create(320f, 400f, 640f, 480f, 3f).panBy(70f, -90f)
        val screen = view.toScreen(anchor)
        val restored = view.toMap(screen)!!
        assertEquals(anchor.x, restored.x, 0.00001f)
        assertEquals(anchor.y, restored.y, 0.00001f)
        assertTrue(view.hit(screen, fixture))
        assertFalse(view.hit(view.toScreen(MapPoint(0.8f, 0.8f)), fixture))
        assertFalse(view.hit(view.toScreen(MapPoint(0.8f, 0.8f)), MapBounds(0.7f, 0.7f, 1f, 1f))) // off viewport
    }

    @Test fun `zoom keeps the focused geographic point under the same finger when not edge clamped`() {
        val view = MapViewport.create(300f, 300f, 300f, 300f, 2f)
        val focus = MapPoint(170f, 180f)
        val point = view.toMap(focus)!!
        val zoomed = view.zoomAt(1.5f, focus)
        assertEquals(focus.x, zoomed.toScreen(point).x, 0.001f)
        assertEquals(focus.y, zoomed.toScreen(point).y, 0.001f)
        assertEquals(5f, zoomed.zoomAt(100f, focus).zoom, 0.001f)
    }

    @Test fun `fit uses pixel ratios in portrait and landscape without inventing pin coordinates`() {
        val point = MapPoint(0.75f, 0.25f)
        for ((width, height) in listOf(320f to 600f, 600f to 320f)) {
            val view = MapViewport.create(width, height, 260f, 171f, 2f)
            val result = view.toMap(view.toScreen(point))!!
            assertEquals(point.x, result.x, 0.00001f)
            assertEquals(point.y, result.y, 0.00001f)
        }
    }

    @Test fun `focus on audited section enlarges it and preserves correct inverse hit testing`() {
        val bounds = MapBounds(0.40f, 0.30f, 0.44f, 0.36f)
        for ((width, height) in listOf(320f to 600f, 600f to 320f)) {
            val starting = MapViewport.create(width, height, 28f, 18f)
            val focused = starting.focusOn(bounds)
            assertEquals(3f, focused.zoom, 0.001f)
            val middle = MapPoint((bounds.left + bounds.right) / 2f,
                (bounds.top + bounds.bottom) / 2f)
            val screen = focused.toScreen(middle)
            // Letterboxed axes may not have enough image overflow to center exactly.
            assertTrue(screen.x in 0f..width)
            assertTrue(screen.y in 0f..height)
            assertTrue(focused.hit(screen, bounds))
        }
    }

    @Test fun `focus edge section remains reachable with clamped pan and never reduces manual zoom`() {
        val edge = MapBounds(0f, 0.6f, 1f / 28f, 0.66f)
        val view = MapViewport.create(320f, 260f, 28f, 18f, 4f)
        val focused = view.focusOn(edge)
        assertEquals(4f, focused.zoom, 0.001f)
        val cellCenter = MapPoint((edge.left + edge.right) / 2f, (edge.top + edge.bottom) / 2f)
        val screen = focused.toScreen(cellCenter)
        assertTrue(screen.x in 0f..320f && screen.y in 0f..260f)
        assertTrue(focused.hit(screen, edge))
        try {
            view.focusOn(edge, Float.NaN)
            fail("NaN zoom must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun `invalid image dimensions transforms and geographic bounds fail rather than producing NaN pins`() {
        fun rejected(block: () -> Unit) { try { block(); fail("Invalid geometry accepted") } catch (_: IllegalArgumentException) { } }
        rejected { MapViewport.create(0f, 300f, 200f, 100f) }
        rejected { MapViewport.create(300f, 300f, Float.NaN, 100f) }
        rejected { MapViewport.create(300f, 300f, 200f, 100f, panX = Float.POSITIVE_INFINITY) }
        rejected { MapBounds(-0.1f, 0f, 0.5f, 1f) }
        rejected { MapBounds(0.7f, 0f, 0.5f, 1f) }
        rejected { MapViewport.create(300f, 300f, 200f, 100f).toScreen(MapPoint(2f, 0f)) }
    }
}
