package com.duoc.rotompedia

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.ui.screens.MapFeatureScreen
import com.duoc.rotompedia.ui.theme.*
import com.duoc.rotompedia.util.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class RotomVisualFoundationTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before fun resetPreferences() {
        context.getSharedPreferences("rotom_appearance_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("rotom_theme_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("rotompedia_map_navigation", Context.MODE_PRIVATE).edit().clear().commit()
        RotomAppearanceManager.init(context)
        RotomThemeManager.init(context)
    }

    @Test fun `missing and unknown preference default to system and mode resolves independently`() {
        assertEquals(AppearanceMode.SYSTEM, RotomAppearanceManager.mode.value)
        context.getSharedPreferences("rotom_appearance_prefs", Context.MODE_PRIVATE).edit()
            .putString("appearance_mode", "not-a-mode").commit()
        RotomAppearanceManager.init(context)
        assertEquals(AppearanceMode.SYSTEM, RotomAppearanceManager.mode.value)
        assertTrue(AppearanceMode.SYSTEM.isDark(true))
        assertFalse(AppearanceMode.SYSTEM.isDark(false))
        assertFalse(AppearanceMode.LIGHT.isDark(true))
        assertTrue(AppearanceMode.DARK.isDark(false))
    }

    @Test fun `appearance survives initialization without overwriting form or other preferences`() {
        val prefs = context.getSharedPreferences("rotom_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("unrelated", "preserve-me").commit()
        RotomThemeManager.setForm(context, RotomForm.LAVADO)
        RotomAppearanceManager.setMode(context, AppearanceMode.LIGHT)
        RotomAppearanceManager.init(context)
        RotomThemeManager.init(context)
        assertEquals(AppearanceMode.LIGHT, RotomAppearanceManager.mode.value)
        assertEquals(RotomForm.LAVADO, RotomThemeManager.currentForm.value)
        assertEquals("preserve-me", prefs.getString("unrelated", null))
        RotomThemeManager.setForm(context, RotomForm.CORTE)
        assertEquals(AppearanceMode.LIGHT, RotomAppearanceManager.mode.value)
        assertEquals("LIGHT", context.getSharedPreferences("rotom_appearance_prefs", Context.MODE_PRIVATE)
            .getString("appearance_mode", null))
    }

    private fun contrast(a: Color, b: Color): Float {
        val x = a.luminance(); val y = b.luminance()
        return (maxOf(x, y) + 0.05f) / (minOf(x, y) + 0.05f)
    }

    @Test fun `six forms in both appearances have readable semantic colors and white toolbar text`() {
        RotomForm.entries.forEach { form -> listOf(true, false).forEach { dark ->
            val p = rotomPalette(form, dark)
            listOf(p.reading, p.raised).forEach { surface ->
                listOf(p.ink, p.muted, p.accent).forEach { text ->
                    assertTrue("$form dark=$dark text=$text ratio=${contrast(text, surface)}", contrast(text, surface) >= 4.5f)
                }
                assertTrue(contrast(p.outline, surface) >= 3f)
            }
            assertTrue(contrast(Color.White, p.frame) >= 4.5f)
            val scheme = p.colorScheme(dark)
            assertTrue(contrast(scheme.onPrimary, scheme.primary) >= 4.5f)
        } }
    }

    @Test fun `system changes update the theme but explicit light overrides system dark`() {
        val system = mutableStateOf(false)
        compose.setContent { RotompediaTheme(darkTheme = system.value) {
            Text(if (MaterialTheme.colorScheme.surface == rotomPalette(RotomForm.NORMAL, true).reading) "dark" else "light")
        } }
        compose.onNodeWithText("light").assertExists()
        compose.runOnIdle { system.value = true }
        compose.onNodeWithText("dark").assertExists()
        compose.runOnIdle { RotomAppearanceManager.setMode(context, AppearanceMode.LIGHT) }
        compose.onNodeWithText("light").assertExists()
    }

    @Test fun `unmigrated routes keep the legacy surface even when light is selected`() {
        RotomAppearanceManager.setMode(context, AppearanceMode.LIGHT)
        compose.setContent { RotompediaTheme(legacy = true) {
            Text(if (MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E)) "legacy-safe" else "incorrect")
        } }
        compose.onNodeWithText("legacy-safe").assertExists()
    }

    @Test fun `portrait face stays outside content at narrow width and large font`() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.5f)) {
                Box(Modifier.width(280.dp).height(600.dp)) {
                    RotomScaffold("Pokédex", {}, actions = { RotomAppearanceAction() }) { Text("Contenido") }
                }
            }
        }
        val face = compose.onNodeWithTag("rotom_face").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("rotom_content").fetchSemanticsNode().boundsInRoot
        assertTrue(face.height > 0)
        assertTrue("face must not overlap content", face.bottom <= panel.top)
        assertEquals(330f / 827f, face.height / face.width, 0.02f)
        compose.onNodeWithContentDescription("Cambiar apariencia").assertHasClickAction()
            .assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
    }

    @Test fun `landscape uses a protected face column instead of shrinking the artwork above content`() {
        compose.setContent { Box(Modifier.width(320.dp).height(220.dp)) {
            RotomScaffold("Mapas", {}) { Text("Regiones") }
        } }
        val face = compose.onNodeWithTag("rotom_face").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("rotom_content").fetchSemanticsNode().boundsInRoot
        assertTrue(face.width > 0)
        assertTrue("face column must not overlap content", face.right <= panel.left)
        compose.onNodeWithText("Regiones").assertIsDisplayed()
    }

    @Test fun `actual map selector changes appearance without losing navigation or form`() {
        RotomThemeManager.setForm(context, RotomForm.FRIO)
        compose.setContent {
            val nav = rememberNavController()
            RotompediaTheme(legacy = true) { NavHost(nav, startDestination = "maps") {
                composable("maps") { MapFeatureScreen(nav) }
            } }
        }
        compose.onNodeWithContentDescription("Cambiar apariencia").performClick()
        compose.onNodeWithText("Claro", substring = false).performClick()
        assertEquals(AppearanceMode.LIGHT, RotomAppearanceManager.mode.value)
        assertEquals(RotomForm.FRIO, RotomThemeManager.currentForm.value)
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithText("Kanto", substring = false).performClick()
        compose.onNodeWithText("Mapa de Kanto", substring = true).assertExists()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Elige una región").assertExists()
    }
}
