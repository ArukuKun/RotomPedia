package com.duoc.rotompedia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.items.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ItemPocketTest {
    private fun payload(): ItemIndexPayload {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val envelope = moshi.adapter(ItemIndexEnvelope::class.java).fromJson(context.assets.open("item-index.json").bufferedReader().use { it.readText() })!!
        return moshi.adapter(ItemIndexPayload::class.java).fromJson(envelope.payloadJson)!!
    }

    @Test fun `source pockets partition all 2223 identities without duplication or keyword guesses`() {
        val p = payload()
        val metadata = ItemPocketIndex.validated(p)
        assertTrue(metadata.complete)
        assertEquals(8, metadata.pockets.size)
        assertEquals(54, metadata.categories.size)
        val results = metadata.pockets.flatMap { ItemPresentation.filter(p.items, ItemFilter(pocketId = it.id), metadata) }
        assertEquals(2223, results.size)
        assertEquals(p.items.map { it.itemId }.toSet(), results.map { it.itemId }.toSet())
        assertEquals(results.size, results.distinctBy { it.itemId }.size)
        assertEquals(5, metadata.pocket("medicine")!!.id)
        assertEquals(8, metadata.pocket("z-crystals")!!.id)
        assertEquals(1, metadata.pocket("held-items")!!.id)
        assertEquals(2, ItemPresentation.filter(p.items, ItemFilter(pocketId = 5), metadata).count { it.slug == "roseli-berry" })
    }

    @Test fun `pocket intersects query category documented attribute and editorial filters without new item data`() {
        val p = payload()
        val metadata = ItemPocketIndex.validated(p)
        val filter = ItemFilter("acardo", "type-protection", "holdable-active", true, 5)
        assertEquals(listOf(168), ItemPresentation.filter(p.items, filter, metadata).map { it.itemId })
        assertTrue(ItemPresentation.filter(p.items, filter.copy(pocketId = 2), metadata).isEmpty())
        assertTrue(ItemPresentation.filter(p.items, ItemFilter(pocketId = ItemPocketIndex.UNKNOWN), metadata).isEmpty())
    }

    @Test fun `unknown categories and legacy metadata remain accessible with no invented assignment`() {
        val p = payload()
        val unknown = p.items.first().copy(category = "future-category")
        val partial = ItemPocketIndex.validated(p.copy(items = listOf(unknown), pocketsComplete = false))
        assertNull(partial.pocket(unknown.category))
        assertEquals(listOf(unknown), ItemPresentation.filter(listOf(unknown), ItemFilter(pocketId = ItemPocketIndex.UNKNOWN), partial))
        val legacy = ItemPocketIndex.validated(ItemIndexPayload(p.revision, p.items))
        assertFalse(legacy.complete)
        assertEquals(p.items.size, ItemPresentation.filter(p.items, ItemFilter(), legacy).size)
        assertEquals(p.items.size, ItemPresentation.filter(p.items, ItemFilter(pocketId = ItemPocketIndex.UNKNOWN), legacy).size)
    }

    @Test fun `bad or incomplete pocket metadata is rejected before use`() {
        val p = payload()
        for (bad in listOf(p.copy(pockets = p.pockets + p.pockets.first()),
            p.copy(categoryPockets = p.categoryPockets + p.categoryPockets.first()),
            p.copy(categoryPockets = p.categoryPockets.map { it.copy(pocketId = 999) }),
            p.copy(categoryPockets = emptyList()), p.copy(items = listOf(p.items.first().copy(category = "future"))))) {
            try { ItemPocketIndex.validated(bad); fail("Invalid pocket metadata accepted") } catch (_: IllegalArgumentException) { }
        }
    }
}
