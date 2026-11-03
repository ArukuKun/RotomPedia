package com.duoc.rotompedia

import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.data.model.NamedApiResource
import org.junit.Assert.*
import org.junit.Test
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class ItemPresentationTest {
    private fun resource(slug: String) = NamedApiResource(slug, "")
    private val data = ItemDetail(168, "shuca-berry", resource("type-protection"), descriptions = listOf(
        ItemFlavor("Texto histórico erróneo: cura PS", resource("es"), resource("x-y")),
        ItemFlavor("Reduce daño de Tierra", resource("es"), resource("sun-moon")),
        ItemFlavor("Ground damage", resource("en"), resource("sun-moon")),
        ItemFlavor("English only", resource("en"), resource("sword-shield"))))
    @Test fun `descriptions are edition specific with explicit language and known bad text hidden`() {
        assertNull(ItemPresentation.description(data, null))
        assertNull(ItemPresentation.description(data, "x-y"))
        assertNull(ItemPresentation.description(data, "scarlet-violet"))
        assertEquals("es", ItemPresentation.description(data, "sun-moon")!!.language)
        assertEquals("en", ItemPresentation.description(data, "sword-shield")!!.language)
        assertTrue(ItemEditorial.shucaEffect.contains("Tierra"))
        assertFalse(ItemEditorial.shucaEffect.contains("Agua"))
    }
    @Test fun `item sprites never use Pokemon id endpoints or mismatched item resources`() {
        val sprite = ItemPresentation.sprite("shuca-berry", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/168.png")!!
        assertTrue(sprite.endsWith("/items/shuca-berry.png"))
        val machine = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/tm-normal.png"
        assertEquals(machine, ItemPresentation.sprite("tm01", machine))
        assertNull(ItemPresentation.sprite("../168"))
    }
    @Test fun `unknown categories and missing facts are not fabricated`() {
        assertTrue(ItemLabels.category("future-category").contains("sin traducir"))
        assertTrue(ItemLabels.attribute("future-attribute").contains("sin traducir"))
        assertNull(ItemPresentation.effect(data))
        assertTrue(data.prices.isEmpty())
    }
    @Test fun `prices preserve edition currency and missing purchase data without guessing legacy cost`() {
        val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(ItemDetail::class.java)
        val result = adapter.fromJson("""{"id":305,"name":"tm01","category":{"name":"all-machines","url":""},"cost":3000,"prices":[{"purchase_price":null,"sell_price":1500,"currency":{"name":"poke-dollar","url":""},"version_group":{"name":"gold-silver","url":""}}]}""")!!
        assertNull(result.prices.single().purchasePrice)
        assertEquals(1500, result.prices.single().sellPrice)
        assertEquals("gold-silver", result.prices.single().versionGroup.name)
        assertTrue(adapter.fromJson("""{"id":168,"name":"shuca-berry","category":{"name":"type-protection","url":""},"cost":20}""")!!.prices.isEmpty())
    }
}
