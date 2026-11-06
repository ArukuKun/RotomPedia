package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.ui.screens.AbilityHolderCounts
import com.duoc.rotompedia.ui.screens.AbilityReferenceSheet
import com.duoc.rotompedia.data.items.ItemLabels
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class AbilityCountsUiTest {
    @get:Rule val compose = createComposeRule()
    private val ability = ReferenceAbility(54, "truant", "Ausente", "Truant", true, null, null,
        listOf(ReferenceText("scarlet-violet", "es", "Texto actual")))
    private fun index(links: List<AbilityLink>) = ReferenceIndex("test", emptyList(), emptyList(), listOf(ability), links, emptyList())

    @Test fun `sheet counts varieties once preserves all slots sprites roles pagination and navigation`() {
        val links = (1..41).map { AbilityLink(it, 54, false, 1) } + listOf(
            AbilityLink(1, 54, false, 2), AbilityLink(1, 54, false, 1), AbilityLink(1, 54, true, 3))
        var selected: String? = null
        compose.setContent { MaterialTheme {
            AbilityReferenceSheet(ability, index(links), (1..41).associateWith { "Variedad $it" }, {}, { selected = it })
        } }
        compose.onNodeWithText("Pereza").assertExists()
        compose.onNodeWithText("Antes de la novena generación: Ausente").assertExists()
        compose.onNodeWithText("Variedades asociadas · 41").assertExists()
        compose.onNodeWithText("Normal: 41").assertExists()
        compose.onNodeWithText("Oculta: 1").assertExists()
        compose.onNodeWithText("Una variedad puede figurar en ambos roles.").assertExists()
        compose.onNodeWithText("Variedad 1 · Normal · slots 1, 2 · Oculta · slot 3").assertExists()
        compose.onNodeWithTag("ability_sprite_1_1", useUnmergedTree = true).assertExists()
        compose.onNodeWithContentDescription("Edición del texto: ${ItemLabels.version("scarlet-violet")}").performScrollTo().performClick()
        compose.onNode(hasText(ItemLabels.version("scarlet-violet"), substring = false) and
            !hasContentDescription("Edición del texto: ${ItemLabels.version("scarlet-violet")}")).performClick()
        compose.onNodeWithText("Texto actual").assertExists()
        compose.onNodeWithText("Normal: 41").assertExists()
        compose.onNodeWithText("Oculta: 1").assertExists()
        compose.onNodeWithText("Cargar 40 más").performScrollTo().performClick()
        compose.onNodeWithText("Variedad 41 · Normal · slot 1").performScrollTo().performClick()
        assertEquals("41", selected)
        compose.onNodeWithText("Cargar 40 más").assertDoesNotExist()
    }

    @Test fun `counter labels remain readable at narrow width and large font without species units`() {
        val summary = AbilityPresentation.holders(index(listOf(AbilityLink(38, 54, false, 1), AbilityLink(10104, 54, true, 3))), 54)
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.6f)) {
                MaterialTheme { Column(Modifier.width(280.dp)) { AbilityHolderCounts(summary) } }
            }
        }
        compose.onNodeWithText("Variedades asociadas · 2").assertIsDisplayed()
        compose.onNodeWithText("Normal: 1").assertIsDisplayed()
        compose.onNodeWithText("Oculta: 1").assertIsDisplayed()
        compose.onNodeWithText("Una variedad puede figurar en ambos roles.").assertDoesNotExist()
    }

    @Test fun `empty ability coverage displays zero roles without invented holders`() {
        compose.setContent { MaterialTheme { AbilityReferenceSheet(ability, index(emptyList()), emptyMap(), {}, {}) } }
        compose.onNodeWithText("Variedades asociadas · 0").assertExists()
        compose.onNodeWithText("Normal: 0").assertExists()
        compose.onNodeWithText("Oculta: 0").assertExists()
        compose.onNodeWithText("No hay Pokémon para mostrar.").assertExists()
    }
}
