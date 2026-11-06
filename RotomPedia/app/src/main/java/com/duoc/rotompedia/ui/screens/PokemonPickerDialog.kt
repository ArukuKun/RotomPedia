package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun PokemonPickerDialog(pokemonViewModel: PokemonViewModel, onDismiss: () -> Unit,
    onPokemonSelected: (Int, String, String, String, String?, Int, Int, Int, Int, Int, Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val explorer = remember(pokemonViewModel) { pokemonViewModel.createExplorer(scope) }
    var selecting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(0.94f), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Elige un Pokémon", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (selecting) LinearProgressIndicator(Modifier.fillMaxWidth())
                PokemonExplorerContent(explorer, modifier = Modifier.fillMaxWidth().weight(1f), onChoose = choose@{ entry ->
                    if (selecting) return@choose
                    selecting = true
                    error = null
                    scope.launch {
                        try {
                            val data = pokemonViewModel.loadFormData(entry.pokemonId.toString())
                            if (data == null) error = "No se pudo cargar ${entry.displayName}. Comprueba la conexión y vuelve a seleccionarlo."
                            else onPokemonSelected(data.id, entry.displayName, data.spriteUrl,
                                data.types.first(), data.types.getOrNull(1), data.hp, data.atk, data.def, data.spa, data.spd, data.spe)
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            error = "No se pudo completar la selección. Inténtalo de nuevo."
                        } finally { selecting = false }
                    }
                })
            }
        }
    }
}
