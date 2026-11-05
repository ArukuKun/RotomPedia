package com.duoc.rotompedia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.duoc.rotompedia.util.RotomAppearanceManager
import com.duoc.rotompedia.util.RotomThemeManager

@Composable
fun RotompediaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Temporary boundary for destinations not yet migrated. Never mix light text with legacy whites.
    legacy: Boolean = false,
    content: @Composable () -> Unit
) {
    val form by RotomThemeManager.currentForm.collectAsState()
    val appearance by RotomAppearanceManager.mode.collectAsState()
    val dark = appearance.isDark(darkTheme)
    val palette = rotomPalette(form, if (legacy) true else dark)
    val scheme = if (legacy) darkColorScheme(
        primary = form.primaryColor,
        secondary = form.secondaryColor,
        tertiary = form.accentColor,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E)
    ) else palette.colorScheme(dark)

    CompositionLocalProvider(LocalRotomPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
    }
}
