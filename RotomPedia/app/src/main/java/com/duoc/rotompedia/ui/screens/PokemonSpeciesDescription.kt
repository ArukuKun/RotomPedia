package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.viewmodel.EvolutionState

@Composable
internal fun PokemonSpeciesDescription(isDefault: Boolean, state: EvolutionState) {
    val description = (state as? EvolutionState.Success)?.description?.takeIf {
        isDefault && it.isNotBlank() && it != "No se encontró descripción en español."
    } ?: return
    Text("Descripción", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Text(description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
    }
}
