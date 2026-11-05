package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.PokemonBiology
import com.duoc.rotompedia.data.SpeciesCategoryState
import com.duoc.rotompedia.data.model.PokemonData

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PokemonBiologyCard(pokemon: PokemonData, speciesId: Int, speciesName: String,
    category: SpeciesCategoryState, onRetryCategory: () -> Unit, abilities: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().testTag("pokemon_biology"), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Datos biológicos", style = MaterialTheme.typography.titleMedium)
            Text("Especie: $speciesName", fontWeight = FontWeight.SemiBold)
            val categoryLabel = when {
                category.loading -> "Cargando…"
                category.speciesId != speciesId -> "—"
                else -> category.label ?: "—"
            }
            Text("Categoría de especie: $categoryLabel")
            if (category.failed) TextButton(onRetryCategory) { Text("Reintentar categoría") }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Altura: ${PokemonBiology.measurement(pokemon.height, "m")}")
                Text("Peso: ${PokemonBiology.measurement(pokemon.weight, "kg")}")
            }
            Text("PokeAPI · medidas de esta variedad", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            abilities()
        }
    }
}
