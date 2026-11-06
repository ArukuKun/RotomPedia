package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.explorer.ExplorerPokemon
import com.duoc.rotompedia.data.explorer.ExplorerRegionalDex
import com.duoc.rotompedia.data.images.PokemonImageStyle
import com.duoc.rotompedia.ui.components.RotomCard

/** Flat national results only. The team picker retains its grouped variety browser. */
@Composable
internal fun PokedexResults(entries: List<ExplorerPokemon>, grid: Boolean, favorites: Set<Int>,
    onFavorite: ((ExplorerPokemon) -> Unit)?, onChoose: (ExplorerPokemon) -> Unit, modifier: Modifier = Modifier,
    speciesNames: Boolean = true, regionalOrder: ExplorerRegionalDex? = null) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    var previousGrid by rememberSaveable { mutableStateOf(grid) }
    var anchorId by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(grid) {
        if (grid != previousGrid && entries.isNotEmpty()) {
            val first = if (previousGrid) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
            val retained = anchorId?.let { id -> entries.indexOfFirst { it.pokemonId == id } } ?: -1
            // A grid starts at the beginning of a row, not necessarily at the chosen anchor.
            // Retain that identity on a round trip unless the user has moved to another row.
            val index = if (previousGrid && retained >= 0 && retained / 3 == first / 3) retained
                else first.coerceIn(entries.indices)
            anchorId = entries[index].pokemonId
            previousGrid = grid
            // Pixel offsets belong to different-sized cards; preserve the Pokémon, not those pixels.
            if (grid) gridState.scrollToItem(index) else listState.scrollToItem(index)
        }
    }
    // Some upstream variants share a localized name (e.g. two Toxtricity Gigamax).
    // Keep the verified technical identifier visible rather than invent a translation.
    val ambiguousNames = remember(entries) { entries.groupingBy { it.displayName }.eachCount().filterValues { it > 1 }.keys }
    if (grid) LazyVerticalGrid(columns = GridCells.Fixed(3), state = gridState,
        modifier = modifier.testTag("pokedex-grid"), contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.pokemonId }) { pokemon ->
            PokedexCard(pokemon, true, favorites, onFavorite, onChoose, speciesNames, pokemon.displayName in ambiguousNames, regionalOrder)
        }
    } else LazyColumn(state = listState, modifier = modifier.testTag("pokedex-list"),
        contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.pokemonId }) { pokemon ->
            PokedexCard(pokemon, false, favorites, onFavorite, onChoose, speciesNames, pokemon.displayName in ambiguousNames, regionalOrder)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PokedexCard(pokemon: ExplorerPokemon, grid: Boolean, favorites: Set<Int>,
    onFavorite: ((ExplorerPokemon) -> Unit)?, onChoose: (ExplorerPokemon) -> Unit, speciesNames: Boolean, showTechnicalName: Boolean,
    regionalOrder: ExplorerRegionalDex?) {
    val name = if (speciesNames && pokemon.isDefault) pokemon.speciesName else pokemon.displayName
    val regionalNumber = pokemon.regionalNumbers.firstOrNull { it.dex.id == regionalOrder?.id }
    RotomCard(Modifier.fillMaxWidth().testTag("pokemon_${pokemon.pokemonId}").clickable { onChoose(pokemon) }) {
        if (grid) Column(Modifier.fillMaxWidth().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PokedexSprite(pokemon)
            Text(pokemon.numberLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            regionalNumber?.let { Text("${it.dex.name} #${it.number.toString().padStart(3, '0')}", fontSize = 11.sp) }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            pokemon.originLabel?.let { Text(it, fontSize = 11.sp, textAlign = TextAlign.Center) }
            if (showTechnicalName) Text("Forma: ${pokemon.slug}", fontSize = 10.sp, textAlign = TextAlign.Center)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                pokemon.types.forEach { BadgeTipo(it) }
            }
            FavoriteAction(pokemon, favorites, onFavorite)
        } else Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).heightIn(min = 80.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PokedexSprite(pokemon)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(pokemon.numberLabel, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                regionalNumber?.let { Text("${it.dex.name} #${it.number.toString().padStart(3, '0')}", fontSize = 12.sp) }
                Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                pokemon.originLabel?.let { Text(it, fontSize = 12.sp) }
                if (showTechnicalName) Text("Forma: ${pokemon.slug}", fontSize = 11.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { BadgeTipo(it) }
                }
            }
            FavoriteAction(pokemon, favorites, onFavorite)
        }
    }
}

@Composable
private fun PokedexSprite(pokemon: ExplorerPokemon) {
    // Restore the 72dp artwork presentation from 2590bf5, with the identity-safe resolver.
    PokemonImage(pokemonId = pokemon.pokemonId, contentDescription = pokemon.displayName,
        style = PokemonImageStyle.ARTWORK, modifier = Modifier.size(72.dp).testTag("sprite_${pokemon.pokemonId}"))
}

@Composable
private fun FavoriteAction(pokemon: ExplorerPokemon, favorites: Set<Int>, onFavorite: ((ExplorerPokemon) -> Unit)?) {
    if (onFavorite != null) IconButton(onClick = { onFavorite(pokemon) }, modifier = Modifier.size(48.dp)) {
        Icon(if (pokemon.pokemonId in favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorito de ${pokemon.displayName}",
            tint = if (pokemon.pokemonId in favorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
