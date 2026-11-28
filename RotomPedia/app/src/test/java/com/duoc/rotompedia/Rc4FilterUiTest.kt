package com.duoc.rotompedia

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.duoc.rotompedia.data.explorer.ExplorerFilter
import com.duoc.rotompedia.ui.screens.ExplorerFilterDialog
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class Rc4FilterUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `draft confirmation does not apply cancellation discards and reset only changes draft`() {
        var applied by mutableStateOf(ExplorerFilter(query = "Pikachu", type = "electric"))
        var open by mutableStateOf(true)
        var applyCount = 0
        compose.setContent { RotompediaTheme {
            Button(onClick = { open = true }) { Text("Abrir") }
            if (open) ExplorerFilterDialog(applied, true, emptyList(), true,
                onDismiss = { open = false }, onApply = { applied = it; applyCount++; open = false })
        } }
        compose.onNodeWithText("Tipo", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Fuego", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Confirmar selección").performClick()
        compose.runOnIdle { assertEquals("electric", applied.type); assertEquals(0, applyCount) }
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Abrir").performClick()
        compose.onNodeWithText("Eléctrico", substring = false).assertExists()
        compose.onNodeWithText("Restablecer").performClick()
        compose.runOnIdle { assertEquals("Pikachu", applied.query); assertEquals("electric", applied.type) }
        compose.onNodeWithText("Aplicar y cerrar").performClick()
        compose.runOnIdle { assertEquals(ExplorerFilter(), applied); assertEquals(1, applyCount); assertFalse(open) }
    }
}
