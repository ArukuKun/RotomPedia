package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import com.duoc.rotompedia.viewmodel.PokemonMovesViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PokemonMovesUiTest {
    @get:Rule val compose = createComposeRule()
    private val move = ReferenceMove(53, "flamethrower", "Lanzallamas", "Flamethrower", "fire", "special", 90, 15, 100, 0, "Puede quemar.", null, emptyList())
    private val index = ReferenceIndex("test", listOf(ReferenceVersion(15, "x-y", 15), ReferenceVersion(7, "ruby-sapphire", 7)),
        listOf(move), emptyList(), emptyList(), emptyList())
    private val rows = listOf(LearnRow(53, "flamethrower", "x-y", "level-up", 10),
        LearnRow(53, "flamethrower", "x-y", "level-up", 20),
        LearnRow(53, "flamethrower", "x-y", "machine", 0),
        LearnRow(53, "flamethrower", "x-y", "tutor", 0),
        LearnRow(53, "flamethrower", "ruby-sapphire", "egg", 0),
        LearnRow(53, "flamethrower", "x-y", "form-change", 0),
        LearnRow(99000, "unlisted-move", "x-y", "zygarde-cube", 0))
    private class Repo(val index: ReferenceIndex, val rows: List<LearnRow>, val failures: Int = 0) : MoveRepository {
        var calls = 0
        var target = 0
        var refresh = false
        override suspend fun index() = index
        override suspend fun learnset(pokemonId: Int, refresh: Boolean): LearnsetResult {
            calls++; target = pokemonId; this.refresh = refresh
            if (calls <= failures) error("internal failure")
            return LearnsetResult(Learnset(pokemonId, rows), false, 1)
        }
    }
    private fun show(repo: Repo, saved: SavedStateHandle = SavedStateHandle(mapOf("edition" to "x-y"))): PokemonMovesViewModel {
        val model = PokemonMovesViewModel(10117, repo, saved)
        compose.setContent { RotompediaTheme { PokemonMovesScreen(model, "Variedad elegida", {}) } }
        compose.waitUntil(10_000) { model.data.value != null }
        return model
    }

    @Test fun `detail section is one button and forwards exact selected variety`() {
        var target = 0
        compose.setContent { MaterialTheme { PokemonMovesSection(10117) { target = it } } }
        compose.onAllNodesWithText("Movimientos", substring = false).assertCountEquals(1)
        compose.onNodeWithText("Consultar movimientos").assertDoesNotExist()
        compose.onNodeWithText("Movimientos").performClick()
        assertEquals(10117, target)
    }

    @Test fun `four tabs and swipe reuse one load preserve levels and open same edition move sheet`() {
        val repo = Repo(index, rows)
        val model = show(repo)
        compose.onNodeWithText("Nivel: 10, 20").assertExists()
        compose.onNodeWithText("Fuego", substring = false).assertIsDisplayed()
        compose.onNodeWithText("Especial", substring = false).assertIsDisplayed()
        compose.onNodeWithText("Lanzallamas", substring = false).performClick()
        compose.onNodeWithContentDescription("Edición: X / Y").assertExists()
        compose.onNodeWithText("Puede quemar.").assertExists()
        compose.onNodeWithText("Cerrar ficha").performClick()
        compose.onNodeWithTag("learn_tab_machine").performClick()
        compose.onNodeWithTag("learn_list_machine").assertExists()
        compose.onNodeWithText("Lanzallamas", substring = false).assertIsDisplayed()
        compose.onNodeWithTag("learn_pager").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("learn_list_tutor").assertExists()
        assertEquals("x-y", model.edition.value)
        assertEquals(1, repo.calls)
        assertEquals(10117, repo.target)
    }

    @Test fun `extra methods and unlisted reference stay accessible without changing tutor data`() {
        val repo = Repo(index, rows)
        show(repo)
        compose.onNodeWithContentDescription("Otros métodos").performClick()
        compose.onNodeWithText("Cambio de forma").assertExists()
        compose.onNodeWithText("Arca de Zygarde").assertExists()
        compose.onNodeWithTag("learn_other_list").performScrollToNode(hasText("Unlisted Move"))
        compose.onNodeWithText("Unlisted Move").assertExists()
        compose.onNodeWithText("Ficha no disponible").assertExists()
        assertEquals(1, repo.calls)
    }

    @Test fun `edition filtering preserves all methods without manufacturing learning relations`() {
        val xy = learningMoves(index, rows, "x-y", "level-up", "", null, null)
        assertEquals(listOf(10, 20), xy.single().relations.map { it.level })
        assertTrue(learningMoves(index, rows, "x-y", "egg", "", null, null).isEmpty())
        assertEquals(1, learningMoves(index, rows, "ruby-sapphire", "egg", "", null, null).size)
        assertTrue(learningMoves(index, rows, null, "level-up", "", null, null).isEmpty())
        assertTrue(learningMoves(index, rows, "x-y", "machine", "", "water", null).isEmpty())
        assertEquals(1, learningMoves(index, rows, "x-y", "machine", "llamas", "fire", "special").size)
    }

    @Test fun `saved edition page and filters restore without overwriting exact variety`() {
        val saved = SavedStateHandle(mapOf("edition" to "x-y", "page" to 2, "query" to "llamas", "type" to "fire", "category" to "special"))
        val repo = Repo(index, rows)
        val model = show(repo, saved)
        compose.onNodeWithTag("learn_list_machine").assertExists()
        assertEquals(2, model.page.value)
        assertEquals("llamas", model.query.value)
        assertEquals("fire", model.type.value)
        assertEquals("special", model.category.value)
        assertEquals(10117, repo.target)
        assertEquals(1, repo.calls)
    }

    @Test fun `retry recovers an initial failure without exposing internals or changing variety`() {
        val repo = Repo(index, rows, failures = 1)
        val model = PokemonMovesViewModel(10117, repo, SavedStateHandle(mapOf("edition" to "x-y")))
        compose.setContent { RotompediaTheme { PokemonMovesScreen(model, "Variedad elegida", {}) } }
        compose.waitUntil(10_000) { model.failed.value }
        compose.onNodeWithText("internal failure").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reintentar").performClick()
        compose.waitUntil(10_000) { model.data.value != null }
        assertEquals(2, repo.calls)
        assertEquals(10117, repo.target)
        assertTrue(repo.refresh)
        assertEquals("x-y", model.edition.value)
    }

    @Test fun `all learning results remain reachable beyond forty entries`() {
        val moves = (1..65).map { id -> move.copy(id = id, slug = "fixture-$id", nameEs = "Movimiento $id", nameEn = "Fixture $id") }
        val many = (1..65).map { id -> LearnRow(id, "fixture-$id", "x-y", "level-up", id) }
        val repo = Repo(index.copy(moves = moves), many)
        show(repo)
        compose.onNodeWithTag("learn_list_level-up").performScrollToIndex(64)
        compose.onNodeWithText("Movimiento 65").assertIsDisplayed()
        assertEquals(1, repo.calls)
    }
}
