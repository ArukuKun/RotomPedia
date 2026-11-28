package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.duoc.rotompedia.ui.components.RotomNavigationShell
import com.duoc.rotompedia.ui.components.navigateMainDestination
import com.duoc.rotompedia.ui.screens.RotomHomeScreen
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class Rc4NavigationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `N01 Home card to Pokedex then navbar Home returns to Home not restored Pokedex`() {
        lateinit var nav: NavHostController
        compose.setContent {
            nav = rememberNavController()
            RotompediaTheme { RotomNavigationShell(nav) { padding ->
                NavHost(nav, startDestination = "menu_principal_screen",
                    modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
                    composable("menu_principal_screen") { RotomHomeScreen({ nav.navigate(it) }, {}) }
                    composable("pokedex") { Text("Pokédex abierta") }
                }
            } }
        }
        compose.onNode(hasText("Pokédex") and hasAnyAncestor(hasTestTag("rotom_home_destinations"))).performClick()
        compose.onNodeWithText("Pokédex abierta").assertIsDisplayed()
        compose.onNode(hasText("Inicio") and hasAnyAncestor(hasTestTag("rotom_global_nav"))).performClick()
        compose.runOnIdle { assertEquals("menu_principal_screen", nav.currentDestination?.route) }
        compose.onNodeWithText("Accesos a tus herramientas").assertIsDisplayed()
    }

    @Test fun `tab stacks preserve draft screen state and map detail retains origin without duplicates`() {
        lateinit var nav: NavHostController
        compose.setContent {
            nav = rememberNavController()
            RotompediaTheme { RotomNavigationShell(nav) { padding ->
                NavHost(nav, startDestination = "menu_principal_screen", modifier = Modifier.padding(padding)) {
                    composable("menu_principal_screen") { RotomHomeScreen({ nav.navigateMainDestination(it) }, {}) }
                    composable("pokedex") {
                        var query by rememberSaveable { mutableStateOf("") }
                        OutlinedTextField(query, { query = it }, label = { Text("Consulta guardada") })
                    }
                    composable("map_feature_screen") { Text("Selector de mapas") }
                    composable("pokemon_detail/{pokemonId}") { Text("Detalle desde mapa") }
                }
            } }
        }
        fun tab(label: String) = compose.onNode(hasText(label) and hasAnyAncestor(hasTestTag("rotom_global_nav")))
        tab("Pokédex").performClick()
        compose.onNodeWithText("Consulta guardada").performTextInput("Ralts")
        repeat(3) {
            tab("Inicio").performClick()
            tab("Pokédex").performClick()
            compose.onNodeWithText("Ralts").assertExists()
        }
        tab("Mapas").performClick()
        compose.runOnIdle { nav.navigate("pokemon_detail/280") }
        tab("Mapas").assertIsSelected()
        compose.runOnIdle { nav.popBackStack(); assertEquals("map_feature_screen", nav.currentDestination?.route) }
        tab("Inicio").performClick()
        compose.runOnIdle { assertEquals("menu_principal_screen", nav.currentDestination?.route) }
        // No intermediate duplicate Home or Map screen remains when going to another tab.
        tab("Pokédex").performClick()
        compose.runOnIdle { nav.popBackStack(); assertEquals("menu_principal_screen", nav.currentDestination?.route) }
    }
}
