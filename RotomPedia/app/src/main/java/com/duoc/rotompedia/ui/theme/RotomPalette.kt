package com.duoc.rotompedia.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.util.RotomForm

data class RotomPalette(
    val frame: Color,
    val accent: Color,
    val reading: Color,
    val raised: Color,
    val ink: Color,
    val muted: Color,
    val outline: Color,
    val backgroundScrim: Color
) {
    fun colorScheme(dark: Boolean): ColorScheme {
        val base = if (dark) darkColorScheme() else lightColorScheme()
        return base.copy(
            primary = accent, onPrimary = if (dark) Color(0xFF142E3C) else Color.White,
            primaryContainer = raised, onPrimaryContainer = ink,
            secondary = accent, onSecondary = if (dark) Color(0xFF142E3C) else Color.White,
            secondaryContainer = raised, onSecondaryContainer = ink,
            tertiary = accent, onTertiary = if (dark) Color(0xFF142E3C) else Color.White,
            tertiaryContainer = raised, onTertiaryContainer = ink,
            background = reading, onBackground = ink, surface = reading, onSurface = ink,
            surfaceVariant = raised, onSurfaceVariant = muted,
            surfaceContainer = raised, surfaceContainerLow = reading, surfaceContainerHigh = raised,
            surfaceContainerHighest = raised, surfaceContainerLowest = reading,
            surfaceBright = raised, surfaceDim = reading,
            outline = outline, outlineVariant = outline.copy(alpha = 0.6f), surfaceTint = Color.Transparent
        )
    }
}

/** Semantic roles, not Pokémon type colors. Each form retains the same layout. */
fun rotomPalette(form: RotomForm, dark: Boolean): RotomPalette {
    val colors = when (form) {
        RotomForm.NORMAL -> Triple(0xFFB63C18, 0xFF8BDEF2, 0xFF075C75)
        RotomForm.CALOR -> Triple(0xFFA83228, 0xFFFFBD83, 0xFF873B12)
        RotomForm.LAVADO -> Triple(0xFF155B95, 0xFF96DBFF, 0xFF075C75)
        RotomForm.FRIO -> Triple(0xFF12656C, 0xFFA9EEED, 0xFF0C6262)
        RotomForm.VENTILADOR -> Triple(0xFF68500B, 0xFFFFE285, 0xFF625006)
        RotomForm.CORTE -> Triple(0xFF276336, 0xFFB5E898, 0xFF315D20)
    }
    return RotomPalette(
        frame = Color(colors.first), accent = Color(if (dark) colors.second else colors.third),
        reading = Color(if (dark) 0xFF18303C else 0xFFF7FDFE),
        raised = Color(if (dark) 0xFF213F4C else 0xFFE4F2F5),
        ink = Color(if (dark) 0xFFF3FBFF else 0xFF142E3C),
        muted = Color(if (dark) 0xFFBED1DA else 0xFF405E6C),
        outline = Color(if (dark) 0xFF8CA6AF else 0xFF577883),
        backgroundScrim = Color(if (dark) 0xFF091F2B else 0xFFD6EEF3).copy(alpha = if (dark) 0.66f else 0.3f)
    )
}

val LocalRotomPalette = staticCompositionLocalOf { rotomPalette(RotomForm.NORMAL, true) }

object RotomDimensions {
    val ScreenGutter = 12.dp
    val PanelCorner = 16.dp
    val CardCorner = 14.dp
    val TouchTarget = 48.dp
    // Face bottom in the existing 827×1477 illustration, with a little breathing room.
    const val FaceAspect = 330f / 827f
}
