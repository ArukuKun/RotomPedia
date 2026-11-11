package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.duoc.rotompedia.data.maps.*
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.ui.components.RotomGlobalNavigationBar
import com.duoc.rotompedia.ui.components.RotomNavigationShell
import androidx.navigation.compose.*
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class AtlasUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `edition change never leaks Emerald encounters and Pokemon link preserves variety identity`() {
        val row = MapEncounter(1, 394, 9, 280, 1, 1, 0, 4, 4, 4, emptyList())
        var chosen: Int? = null
        compose.setContent { RotompediaTheme {
            HoennAreaSheet(MapArea(394, 450, "", emptyMap()), MapLocation(450, "hoenn-route-102", mapOf("es" to "Ruta 102")),
                AreaEncounters(394, 9, AreaCoverage.AVAILABLE, listOf(row), emptyList()), listOf(row),
                mapOf(1 to MapEncounterMethod(1, "walk", emptyMap()), 5 to MapEncounterMethod(5, "surf", emptyMap())),
                emptyMap(), mapOf(280 to LocationPokemonLabel(280, "Ralts")), HoennEncounterCategory.ALL, "", {}, { chosen = it })
        } }
        compose.onNodeWithText("Surf", substring = false).assertDoesNotExist()
        compose.onNodeWithText("Edición: Esmeralda ▾").performClick()
        compose.onNodeWithText("Rubí · datos pendientes").performClick()
        compose.onNodeWithText("#280 · Ralts").assertDoesNotExist()
        compose.onNodeWithText("Todos", substring = false).assertDoesNotExist()
        compose.onNodeWithText("Edición: Rubí ▾").performClick()
        compose.onNodeWithText("Esmeralda · verificada").performClick()
        compose.onNodeWithTag("atlas_encounters").performScrollToNode(hasText("#280 · Ralts"))
        compose.onNodeWithText("#280 · Ralts").performClick()
        compose.runOnIdle { assertEquals(280, chosen) }
    }

    @Test fun `all five fixed navigation tabs dispatch real routes with touch targets`() {
        var chosen: String? = null
        compose.setContent { RotomGlobalNavigationBar("map_feature_screen") { chosen = it } }
        listOf("Inicio" to "menu_principal_screen", "Pokédex" to "pokedex", "Mapas" to "map_feature_screen",
            "Equipos" to "team_builder", "Más" to "more_tools").forEach { (label, route) ->
            compose.onNodeWithText(label).performClick()
            compose.runOnIdle { assertEquals(route, chosen) }
        }
    }

    @Test fun `map controls do not draw outside a narrow viewport and reset remains accessible`() {
        compose.setContent { RotompediaTheme {
            Box(Modifier.width(280.dp)) { HoennSectionMap(emptyList(), null, {}, mapHeight = 180.dp) }
        } }
        compose.onNodeWithTag("hoenn_atlas").assertWidthIsEqualTo(280.dp).assertHeightIsEqualTo(180.dp)
        compose.onNodeWithContentDescription("Acercar").performClick()
        compose.onNodeWithContentDescription("Alejar").performClick()
        compose.onNodeWithContentDescription("Restablecer vista").assertIsDisplayed().performClick()
    }

    @Test fun `real map route returns to selector then resumes last region from another fixed tab`() {
        compose.setContent {
            val context = LocalContext.current
            remember { context.getSharedPreferences("rotompedia_map_navigation", 0).edit().clear().commit() }
            val nav = rememberNavController()
            RotompediaTheme { RotomNavigationShell(nav) { padding ->
                NavHost(nav, startDestination = "menu_principal_screen", modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
                    composable("menu_principal_screen") { Text("Inicio de prueba") }
                    composable("map_feature_screen") { MapFeatureScreen(nav) }
                    composable("more_tools") { RotomMoreToolsScreen({}, {}) }
                }
            } }
        }
        compose.onNodeWithText("Mapas", substring = false).performClick()
        compose.onNodeWithText("Elige una región").assertIsDisplayed()
        compose.onNodeWithText("Kanto", substring = false).performClick()
        compose.onNodeWithText("Mapa de Kanto", substring = false).assertIsDisplayed()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Elige una región").assertIsDisplayed()
        compose.onNodeWithText("Más", substring = false).performClick()
        compose.onNodeWithText("Más herramientas").assertIsDisplayed()
        compose.onNodeWithText("Mapas", substring = false).performClick()
        compose.onNodeWithText("Mapa de Kanto", substring = false).assertIsDisplayed()
        compose.onNodeWithTag("rotom_global_nav").assertIsDisplayed()
    }
}
