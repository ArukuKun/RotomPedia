package com.duoc.rotompedia

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.data.model.NamedApiResource
import com.duoc.rotompedia.data.remote.PokeApiService
import com.duoc.rotompedia.ui.screens.CompetitiveItemsScreen
import kotlinx.coroutines.flow.first
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
class ItemPocketsUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var repo: ItemRepository
    private lateinit var api: PokeApiService
    private lateinit var rows: List<IndexedItem>
    private lateinit var pockets: ItemPocketIndex
    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        api = Mockito.mock(PokeApiService::class.java)
        repo = DefaultItemRepository(db, api, com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()) {
            context.assets.open("item-index.json").bufferedReader().use { it.readText() }
        }
        runBlocking { pockets = repo.pocketIndex(); rows = repo.observeItems().first() }
    }
    @After fun close() { db.close() }
    private fun choosePocket(id: Int) {
        compose.onNodeWithTag("item_pockets").performScrollToNode(hasTestTag("pocket_$id"))
        compose.onNodeWithTag("pocket_$id").performClick()
    }
    private fun waitForCount(count: Int) {
        compose.waitUntil(20_000) { compose.onAllNodesWithText("$count / 2223 objetos").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun `production pockets use source grouping not category wording and list all objects locally`() {
        compose.setContent { MaterialTheme { CompetitiveItemsScreen({}, itemRepository = repo) } }
        waitForCount(2223)
        choosePocket(3)
        waitForCount(38)
        compose.onNodeWithTag("pocket_3").assertIsSelected()
        choosePocket(2)
        waitForCount(106)
        choosePocket(5)
        waitForCount(74)
        listOf(4 to 338, 8 to 391, 1 to 1204, 7 to 36, 6 to 36).forEach { (pocket, count) ->
            choosePocket(pocket)
            waitForCount(count)
            compose.onNodeWithTag("pocket_$pocket").assertIsSelected()
        }
        compose.onNodeWithText("Bolsillos de referencia · varían según el juego").assertExists()
        compose.onNodeWithTag("item_pockets").performScrollToNode(hasTestTag("pocket_all"))
        compose.onNodeWithTag("pocket_all").performClick()
        waitForCount(2223)
        Mockito.verifyNoInteractions(api)
    }

    @Test fun `pocket query category attribute and editorial selection survive state restoration`() {
        val restore = StateRestorationTester(compose)
        restore.setContent { MaterialTheme { CompetitiveItemsScreen({}, itemRepository = repo) } }
        waitForCount(2223)
        choosePocket(5)
        compose.onNodeWithText("Nombre, categoría o ID del objeto").performTextInput("acardo")
        waitForCount(1)
        compose.onNodeWithContentDescription("Filtrar objetos").performClick()
        compose.onNodeWithText("Categoría oficial: Sin seleccionar").performClick()
        compose.onNodeWithText("Protección por tipo").performScrollTo().performClick()
        compose.onNodeWithText("Atributo documentado: Sin seleccionar").performClick()
        compose.onNodeWithText("Se activa al llevarlo").performScrollTo().performClick()
        compose.onNode(isToggleable()).performClick()
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        waitForCount(1)
        restore.emulateSavedInstanceStateRestore()
        waitForCount(1)
        compose.onNodeWithTag("item_168").assertIsDisplayed()
        compose.onNodeWithContentDescription("Filtrar objetos").performClick()
        compose.onNodeWithText("Categoría oficial: Protección por tipo").assertExists()
        compose.onNodeWithText("Atributo documentado: Se activa al llevarlo").assertExists()
        compose.onNode(isToggleable()).assertIsOn()
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.onNodeWithText("Limpiar", substring = false).performClick()
        waitForCount(2223)
        Mockito.verifyNoInteractions(api)
    }

    @Test fun `closing item detail preserves pocket and exact visible list position`() {
        val berries = ItemPresentation.filter(rows, ItemFilter(pocketId = 5), pockets)
        val item = berries[12]
        runBlocking { Mockito.`when`(api.getItem(item.itemId.toString())).thenReturn(ItemDetail(item.itemId, item.slug, NamedApiResource(item.category, ""))) }
        compose.setContent { MaterialTheme { CompetitiveItemsScreen({}, itemRepository = repo) } }
        waitForCount(2223)
        choosePocket(5)
        waitForCount(74)
        compose.onNodeWithTag("item_list").performScrollToIndex(12)
        val top = compose.onNodeWithTag("item_${item.itemId}").fetchSemanticsNode().boundsInRoot.top
        compose.onNodeWithTag("item_${item.itemId}").performClick()
        compose.onNodeWithText("Cerrar", substring = false).performClick()
        compose.onNodeWithTag("pocket_5").assertIsSelected()
        compose.onNodeWithTag("item_${item.itemId}").assertIsDisplayed()
        assertEquals(top, compose.onNodeWithTag("item_${item.itemId}").fetchSemanticsNode().boundsInRoot.top, 0.5f)
    }
}
