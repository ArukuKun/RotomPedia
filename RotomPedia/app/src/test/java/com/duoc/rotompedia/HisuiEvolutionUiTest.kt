package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.ui.screens.HisuiEvolutionSection
import com.duoc.rotompedia.ui.screens.PokemonEvolutionSection
import com.duoc.rotompedia.viewmodel.EvolutionStage
import com.duoc.rotompedia.viewmodel.EvolutionState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class HisuiEvolutionUiTest {
    @get:Rule val compose = createComposeRule()
    private fun entry(id: Int, species: Int, slug: String, name: String) = ExplorerPokemon(
        species, species, id, null, name, slug, name, id == species, 8, VarietyCategory.REGIONAL,
        "hisui", emptyList(), 1)
    private val qwilfish = entry(10234, 211, "qwilfish-hisui", "Qwilfish de Hisui")
    private val overqwil = entry(904, 904, "overqwil", "Overqwil")

    @Test fun `Overqwil changes edition without mixing methods and restores edition after recreation`() {
        val restore = StateRestorationTester(compose)
        var chosen: String? = null
        restore.setContent { MaterialTheme { Column(Modifier.verticalScroll(rememberScrollState())) {
            HisuiEvolutionSection("overqwil", listOf(qwilfish, overqwil), { chosen = it }, {})
        } } }
        compose.onNodeWithText("Usar Mil Púas Tóxicas 20 veces en estilo fuerte; después elegir Evolucionar.").assertExists()
        compose.onNodeWithText("Desde Qwilfish de Hisui").performScrollTo().performClick()
        assertEquals("10234", chosen)
        compose.onNodeWithText("Escarlata / Púrpura").performScrollTo().performClick()
        compose.onNodeWithText("Subir de nivel con Mil Púas Tóxicas aprendido.").assertExists()
        compose.onNodeWithText("20 veces", substring = true).assertDoesNotExist()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Subir de nivel con Mil Púas Tóxicas aprendido.").assertExists()
        compose.onNodeWithText("#904 · Overqwil").performScrollTo().performClick()
        assertEquals("904", chosen)
    }

    @Test fun `Kleavor item link is specific to Arceus and unavailable local evolution stays explicit`() {
        var item: String? = null
        val rows = listOf(entry(123, 123, "scyther", "Scyther"), entry(900, 900, "kleavor", "Kleavor"))
        compose.setContent { MaterialTheme { Column(Modifier.verticalScroll(rememberScrollState())) {
            HisuiEvolutionSection("kleavor", rows, {}, { item = it })
        } } }
        compose.onNodeWithText("Usar Mineral Negro.").assertExists()
        compose.onNodeWithText("Consultar objeto").performScrollTo().performClick()
        assertEquals("black-augurite", item)
        compose.onNodeWithText("Escarlata / Púrpura").performScrollTo().performClick()
        compose.onNodeWithText("No evoluciona a esta forma aquí.", substring = true).assertExists()
        compose.onNodeWithText("Consultar objeto").assertDoesNotExist()
        compose.onNodeWithText("Usar Mineral Negro.").assertDoesNotExist()
    }

    @Test fun `ordinary Qwilfish and Bloodmoon Ursaluna do not receive Hisui evolution rules`() {
        compose.setContent { MaterialTheme { Column {
            HisuiEvolutionSection("qwilfish", listOf(qwilfish, overqwil), {}, {})
            HisuiEvolutionSection("ursaluna-bloodmoon", emptyList(), {}, {})
        } } }
        compose.onNodeWithText("Evoluciones de Hisui").assertDoesNotExist()
        compose.onNodeWithText("Mil Púas", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Ursaluna Luna Carmesí no se obtiene evolucionando a Ursaring.").assertExists()
    }

    @Test fun `missing shared index shows loading rather than navigating to an invented species id`() {
        compose.setContent { MaterialTheme { Column {
            HisuiEvolutionSection("overqwil", listOf(overqwil), {}, {})
        } } }
        compose.onNodeWithText("Cargando variedades…").assertExists()
        compose.onNodeWithText("Desde Qwilfish", substring = true).assertDoesNotExist()
    }

    @Test fun `production evolution content shows verified Hisui rules even when remote species chain fails`() {
        compose.setContent { MaterialTheme { Column(Modifier.verticalScroll(rememberScrollState())) {
            PokemonEvolutionSection("overqwil", 904, listOf(qwilfish, overqwil), EvolutionState.Error("remote failure"), {}, {})
        } } }
        compose.onNodeWithText("Usar Mil Púas Tóxicas 20 veces en estilo fuerte; después elegir Evolucionar.").assertExists()
        compose.onNodeWithText("remote failure").assertDoesNotExist()
        compose.onNodeWithText("Ver línea de la especie").performScrollTo().performClick()
        compose.onNodeWithText("remote failure").assertDoesNotExist()
        compose.onNodeWithText("No se pudo cargar la evolución.").assertExists()
    }

    @Test fun `production ordinary Qwilfish does not show its regional counterparts evolution`() {
        val chain = listOf(EvolutionStage("211", "Qwilfish", "Forma Base", true),
            EvolutionStage("904", "Overqwil", "Condición especial", evolvesFromId = "211"))
        compose.setContent { MaterialTheme { Column {
            PokemonEvolutionSection("qwilfish", 211, emptyList(), EvolutionState.Success(chain, ""), {}, {})
        } } }
        compose.onNodeWithText("Este Pokémon no evoluciona.").assertExists()
        compose.onNodeWithText("Overqwil", substring = true).assertDoesNotExist()
    }
}
