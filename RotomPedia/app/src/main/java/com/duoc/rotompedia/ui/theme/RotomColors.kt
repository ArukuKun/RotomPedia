package com.duoc.rotompedia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Semantic UI colors. Pokémon type and stat identity colors remain separate. */
object RotomColors {
    fun inkOn(background: Color): Color = if (background.luminance() > 0.179f) Color.Black else Color.White
    val text: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val raised: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val outline: Color @Composable get() = MaterialTheme.colorScheme.outline
    val accent: Color @Composable get() = MaterialTheme.colorScheme.primary
    val onAccent: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
    val negative: Color @Composable get() = MaterialTheme.colorScheme.error
    val negativePanel: Color @Composable get() = MaterialTheme.colorScheme.errorContainer
    val positive: Color @Composable get() = if (surface.luminance() > 0.5f) Color(0xFF206929) else Color(0xFF9BE3A2)
    val warning: Color @Composable get() = if (surface.luminance() > 0.5f) Color(0xFF81500D) else Color(0xFFFFD18C)
    val frame: Color @Composable get() = LocalRotomPalette.current.frame
}
