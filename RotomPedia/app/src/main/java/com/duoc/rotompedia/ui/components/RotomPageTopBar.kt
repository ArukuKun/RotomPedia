package com.duoc.rotompedia.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.duoc.rotompedia.ui.theme.LocalRotomPalette

/** The header is part of the device shell, not a reading surface. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotomPageTopBar(title: @Composable () -> Unit, navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}) {
    val palette = MaterialTheme.colorScheme
    val frame = LocalRotomPalette.current.frame
    MaterialTheme(colorScheme = palette.copy(onSurface = Color.White, onSurfaceVariant = Color.White, primary = Color.White)) {
        TopAppBar(title = title, navigationIcon = navigationIcon, actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = frame, titleContentColor = Color.White,
                navigationIconContentColor = Color.White, actionIconContentColor = Color.White))
    }
}
