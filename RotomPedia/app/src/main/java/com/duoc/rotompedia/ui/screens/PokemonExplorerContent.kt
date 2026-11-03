package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.*

@Composable
fun nationalNumberLabel(viewModel: PokemonViewModel, pokemonId: Int): String {
    val entries by viewModel.explorerEntries.collectAsState()
    return entries.firstOrNull { it.pokemonId == pokemonId }?.numberLabel ?: "N.º nacional pendiente"
}

@Composable
fun PokemonExplorerContent(
    controller: PokemonExplorerController,
    onChoose: (ExplorerPokemon) -> Unit,
    modifier: Modifier = Modifier,
    favoriteIds: Set<Int> = emptySet(),
    onFavorite: ((ExplorerPokemon) -> Unit)? = null
) {
    val filters by controller.filter.collectAsState()
    val result by controller.result.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.failure.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(emptySet<Int>()) }
    Column(modifier) {
        OutlinedTextField(value = filters.query, onValueChange = { controller.filter.value = filters.copy(query = it) },
            label = { Text("Nombre, forma o número nacional") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${result.speciesCount} especies · ${result.variantCount} variantes", fontSize = 12.sp)
            TextButton(onClick = { showFilters = !showFilters }) { Text(if (showFilters) "Cerrar filtros" else "Filtros") }
        }
        if (showFilters) ExplorerFilters(filters, onFavorite != null) { controller.filter.value = it }
        val summary = listOfNotNull(filters.category.takeIf { it != VarietyCategory.ALL }?.label,
            filters.generationId?.let { "Gen. $it" }, filters.regionalOrigin?.let { "Forma de ${it.replaceFirstChar(Char::uppercase)}" },
            filters.type?.let(::traducirTipo), "Favoritos".takeIf { filters.favoritesOnly })
        if (summary.isNotEmpty()) Text(summary.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text("Orden nacional", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!loading && result.groups.isEmpty() && error == null) {
            Text("No hay Pokémon con estos filtros.", modifier = Modifier.padding(vertical = 20.dp))
        }
        LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)) {
            items(result.groups, key = { "species_${it.speciesId}" }) { group ->
                val children = group.matching.filter { it.isVariant }
                val open = group.autoExpanded || group.speciesId in expanded
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        ExplorerRow(group.primary, group.primary in group.matching, favoriteIds, onFavorite,
                            onChoose, label = group.speciesName)
                        if (children.isNotEmpty()) {
                            TextButton(onClick = {
                                expanded = if (group.speciesId in expanded) expanded - group.speciesId else expanded + group.speciesId
                            }, enabled = !group.autoExpanded) {
                                Text(if (open) "▾ ${children.size} variantes" else "▸ ${children.size} variantes")
                            }
                            if (open) children.forEach { child ->
                                key(child.pokemonId) {
                                    Box(Modifier.padding(start = 12.dp, top = 4.dp)) {
                                        ExplorerRow(child, true, favoriteIds, onFavorite, onChoose)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplorerRow(pokemon: ExplorerPokemon, selectable: Boolean, favorites: Set<Int>,
    onFavorite: ((ExplorerPokemon) -> Unit)?, onChoose: (ExplorerPokemon) -> Unit, label: String = pokemon.displayName) {
    Row(Modifier.fillMaxWidth().clickable(enabled = selectable) { onChoose(pokemon) }.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        ExplorerSprite(pokemon)
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text("${pokemon.numberLabel} · $label", fontWeight = FontWeight.Bold,
                color = if (selectable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            if (label == pokemon.displayName && pokemon.isVariant) Text(pokemon.category.label, fontSize = 11.sp)
            if (selectable) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pokemon.types.forEach { BadgeTipo(it) }
            } else Text("Especie de los resultados siguientes", fontSize = 11.sp)
        }
        if (onFavorite != null && selectable) IconButton(onClick = { onFavorite(pokemon) }) {
            Icon(if (pokemon.pokemonId in favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorito de ${pokemon.displayName}")
        }
    }
}

@Composable
private fun ExplorerSprite(pokemon: ExplorerPokemon) {
    // Full source selection is deliberately reserved for C. Only the exact variety is requested.
    val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemon.pokemonId}.png"
    var failed by remember(url) { mutableStateOf(false) }
    Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (failed) Text("Sin\nimagen", fontSize = 9.sp)
        else AsyncImage(model = url, contentDescription = pokemon.displayName, modifier = Modifier.size(44.dp),
            onError = { failed = true; com.duoc.rotompedia.data.explorer.MissingSpriteRegistry.record(pokemon, url, it.result.throwable) })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExplorerFilters(filter: ExplorerFilter, allowFavorites: Boolean, onChange: (ExplorerFilter) -> Unit) {
    val generations = listOf("Kanto", "Johto", "Hoenn", "Sinnoh", "Teselia", "Kalos", "Alola", "Galar", "Paldea")
    Card(Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(10.dp)) {
            Text("Categoría", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                VarietyCategory.entries.forEach { category ->
                    FilterChip(selected = filter.category == category,
                        onClick = { onChange(filter.copy(category = category)) }, label = { Text(category.label, fontSize = 11.sp) })
                }
            }
            Text("Generación de la especie", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = filter.generationId == null, onClick = { onChange(filter.copy(generationId = null)) }, label = { Text("Todas") })
                generations.forEachIndexed { i, region ->
                    FilterChip(selected = filter.generationId == i + 1, onClick = { onChange(filter.copy(generationId = i + 1)) }, label = { Text(region, fontSize = 11.sp) })
                }
            }
            Text("Origen regional de la variante", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = filter.regionalOrigin == null, onClick = { onChange(filter.copy(regionalOrigin = null)) }, label = { Text("Todos") })
                listOf("alola", "galar", "hisui", "paldea").forEach { region ->
                    FilterChip(selected = filter.regionalOrigin == region, onClick = { onChange(filter.copy(regionalOrigin = region)) }, label = { Text(region.replaceFirstChar(Char::uppercase)) })
                }
            }
            Text("Tipo", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                allPokemonTypes.forEach { type ->
                    FilterChip(selected = (filter.type ?: "all") == type,
                        onClick = { onChange(filter.copy(type = type.takeUnless { it == "all" })) }, label = { Text(traducirTipo(type), fontSize = 11.sp) })
                }
            }
            if (allowFavorites) FilterChip(selected = filter.favoritesOnly,
                onClick = { onChange(filter.copy(favoritesOnly = !filter.favoritesOnly)) }, label = { Text("⭐ Favoritos") })
            TextButton(onClick = { onChange(ExplorerFilter()) }) { Text("Limpiar filtros y búsqueda") }
        }
    }
}
