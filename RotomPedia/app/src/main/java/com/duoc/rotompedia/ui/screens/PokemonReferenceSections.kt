package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.model.PokemonEncounter
import com.duoc.rotompedia.data.remote.PokemonApi
import kotlinx.coroutines.CancellationException

@Composable
fun PokemonMovesSection(pokemonId: Int) {
    var expanded by remember(pokemonId) { mutableStateOf(false) }
    Text("Movimientos", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
    TextButton(onClick = { expanded = !expanded }) {
        Text(if (expanded) "Ocultar movimientos" else "Consultar movimientos")
    }
    if (!expanded) return
    MoveLearnsetPane(pokemonId, modifier = Modifier.fillMaxWidth().height(560.dp))
}

@Composable
fun PokemonLocationsSection(pokemonId: Int) {
    var expanded by remember(pokemonId) { mutableStateOf(false) }
    var encounters by remember(pokemonId) { mutableStateOf<List<PokemonEncounter>?>(null) }
    var error by remember(pokemonId) { mutableStateOf(false) }
    var attempt by remember(pokemonId) { mutableStateOf(0) }
    LaunchedEffect(pokemonId, expanded, attempt) {
        if (expanded && encounters == null) {
            error = false
            try {
                encounters = PokemonApi.service.getPokemonEncounters(pokemonId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                error = true
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Text("Localizaciones", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
    TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Ocultar localizaciones" else "Consultar encuentros") }
    if (!expanded) return
    when {
        error -> {
            Text("No se pudieron cargar los encuentros.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { attempt++ }) { Text("Reintentar") }
        }
        encounters == null -> CircularProgressIndicator(Modifier.size(24.dp))
        encounters!!.isEmpty() -> Text("PokeAPI no registra encuentros salvajes para esta forma. No implica que sea imposible obtenerla.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        else -> encounters!!.forEach { encounter ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(encounter.locationArea.name.replace('-', ' '), fontWeight = FontWeight.SemiBold)
                    Text(encounter.versionDetails.joinToString { it.version.name.replace('-', ' ') }, fontSize = 12.sp)
                }
            }
        }
    }
}
