package com.duoc.rotompedia

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.model.NamedApiResource
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.ui.screens.CompetitiveItemsScreen
import com.duoc.rotompedia.ui.screens.EvolutionChainColumn
import com.duoc.rotompedia.viewmodel.EvolutionStage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ItemUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var repo: ItemRepository
    private lateinit var api: PokeApiService
    private fun resource(name: String) = NamedApiResource(name, "")
    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        api = Mockito.mock(PokeApiService::class.java)
        repo = DefaultItemRepository(db, api, Moshi.Builder().add(KotlinJsonAdapterFactory()).build()) {
            context.assets.open("item-index.json").bufferedReader().use { it.readText() }
        }
        runBlocking { repo.ensureIndex() }
    }
    @After fun close() { db.close() }

    @Test fun `local item search opens correct berry and displays reviewed Spanish effect`() {
        runBlocking { Mockito.`when`(api.getItem("168")).thenReturn(ItemDetail(168, "shuca-berry", resource("type-protection"))) }
        compose.setContent { MaterialTheme { CompetitiveItemsScreen({}, itemRepository = repo) } }
        compose.onNodeWithText("Buscar objeto").performTextInput("acardo")
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Baya Acardo").fetchSemanticsNodes().isNotEmpty() }
        Mockito.verifyNoInteractions(api)
        compose.onNodeWithText("Baya Acardo").performScrollTo().assertIsDisplayed().performClick()
        compose.waitUntil(15_000) {
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            compose.onAllNodesWithText(ItemEditorial.shucaEffect).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(ItemEditorial.shucaEffect).performScrollTo().assertIsDisplayed()
        runBlocking { Mockito.verify(api, Mockito.times(1)).getItem("168") }
    }

    @Test fun `missing offline detail has explicit error and can retry without losing item identity`() {
        runBlocking { Mockito.`when`(api.getItem("168")).thenAnswer { throw java.io.IOException("offline") } }
        compose.setContent { MaterialTheme { CompetitiveItemsScreen({}, initialSlug = "shuca-berry", itemRepository = repo) } }
        val message = "No se pudo cargar la ficha. Comprueba tu conexión e inténtalo de nuevo."
        compose.waitUntil(15_000) { compose.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(message).assertIsDisplayed()
        compose.onNodeWithText("Reintentar").performClick()
        compose.waitForIdle()
        runBlocking { assertNull(db.itemDao().cached(168)) }
        compose.onAllNodesWithText("Baya Acardo").onLast().assertIsDisplayed()
    }

    @Test fun `evolution item link preserves resource slug and does not select another Pokemon`() {
        var item: String? = null
        var pokemon: String? = null
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    EvolutionChainColumn(listOf(EvolutionStage("133", "Eevee", "Forma Base", true),
                        EvolutionStage("136", "Flareon", "Piedra Fuego", evolvesFromId = "133", itemSlugs = listOf("fire-stone"))),
                        onItemClick = { item = it }, onPokemonClick = { pokemon = it })
                }
            }
        }
        compose.onNodeWithText("Consultar objeto de evolución · fire-stone").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("fire-stone", item); assertNull(pokemon) }
    }
}
