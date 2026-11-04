package com.duoc.rotompedia

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.DefaultPokemonRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PokemonExplorerTest {
    private fun checkEntries(block: (List<ExplorerPokemon>) -> Unit) = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val api = Mockito.mock(PokeApiService::class.java)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        try {
            val repo = DefaultPokemonRepository(db, api, PokemonIndexImporter(db, moshi)) {
                context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
            }
            repo.ensureIndex()
            block(repo.observeExplorer().first())
            Mockito.verifyNoInteractions(api)
        } finally { db.close() }
    }

    @Test fun `default catalogue is national compact and identity safe`() = checkEntries { entries ->
        val result = PokemonExplorer.filter(entries, ExplorerFilter())
        assertEquals((1..1025).toList(), result.groups.map { it.nationalNumber })
        assertEquals("bulbasaur", result.groups.first().primary.slug)
        assertTrue(result.groups.none { it.autoExpanded })
        val expected = mapOf(10104 to 38, 10034 to 6, 10126 to 745, 10026 to 681, 10301 to 718)
        expected.forEach { (id, national) ->
            val entry = entries.single { it.pokemonId == id }
            assertEquals(national, entry.nationalNumber)
            assertEquals(national, entry.speciesId)
            assertNotNull(entry.pokemonFormId)
            assertEquals("#${national.toString().padStart(3, '0')}", entry.numberLabel)
        }
        assertEquals(10206, entries.single { it.pokemonId == 10104 }.pokemonFormId)
        assertEquals(10526, entries.single { it.pokemonId == 10301 }.pokemonFormId)
        assertTrue(entries.all { it.types.isNotEmpty() })
    }

    @Test fun `mandatory partial searches expand and retain national association`() = checkEntries { entries ->
        fun search(query: String) = PokemonExplorer.filter(entries, ExplorerFilter(query = query))
        val megas = search("mega")
        assertTrue(megas.groups.isNotEmpty())
        assertEquals(megas.groups.map { it.nationalNumber }.sorted(), megas.groups.map { it.nationalNumber })
        assertTrue(megas.groups.flatMap { it.matching }.all { it.category == VarietyCategory.MEGA })
        assertTrue(search("hisui").groups.flatMap { it.matching }.all { it.regionalOrigin == "hisui" })
        val lycanroc = search("lycanroc").groups.single()
        assertEquals(3, lycanroc.matching.size)
        assertTrue(lycanroc.matching.all { it.category == VarietyCategory.STABLE })
        assertTrue(lycanroc.autoExpanded)
        assertEquals(setOf("sceptile", "sceptile-mega"), search("sceptile").groups.single().matching.map { it.slug }.toSet())
        assertEquals(setOf("ninetales", "ninetales-alola"), search("#038").groups.single().matching.map { it.slug }.toSet())
        assertEquals(10126, search("medianoch").groups.single().matching.single().pokemonId)
        assertEquals(681, search("aegislash filo").groups.single().nationalNumber)
    }

    @Test fun `category origin generation type and favorites intersect locally`() = checkEntries { entries ->
        val filter = ExplorerFilter(category = VarietyCategory.REGIONAL, generationId = 1, regionalOrigin = "alola", type = "ice")
        val matching = PokemonExplorer.filter(entries, filter).groups.flatMap { it.matching }
        assertEquals(setOf("vulpix-alola", "ninetales-alola", "sandshrew-alola", "sandslash-alola"), matching.map { it.slug }.toSet())
        assertEquals(listOf(10104), PokemonExplorer.filter(entries, filter.copy(favoritesOnly = true), setOf(10104)).groups.flatMap { it.matching }.map { it.pokemonId })
        assertTrue(PokemonExplorer.filter(entries, filter.copy(generationId = 7)).groups.isEmpty())
        assertEquals(VarietyCategory.BATTLE, entries.single { it.slug == "aegislash-blade" }.category)
        assertEquals(VarietyCategory.MEGA, entries.single { it.slug == "zygarde-mega" }.category)
        assertEquals(listOf("darmanitan-galar-zen"), PokemonExplorer.filter(entries,
            ExplorerFilter(category = VarietyCategory.BATTLE, regionalOrigin = "galar", type = "ice"))
            .groups.flatMap { it.matching }.map { it.slug })
        assertEquals(VarietyCategory.OTHER, entries.single { it.slug == "pikachu-rock-star" }.category)
        assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(query = "eléctrico")).groups.flatMap { it.matching }
            .all { "electric" in it.types })
        assertEquals(listOf(133, 134, 135, 136, 196, 197, 470, 471, 700),
            entries.filter { it.speciesName in listOf("Eevee", "Vaporeon", "Jolteon", "Flareon", "Espeon", "Umbreon", "Leafeon", "Glaceon", "Sylveon") && it.isDefault }.map { it.nationalNumber })
    }

    @Test fun `Pokedex national mode contains exactly one default per species without expansion`() = checkEntries { entries ->
        val result = PokemonExplorer.filter(entries, ExplorerFilter(), nationalOnly = true)
        assertEquals((1..1025).toList(), result.entries.map { it.nationalNumber })
        assertEquals(1025, result.entries.distinctBy { it.speciesId }.size)
        assertEquals(0, result.variantCount)
        assertTrue(result.entries.all { it.isDefault })
        assertEquals(listOf("bulbasaur", "ivysaur", "venusaur", "charmander", "charmeleon", "charizard"), result.entries.take(6).map { it.slug })
        assertTrue(result.groups.all { it.matching.size == 1 && !it.autoExpanded })
        assertEquals(listOf(38), PokemonExplorer.filter(entries, ExplorerFilter(query = "#038"), nationalOnly = true).entries.map { it.pokemonId })
        // Picker remains able to select the regional variety with the same national number.
        assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(query = "#038")).entries.any { it.pokemonId == 10104 })
    }

    @Test fun `egg groups are verified species metadata and intersect with form type and favorites`() = checkEntries { entries ->
        assertTrue(entries.all { it.eggGroups.size in 1..2 })
        assertEquals(15, entries.flatMap { it.eggGroups }.distinctBy { it.slug }.size)
        assertEquals(setOf("monster", "plant"), entries.single { it.slug == "bulbasaur" }.eggGroups.map { it.slug }.toSet())
        assertEquals(listOf("ditto"), PokemonExplorer.filter(entries, ExplorerFilter(eggGroup = "ditto"), nationalOnly = true).entries.map { it.slug })
        assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(eggGroup = "no-eggs"), nationalOnly = true).entries.any { it.slug == "mewtwo" })
        val filter = ExplorerFilter(category = VarietyCategory.REGIONAL, regionalOrigin = "alola", type = "ice", eggGroup = "ground", favoritesOnly = true)
        val result = PokemonExplorer.filter(entries, filter, setOf(10104), nationalOnly = true)
        assertEquals(listOf(10104), result.entries.map { it.pokemonId })
        assertEquals(listOf(38), result.entries.map { it.nationalNumber })
        assertTrue(PokemonExplorer.filter(entries, filter.copy(eggGroup = "mineral"), setOf(10104), nationalOnly = true).entries.isEmpty())
    }

    @Test fun `Mega and Gigamax categories use verified forms keep national association and order`() = checkEntries { entries ->
        val megas = PokemonExplorer.filter(entries, ExplorerFilter(category = VarietyCategory.MEGA), nationalOnly = true).entries
        assertTrue(megas.isNotEmpty())
        assertTrue(megas.all { it.category == VarietyCategory.MEGA && !it.isDefault })
        val gmax = PokemonExplorer.filter(entries, ExplorerFilter(category = VarietyCategory.GMAX), nationalOnly = true).entries
        assertEquals(34, gmax.size)
        assertTrue(gmax.all { it.slug.endsWith("-gmax") && it.pokemonFormId != null })
        assertEquals(gmax.map { it.nationalNumber }.sorted(), gmax.map { it.nationalNumber })
        assertEquals(6, gmax.single { it.pokemonId == 10196 }.nationalNumber)
        assertFalse(gmax.any { it.pokemonId in megas.map { m -> m.pokemonId } })
        assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(category = VarietyCategory.GMAX, type = "fire", eggGroup = "monster"), nationalOnly = true)
            .entries.any { it.pokemonId == 10196 })
    }

    @Test fun `visible national number never derives from technical species or variety id`() = checkEntries { entries ->
        val sample = entries.first().copy(speciesId = 999, nationalNumber = 1, pokemonId = 12345)
        val result = PokemonExplorer.filter(listOf(sample), ExplorerFilter(query = "#001"), nationalOnly = true)
        assertEquals(12345, result.entries.single().pokemonId)
        assertEquals("#001", result.entries.single().numberLabel)
    }

    @Test fun `egg metadata rehydrates offline even when the Room snapshot is already imported`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val api = Mockito.mock(PokeApiService::class.java)
        val json = context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        try {
            assertTrue(PokemonIndexImporter(db, moshi).importJson(json))
            val reopenedImporter = PokemonIndexImporter(db, moshi)
            val repo = DefaultPokemonRepository(db, api, reopenedImporter) { json }
            assertTrue(repo.observeExplorer().first().isEmpty()) // Do not restore a regional position using national-only metadata.
            assertFalse(reopenedImporter.importJson(json)) // Room identity rows do not need reimporting.
            val entries = repo.observeExplorer().first()
            assertTrue(entries.all { it.eggGroups.isNotEmpty() })
            assertEquals(listOf("decidueye-hisui", "typhlosion-hisui", "samurott-hisui"),
                PokemonExplorer.filter(entries, ExplorerFilter(regionalOrigin = "hisui"), nationalOnly = true).entries.take(3).map { it.slug })
            assertEquals(listOf("ditto"), PokemonExplorer.filter(entries, ExplorerFilter(eggGroup = "ditto"), nationalOnly = true).entries.map { it.slug })
            Mockito.verifyNoInteractions(api)
        } finally { db.close() }
    }

    @Test fun `invalid egg relationship cannot replace validated index or metadata`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val json = context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        val envelopeAdapter = moshi.adapter(com.duoc.rotompedia.data.index.IndexEnvelope::class.java)
        val payloadAdapter = moshi.adapter(com.duoc.rotompedia.data.index.IndexPayload::class.java)
        try {
            val importer = PokemonIndexImporter(db, moshi)
            importer.importJson(json)
            val previous = importer.metadata.value
            val state = db.pokemonIndexDao().state()
            val envelope = envelopeAdapter.fromJson(json)!!
            val payload = payloadAdapter.fromJson(envelope.payloadJson)!!
            val brokenJson = payloadAdapter.toJson(payload.copy(eggGroupsComplete = false, speciesEggGroups = payload.speciesEggGroups +
                com.duoc.rotompedia.data.index.SpeciesEggGroupRecord(1, 999)))
            val broken = envelope.copy(payloadJson = brokenJson, contentSha256 = com.duoc.rotompedia.data.index.IndexValidator.sha256(brokenJson))
            try { importer.importJson(envelopeAdapter.toJson(broken)); fail("Orphan relationship accepted") }
            catch (_: IllegalArgumentException) { }
            assertEquals(state, db.pokemonIndexDao().state())
            assertEquals(previous, importer.metadata.value)
        } finally { db.close() }
    }

    @Test fun `Hisui regional order starts with its three fully evolved starters and preserves national identity`() = checkEntries { entries ->
        val result = PokemonExplorer.filter(entries, ExplorerFilter(regionalOrigin = "hisui"), nationalOnly = true)
        assertEquals("hisui", result.regionalOrder!!.slug)
        assertEquals(listOf("legends-arceus"), result.regionalOrder!!.versionGroups)
        assertEquals(listOf("decidueye-hisui", "typhlosion-hisui", "samurott-hisui"), result.entries.take(3).map { it.slug })
        assertEquals(listOf(724, 157, 503), result.entries.take(3).map { it.nationalNumber })
        assertEquals(listOf(3, 6, 9), result.entries.take(3).map { p -> p.regionalNumbers.single { it.dex.region == "hisui" }.number })
        assertEquals(16, result.entries.size)
        assertEquals(0, result.missingRegionalNumbers)
        assertEquals((1..1025).toList(), PokemonExplorer.filter(entries, ExplorerFilter(), nationalOnly = true).entries.map { it.nationalNumber })
    }

    @Test fun `regional reference editions are explicit and Galar DLC absences are never invented`() = checkEntries { entries ->
        val result = PokemonExplorer.filter(entries, ExplorerFilter(regionalOrigin = "galar"), nationalOnly = true)
        assertEquals(listOf("sword-shield"), result.regionalOrder!!.versionGroups)
        assertTrue(result.missingRegionalNumbers > 0)
        val numbered = result.entries.takeWhile { p -> p.regionalNumbers.any { it.dex.region == "galar" } }
        assertEquals(result.entries.size - result.missingRegionalNumbers, numbered.size)
        val numbers = numbered.map { p -> p.regionalNumbers.single { it.dex.region == "galar" }.number }
        assertEquals(numbers.sorted(), numbers)
        val missing = result.entries.drop(numbered.size)
        assertEquals(missing.map { it.nationalNumber }.sorted(), missing.map { it.nationalNumber })
    }

    @Test fun `Totem presentation is hidden only in Pokedex and alternative identities remain available`() = checkEntries { entries ->
        assertEquals(12, entries.count { it.isTotem })
        val alola = PokemonExplorer.filter(entries, ExplorerFilter(regionalOrigin = "alola"), nationalOnly = true).entries
        assertTrue(alola.none { it.isTotem })
        assertEquals(1, alola.count { it.speciesId == 20 })
        assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(query = "totem")).entries.isNotEmpty())
        assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(query = "totem")).entries.map { it.pokemonId }
            .containsAll(entries.filter { it.isTotem }.map { it.pokemonId }))
        assertEquals(3, PokemonExplorer.filter(entries, ExplorerFilter(query = "lycanroc")).entries.size)
        listOf("dialga-origin", "palkia-origin", "giratina-origin", "landorus-therian", "thundurus-therian").forEach { slug ->
            assertTrue(entries.any { it.slug == slug })
            assertTrue(PokemonExplorer.filter(entries, ExplorerFilter(query = slug)).entries.any { it.slug == slug })
        }
    }
}
