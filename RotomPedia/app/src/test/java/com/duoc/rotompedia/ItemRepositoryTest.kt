package com.duoc.rotompedia

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.data.index.IndexValidator
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.model.NamedApiResource
import com.duoc.rotompedia.data.remote.PokeApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
class ItemRepositoryTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val api = Mockito.mock(PokeApiService::class.java)
    private fun source() = context.assets.open("item-index.json").bufferedReader().use { it.readText() }
    private fun db() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    private fun repo(db: AppDatabase) = DefaultItemRepository(db, api, moshi) { source() }
    private fun resource(name: String) = NamedApiResource(name, "")
    private fun shuca() = ItemDetail(168, "shuca-berry", resource("type-protection"),
        attributes = listOf(resource("holdable-active")),
        descriptions = listOf(ItemFlavor("Debilita un ataque supereficaz de tipo Tierra.", resource("es"), resource("sun-moon"))),
        effects = listOf(ItemEffect(shortEffect = "Halves super-effective Ground damage.", language = resource("en"))))

    @Test fun `bundled index is complete local searchable and does not query API`() = runTest {
        val db = db()
        try {
            val repo = repo(db); repo.ensureIndex(); repo.ensureIndex()
            val rows = repo.observeItems().first()
            assertTrue(rows.size >= 2200)
            assertEquals(rows.size, rows.distinctBy { it.itemId }.size)
            assertTrue(rows.all { it.category in ItemLabels.categories })
            assertEquals("Baya Acardo", repo.resolve("shuca-berry")!!.name)
            val found = ItemPresentation.filter(rows, ItemFilter("acardo", "type-protection", "holdable-active"))
            assertEquals(listOf(168), found.map { it.itemId })
            assertTrue(ItemPresentation.filter(rows, ItemFilter("pocion")).any { it.slug == "potion" })
            for (slug in listOf("choice-band", "fire-stone", "potion", "tm01", "yache-berry"))
                assertNotNull(repo.resolve(slug))
            assertEquals(2, rows.count { it.slug == "roseli-berry" })
            Mockito.verifyNoInteractions(api)
        } finally { db.close() }
    }

    @Test fun `detail requests coalesce and persisted cached copy survives reopening offline`() = runTest {
        val name = "item-detail-test.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()
        try {
            Mockito.`when`(api.getItem("168")).thenReturn(shuca())
            val repo = repo(db)
            val results = (1..4).map { async { repo.detail(168) } }.awaitAll()
            assertTrue(results.all { it.detail.id == 168 })
            Mockito.verify(api, Mockito.times(1)).getItem("168")
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()
            Mockito.`when`(api.getItem("168")).thenAnswer { throw java.io.IOException("offline") }
            val cached = repo(db).detail(168, refresh = true)
            assertTrue(cached.offline)
            assertEquals(shuca(), cached.detail)
        } finally { db.close() }
    }

    @Test fun `wrong API identity is rejected without poisoning cache`() = runTest {
        val db = db()
        try {
            Mockito.`when`(api.getItem("168")).thenReturn(shuca().copy(id = 999))
            try { repo(db).detail(168); fail("Wrong identity accepted") } catch (_: IllegalArgumentException) { }
            assertNull(db.itemDao().cached(168))
            Mockito.`when`(api.getItem("168")).thenReturn(shuca())
            repo(db).detail(168)
            val before = db.itemDao().cached(168)
            Mockito.`when`(api.getItem("168")).thenReturn(shuca().copy(name = "wrong-berry"))
            assertTrue(repo(db).detail(168, true).offline)
            assertEquals(before, db.itemDao().cached(168))
        } finally { db.close() }
    }

    @Test fun `corrupt or reassigned index is rejected atomically`() = runTest {
        val db = db()
        try {
            repo(db).ensureIndex()
            val previous = db.itemDao().items()
            val envelope = moshi.adapter(ItemIndexEnvelope::class.java).fromJson(source())!!
            val payload = moshi.adapter(ItemIndexPayload::class.java).fromJson(envelope.payloadJson)!!
            val changed = payload.copy(items = payload.items.map { if (it.itemId == 168) it.copy(slug = "wrong-berry") else it })
            val json = moshi.adapter(ItemIndexPayload::class.java).toJson(changed)
            val bad = moshi.adapter(ItemIndexEnvelope::class.java).toJson(envelope.copy(payloadJson = json, sha256 = IndexValidator.sha256(json)))
            try { DefaultItemRepository(db, api, moshi) { bad }.ensureIndex(); fail("Reassignment accepted") } catch (_: IllegalArgumentException) { }
            assertEquals(previous, db.itemDao().items())
            Mockito.verifyNoInteractions(api)
        } finally { db.close() }
    }

    @Test fun `duplicate slugs resolve through canonical API id not arbitrary first row`() = runTest {
        val db = db()
        try {
            val repo = repo(db)
            Mockito.`when`(api.getItem("roseli-berry")).thenReturn(ItemDetail(723, "roseli-berry", resource("type-protection")))
            assertEquals(723, repo.resolve("roseli-berry")!!.itemId)
            Mockito.`when`(api.getItem("roseli-berry")).thenAnswer { throw java.io.IOException("offline") }
            assertNull(repo.resolve("roseli-berry"))
        } finally { db.close() }
    }
}
