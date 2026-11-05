package com.duoc.rotompedia

import com.duoc.rotompedia.data.PokemonBiology
import com.duoc.rotompedia.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class PokemonBiologyTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test fun `measurements convert decimetres and hectograms without rounding to integers`() {
        assertEquals("1,7 m", PokemonBiology.measurement(17, "m"))
        assertEquals("90,5 kg", PokemonBiology.measurement(905, "kg"))
        assertEquals("110,5 kg", PokemonBiology.measurement(1105, "kg"))
        assertEquals("0,1 kg", PokemonBiology.measurement(1, "kg"))
        assertEquals("10,0 m", PokemonBiology.measurement(100, "m"))
        listOf(null, 0, -1).forEach { assertEquals("—", PokemonBiology.measurement(it, "kg")) }
    }

    @Test fun `category uses verified Spanish genus only not English or regional assumptions`() {
        assertEquals("Pokémon Llama", PokemonBiology.spanishCategory(listOf(
            PokemonGenus("Flame Pokémon", Language("en")), PokemonGenus("Pokémon Llama", Language("es")))))
        assertNull(PokemonBiology.spanishCategory(listOf(PokemonGenus("Flame Pokémon", Language("en")))))
        assertNull(PokemonBiology.spanishCategory(listOf(PokemonGenus("  ", Language("es")))))
        val response = moshi.adapter(PokemonSpeciesResponse::class.java).fromJson("""
            {"id":6,"evolution_chain":{"url":"chain"},"flavor_text_entries":[],
             "genera":[{"genus":"Pokémon Llama","language":{"name":"es"}}]}
        """)!!
        assertEquals(6, response.id)
        assertEquals("Pokémon Llama", PokemonBiology.spanishCategory(response.genera))
    }

    @Test fun `optional dimensions preserve variety response and legacy payloads without invented data`() {
        val adapter = moshi.adapter(PokemonData::class.java)
        val base = """{"id":6,"name":"charizard","sprites":{"front_default":null},"types":[],"stats":[],"height":17,"weight":905}"""
        val mega = base.replace("\"id\":6", "\"id\":10034").replace("\"charizard\"", "\"charizard-mega-x\"").replace("905", "1105")
        assertEquals(905, adapter.fromJson(base)!!.weight)
        assertEquals(1105, adapter.fromJson(mega)!!.weight)
        val old = adapter.fromJson("""{"id":6,"name":"charizard","sprites":{"front_default":null},"types":[],"stats":[]}""")!!
        assertNull(old.height)
        assertNull(old.weight)
        val oldSpecies = moshi.adapter(PokemonSpeciesResponse::class.java).fromJson(
            """{"evolution_chain":{"url":"chain"},"flavor_text_entries":[]}""")!!
        assertTrue(oldSpecies.genera.isEmpty())
    }
}
