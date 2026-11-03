package com.duoc.rotompedia

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.index.PokemonIndexImporter
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.data.repository.DefaultPokemonRepository
import com.duoc.rotompedia.ui.screens.PokemonExplorerContent
import com.duoc.rotompedia.viewmodel.PokemonExplorerController
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PokemonExplorerUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var controller: PokemonExplorerController
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
            controller = remember { PokemonExplorerController(scope, repo, flowOf(emptySet())) }
            MaterialTheme {
                PokemonExplorerContent(controller, { chosen = it.pokemonId }, Modifier.fillMaxSize())
            }
        }
        compose.waitUntil(20_000) { controller.result.value.groups.size == 1025 }
    }

    @After fun close() { db.close() }

    @Test fun `national list starts compact and expands Venusaur`() {
        compose.onNodeWithText("#001 · Bulbasaur").assertIsDisplayed()
        compose.onNodeWithText("#003 · Mega Venusaur").assertDoesNotExist()
        compose.onAllNodesWithText("▸ 2 variantes").onFirst().performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("#003 · Mega Venusaur").performScrollTo().assertIsDisplayed()
    }

    @Test fun `number search expands Alola and selection keeps technical id`() {
        compose.onNodeWithText("Nombre, forma o número nacional").performTextInput("#038")
        compose.waitUntil(20_000) { controller.result.value.groups.size == 1 }
        compose.onNodeWithText("#038 · Ninetales de Alola").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(10104, chosen) }
    }
}
