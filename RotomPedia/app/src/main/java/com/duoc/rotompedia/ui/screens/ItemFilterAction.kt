package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun ItemFilterAction(activeCount: Int, onClick: () -> Unit) {
    FilledTonalButton(onClick, modifier = Modifier.padding(end = 8.dp).widthIn(min = 112.dp).heightIn(min = 48.dp).semantics {
        contentDescription = "Filtrar objetos"
        stateDescription = if (activeCount == 0) "Sin filtros adicionales" else "$activeCount filtros adicionales activos"
    }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
        Icon(Icons.Default.FilterList, null, Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text("Filtros", fontWeight = FontWeight.SemiBold)
        if (activeCount > 0) {
            Spacer(Modifier.width(6.dp))
            Badge { Text(activeCount.toString()) }
        }
    }
}
