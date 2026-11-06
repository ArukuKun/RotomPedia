package com.duoc.rotompedia.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.di.AppContainer
import com.duoc.rotompedia.viewmodel.PokemonMovesViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun PokemonMovesRoute(pokemonId: Int, onBack: () -> Unit, onItemClick: (Int) -> Unit) {
    val container = AppContainer.get(LocalContext.current)
    val factory = remember(container, pokemonId) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                PokemonMovesViewModel(pokemonId, container.moves, extras.createSavedStateHandle()) as T
        }
    }
    val model: PokemonMovesViewModel = viewModel(factory = factory)
    val pokemonFlow = remember(container) { container.pokemon.observeExplorer() }
    val pokemon by pokemonFlow.collectAsState(initial = emptyList())
    val selected = pokemon.firstOrNull { it.pokemonId == pokemonId }
    val itemFlow = remember(container) { container.items.observeItems() }
    val items by itemFlow.collectAsState(initial = emptyList())
    LaunchedEffect(container) {
        try { container.items.ensureIndex() }
        catch (e: Exception) { if (e is CancellationException) throw e }
    }
    PokemonMovesScreen(model, selected?.let { "${it.numberLabel} · ${it.displayName}" } ?: "Movimientos", onBack, items, onItemClick)
}
