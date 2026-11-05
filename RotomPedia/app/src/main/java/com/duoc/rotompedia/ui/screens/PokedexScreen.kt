package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import com.duoc.rotompedia.viewmodel.PokemonViewModelFactory
import com.duoc.rotompedia.viewmodel.PokemonExplorerController
import com.duoc.rotompedia.data.explorer.ExplorerPokemon
import com.duoc.rotompedia.ui.components.RotomScaffold
import com.duoc.rotompedia.ui.components.RotomAppearanceAction
import com.duoc.rotompedia.ui.theme.RotomDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(onPokemonClick: (String) -> Unit, onNavigateBack: () -> Unit, viewModelFactory: PokemonViewModelFactory) {
    val pokemonViewModel: PokemonViewModel = viewModel(factory = viewModelFactory)
    val scope = rememberCoroutineScope()
    val filters = rememberPokedexFilter()
    val explorer = remember(pokemonViewModel, filters) { pokemonViewModel.createExplorer(scope, nationalOnly = true, filter = filters) }
    val favorites by pokemonViewModel.favoritePokemonIds.collectAsState()
    PokedexExplorerScreen(explorer, onNavigateBack, { onPokemonClick(it.pokemonId.toString()) }, favorites) {
        pokemonViewModel.toggleFavorite(it.pokemonId, it.displayName, it.pokemonId in favorites)
    }
}

/** Shared by the actual route and UI tests, including its navigation bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokedexExplorerScreen(explorer: PokemonExplorerController, onNavigateBack: () -> Unit,
    onChoose: (ExplorerPokemon) -> Unit, favoriteIds: Set<Int> = emptySet(), onFavorite: ((ExplorerPokemon) -> Unit)? = null) {
    val filterPanel = rememberSaveable { mutableStateOf(false) }
    RotomScaffold("Pokédex", onNavigateBack, actions = {
            RotomAppearanceAction()
            IconButton(onClick = { filterPanel.value = true }, modifier = Modifier.size(RotomDimensions.TouchTarget)) {
                Icon(Icons.Default.FilterList, "Filtrar Pokémon")
            }
    }) {
        PokemonExplorerContent(explorer, onChoose = onChoose,
            modifier = Modifier.fillMaxSize(),
            favoriteIds = favoriteIds, onFavorite = onFavorite, filterPanelState = filterPanel)
    }
}
