package com.duoc.rotompedia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.ui.screens.TypeChartContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class TypeChartScrollUiTest {
    @get:Rule val compose = createComposeRule()
    private fun bounds(tag: String) = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun scroll(tag: String, x: Float, y: Float) = compose.onNodeWithTag(tag)
        .performSemanticsAction(SemanticsActions.ScrollBy) { it(x, y) }

    @Test fun `frozen corner and both headers stay aligned after scrolling either axis from header or body`() {
        compose.setContent { MaterialTheme { TypeChartContent(Modifier.size(320.dp, 400.dp)) } }
        val corner = bounds("type_corner")
        val headerTop = bounds("type_column_normal").top
        scroll("type_rows", 0f, 250f)
        scroll("type_columns", 300f, 0f)
        assertEquals(corner, bounds("type_corner"))
        assertEquals(headerTop, bounds("type_column_normal").top)
        assertEquals(corner.left, bounds("type_row_normal").left)
        assertEquals(bounds("type_column_grass").left.value, bounds("type_cell_5_4").left.value, 1f)
        assertEquals(bounds("type_row_ice").top.value, bounds("type_cell_5_4").top.value, 1f)
        compose.onNodeWithTag("type_column_grass").assertIsDisplayed()
        compose.onNodeWithTag("type_row_ice").assertIsDisplayed()
        // Gestures on the matrix itself must move the same headers, not a separate copy.
        val beforeX = bounds("type_column_grass").left
        val beforeY = bounds("type_row_ice").top
        compose.onNodeWithTag("type_matrix").performTouchInput { swipeLeft() }
        assertTrue(bounds("type_column_grass").left < beforeX)
        compose.onNodeWithTag("type_matrix").performTouchInput { swipeUp() }
        assertTrue(bounds("type_row_ice").top < beforeY)
        assertEquals(corner, bounds("type_corner"))
    }

    @Test fun `both table offsets restore together without moving the frozen corner`() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { MaterialTheme { TypeChartContent(Modifier.size(320.dp, 400.dp)) } }
        scroll("type_rows", 0f, 250f)
        scroll("type_columns", 300f, 0f)
        val cell = bounds("type_cell_5_4")
        val corner = bounds("type_corner")
        restoration.emulateSavedInstanceStateRestore()
        assertEquals(cell, bounds("type_cell_5_4"))
        assertEquals(corner, bounds("type_corner"))
        assertEquals(bounds("type_column_grass").left.value, cell.left.value, 1f)
        assertEquals(bounds("type_row_ice").top.value, cell.top.value, 1f)
    }

    @Test fun `large text scales all four panes equally and keeps attack defense labels visible`() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.5f)) {
                MaterialTheme { TypeChartContent(Modifier.size(320.dp, 400.dp)) }
            }
        }
        compose.onNodeWithText("Defensa →\nAtaque ↓").assertIsDisplayed()
        scroll("type_rows", 0f, 200f)
        scroll("type_columns", 300f, 0f)
        assertEquals(bounds("type_column_water").left.value, bounds("type_cell_3_2").left.value, 1f)
        assertEquals(bounds("type_row_electric").top.value, bounds("type_cell_3_2").top.value, 1f)
        val corner = bounds("type_corner")
        val row = bounds("type_row_electric")
        assertEquals(corner.right - corner.left, row.right - row.left)
    }
}
