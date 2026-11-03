package com.duoc.rotompedia

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.index.*
import com.duoc.rotompedia.data.local.*
import com.duoc.rotompedia.data.model.*
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PokemonRepositoryTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private fun memoryDb() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    private fun fixture(extra: Boolean = false): IndexPayload {
        val species = listOf(SpeciesRecord(3, 1, "venusaur", 1, null, listOf(NameRecord("es", "Venusaur"))),
            SpeciesRecord(718, 2, "zygarde", 6, null, listOf(NameRecord("es", "Zygarde")))) +
            if (extra) listOf(SpeciesRecord(745, 3, "lycanroc", 7, null, listOf(NameRecord("es", "Lycanroc")))) else emptyList()
        val varieties = listOf(VarietyRecord(3, 3, "venusaur", true), VarietyRecord(10033, 3, "venusaur-mega", false),
            VarietyRecord(718, 718, "zygarde-50", true), VarietyRecord(10301, 718, "zygarde-mega", false)) +
            if (extra) listOf(VarietyRecord(745, 745, "lycanroc-midday", true)) else emptyList()
        return IndexPayload("https://pokeapi.co/api/v2/pokedex/national/", species.size, varieties.size, species, varieties)
    }
    private fun json(payload: IndexPayload = fixture(), date: String = "2026-11-03T00:00:00Z"): String {
        val content = moshi.adapter(IndexPayload::class.java).toJson(payload)
        return moshi.adapter(IndexEnvelope::class.java).toJson(IndexEnvelope(1, date, "1", IndexValidator.sha256(content), content))
    }
    private fun repository(db: AppDatabase, api: PokeApiService = Mockito.mock(PokeApiService::class.java), source: String = json()) =
        DefaultPokemonRepository(db, api, PokemonIndexImporter(db, moshi)) { source }

    @Test fun `bundled generated snapshot imports completely and survives reopening offline`() = runTest {
        val name = "persistent-index.db"
        context.deleteDatabase(name)
        val source = context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        val api = Mockito.mock(PokeApiService::class.java)
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()
        try {
            val repo = repository(db, api, source)
            repo.ensureIndex()
            val rows = repo.observeNational().first()
            assertTrue(rows.size >= 1025)
            assertEquals((1..rows.size).toList(), rows.map { it.nationalNumber })
            assertEquals(PokemonIdentity(718, 718, 10301, "zygarde-mega"), repo.resolve(10301))
            assertEquals(3, repo.observeVarieties(745).first().size)
            assertFalse(repo.observeIndexState().first()!!.formsComplete)
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()
            val reopened = repository(db, api, source)
            assertEquals(rows, reopened.observeNational().first())
            assertEquals(718, reopened.resolve(10301)!!.nationalNumber)
            Mockito.verifyNoInteractions(api)
        } finally { db.close() }
    }

    @Test fun `snapshot is idempotent and rejects corruption orphan rows and disappearance`() = runTest {
        val db = memoryDb()
        try {
            val repo = repository(db)
            assertTrue(repo.importIndex(json()))
            assertFalse(repo.importIndex(json()))
            val previous = db.pokemonIndexDao().state()
            val orphan = fixture().let { it.copy(varieties = it.varieties.map { row -> row.copy(speciesId = 0) }) }
            val missing = fixture().let { it.copy(varietyCount = 3, varieties = it.varieties.dropLast(1)) }
            for (bad in listOf(json().replace("contentSha256", "wrongField"), json(orphan), json(missing),
                json(fixture(true), "2026-11-02T00:00:00Z"))) {
                try { repo.importIndex(bad); fail("Bad snapshot accepted") } catch (_: Exception) { }
                assertEquals(previous, db.pokemonIndexDao().state())
                assertEquals(4, db.pokemonIndexDao().varietyCount())
            }
        } finally { db.close() }
    }

    @Test fun `SQL failure rolls back the whole index and never touches personal data`() = runTest {
        val db = memoryDb()
        try {
            val repo = repository(db)
            repo.ensureIndex()
            db.favoritoDao().agregarFavorito(PokemonFavorito("ash", 10051, "Mega Blaziken"))
            val previous = db.pokemonIndexDao().state()
            db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_names BEFORE INSERT ON pokemon_names BEGIN SELECT RAISE(ABORT,'injected failure'); END")
            try { repo.importIndex(json(fixture(true), "2026-11-04T00:00:00Z")); fail("Injected failure ignored") }
            catch (_: Exception) { }
            assertEquals(previous, db.pokemonIndexDao().state())
            assertNull(db.pokemonIndexDao().species(745))
            assertNull(db.pokemonIndexDao().variety(745))
            assertEquals("Mega Blaziken", db.favoritoDao().obtenerPorId("ash", 10051)!!.nombre)
        } finally { db.close() }
    }

    @Test fun `concurrent detail requests share a fetch and persisted fallback retains species identity`() = runTest {
        val db = memoryDb()
        val calls = AtomicInteger()
        var offline = false
        val api = object : PokeApiService by Mockito.mock(PokeApiService::class.java) {
            override suspend fun getPokemonByName(name: String): PokemonData {
                calls.incrementAndGet()
                delay(25)
                if (offline) error("offline")
                return PokemonData(10033, "venusaur-mega", SpritesData(null), listOf(TypesSlot(1, TypeData("grass"))),
                    listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed").map { StatInfo(80, Stat(it)) })
            }
        }
        try {
            val repo = repository(db, api)
            repo.ensureIndex()
            val first = async { repo.getPokemon("venusaur-mega") }
            val second = async { repo.getPokemon("10033") }
            assertEquals(first.await(), second.await())
            assertEquals(1, calls.get())
            offline = true
            val fallback = repository(db, api).getPokemon("10033")
            assertEquals(10033, fallback.id)
            assertEquals("https://pokeapi.co/api/v2/pokemon-species/3/", fallback.species!!.url)
            assertEquals(80, fallback.stats.first().baseStat)
        } finally { db.close() }
    }

    @Test fun `legacy mismatched names remain conflicts without rewriting stored IDs`() = runTest {
        val db = memoryDb()
        try {
            val repo = repository(db)
            repo.ensureIndex()
            assertTrue(repo.resolveLegacy(10033, "Mega Venusaur") is IdentityResolution.Resolved)
            assertTrue(repo.resolveLegacy(10033, "Mega Blaziken") is IdentityResolution.Conflict)
            assertEquals(IdentityResolution.Unknown, repo.resolveLegacy(999999, "Unknown"))
        } finally { db.close() }
    }

    @Test fun `form IDs are distinct resources and cannot refer to an unknown variety`() = runTest {
        val db = memoryDb()
        val api = Mockito.mock(PokeApiService::class.java)
        try {
            val repo = repository(db, api)
            repo.ensureIndex()
            Mockito.`when`(api.getPokemonFormIdentity(10526)).thenReturn(PokemonFormIdentityResponse(10526, "zygarde-mega",
                NamedApiResource("zygarde-mega", "https://pokeapi.co/api/v2/pokemon/10301/"), true, true))
            repo.refreshForm(10526)
            assertEquals(10301, db.pokemonIndexDao().form(10526)!!.pokemonId)
            Mockito.`when`(api.getPokemonFormIdentity(10527)).thenReturn(PokemonFormIdentityResponse(10527, "unknown",
                NamedApiResource("unknown", "https://pokeapi.co/api/v2/pokemon/99999/"), false, false))
            try { repo.refreshForm(10527); fail("Orphan accepted") } catch (_: IllegalArgumentException) { }
            assertNull(db.pokemonIndexDao().form(10527))
        } finally { db.close() }
    }

    @Test fun `species refresh validates parent identity and adds only linked varieties`() = runTest {
        val db = memoryDb()
        val api = Mockito.mock(PokeApiService::class.java)
        try {
            val repo = repository(db, api)
            repo.ensureIndex()
            val response = PokemonSpeciesVarieties(718, "zygarde", EvolutionChainUrl("https://pokeapi.co/api/v2/evolution-chain/370/"),
                emptyList(), varieties = listOf(
                    PokemonVariety(true, NamedApiResource("zygarde-50", "https://pokeapi.co/api/v2/pokemon/718/")),
                    PokemonVariety(false, NamedApiResource("zygarde-mega", "https://pokeapi.co/api/v2/pokemon/10301/")),
                    PokemonVariety(false, NamedApiResource("zygarde-complete", "https://pokeapi.co/api/v2/pokemon/10120/"))))
            Mockito.`when`(api.getPokemonSpeciesWithVarieties(718)).thenReturn(response)
            repo.refreshSpecies(718)
            assertEquals(718, repo.resolve(10120)!!.speciesId)
            assertEquals(5, repo.observeIndexState().first()!!.varietyCount)
            Mockito.`when`(api.getPokemonSpeciesWithVarieties(718)).thenReturn(response.copy(id = 3))
            try { repo.refreshSpecies(718); fail("Wrong species accepted") } catch (_: IllegalArgumentException) { }
            assertEquals(3, repo.observeVarieties(718).first().size)
        } finally { db.close() }
    }
}
