package com.duoc.rotompedia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.index.IndexEnvelope
import com.duoc.rotompedia.data.index.IndexPayload
import com.duoc.rotompedia.data.index.IndexValidator
import com.duoc.rotompedia.data.maps.LocationPilotStore
import com.duoc.rotompedia.data.maps.HoennAreaSearch
import com.duoc.rotompedia.data.maps.HoennEncounterCategory
import com.duoc.rotompedia.data.maps.LocationPokemonNames
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class LocationPilotIntegrationTest {
    @Test fun `bundled Emerald pilot uses pinned national identities and excludes bonus disc`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val nationalText = context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        val national = moshi.adapter(IndexEnvelope::class.java).fromJson(nationalText)!!
        val pokemon = moshi.adapter(IndexPayload::class.java).fromJson(national.payloadJson)!!
        IndexValidator.validate(national, pokemon)
        val store = LocationPilotStore(moshi,
            { context.assets.open("hoenn-emerald-location-pilot.json").bufferedReader().use { it.readText() } },
            { national.contentSha256 to pokemon.varieties.map { it.pokemonId }.toSet() })
        val catalogue = store.catalogue()
        assertEquals("emerald", catalogue.pilot.version)
        assertEquals(171, catalogue.pilot.coverage.declaredAreas)
        assertEquals(1644, catalogue.sourceCoverage.encounterRows)
        assertEquals(1642, catalogue.pilot.encounters.size)
        assertEquals(114, catalogue.pilot.coverage.areasWithEncounters)
        assertTrue(catalogue.pilot.encounters.none { it.id == 70694 || it.id == 70697 })
        val labels = LocationPokemonNames.fromNationalIndex(pokemon)
        assertEquals(pokemon.varieties.size, labels.size)
        assertEquals(38, labels[10104]?.nationalNumber) // Internal variety ID != national Dex number
        assertEquals(1, labels[1]?.nationalNumber)
        assertTrue(catalogue.pilot.encounters.all { labels.containsKey(it.pokemonId) })

        val search = HoennAreaSearch(catalogue, labels)
        val ralts = search.filter("Ralts")
        assertEquals(listOf(394), ralts.map { it.area.id })
        assertEquals(ralts.map { it.area.id }, search.filter("#280").map { it.area.id })
        // A Pokémon search narrows the encounters shown in the selected area's sheet.
        assertEquals(1, ralts.single().recordCount)
        assertEquals(1, ralts.single().varietyCount)
        assertEquals(listOf(280), search.encountersForArea(394, "Ralts").map { it.pokemonId })
        assertEquals(listOf(280), search.encountersForArea(394, "#280").map { it.pokemonId })
        assertEquals(0, search.encountersForArea(394, "Ralts", HoennEncounterCategory.FISH).size)
        assertTrue(search.filter("Ralts", HoennEncounterCategory.FISH).isEmpty())
        // A location search must retain the area's complete set, not hide unrelated Pokémon.
        assertTrue(search.encountersForArea(394, "Road 102").size > 1)
        assertTrue(search.encountersForArea(-1, "Ralts").isEmpty())
        assertEquals(171, search.filter("", onlyWithEncounters = false).size)
        assertEquals(114, search.filter("").size)
        assertTrue(search.filter("", HoennEncounterCategory.FISH).isNotEmpty())
        assertTrue(search.filter("", HoennEncounterCategory.WATER).isNotEmpty())
        assertTrue(search.filter("", HoennEncounterCategory.WALK).isNotEmpty())
        assertTrue(search.filter("", HoennEncounterCategory.FISH).all { it.recordCount > 0 })
        assertEquals(emptyList<Int>(), search.filter("inválido sin coincidencias").map { it.area.id })
        // Never let data from excluded Colosseum distribution reappear through a search index.
        assertTrue(search.filter("", HoennEncounterCategory.OTHER).all { it.recordCount > 0 })
    }
}
