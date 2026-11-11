package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.maps.AtlasEdition

@Composable
internal fun AtlasEditionSelector(editions: List<AtlasEdition>, selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val edition = editions.first { it.versionId == selected }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Edición: ${edition.title} ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            editions.forEach { option ->
                DropdownMenuItem(text = { Text(option.title + if (option.verified) " · verificada" else " · datos pendientes") },
                    onClick = { expanded = false; onSelect(option.versionId) })
            }
        }
    }
    Text(if (edition.verified) "Encuentros verificados para esta edición" else "Datos pendientes para esta edición; el mapa regional no cambia.",
        style = MaterialTheme.typography.labelSmall)
}
