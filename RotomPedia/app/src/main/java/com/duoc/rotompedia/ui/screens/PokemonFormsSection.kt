package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.PokemonFormData
import com.duoc.rotompedia.viewmodel.PokemonFormsState

@Composable
internal fun PokemonFormsSection(pokemonId: Int, forms: List<PokemonFormData>, state: PokemonFormsState,
    onRetry: () -> Unit, onFormClick: (Int) -> Unit) {
    // A pending/failed request is not proof that the species has no other varieties.
    if (state is PokemonFormsState.Loading || state is PokemonFormsState.Idle) return
    val hasAlternatives = forms.any { it.id != pokemonId }
    if (!hasAlternatives && state is PokemonFormsState.Success) return
    Column(Modifier.fillMaxWidth().testTag("pokemon_forms")) {
        if (hasAlternatives) {
            Text("Formas", fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                items(forms, key = { it.id }) { form ->
                    val selected = form.id == pokemonId
                    Card(onClick = { if (!selected) onFormClick(form.id) }, modifier = Modifier.width(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (selected) 2.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            PokemonImage(pokemonId = form.id, savedUrl = form.spriteUrl,
                                contentDescription = form.formDisplayName, modifier = Modifier.size(96.dp))
                            Text(form.formDisplayName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text(if (selected) "✓ Seleccionada" else if (form.isDefault) "Predeterminada" else
                                PokemonFormCatalog.find(form.name)?.formTag ?: "Variante", fontSize = 12.sp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            form.types.forEach { Text(traducirTipo(it), fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
        if (state is PokemonFormsState.Error) {
            TextButton(onRetry) { Text("Reintentar formas") }
        }
        Spacer(Modifier.height(24.dp))
    }
}
