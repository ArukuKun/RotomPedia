package com.duoc.rotompedia

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.duoc.rotompedia.data.local.EquipoConMiembros
import com.duoc.rotompedia.data.local.EquipoPokemon
import com.duoc.rotompedia.ui.screens.TeamBuilderScreen
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import com.duoc.rotompedia.util.TypeEffectiveness
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import com.duoc.rotompedia.viewmodel.TeamBuilderViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class Rc4TeamsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `team dropdown selects ID rename rejects blank and deletion requires named confirmation`() {
        val first = EquipoConMiembros(EquipoPokemon(id = 41, usuario = "qa", nombre = "Equipo idéntico"), emptyList())
        val second = EquipoConMiembros(EquipoPokemon(id = 42, usuario = "qa", nombre = "Equipo idéntico"), emptyList())
        val teams = mock(TeamBuilderViewModel::class.java)
        `when`(teams.currentUsername).thenReturn(MutableStateFlow("qa"))
        `when`(teams.equipos).thenReturn(MutableStateFlow(listOf(first, second)))
        `when`(teams.selectedEquipoId).thenReturn(MutableStateFlow<Long?>(41))
        `when`(teams.selectedEquipo).thenReturn(MutableStateFlow<EquipoConMiembros?>(first))
        `when`(teams.teamAnalysis).thenReturn(MutableStateFlow(TypeEffectiveness.analyzeTeam(emptyList())))
        val pokemon = mock(PokemonViewModel::class.java)
        compose.setContent { RotompediaTheme { TeamBuilderScreen(teams, pokemon, {}) } }
        compose.onNodeWithText("Equipo idéntico", substring = false).performClick()
        compose.onAllNodesWithText("Equipo idéntico", substring = false).onLast().performClick()
        verify(teams).seleccionarEquipo(42)
        compose.onNodeWithContentDescription("Renombrar equipo").performClick()
        compose.onNodeWithText("Nombre del equipo").performTextClearance()
        compose.onNodeWithText("Guardar", substring = false).assertIsNotEnabled()
        compose.onNodeWithText("Nombre del equipo").performTextInput("Nuevo nombre")
        compose.onNodeWithText("Guardar", substring = false).performClick()
        verify(teams).actualizarNombreEquipo(41, "Nuevo nombre")
        compose.onNodeWithContentDescription("Eliminar Equipo").performClick()
        compose.onNodeWithText("¿Eliminar «Equipo idéntico»? Esta acción no se puede deshacer.").assertExists()
        verify(teams, never()).eliminarEquipo(anyLong())
        compose.onNodeWithText("Cancelar", substring = false).performClick()
        verify(teams, never()).eliminarEquipo(anyLong())
        compose.onNodeWithContentDescription("Eliminar Equipo").performClick()
        compose.onNodeWithText("Eliminar", substring = false).performClick()
        verify(teams).eliminarEquipo(41)
    }
}
