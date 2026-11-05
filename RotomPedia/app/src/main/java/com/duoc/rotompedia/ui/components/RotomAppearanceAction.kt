package com.duoc.rotompedia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import com.duoc.rotompedia.ui.theme.RotomDimensions
import com.duoc.rotompedia.util.AppearanceMode
import com.duoc.rotompedia.util.RotomAppearanceManager

@Composable
fun RotomAppearanceAction(labelled: Boolean = false) {
    var open by rememberSaveable { mutableStateOf(false) }
    if (labelled) OutlinedButton(onClick = { open = true }) { Text("Apariencia") }
    else IconButton(onClick = { open = true }, modifier = Modifier.size(RotomDimensions.TouchTarget)) {
        Icon(Icons.Default.Palette, "Cambiar apariencia")
    }
    if (open) RotomAppearanceDialog { open = false }
}

@Composable
internal fun RotomAppearanceDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val selected by RotomAppearanceManager.mode.collectAsState()
    RotompediaTheme {
        AlertDialog(onDismissRequest = onDismiss, title = { Text("Apariencia") }, text = {
            Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                AppearanceMode.entries.forEach { mode ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .selectable(selected == mode, role = Role.RadioButton,
                            onClick = { RotomAppearanceManager.setMode(context, mode) }).padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected == mode, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(mode.label, modifier = Modifier.weight(1f))
                    }
                }
            }
        }, confirmButton = { TextButton(onDismiss) { Text("Cerrar") } })
    }
}
