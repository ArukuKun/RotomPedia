package com.duoc.rotompedia

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duoc.rotompedia.data.explorer.ExplorerFilter
import com.duoc.rotompedia.ui.components.RotomNavigationShell
import com.duoc.rotompedia.ui.components.navigateMainDestination
import com.duoc.rotompedia.ui.screens.ExplorerFilterDialog
import com.duoc.rotompedia.ui.screens.RotomHomeScreen
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Device coverage of production Home/shell and filter sheet, not an end-to-end account test. */
@RunWith(AndroidJUnit4::class)
class Rc4DeviceUiTest {
    @get:Rule val compose = createComposeRule()

    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "rc4-evidence").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val variant = InstrumentationRegistry.getArguments().getString("evidenceVariant", "portrait")
        File(directory, "$variant-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun homeCardAndNavbarReturnToHome() {
        lateinit var nav: NavHostController
        compose.setContent { RotompediaTheme {
            nav = rememberNavController()
            RotomNavigationShell(nav) { padding ->
                NavHost(nav, startDestination = "menu_principal_screen", modifier = Modifier.padding(padding)) {
                    composable("menu_principal_screen") { RotomHomeScreen({ nav.navigateMainDestination(it) }, {}) }
                    composable("pokedex") { Text("Destino de prueba Pokédex") }
                }
            }
        } }
        compose.onNode(hasText("Pokédex") and hasAnyAncestor(hasTestTag("rotom_home_destinations"))).performClick()
        compose.onNodeWithText("Destino de prueba Pokédex").assertIsDisplayed()
        compose.onNode(hasText("Inicio") and hasAnyAncestor(hasTestTag("rotom_global_nav"))).performClick()
        compose.runOnIdle { assertEquals("menu_principal_screen", nav.currentDestination?.route) }
        compose.onNodeWithText("Accesos a tus herramientas").assertIsDisplayed()
        capture("navigation-home")
    }

    @Test fun filterDraftIsNotAppliedUntilConfirmation() {
        var applied = ExplorerFilter(type = "electric")
        var open by mutableStateOf(true)
        var count = 0
        compose.setContent { RotompediaTheme {
            if (open) ExplorerFilterDialog(applied, true, emptyList(), true,
                onDismiss = { open = false }, onApply = { applied = it; count++; open = false })
        } }
        compose.onNodeWithText("Tipo", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Fuego", substring = false).performScrollTo().performClick()
        compose.runOnIdle { assertEquals("electric", applied.type); assertEquals(0, count) }
        capture("filter-type-options")
        compose.onNodeWithText("Confirmar selección").performClick()
        compose.onNodeWithText("Aplicar y cerrar").assertIsDisplayed()
        capture("filter-summary-draft")
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.runOnIdle { assertEquals("fire", applied.type); assertEquals(1, count); assertFalse(open) }
    }
}
