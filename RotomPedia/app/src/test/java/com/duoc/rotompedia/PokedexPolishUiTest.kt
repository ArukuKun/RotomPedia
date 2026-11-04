package com.duoc.rotompedia

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.DefaultPokemonRepository
import com.duoc.rotompedia.ui.screens.PokemonExplorerContent
import com.duoc.rotompedia.viewmodel.PokemonExplorerController
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
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
class PokedexPolishUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var controller: PokemonExplorerController
    private val favorites = MutableStateFlow(emptySet<Int>())
    private var chosen: Int? = null

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val repo = DefaultPokemonRepository(db, Mockito.mock(PokeApiService::class.java), PokemonIndexImporter(db, moshi)) {
            context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        }
        runBlocking { repo.ensureIndex() }
        compose.setContent {
            val scope = rememberCoroutineScope()
            controller = remember { PokemonExplorerController(scope, repo, favorites, nationalOnly = true) }
            val ids by favorites.collectAsState()
            MaterialTheme {
                PokemonExplorerContent(controller, { chosen = it.pokemonId }, Modifier.fillMaxSize(), ids,
                    onFavorite = { pokemon -> favorites.value = if (pokemon.pokemonId in favorites.value) favorites.value - pokemon.pokemonId else favorites.value + pokemon.pokemonId })
            }
        }
        compose.waitUntil(20_000) { controller.result.value.entries.size == 1025 }
    }

    @After fun close() { db.close() }

    @Test fun `list is default with original sprite proportions and no variety dropdowns`() {
        compose.onNodeWithTag("pokedex-list").assertExists()
        compose.onNodeWithTag("pokedex-grid").assertDoesNotExist()
        compose.onNodeWithTag("sprite_1", useUnmergedTree = true).assertWidthIsEqualTo(72.dp).assertHeightIsEqualTo(72.dp)
        compose.onNodeWithTag("pokemon_1").assertHeightIsAtLeast(96.dp)
        compose.onNodeWithText("Bulbasaur").assertExists()
        compose.onNodeWithText("▸ 2 variantes").assertDoesNotExist()
        compose.onNodeWithTag("pokedex-list").performScrollToIndex(2)
        compose.onNodeWithText("Venusaur").assertExists()
        compose.onNodeWithText("Mega Venusaur").assertDoesNotExist()
    }

    @Test fun `grid has three columns and scrolls beyond nine through all results`() {
        compose.onNodeWithText("Cuadrícula").performClick()
        val bounds = (1..3).map { compose.onNodeWithTag("pokemon_$it").fetchSemanticsNode().boundsInRoot }
        assertEquals(bounds[0].top, bounds[1].top)
        assertEquals(bounds[1].top, bounds[2].top)
        assertTrue(bounds[0].left < bounds[1].left && bounds[1].left < bounds[2].left)
        compose.onNodeWithTag("pokedex-grid").performScrollToIndex(24)
        compose.onNodeWithTag("pokemon_25").assertExists()
        compose.onNodeWithTag("pokedex-grid").performScrollToIndex(1024)
        compose.onNodeWithText("#1025").assertExists()
    }

    @Test fun `search favorites navigation and filters survive view changes`() {
        compose.onNodeWithText("Nombre, forma o número nacional").performTextInput("#038")
        compose.waitUntil(20_000) { controller.result.value.entries.size == 1 }
        compose.onNodeWithContentDescription("Favorito de Ninetales").performScrollTo().performClick()
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.onNode(hasTestTag("pokemon_38") and hasText("#038")).assertExists()
        compose.onNodeWithText("Ninetales de Alola").assertDoesNotExist()
        compose.runOnIdle { controller.filter.value = controller.filter.value.copy(favoritesOnly = true, eggGroup = "ground") }
        compose.waitUntil(20_000) { controller.result.value.entries.map { it.pokemonId } == listOf(38) }
        compose.onNodeWithTag("pokemon_38").performClick()
        compose.runOnIdle { assertEquals(38, chosen); assertEquals(setOf(38), favorites.value) }
        compose.onNodeWithText("Lista").performClick()
        compose.onNodeWithText("Ninetales").assertExists()
        compose.runOnIdle { assertEquals("#038", controller.filter.value.query); assertEquals("ground", controller.filter.value.eggGroup) }
    }

    @Test fun `Gigamax filter is available and navigates to exact variety not national id`() {
        compose.onNodeWithText("Filtros", substring = false).performClick()
        compose.onNodeWithText("Gigamax", substring = false).performScrollTo().performClick()
        compose.waitUntil(20_000) { controller.result.value.entries.size == 34 }
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.onNodeWithTag("pokedex-list").performScrollToIndex(1)
        compose.onNodeWithText("Charizard Gigamax").performClick()
        compose.runOnIdle { assertEquals(10196, chosen) }
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.runOnIdle { assertEquals(VarietyCategory.GMAX, controller.filter.value.category) }
    }

    @Test fun `egg group filter from shared Spanish metadata is interactive`() {
        compose.onNodeWithText("Filtros", substring = false).performClick()
        compose.onNodeWithText("Ditto", substring = false).performScrollTo().performClick()
        compose.waitUntil(20_000) { controller.result.value.entries.map { it.slug } == listOf("ditto") }
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.onNodeWithTag("pokemon_132").assertExists()
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.onNodeWithTag("pokemon_132").assertExists()
    }
}
