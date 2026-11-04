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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.explorer.ExplorerPokemon

/** Flat national results only. The team picker retains its grouped variety browser. */
@Composable
internal fun PokedexResults(entries: List<ExplorerPokemon>, grid: Boolean, favorites: Set<Int>,
    onFavorite: ((ExplorerPokemon) -> Unit)?, onChoose: (ExplorerPokemon) -> Unit, modifier: Modifier = Modifier,
    speciesNames: Boolean = true) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    // Some upstream variants share a localized name (e.g. two Toxtricity Gigamax).
    // Keep the verified technical identifier visible rather than invent a translation.
    val ambiguousNames = remember(entries) { entries.groupingBy { it.displayName }.eachCount().filterValues { it > 1 }.keys }
    if (grid) LazyVerticalGrid(columns = GridCells.Fixed(3), state = gridState,
        modifier = modifier.testTag("pokedex-grid"), contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.pokemonId }) { pokemon ->
            PokedexCard(pokemon, true, favorites, onFavorite, onChoose, speciesNames, pokemon.displayName in ambiguousNames)
        }
    } else LazyColumn(state = listState, modifier = modifier.testTag("pokedex-list"),
        contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.pokemonId }) { pokemon ->
            PokedexCard(pokemon, false, favorites, onFavorite, onChoose, speciesNames, pokemon.displayName in ambiguousNames)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PokedexCard(pokemon: ExplorerPokemon, grid: Boolean, favorites: Set<Int>,
    onFavorite: ((ExplorerPokemon) -> Unit)?, onChoose: (ExplorerPokemon) -> Unit, speciesNames: Boolean, showTechnicalName: Boolean) {
    val name = if (speciesNames && pokemon.isDefault) pokemon.speciesName else pokemon.displayName
    Card(Modifier.fillMaxWidth().testTag("pokemon_${pokemon.pokemonId}").clickable { onChoose(pokemon) },
        shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        if (grid) Column(Modifier.fillMaxWidth().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PokedexSprite(pokemon)
            Text(pokemon.numberLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
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
                Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
    // Restore RotomPedia's 72dp sprite from 2590bf5, retaining the v3.4 identity-safe resolver.
    PokemonImage(pokemonId = pokemon.pokemonId, contentDescription = pokemon.displayName,
        modifier = Modifier.size(72.dp).testTag("sprite_${pokemon.pokemonId}"))
}

@Composable
private fun FavoriteAction(pokemon: ExplorerPokemon, favorites: Set<Int>, onFavorite: ((ExplorerPokemon) -> Unit)?) {
    if (onFavorite != null) IconButton(onClick = { onFavorite(pokemon) }, modifier = Modifier.size(48.dp)) {
        Icon(if (pokemon.pokemonId in favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorito de ${pokemon.displayName}",
            tint = if (pokemon.pokemonId in favorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
