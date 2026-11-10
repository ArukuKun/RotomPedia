package com.duoc.rotompedia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.maps.LastMapRegionStore
import com.duoc.rotompedia.data.maps.MapRegionCatalogue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class LastMapRegionStoreTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before fun resetMapNavigation() {
        context.getSharedPreferences("rotompedia_map_navigation", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test fun `first opening shows the selector and Kanto appears first`() {
        assertNull(LastMapRegionStore(context).lastOpenedRegion())
        assertEquals("kanto", MapRegionCatalogue.regions.first().slug)
    }

    @Test fun `last visited map survives creation of a new store`() {
        val store = LastMapRegionStore(context)
        store.rememberRegion("kanto")
        assertEquals("kanto", LastMapRegionStore(context).lastOpenedRegion())
        store.rememberRegion("hoenn")
        assertEquals("hoenn", LastMapRegionStore(context).lastOpenedRegion())
    }

    @Test fun `unknown and unavailable regions never auto open`() {
        assertNull(MapRegionCatalogue.restorableRegion(null))
        assertNull(MapRegionCatalogue.restorableRegion("missing"))
        assertNull(MapRegionCatalogue.restorableRegion("hisui"))
        context.getSharedPreferences("rotompedia_map_navigation", Context.MODE_PRIVATE)
            .edit().putString("last_opened_region", "hisui").commit()
        assertNull(LastMapRegionStore(context).lastOpenedRegion())
    }

    @Test fun `selector cannot persist an unavailable region`() {
        val store = LastMapRegionStore(context)
        try {
            store.rememberRegion("hisui")
            fail("Hisui should remain disabled")
        } catch (expected: IllegalArgumentException) { /* expected */ }
        assertNull(store.lastOpenedRegion())
    }

    @Test fun `map preference does not touch appearance or stored teams`() {
        val other = context.getSharedPreferences("rotom_theme_prefs", Context.MODE_PRIVATE)
        other.edit().putString("unrelated", "keep-me").commit()
        LastMapRegionStore(context).rememberRegion("hoenn")
        assertEquals("keep-me", other.getString("unrelated", null))
    }
}
