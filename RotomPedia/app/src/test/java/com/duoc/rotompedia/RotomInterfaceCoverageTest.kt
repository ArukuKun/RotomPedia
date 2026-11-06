package com.duoc.rotompedia

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.data.local.EquipoConMiembros
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import com.duoc.rotompedia.util.*
import com.duoc.rotompedia.viewmodel.*
import kotlinx.coroutines.flow.MutableStateFlow
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
class RotomInterfaceCoverageTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    @Before fun appearance() {
        RotomAppearanceManager.setMode(context, AppearanceMode.LIGHT)
        RotomThemeManager.setForm(context, RotomForm.NORMAL)
    }
    private fun assertDeviceFrame() {
        val face = compose.onNodeWithTag("rotom_face").fetchSemanticsNode().boundsInRoot
        val content = compose.onNodeWithTag("rotom_content").fetchSemanticsNode().boundsInRoot
        assertTrue(face.bottom <= content.top || face.right <= content.left)
    }
    private fun pokemon(): PokemonViewModel {
        val result = Mockito.mock(PokemonViewModel::class.java)
        Mockito.`when`(result.explorerEntries).thenReturn(MutableStateFlow(emptyList()))
        return result
    }

    @Test fun `new home has all real destinations rather than the legacy button menu`() {
        val choices = listOf("Pokédex", "Equipos", "Objetos", "Movimientos", "Habilidades", "Mapas",
            "Calculadora IV/EV", "Ratio Captura", "Tabla Tipos", "Líderes", "Modo Versus")
        val routes = mutableListOf<String>()
        compose.setContent { RotompediaTheme { RotomHomeScreen({ routes.add(it) }, {}) } }
        assertDeviceFrame()
        compose.onNodeWithText("¿Qué exploramos hoy?").assertExists()
        choices.forEach { title ->
            compose.onNodeWithTag("rotom_home_destinations").performScrollToNode(hasText(title))
            compose.onNodeWithText(title, substring = false).assertHasClickAction().performClick()
        }
        assertEquals(11, routes.distinct().size)
        assertTrue(routes.contains("catch_rate_calculator?pokemonId=1"))
        compose.onNodeWithContentDescription("Cambiar apariencia").assertHasClickAction()
    }

    @Test fun `login preserves username trimming password and remember choice with no account writes`() {
        val auth = Mockito.mock(AuthViewModel::class.java)
        Mockito.`when`(auth.authState).thenReturn(MutableStateFlow(AuthState.IDLE))
        compose.setContent { RotompediaTheme { LoginScreen(auth, {}) } }
        assertDeviceFrame()
        compose.onNodeWithText("Nombre de Usuario").performScrollTo().performTextInput(" Entrenador ")
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(" fixture ")
        compose.onNode(isToggleable()).performScrollTo().performClick()
        compose.onNode(hasText("Iniciar Sesión") and hasClickAction()).performScrollTo().performClick()
        Mockito.verify(auth).iniciarSesion("Entrenador", " fixture ", true)
        Mockito.verify(auth, Mockito.never()).registrarUsuario(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
    }

    @Test fun `register preserves values and reports existing email in the new frame`() {
        val auth = Mockito.mock(AuthViewModel::class.java)
        val state = MutableStateFlow(AuthState.IDLE)
        Mockito.`when`(auth.authState).thenReturn(state)
        compose.setContent { RotompediaTheme { RegisterScreen({}, {}, auth) } }
        assertDeviceFrame()
        compose.onNodeWithText("Nombre de Usuario").performScrollTo().performTextInput(" Entrenador ")
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(" fixture@example.test ")
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput("fixture")
        compose.onNodeWithText("Registrarse", substring = false).performScrollTo().performClick()
        Mockito.verify(auth).registrarUsuario("Entrenador", "fixture@example.test", "fixture")
        compose.runOnIdle { state.value = AuthState.EMAIL_EXISTS }
        compose.onNodeWithText("El correo electrónico ya está registrado.").performScrollTo().assertIsDisplayed()
    }

    @Test fun `stats capture and type chart use the device frame in both appearances`() {
        val model = pokemon()
        val selected = mutableIntStateOf(0)
        compose.setContent { RotompediaTheme {
            when (selected.intValue) {
                0 -> StatCalculatorScreen(model, {})
                1 -> CatchRateCalculatorScreen(model, {})
                else -> TypeChartScreen({})
            }
        } }
        listOf(AppearanceMode.LIGHT, AppearanceMode.DARK).forEach { appearance ->
            compose.runOnIdle { RotomAppearanceManager.setMode(context, appearance) }
            listOf("Calculadora de Stats (IV/EV)", "Calculadora de Captura", "Tabla de Tipos").forEachIndexed { i, title ->
                compose.runOnIdle { selected.intValue = i }
                assertDeviceFrame()
                compose.onNodeWithText(title, substring = false).assertExists()
            }
        }
    }

    @Test fun `team builder retains create dialog action inside the common frame`() {
        val teams = Mockito.mock(TeamBuilderViewModel::class.java)
        Mockito.`when`(teams.equipos).thenReturn(MutableStateFlow<List<EquipoConMiembros>>(emptyList()))
        Mockito.`when`(teams.selectedEquipoId).thenReturn(MutableStateFlow<Long?>(null))
        Mockito.`when`(teams.selectedEquipo).thenReturn(MutableStateFlow<EquipoConMiembros?>(null))
        Mockito.`when`(teams.teamAnalysis).thenReturn(MutableStateFlow(TypeEffectiveness.analyzeTeam(emptyList())))
        val pokemon = pokemon()
        compose.setContent { RotompediaTheme { TeamBuilderScreen(teams, pokemon, {}) } }
        assertDeviceFrame()
        compose.onNodeWithContentDescription("Nuevo Equipo").performClick()
        compose.onNodeWithText("Nombre del equipo").performTextInput("Equipo de prueba")
        compose.onNodeWithText("Crear", substring = false).performClick()
        Mockito.verify(teams).crearEquipo("Equipo de prueba")
    }

    @Test fun `versus and leaders retain their screens within the same device frame`() {
        val versus = Mockito.mock(PokemonVersusViewModel::class.java)
        Mockito.`when`(versus.pokemon1).thenReturn(MutableStateFlow<PokemonData?>(null))
        Mockito.`when`(versus.pokemon2).thenReturn(MutableStateFlow<PokemonData?>(null))
        Mockito.`when`(versus.isLoadingP1).thenReturn(MutableStateFlow(false))
        Mockito.`when`(versus.isLoadingP2).thenReturn(MutableStateFlow(false))
        Mockito.`when`(versus.matchupAnalysis).thenReturn(MutableStateFlow<VersusMatchupAnalysis?>(null))
        val pokemon = pokemon()
        val leaders = mutableStateOf(false)
        compose.setContent { RotompediaTheme {
            if (leaders.value) GymLeadersScreen(rememberNavController()) else PokemonVersusScreen(versus, pokemon, {})
        } }
        assertDeviceFrame()
        compose.onNodeWithText("Comparador Versus").assertExists()
        compose.runOnIdle { leaders.value = true }
        assertDeviceFrame()
        compose.onNodeWithText("Líderes:", substring = true).assertExists()
    }
}
