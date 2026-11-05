package com.duoc.rotompedia

import android.content.Context
import android.content.ClipboardManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.data.model.NamedApiResource
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.viewmodel.EvolutionState
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ClientFacingUiTest {
    @get:Rule val compose = createComposeRule()
    private val item = IndexedItem(999, "test-item", "Objeto de prueba", "es", "healing", "")
    private val detail = ItemDetail(999, "test-item", NamedApiResource("healing", ""),
        effects = listOf(ItemEffect(shortEffect = "Restaura los PS.", language = NamedApiResource("es", ""))))
    private fun noDiagnostics() {
        listOf("PokeAPI", "caché", "copia guardada", "Fuente:", "WikiDex", "ID objeto", "ID habilidad", "ID movimiento", "ID técnico", "revisión")
            .forEach { compose.onAllNodes(hasText(it, substring = true, ignoreCase = true)).assertCountEquals(0) }
    }
    private class Repo(val result: ItemDetailResult, val failFirst: Boolean = false) : ItemRepository {
        var calls = 0
        var refreshed = false
        override fun observeItems() = flowOf(emptyList<IndexedItem>())
        override suspend fun ensureIndex() {}
        override suspend fun pocketIndex() = ItemPocketIndex()
        override suspend fun resolve(slug: String): IndexedItem? = null
        override suspend fun detail(id: Int, refresh: Boolean): ItemDetailResult {
            calls++
            refreshed = refresh
            if (failFirst && calls == 1) error("PokeAPI cache internal failure")
            return result
        }
    }
    private fun showItem(repo: Repo) {
        compose.setContent { MaterialTheme { ItemDetailSheet(item, repo, {}, {}) } }
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Restaura los PS.").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun `online item shows useful content and hides optional empty sections and diagnostics`() {
        showItem(Repo(ItemDetailResult(detail, false, 1000)))
        compose.onNodeWithText("Restaura los PS.").assertExists()
        listOf("Descripción", "Precio", "Características").forEach { compose.onNodeWithText(it, substring = false).assertDoesNotExist() }
        noDiagnostics()
        compose.onNodeWithText("Copiar ficha").performScrollTo().performClick()
        val clipboard = ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copied = clipboard.primaryClip!!.getItemAt(0).text.toString()
        assertTrue(copied.contains(item.name))
        assertFalse(copied.contains("PokeAPI"))
        assertFalse(copied.contains(item.slug))
    }

    @Test fun `offline cached item renders same content without cache banners and preserves refresh`() {
        val repo = Repo(ItemDetailResult(detail, true, 1000))
        showItem(repo)
        noDiagnostics()
        compose.onNodeWithText("Actualizar").performClick()
        compose.waitUntil(15_000) { repo.calls == 2 }
        assertTrue(repo.refreshed)
        compose.onNodeWithText("Restaura los PS.").assertExists()
        noDiagnostics()
    }

    @Test fun `raw backend failure is replaced by actionable error and retry still recovers`() {
        val repo = Repo(ItemDetailResult(detail, false, 1000), failFirst = true)
        compose.setContent { MaterialTheme { ItemDetailSheet(item, repo, {}, {}) } }
        compose.onNodeWithText("No se pudo cargar la ficha. Comprueba tu conexión e inténtalo de nuevo.").assertExists()
        noDiagnostics()
        compose.onNodeWithText("Reintentar").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Restaura los PS.").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(2, repo.calls)
        noDiagnostics()
    }

    @Test fun `ability and move sheets keep game content language and counts without developer footers`() {
        val ability = ReferenceAbility(54, "truant", "Ausente", "Truant", true, "Descansa entre ataques.", null, emptyList())
        val move = ReferenceMove(53, "flamethrower", "Lanzallamas", "Flamethrower", "fire", "special", 90, 15, 100, 0, null, "Burns the target.", emptyList())
        val index = ReferenceIndex("secret-revision", emptyList(), listOf(move), listOf(ability), listOf(AbilityLink(25, 54, false, 1)), emptyList())
        val showMove = mutableStateOf(false)
        compose.setContent { MaterialTheme {
            if (showMove.value) MoveReferenceSheet(move, index, onDismiss = {})
            else AbilityReferenceSheet(ability, index, mapOf(25 to "Pikachu"), {}, {})
        } }
        compose.onNodeWithText("Pereza").assertExists()
        compose.onNodeWithText("Antes de la novena generación: Ausente").assertExists()
        compose.onNodeWithText("Descansa entre ataques.").assertExists()
        compose.onNodeWithText("Variedades asociadas · 1").assertExists()
        noDiagnostics()
        compose.runOnIdle { showMove.value = true }
        compose.onNodeWithText("Inglés: Burns the target.").assertExists()
        compose.onNodeWithText("Pot: 90 · Prec: 100% · PP: 15").assertExists()
        noDiagnostics()
    }

    @Test fun `missing variety description has no notice or reserved space and valid description remains`() {
        val isDefault = mutableStateOf(false)
        val state = mutableStateOf<EvolutionState>(EvolutionState.Success(emptyList(), "Una descripción útil."))
        compose.setContent { MaterialTheme { Column {
            Text("Antes")
            PokemonSpeciesDescription(isDefault.value, state.value)
            Text("Después")
        } } }
        compose.onNodeWithText("Descripción", substring = false).assertDoesNotExist()
        assertEquals(compose.onNodeWithText("Antes").fetchSemanticsNode().boundsInRoot.bottom,
            compose.onNodeWithText("Después").fetchSemanticsNode().boundsInRoot.top, 0.1f)
        noDiagnostics()
        compose.runOnIdle { isDefault.value = true }
        compose.onNodeWithText("Una descripción útil.").assertExists()
        compose.runOnIdle { state.value = EvolutionState.Error("PokeAPI internal failure") }
        compose.onNodeWithText("Descripción", substring = false).assertDoesNotExist()
        noDiagnostics()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test fun `toolbar filter is a labelled touch target with active badge at narrow large text size`() {
        var opened = false
        val active = mutableIntStateOf(0)
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.5f)) {
                MaterialTheme { Box(Modifier.width(280.dp).testTag("toolbar_host")) {
                    TopAppBar(title = { Text("Objetos", maxLines = 1) }, actions = {
                        ItemFilterAction(active.intValue) { opened = true; active.intValue = 2 }
                    })
                } }
            }
        }
        compose.onNodeWithContentDescription("Filtrar objetos").assertHasClickAction()
            .assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(100.dp).performClick()
        assertTrue(opened)
        compose.onNodeWithText("Filtros").assertIsDisplayed()
        compose.onNodeWithText("2", substring = false).assertIsDisplayed()
        compose.onNodeWithContentDescription("Filtrar objetos").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "2 filtros adicionales activos"))
        val action = compose.onNodeWithContentDescription("Filtrar objetos").fetchSemanticsNode().boundsInRoot
        val host = compose.onNodeWithTag("toolbar_host").fetchSemanticsNode().boundsInRoot
        assertTrue(action.left >= host.left && action.right <= host.right)
    }
}
