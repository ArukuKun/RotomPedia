package com.duoc.rotompedia

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.PokemonMoveData
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.model.*
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.DefaultPokemonRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ReferenceRepositoryTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val api = Mockito.mock(PokeApiService::class.java)
    private fun source(name: String) = context.assets.open(name).bufferedReader().use { it.readText() }
    private fun store() = ReferenceStore(moshi) { source("reference-index.json") }
    private fun db() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    private fun pokemon(db: AppDatabase) = DefaultPokemonRepository(db, api, PokemonIndexImporter(db, moshi)) { source("pokemon-index.json") }
    private fun moves(db: AppDatabase, store: ReferenceStore = store()) = DefaultMoveRepository(store, db, api, pokemon(db), moshi)
    private fun resource(endpoint: String, id: Int, name: String) = NamedApiResource(name, "https://pokeapi.co/api/v2/$endpoint/$id/")
    private fun detail(move: ReferenceMove, group: String, method: String, level: Int) = PokemonMoveEntry(
        resource("move", move.id, move.slug), listOf(MoveVersionGroupDetail(level, resource("move-learn-method", 1, method), resource("version-group", 17, group))))
    private fun data(rows: List<PokemonMoveEntry>) = PokemonDataWithMoves(10104, "ninetales-alola", SpritesData(null), emptyList(), emptyList(), moves = rows)

    @Test fun `reference is complete local and supplies Spanish aliases without API`() = runTest {
        val store = store()
        val index = store.index()
        assertTrue(index === store.index())
        assertTrue(index.moves.size >= 937)
        assertTrue(index.abilities.size >= 374)
        assertEquals(53, ReferencePresentation.filterMoves(index, "lanzallamas").single().id)
        assertEquals(53, PokemonMoveData.findMove("Flamethrower")!!.moveId)
        assertEquals(53, PokemonMoveData.findMove("Lanzallamas")!!.moveId)
        val canonical = index.moves.single { it.slug == "vice-grip" }
        assertEquals(canonical.id, PokemonMoveData.findMove(canonical.slug)!!.moveId)
        assertNull(PokemonMoveData.findMove("Movimiento antiguo desconocido"))
        assertNull(index.moves.single { it.slug == "heavy-slam" }.power)
        Mockito.verifyNoInteractions(api)
    }

    @Test fun `client scope removes XD and Colosseum but preserves raw snapshot and legitimate shadow named content`() = runTest {
        val raw = store().index()
        val visible = GameContentScope.catalogue(raw)
        assertEquals(937, raw.moves.size)
        assertEquals(18, raw.moves.count { it.type == "shadow" })
        assertEquals(919, visible.moves.size)
        assertFalse(visible.moves.any { it.type == "shadow" })
        assertFalse(visible.versions.any { it.slug in setOf("xd", "colosseum") })
        assertTrue(visible.moves.any { it.slug == "shadow-ball" })
        assertTrue(visible.moves.any { it.slug == "shadow-force" })
        assertTrue(visible.abilities.any { it.slug == "shadow-tag" })
        assertTrue((visible.moves.flatMap { it.texts } + visible.abilities.flatMap { it.texts }).all { GameContentScope.allowsVersion(it.versionGroup) })
        assertTrue(visible.machines.all { GameContentScope.allowsVersion(it.versionGroup) && visible.moves.any { m -> m.id == it.moveId } })
        assertNull(PokemonMoveData.findMove("shadow-chill"))
        assertNotNull(PokemonMoveData.findMove("shadow-ball"))
        assertEquals(2, raw.versions.count { it.slug in setOf("xd", "colosseum") })
        Mockito.verifyNoInteractions(api)
    }

    @Test fun `learning scope hides known excluded moves and editions but keeps unknown identities and original rows`() {
        val rows = listOf(LearnRow(10006, "shadow-chill", "xd", "level-up", 0),
            LearnRow(10006, "shadow-chill", "sun-moon", "tutor", 0),
            LearnRow(247, "shadow-ball", "xd", "machine", 0),
            LearnRow(247, "shadow-ball", "sun-moon", "machine", 0),
            LearnRow(99999, "unknown-move", "sun-moon", "new-method", 0))
        val raw = Learnset(10104, rows)
        assertEquals(listOf(247, 99999), GameContentScope.learnset(raw).rows.map { it.moveId })
        assertEquals(5, raw.rows.size)
    }

    @Test fun `hidden move identity still validates against raw source and cannot poison cache`() = runTest {
        val db = db()
        try {
            val repo = moves(db)
            val wrong = PokemonMoveEntry(resource("move", 10006, "flamethrower"),
                listOf(MoveVersionGroupDetail(1, resource("move-learn-method", 1, "level-up"), resource("version-group", 17, "sun-moon"))))
            Mockito.`when`(api.getPokemonWithMoves("10104")).thenReturn(data(listOf(wrong)))
            try { repo.learnset(10104); fail("Hidden identity was reassigned") }
            catch (_: IllegalArgumentException) { }
            assertNull(db.learnsetDao().cached(10104))
        } finally { db.close() }
    }

    @Test fun `learnset retains more than sixty moves all methods levels and exact editions`() = runTest {
        val db = db()
        try {
            val repository = moves(db)
            val index = repository.index()
            val rows = index.moves.take(80).map { detail(it, "sun-moon", "level-up", 20) } + listOf(
                detail(index.moves.first(), "sun-moon", "machine", 0),
                detail(index.moves.first(), "sun-moon", "egg", 0),
                detail(index.moves.first(), "sword-shield", "level-up", 50))
            Mockito.`when`(api.getPokemonWithMoves("10104")).thenReturn(data(rows))
            val results = (1..3).map { async { repository.learnset(10104) } }.awaitAll()
            assertEquals(83, results.first().data.rows.size)
            val selected = ReferencePresentation.learned(results.first().data.rows, "sun-moon")
            assertEquals(80, selected.size)
            assertEquals(setOf("level-up", "machine", "egg"), selected.getValue(index.moves.first().id).map { it.method }.toSet())
            assertEquals(50, ReferencePresentation.learned(results.first().data.rows, "sword-shield").values.single().single().level)
            assertTrue(ReferencePresentation.learned(results.first().data.rows, null).isEmpty())
            Mockito.verify(api, Mockito.times(1)).getPokemonWithMoves("10104")
        } finally { db.close() }
    }

    @Test fun `learnset survives reopening offline with exact variety ID`() = runTest {
        val name = "reference-learnset.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()
        try {
            val repo = moves(db)
            Mockito.`when`(api.getPokemonWithMoves("10104")).thenReturn(data(listOf(detail(repo.index().moves.first(), "sun-moon", "egg", 0))))
            val original = repo.learnset(10104)
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()
            Mockito.`when`(api.getPokemonWithMoves("10104")).thenAnswer { throw java.io.IOException("offline") }
            val reopened = moves(db).learnset(10104, true)
            assertTrue(reopened.offline)
            assertEquals(original.data, reopened.data)
            assertEquals(10104, reopened.data.pokemonId)
        } finally { db.close() }
    }

    @Test fun `wrong Pokemon or move identity cannot poison cache`() = runTest {
        val db = db()
        try {
            val repository = moves(db)
            Mockito.`when`(api.getPokemonWithMoves("10104")).thenReturn(data(emptyList()).copy(id = 38))
            try { repository.learnset(10104); fail("Wrong variety accepted") } catch (_: IllegalArgumentException) { }
            assertNull(db.learnsetDao().cached(10104))
            val row = detail(repository.index().moves.first(), "sun-moon", "machine", 0)
            Mockito.`when`(api.getPokemonWithMoves("10104")).thenReturn(data(listOf(row.copy(move = row.move.copy(name = "wrong-slug")))))
            try { repository.learnset(10104); fail("Wrong move accepted") } catch (_: IllegalArgumentException) { }
            assertNull(db.learnsetDao().cached(10104))
        } finally { db.close() }
    }

    @Test fun `ability normal hidden and slot belong to the variety not national species`() = runTest {
        val db = db()
        try {
            val repo = DefaultAbilityRepository(store(), pokemon(db))
            val alola = repo.forPokemon(10104)
            assertEquals(setOf(81, 117), alola.map { it.abilityId }.toSet())
            assertTrue(alola.single { it.abilityId == 117 }.hidden)
            assertEquals(3, alola.single { it.abilityId == 117 }.slot)
            assertEquals(setOf(18, 70), repo.forPokemon(38).map { it.abilityId }.toSet())
            assertEquals("Mar Llamas", repo.index().abilities.single { it.id == 66 }.displayName)
            Mockito.verifyNoInteractions(api)
        } finally { db.close() }
    }

    @Test fun `all bundled abilities retain exact holder identities roles and historical name aliases`() = runTest {
        val index = store().index()
        index.abilities.forEach { ability ->
            val links = index.pokemonAbilities.filter { it.abilityId == ability.id }
            val summary = AbilityPresentation.holders(index, ability.id)
            assertEquals(links.map { it.pokemonId }.toSet().size, summary.total)
            assertEquals(links.filterNot { it.hidden }.map { it.pokemonId }.toSet().size, summary.normal)
            assertEquals(links.filter { it.hidden }.map { it.pokemonId }.toSet().size, summary.hidden)
            assertTrue(AbilityNames.matches(ability, ability.displayName))
            ability.nameEs?.let { assertTrue(AbilityNames.matches(ability, it)) }
            summary.holders.forEach { holder ->
                val source = links.filter { it.pokemonId == holder.pokemonId }
                assertEquals(source.filterNot { it.hidden }.map { it.slot }.toSet(), holder.normalSlots.toSet())
                assertEquals(source.filter { it.hidden }.map { it.slot }.toSet(), holder.hiddenSlots.toSet())
            }
        }
        assertEquals("Pereza", index.abilities.single { it.slug == "truant" }.displayName)
        assertEquals("Ausente", index.abilities.single { it.slug == "truant" }.nameEs)
        assertEquals("Viscosecreción", index.abilities.single { it.slug == "liquid-ooze" }.displayName)
        assertEquals("Esclusa de Aire", index.abilities.single { it.slug == "air-lock" }.displayName)
        Mockito.verifyNoInteractions(api)
    }

    @Test fun `flavor text is edition specific and does not invent Spanish effects`() = runTest {
        val index = store().index()
        val move = index.moves.single { it.id == 53 }
        val spanish = move.texts.first { it.language == "es" }
        assertEquals(spanish, ReferencePresentation.text(move.texts, spanish.versionGroup))
        assertNull(ReferencePresentation.text(move.texts, null))
        assertNull(ReferencePresentation.text(move.texts, "not-a-game"))
        assertEquals(0, move.priority)
        assertFalse(move.effectEn.orEmpty().contains("\$effect_chance"))
        val relation = index.machines.first { it.moveId == 53 }
        assertTrue(relation.itemId > 0)
        assertTrue(index.versions.any { it.slug == relation.versionGroup })
        val unknown = LearnRow(53, "flamethrower", "future-group", "future-method", 0)
        assertTrue("future-group" in ReferencePresentation.groups(index, listOf(unknown)))
    }

    @Test fun `tampered reference is rejected rather than used as facts`() = runTest {
        val original = moshi.adapter(ReferenceEnvelope::class.java).fromJson(source("reference-index.json"))!!
        val broken = moshi.adapter(ReferenceEnvelope::class.java).toJson(original.copy(payloadJson = original.payloadJson + " "))
        try { ReferenceStore(moshi) { broken }.index(); fail("Corruption accepted") } catch (_: IllegalArgumentException) { }
    }
}
