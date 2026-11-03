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
}
