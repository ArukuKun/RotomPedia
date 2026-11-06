package com.duoc.rotompedia

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.DefaultPokemonRepository
import com.duoc.rotompedia.ui.screens.PokedexExplorerScreen
import com.duoc.rotompedia.ui.screens.rememberPokedexFilter
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
class PokedexNavigationUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var controller: PokemonExplorerController
    private lateinit var restoration: StateRestorationTester
    private val favorites = MutableStateFlow(emptySet<Int>())

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val repo = DefaultPokemonRepository(db, Mockito.mock(PokeApiService::class.java), PokemonIndexImporter(db, moshi)) {
            context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
        }
        runBlocking { repo.ensureIndex() }
        restoration = StateRestorationTester(compose)
        restoration.setContent {
            val nav = rememberNavController()
            MaterialTheme {
                NavHost(navController = nav, startDestination = "dex") {
                    composable("dex") {
                        val filters = rememberPokedexFilter()
                        val scope = rememberCoroutineScope()
                        val explorer = remember(filters) { PokemonExplorerController(scope, repo, favorites, true, filters) }
                        controller = explorer
                        PokedexExplorerScreen(explorer, onNavigateBack = { nav.popBackStack() },
                            onChoose = { nav.navigate("detail/${it.pokemonId}") })
                    }
                    composable("detail/{id}") { entry ->
                        Column {
                            Text("Ficha ${entry.arguments?.getString("id")}")
                            Button(onClick = { nav.popBackStack() }) { Text("Volver a Pokédex") }
                        }
                    }
                }
            }
        }
        compose.waitUntil(20_000) { controller.result.value.ready && controller.result.value.entries.size == 1025 }
    }

    @After fun close() { db.close() }

    private fun kalosFurfrou() {
        compose.runOnIdle { controller.filter.value = ExplorerFilter(generationId = 6) }
        compose.waitUntil(20_000) { controller.result.value.ready && controller.result.value.entries.size in 1..100 }
        val index = controller.result.value.entries.indexOfFirst { it.slug == "furfrou" }
        assertTrue(index > 0)
        compose.onNodeWithTag("pokedex-list").performScrollToIndex(index)
        compose.onNodeWithTag("pokedex-list").performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 18f) }
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
    }

    @Test fun `Kalos Furfrou back navigation restores filter and exact list position`() {
        kalosFurfrou()
        val before = compose.onNodeWithTag("pokemon_676").getUnclippedBoundsInRoot().top.value
        compose.onNodeWithTag("pokemon_676").performClick()
        compose.onNodeWithText("Ficha 676").assertExists()
        compose.onNodeWithText("Volver a Pokédex").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokedex-list").fetchSemanticsNodes().size == 1 &&
            controller.result.value.ready && controller.filter.value.generationId == 6 }
        compose.onNodeWithTag("pokedex-list").assertExists()
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
        assertEquals(before, compose.onNodeWithTag("pokemon_676").getUnclippedBoundsInRoot().top.value, 1f)
        assertTrue(controller.result.value.entries.all { it.generationId == 6 })
    }

    @Test fun `Hisui grid back navigation restores regional filter view and position`() {
        compose.runOnIdle { controller.filter.value = ExplorerFilter(regionalOrigin = "hisui") }
        compose.waitUntil(20_000) { controller.result.value.regionalOrder?.region == "hisui" }
        compose.onNodeWithText("Cuadrícula").performClick()
        val target = controller.result.value.entries[9]
        compose.onNodeWithTag("pokedex-grid").performScrollToIndex(9)
        compose.onNodeWithTag("pokedex-grid").performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 18f) }
        val tag = "pokemon_${target.pokemonId}"
        val before = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot().top.value
        compose.onNodeWithTag(tag).performClick()
        compose.onNodeWithText("Ficha ${target.pokemonId}").assertExists()
        compose.onNodeWithText("Volver a Pokédex").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokedex-grid").fetchSemanticsNodes().size == 1 &&
            controller.result.value.ready && controller.result.value.regionalOrder?.region == "hisui" }
        compose.onNodeWithTag("pokedex-grid").assertExists()
        compose.onNodeWithTag(tag).assertIsDisplayed()
        assertEquals(before, compose.onNodeWithTag(tag).getUnclippedBoundsInRoot().top.value, 1f)
        assertEquals(25, controller.result.value.entries.size)
    }

    @Test fun `saved instance restoration keeps filters before restoring scroll`() {
        kalosFurfrou()
        restoration.emulateSavedInstanceStateRestore()
        compose.waitUntil(20_000) { controller.result.value.ready && controller.filter.value.generationId == 6 }
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
        assertTrue(controller.result.value.entries.all { it.generationId == 6 })
    }

    @Test fun `changing filters intentionally starts at first result not half national catalogue`() {
        kalosFurfrou()
        compose.runOnIdle { controller.filter.value = ExplorerFilter() }
        compose.waitUntil(20_000) { controller.result.value.ready && controller.result.value.entries.size == 1025 }
        compose.onNodeWithTag("pokemon_1").assertIsDisplayed()
    }

    @Test fun `changing appearance preserves Kalos Furfrou filter and exact scroll position`() {
        kalosFurfrou()
        val before = compose.onNodeWithTag("pokemon_676").getUnclippedBoundsInRoot().top.value
        compose.onNodeWithContentDescription("Cambiar apariencia").performClick()
        compose.onNodeWithText("Claro", substring = false).performClick()
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
        assertEquals(6, controller.filter.value.generationId)
        assertEquals(before, compose.onNodeWithTag("pokemon_676").getUnclippedBoundsInRoot().top.value, 1f)
        compose.onNodeWithContentDescription("Cambiar apariencia").performClick()
        compose.onNodeWithText("Seguir configuración del sistema").performClick()
        compose.onNodeWithText("Cerrar").performClick()
    }

    @Test fun `filter icon belongs to navigation bar and panel has no technical prose or irrelevant categories`() {
        val title = compose.onNodeWithText("Pokédex").fetchSemanticsNode().boundsInRoot
        val button = compose.onNodeWithContentDescription("Filtrar Pokémon").assertHasClickAction().fetchSemanticsNode().boundsInRoot
        assertEquals(title.center.y, button.center.y, 4f)
        assertTrue(button.left > title.right)
        compose.onNodeWithText("Filtrar", substring = false).assertDoesNotExist()
        compose.onNodeWithContentDescription("Filtrar Pokémon").performClick()
        compose.onNodeWithText("Filtros de Pokémon").assertExists()
        compose.onNodeWithText("Variedades estables").assertDoesNotExist()
        compose.onNodeWithText("Forma alternativa", substring = false).assertDoesNotExist()
        compose.onNodeWithText("Combate", substring = false).assertDoesNotExist()
        compose.onNodeWithText("Cosméticas / otras", substring = false).assertDoesNotExist()
        compose.onNodeWithText("El listado nacional muestra", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Las formas alternativas se consultan", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Megaevoluciones").assertExists()
        compose.onNodeWithText("Gigamax", substring = false).assertExists()
    }

    @Test fun `obsolete presentation category is not restored as a hidden active filter`() {
        compose.runOnIdle { controller.filter.value = ExplorerFilter(category = VarietyCategory.BATTLE) }
        compose.waitUntil(20_000) { controller.result.value.ready && controller.result.value.entries.size in 1..1000 }
        restoration.emulateSavedInstanceStateRestore()
        compose.waitUntil(20_000) { controller.result.value.ready && controller.result.value.entries.size == 1025 }
        assertEquals(VarietyCategory.ALL, controller.filter.value.category)
        compose.onNodeWithTag("pokemon_1").assertIsDisplayed()
    }

    @Test fun `all query category type egg origin generation and favorite filters survive detail`() {
        val target = controller.entries.value.single { it.slug == "samurott-hisui" }
        val expected = ExplorerFilter(query = "samurott", category = VarietyCategory.REGIONAL, generationId = 5,
            regionalOrigin = "hisui", type = "water", favoritesOnly = true, eggGroup = "ground")
        compose.runOnIdle { favorites.value = setOf(target.pokemonId); controller.filter.value = expected }
        compose.waitUntil(20_000) { controller.result.value.entries.map { it.pokemonId } == listOf(target.pokemonId) }
        compose.onNodeWithTag("pokemon_${target.pokemonId}").performClick()
        compose.onNodeWithText("Volver a Pokédex").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithTag("pokedex-list").fetchSemanticsNodes().size == 1 && controller.result.value.ready }
        compose.onNodeWithTag("pokemon_${target.pokemonId}").assertIsDisplayed()
        assertEquals(expected, controller.filter.value)
        assertEquals(listOf(target.pokemonId), controller.result.value.entries.map { it.pokemonId })
    }

    @Test fun `list to grid and back retains a deep Pokemon anchor not the first result`() {
        val target = controller.result.value.entries[601]
        val tag = "pokemon_${target.pokemonId}"
        compose.onNodeWithTag("pokedex-list").performScrollToIndex(601)
        compose.onNodeWithTag(tag).assertIsDisplayed()
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.onNodeWithTag(tag).assertIsDisplayed()
        compose.onNodeWithTag("pokemon_1").assertDoesNotExist()
        compose.onNodeWithText("Lista", substring = false).performClick()
        compose.onNodeWithTag(tag).assertIsDisplayed()
        val top = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot().top.value
        val viewport = compose.onNodeWithTag("pokedex-list").getUnclippedBoundsInRoot().top.value
        assertEquals(viewport + 8, top, 2f)
    }

    @Test fun `scrolling the grid selects its current anchor when returning to list and preserves it through detail`() {
        compose.onNodeWithTag("pokedex-list").performScrollToIndex(301)
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.onNodeWithTag("pokedex-grid").performScrollToIndex(801)
        val target = controller.result.value.entries[801]
        val tag = "pokemon_${target.pokemonId}"
        compose.onNodeWithText("Lista", substring = false).performClick()
        compose.onNodeWithTag(tag).assertIsDisplayed().performClick()
        compose.onNodeWithText("Volver a Pokédex").performClick()
        compose.waitUntil(20_000) { controller.result.value.ready &&
            compose.onAllNodesWithTag("pokedex-list").fetchSemanticsNodes().size == 1 }
        compose.onNodeWithTag("pokedex-list").assertExists()
        compose.onNodeWithTag(tag).assertIsDisplayed()
    }

    @Test fun `filtered grid anchor survives restoration and return to list without changing filters`() {
        kalosFurfrou()
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.waitUntil(20_000) { controller.result.value.ready && controller.filter.value.generationId == 6 }
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
        compose.onNodeWithText("Lista", substring = false).performClick()
        compose.onNodeWithTag("pokemon_676").assertIsDisplayed()
        assertEquals(6, controller.filter.value.generationId)
    }

    @Test fun `Hisui Wyrdeer is labeled as species and keeps filter and identity through grid detail return`() {
        compose.runOnIdle { controller.filter.value = ExplorerFilter(regionalOrigin = "hisui", query = "wyrdeer") }
        compose.waitUntil(20_000) { controller.result.value.ready && controller.result.value.entries.map { it.pokemonId } == listOf(899) }
        compose.onNodeWithText("Especie de Hisui").assertIsDisplayed()
        compose.onNodeWithText("Hisui · formas y especies").assertExists()
        compose.onNodeWithText("Cuadrícula").performClick()
        compose.onNodeWithText("Especie de Hisui").assertIsDisplayed()
        compose.onNodeWithTag("pokemon_899").performClick()
        compose.onNodeWithText("Ficha 899").assertExists()
        compose.onNodeWithText("Volver a Pokédex").performClick()
        compose.waitUntil(20_000) { controller.result.value.ready && compose.onAllNodesWithTag("pokedex-grid").fetchSemanticsNodes().size == 1 }
        compose.onNodeWithTag("pokemon_899").assertIsDisplayed()
        assertEquals("hisui", controller.filter.value.regionalOrigin)
    }
}
