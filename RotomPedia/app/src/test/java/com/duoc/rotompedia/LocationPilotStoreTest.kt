package com.duoc.rotompedia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.index.*
import com.duoc.rotompedia.data.maps.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class LocationPilotStoreTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val text get() = javaClass.getResource("/hoenn-emerald-location-candidate.json")!!.readText()
    private fun identities(): Pair<String, Set<Int>> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val json = context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        val e = moshi.adapter(IndexEnvelope::class.java).fromJson(json)!!
        val p = moshi.adapter(IndexPayload::class.java).fromJson(e.payloadJson)!!
        IndexValidator.validate(e, p)
        return e.contentSha256 to p.varieties.map { it.pokemonId }.toSet()
    }
    private fun payload(): LocationPilot {
        val e = moshi.adapter(LocationEnvelope::class.java).fromJson(text)!!
        return moshi.adapter(LocationPilot::class.java).fromJson(e.payloadJson)!!
    }

    @Test fun `pinned real candidate loads once and uses the existing national variety identities without network`() = runBlocking {
        var reads = 0
        val identities = identities()
        val store = LocationPilotStore(moshi, { reads++; text }, { identities })
        val results = (1..3).map { async { store.catalogue() } }.awaitAll()
        assertEquals(1, reads)
        assertTrue(results.all { it === results.first() })
        val p = results.first().pilot
        assertEquals("hoenn", p.region)
        assertEquals("emerald", p.version)
        assertEquals(115, results.first().sourceCoverage.areasWithEncounters)
        assertEquals(114, p.coverage.areasWithEncounters)
        assertEquals(1644, results.first().sourceCoverage.encounterRows)
        assertEquals(1642, p.encounters.size)
        assertEquals(155, results.first().sourceCoverage.pokemonVarieties)
        assertEquals(154, p.coverage.pokemonVarieties)
        assertTrue(p.encounters.all { it.versionId == 9 && it.pokemonId in identities.second })
        assertEquals("not-sourced", p.coverage.cartography)
        assertFalse(p.methods.any { "colosseum" in it.slug || "xd" in it.slug.split('-') })
    }

    @Test fun `declared missing areas and unsupported editions stay unknown not urban safe zones or empty encounter facts`() = runBlocking {
        val catalogue = LocationPilotStore(moshi, { text }, { identities() }).catalogue()
        val p = catalogue.pilot
        val gap = p.areas.first { area -> p.encounters.none { it.areaId == area.id } }
        assertEquals(AreaCoverage.NO_SOURCE_ROWS, catalogue.area(gap.id, 9).coverage)
        assertEquals(AreaCoverage.NOT_IN_PACKAGE, catalogue.area(gap.id, 6).coverage)
        assertEquals(AreaCoverage.NOT_IN_PACKAGE, catalogue.area(Int.MAX_VALUE, 9).coverage)
        val area = p.encounters.first().areaId
        val rows = catalogue.area(area, 9)
        assertEquals(AreaCoverage.AVAILABLE, rows.coverage)
        assertEquals(p.encounters.filter { it.areaId == area }, rows.encounters)
        assertEquals(p.methodRates.filter { it.areaId == area }, rows.methodRates)
    }

    @Test fun `bad payload hash or mismatched national snapshot cannot expose a partly validated candidate`() = runBlocking {
        val e = moshi.adapter(LocationEnvelope::class.java).fromJson(text)!!
        val broken = moshi.adapter(LocationEnvelope::class.java).toJson(e.copy(payloadJson = e.payloadJson + " "))
        for (store in listOf(LocationPilotStore(moshi, { broken }, { identities() }),
            LocationPilotStore(moshi, { text }, { "a".repeat(64) to identities().second }))) {
            try { store.catalogue(); fail("Bad candidate accepted") } catch (_: IllegalArgumentException) { }
        }
    }

    @Test fun `mixed version unknown Pokemon conditions duplicates and fabricated coverage fail validation`() {
        val p = payload()
        val identities = identities()
        val first = p.encounters.first()
        val corrupt = listOf(
            p.copy(encounters = p.encounters + first),
            p.copy(encounters = listOf(first.copy(versionId = 6)) + p.encounters.drop(1)),
            p.copy(encounters = listOf(first.copy(pokemonId = Int.MAX_VALUE)) + p.encounters.drop(1)),
            p.copy(encounters = listOf(first.copy(conditionIds = listOf(Int.MAX_VALUE))) + p.encounters.drop(1)),
            p.copy(encounters = listOf(first.copy(chance = 101)) + p.encounters.drop(1)),
            p.copy(coverage = p.coverage.copy(objects = "complete")),
            p.copy(coverage = p.coverage.copy(cartography = "complete")))
        for (candidate in corrupt) {
            try { LocationPilotStore.validate(candidate, identities.first, identities.second); fail("Bad relation accepted") }
            catch (_: IllegalArgumentException) { }
        }
    }
}
