package com.duoc.rotompedia

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.PokemonMove
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.data.items.IndexedItem
import com.duoc.rotompedia.data.items.ItemLabels
import com.duoc.rotompedia.ui.screens.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ReferenceUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var reference: ReferenceIndex
    @Before fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        reference = ReferenceStore(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()) {
            context.assets.open("reference-index.json").bufferedReader().use { it.readText() }
        }.index()
    }

    @Test fun `picker requires exact edition preserves both methods and selects technical move identity`() {
        var chosen: PokemonMove? = null
        val repo = object : MoveRepository {
            override suspend fun index() = reference
            override suspend fun learnset(pokemonId: Int, refresh: Boolean) = LearnsetResult(Learnset(pokemonId, listOf(
                LearnRow(53, "flamethrower", "sun-moon", "level-up", 10),
                LearnRow(53, "flamethrower", "sun-moon", "machine", 0))), false, 1000)
        }
        compose.setContent { MaterialTheme { MoveLearnsetPane(10104, Modifier.fillMaxSize(), repo, onSelect = { chosen = it }) } }
        compose.onNodeWithText("Selecciona una edición.").assertExists()
        compose.onNodeWithText("Lanzallamas").assertDoesNotExist()
        compose.onNodeWithContentDescription("Edición de aprendizaje: Sin seleccionar").performClick()
        compose.onNodeWithText("Sol / Luna").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Lanzallamas"))
        compose.onNodeWithText("Lanzallamas").assertIsDisplayed()
        compose.onNodeWithText("Nivel: nivel 10 · MT / MO").assertExists()
        compose.onNodeWithText("Lanzallamas").performClick()
        compose.runOnIdle { assertEquals(53, chosen?.moveId); assertEquals("Flamethrower", chosen?.nameEn) }
    }

    @Test fun `general catalogue is explicit and pagination does not refetch remote learnsets`() {
        var calls = 0
        val repo = object : MoveRepository {
            override suspend fun index() = reference
            override suspend fun learnset(pokemonId: Int, refresh: Boolean): LearnsetResult { calls++; throw java.io.IOException("offline") }
        }
        compose.setContent { MaterialTheme { MoveLearnsetPane(10104, Modifier.fillMaxSize(), repo) } }
        compose.onNodeWithText("No se pudieron cargar los movimientos. Comprueba tu conexión e inténtalo de nuevo.").assertExists()
        compose.onNode(isToggleable()).performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Cargar 40 más"))
        compose.onNodeWithText("Cargar 40 más").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        compose.onNodeWithText("937 movimientos · mostrando 80").assertExists()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun `ability to Pokemon link retains Alola variety and hidden slot`() {
        var chosen: String? = null
        compose.setContent { MaterialTheme {
            AbilityReferenceSheet(reference.abilities.single { it.id == 117 }, reference,
                mapOf(10104 to "#038 · Ninetales de Alola"), {}, { chosen = it })
        } }
        compose.onNodeWithText("#038 · Ninetales de Alola · Oculta · slot 3").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("10104", chosen) }
    }

    @Test fun `unknown historical move text is visible and never presented as empty`() {
        compose.setContent { MaterialTheme { MoveSlotCard(1, "Movimiento legado desconocido", {}, {}) } }
        compose.onNodeWithText("Movimiento 1: Movimiento legado desconocido").assertExists()
        compose.onNodeWithText("Movimiento 1: (Vacío)").assertDoesNotExist()
        compose.onNodeWithText("Toca para revisar este movimiento.").assertExists()
    }

    @Test fun `ability description selects Spanish for the exact requested edition`() {
        val ability = reference.abilities.single { it.id == 66 }
        val spanish = ability.texts.first { it.language == "es" }
        compose.setContent { MaterialTheme { AbilityReferenceSheet(ability, reference, emptyMap(), {}, {}) } }
        compose.onNodeWithContentDescription("Edición del texto: Sin seleccionar").performScrollTo().performClick()
        compose.onNodeWithText(ItemLabels.version(spanish.versionGroup)).performScrollTo().performClick()
        compose.onNodeWithText(spanish.text).performScrollTo().assertIsDisplayed()
    }

    @Test fun `machine link respects selected edition and sends item ID not move ID`() {
        var item: Int? = null
        val move = reference.moves.single { it.id == 5 }
        assertTrue(reference.machines.any { it.moveId == 5 && it.itemId == 305 && it.versionGroup == "red-blue" })
        compose.setContent { MaterialTheme {
            MoveReferenceSheet(move, reference, listOf(IndexedItem(305, "tm01", "MT01", "es", "all-machines", "")), {}, { item = it })
        } }
        compose.onNodeWithContentDescription("Edición: Sin seleccionar").performScrollTo().performClick()
        compose.onNodeWithText("Rojo / Azul").performClick()
        compose.onNodeWithText("MT01").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(305, item) }
    }
}
