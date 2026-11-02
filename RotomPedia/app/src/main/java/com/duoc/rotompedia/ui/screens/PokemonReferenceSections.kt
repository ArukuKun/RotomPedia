package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.model.MoveDetailResponse
import com.duoc.rotompedia.data.model.PokemonEncounter
import com.duoc.rotompedia.data.remote.PokemonApi
import com.duoc.rotompedia.viewmodel.PokemonMovesState
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun PokemonMovesSection(pokemonId: Int, pokemonViewModel: PokemonViewModel) {
    var expanded by remember(pokemonId) { mutableStateOf(false) }
    var method by remember(pokemonId) { mutableStateOf(0) }
    val state by pokemonViewModel.pokemonMovesState.collectAsState()
    LaunchedEffect(pokemonId, expanded) {
        if (expanded) pokemonViewModel.loadPokemonMoves(pokemonId)
    }
    Text("Movimientos", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
    TextButton(onClick = { expanded = !expanded }) {
        Text(if (expanded) "Ocultar movimientos" else "Consultar movimientos")
    }
    if (!expanded) return
    when (val current = state) {
        is PokemonMovesState.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
        is PokemonMovesState.Error -> {
            Text(current.message, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { pokemonViewModel.loadPokemonMoves(pokemonId) }) { Text("Reintentar") }
        }
        is PokemonMovesState.Success -> {
            val labels = listOf("Nivel", "MT/MO", "Huevo", "Tutor")
            ScrollableTabRow(selectedTabIndex = method, edgePadding = 0.dp) {
                labels.forEachIndexed { i, label ->
                    Tab(selected = i == method, onClick = { method = i }, text = { Text(label) })
                }
            }
            val moves = when (method) {
                0 -> current.levelUpMoves
                1 -> current.machineMoves
                2 -> current.eggMoves
                else -> current.tutorMoves
            }
            Text("Referencia general de PokeAPI; puede variar según el juego. Hasta 60 movimientos por método (30 de tutor).",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (moves.isEmpty()) Text("Sin movimientos registrados para este método.")
            moves.forEach { move -> MoveReferenceCard(move) }
        }
        else -> Unit
    }
}

@Composable
private fun MoveReferenceCard(move: MoveDetailResponse) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(move.names.firstOrNull { it.language.name == "es" }?.name ?: move.name.replace('-', ' '),
                fontWeight = FontWeight.SemiBold)
            Text("Potencia: ${move.power ?: "—"} · Precisión: ${move.accuracy?.let { "$it%" } ?: "—"} · PP: ${move.pp ?: "—"}",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
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
