package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.duoc.rotompedia.data.SpeciesCategoryState
import com.duoc.rotompedia.data.model.*
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.ui.screens.PokemonAbilitySummary
import com.duoc.rotompedia.ui.screens.PokemonBiologyCard
import com.duoc.rotompedia.ui.screens.AbilityReferenceSheet
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PokemonBiologyUiTest {
    @get:Rule val compose = createComposeRule()
    private val normal = ReferenceAbility(66, "blaze", "Mar Llamas", "Blaze", true, null, null, emptyList())
    private val hidden = ReferenceAbility(94, "solar-power", "Poder Solar", "Solar Power", true, null, null, emptyList())
    private val mega = ReferenceAbility(181, "tough-claws", "Garra Dura", "Tough Claws", true, null, null, emptyList())
    private val index = ReferenceIndex("test", emptyList(), emptyList(), listOf(normal, hidden, mega), emptyList(), emptyList())
    private fun pokemon(id: Int = 6, weight: Int? = 905) = PokemonData(id, "charizard", SpritesData(null), emptyList(), emptyList(), height = 17, weight = weight)
    private fun repo(load: suspend (Int) -> List<AbilityLink>) = object : AbilityRepository {
        override suspend fun index() = this@PokemonBiologyUiTest.index
        override suspend fun forPokemon(pokemonId: Int) = load(pokemonId)
    }

    @Test fun `compact card shows dimensions Spanish category and clickable normal and hidden abilities`() {
        var chosen: ReferenceAbility? = null
        compose.setContent {
            var sheet by remember { mutableStateOf<ReferenceAbility?>(null) }
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    PokemonBiologyCard(pokemon(), 6, "Charizard", SpeciesCategoryState(6, "Pokémon Llama", false), {}) {
                        PokemonAbilitySummary(6, remember { repo { listOf(AbilityLink(6, 66, false, 1), AbilityLink(6, 94, true, 3)) } }) { ability, _ -> chosen = ability; sheet = ability }
                    }
                }
                sheet?.let { AbilityReferenceSheet(it, index, emptyMap(), { sheet = null }, {}) }
            }
        }
        compose.onNodeWithText("Especie: Charizard").assertExists()
        compose.onNodeWithText("Categoría de especie: Pokémon Llama").assertExists()
        compose.onNodeWithText("Altura: 1,7 m").assertExists()
        compose.onNodeWithText("Peso: 90,5 kg").assertExists()
        compose.onNodeWithText("Consultar habilidades").assertDoesNotExist()
        compose.onNodeWithText("Poder Solar (oculta)").performScrollTo().performClick()
        assertEquals(hidden, chosen)
        compose.onNodeWithText("Poder Solar", substring = false).assertExists()
        compose.onNodeWithText("Cerrar ficha").performClick()
        compose.onNodeWithText("Mar Llamas").performScrollTo().performClick()
        assertEquals(normal, chosen)
        compose.onAllNodesWithText("Mar Llamas", substring = false).onLast().assertExists()
    }

    @Test fun `narrow large text card wraps measurements and rejects category from another species`() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.6f)) {
                MaterialTheme {
                    Column(Modifier.width(280.dp).verticalScroll(rememberScrollState())) {
                        PokemonBiologyCard(pokemon(weight = null), 6, "Charizard", SpeciesCategoryState(25, "Pokémon Ratón", false), {}) {}
                    }
                }
            }
        }
        compose.onNodeWithText("Categoría de especie: —").assertExists()
        compose.onNodeWithText("Pokémon Ratón", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Peso: —").performScrollTo().assertIsDisplayed()
        val card = compose.onNodeWithTag("pokemon_biology").fetchSemanticsNode().boundsInRoot
        listOf("Altura: 1,7 m", "Peso: —").forEach { text ->
            val bounds = compose.onNodeWithText(text).fetchSemanticsNode().boundsInRoot
            assertTrue(bounds.left >= card.left && bounds.right <= card.right)
        }
    }

    @Test fun `changing variety cancels stale ability load and shows only exact Mega ability`() {
        val pending = CompletableDeferred<List<AbilityLink>>()
        val id = mutableIntStateOf(6)
        val repository = repo { if (it == 6) pending.await() else listOf(AbilityLink(it, 181, false, 1)) }
        compose.setContent { MaterialTheme { Column { PokemonAbilitySummary(id.intValue, repository) { _, _ -> } } } }
        compose.runOnIdle { id.intValue = 10034 }
        compose.onNodeWithText("Garra Dura").assertExists()
        compose.runOnIdle { pending.complete(listOf(AbilityLink(6, 94, true, 3))) }
        compose.onNodeWithText("Poder Solar (oculta)").assertDoesNotExist()
        compose.onNodeWithText("Garra Dura").assertExists()
    }

    @Test fun `ability failure can retry and empty coverage is explicit`() {
        var calls = 0
        val repository = repo { if (calls++ == 0) error("offline") else emptyList() }
        compose.setContent { MaterialTheme { Column { PokemonAbilitySummary(6, repository) { _, _ -> } } } }
        compose.onNodeWithText("Reintentar habilidades").performClick()
        compose.onNodeWithText("No hay habilidades para mostrar.").assertExists()
        compose.onNodeWithText("Reintentar habilidades").assertDoesNotExist()
    }

    @Test fun `ability links for a different variety are rejected instead of displayed`() {
        val repository = repo { listOf(AbilityLink(10034, 181, false, 1)) }
        compose.setContent { MaterialTheme { Column { PokemonAbilitySummary(6, repository) { _, _ -> } } } }
        compose.onNodeWithText("No se pudieron consultar las habilidades.").assertExists()
        compose.onNodeWithText("Garra Dura").assertDoesNotExist()
    }

    @Test fun `unavailable category can retry while exact variety measurements remain visible`() {
        var retried = false
        compose.setContent {
            MaterialTheme {
                PokemonBiologyCard(pokemon(), 6, "Charizard", SpeciesCategoryState(loading = false, failed = true), { retried = true }) {}
            }
        }
        compose.onNodeWithText("Categoría de especie: —").assertExists()
        compose.onNodeWithText("Peso: 90,5 kg").assertExists()
        compose.onNodeWithText("Reintentar categoría").performClick()
        assertTrue(retried)
    }
}
