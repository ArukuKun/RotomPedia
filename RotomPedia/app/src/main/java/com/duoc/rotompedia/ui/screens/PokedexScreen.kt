package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import com.duoc.rotompedia.viewmodel.PokemonViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(onPokemonClick: (String) -> Unit, onNavigateBack: () -> Unit, viewModelFactory: PokemonViewModelFactory) {
    val pokemonViewModel: PokemonViewModel = viewModel(factory = viewModelFactory)
    val scope = rememberCoroutineScope()
    val filters = rememberPokedexFilter()
    val explorer = remember(pokemonViewModel, filters) { pokemonViewModel.createExplorer(scope, nationalOnly = true, filter = filters) }
    val favorites by pokemonViewModel.favoritePokemonIds.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Pokédex") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD11527),
            titleContentColor = Color.White, navigationIconContentColor = Color.White))
    }) { padding ->
        PokemonExplorerContent(explorer, onChoose = { onPokemonClick(it.pokemonId.toString()) },
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            favoriteIds = favorites, onFavorite = {
                pokemonViewModel.toggleFavorite(it.pokemonId, it.displayName, it.pokemonId in favorites)
            })
    }
}
