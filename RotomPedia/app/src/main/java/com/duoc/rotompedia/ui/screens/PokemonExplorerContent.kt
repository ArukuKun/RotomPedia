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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.explorer.*
import com.duoc.rotompedia.data.items.ItemLabels
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.*
import com.duoc.rotompedia.ui.components.RotomFilterSheet

@Composable
fun nationalNumberLabel(viewModel: PokemonViewModel, pokemonId: Int): String {
    val entries by viewModel.explorerEntries.collectAsState()
    return entries.firstOrNull { it.pokemonId == pokemonId }?.numberLabel ?: "N.º nacional pendiente"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonExplorerContent(
    controller: PokemonExplorerController,
    onChoose: (ExplorerPokemon) -> Unit,
    modifier: Modifier = Modifier,
    favoriteIds: Set<Int> = emptySet(),
    onFavorite: ((ExplorerPokemon) -> Unit)? = null,
    filterPanelState: MutableState<Boolean>? = null
) {
    val filters by controller.filter.collectAsState()
    val result by controller.result.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.failure.collectAsState()
    val entries by controller.entries.collectAsState()
    val eggGroups = remember(entries) { entries.flatMap { it.eggGroups }.distinctBy { it.slug }.sortedBy { it.label } }
    val localFilterPanel = rememberSaveable { mutableStateOf(false) }
    var showFilters by (filterPanelState ?: localFilterPanel)
    var grid by rememberSaveable { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(emptySet<Int>()) }
    Column(modifier) {
        OutlinedTextField(value = filters.query, onValueChange = { controller.filter.value = filters.copy(query = it) },
            label = { Text("Nombre, forma o número nacional") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (filterPanelState == null) OutlinedButton(onClick = { showFilters = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Filtrar")
            }
            Text(if (controller.nationalOnly) "${result.entries.size} resultados · ${result.speciesCount} especies"
                else "${result.speciesCount} especies · ${result.variantCount} variantes", fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp).weight(1f))
        }
        if (controller.nationalOnly) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !grid, onClick = { grid = false }, label = { Text("Lista") })
            FilterChip(selected = grid, onClick = { grid = true }, label = { Text("Cuadrícula") })
        }
        val summary = listOfNotNull(filters.category.takeIf { it != VarietyCategory.ALL }?.label,
            filters.generationId?.let { "Gen. $it" }, filters.regionalOrigin?.let {
                if (it == "hisui") "Hisui · formas y especies" else "Forma de ${it.replaceFirstChar(Char::uppercase)}" },
            filters.type?.let(::traducirTipo), filters.eggGroup?.let { slug -> eggGroups.firstOrNull { it.slug == slug }?.label ?: slug },
            "Favoritos".takeIf { filters.favoritesOnly })
        if (summary.isNotEmpty()) Text(summary.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        val regionalOrder = result.regionalOrder
        Text(if (regionalOrder == null) "Orden nacional" else "${regionalOrder.name} · ${regionalOrder.versionGroups.joinToString(" / ", transform = ItemLabels::version)}",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (result.missingRegionalNumbers > 0) Text("${result.missingRegionalNumbers} sin número regional · al final", fontSize = 11.sp)
        if (loading || (!result.ready && error == null)) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text("No se pudo cargar la lista de Pokémon.", color = MaterialTheme.colorScheme.error) }
        if (!loading && result.ready && result.groups.isEmpty() && error == null) {
            Text("No hay Pokémon con estos filtros.", modifier = Modifier.padding(vertical = 20.dp))
        }
        if (controller.nationalOnly && result.ready) key(filters) {
            PokedexResults(result.entries, grid, favoriteIds, onFavorite, onChoose,
                Modifier.fillMaxWidth().weight(1f), speciesNames = filters.category in setOf(VarietyCategory.ALL, VarietyCategory.SPECIES) && filters.regionalOrigin == null,
                regionalOrder = regionalOrder)
        } else if (!controller.nationalOnly) LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp),
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
    if (showFilters) ExplorerFilterDialog(filters, onFavorite != null, eggGroups, controller.nationalOnly,
        onDismiss = { showFilters = false }, onApply = { controller.filter.value = it; showFilters = false })
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
    Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        PokemonImage(pokemonId = pokemon.pokemonId, contentDescription = pokemon.displayName, modifier = Modifier.size(44.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExplorerFilterDialog(applied: ExplorerFilter, allowFavorites: Boolean, eggGroups: List<ExplorerEggGroup>,
    nationalOnly: Boolean, onDismiss: () -> Unit, onApply: (ExplorerFilter) -> Unit) {
    var filter by remember { mutableStateOf(applied) }
    var group by remember { mutableStateOf<String?>(null) }
    val onChange: (ExplorerFilter) -> Unit = { filter = it }
    val generations = listOf("Kanto", "Johto", "Hoenn", "Sinnoh", "Teselia", "Kalos", "Alola", "Galar / Hisui", "Paldea")
    RotomFilterSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().heightIn(max = 600.dp).padding(horizontal = 16.dp)) {
            Text(group ?: "Filtros de Pokémon", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            if (group == null) {
                val summaries = listOf(
                    "Categoría" to filter.category.label,
                    "Grupo huevo" to (eggGroups.firstOrNull { it.slug == filter.eggGroup }?.label ?: "Cualquier grupo huevo"),
                    "Generación" to (filter.generationId?.let { "Gen. $it · ${generations[it - 1]}" } ?: "Todas"),
                    "Origen regional" to (filter.regionalOrigin ?: "Todos"),
                    "Tipo" to (filter.type?.let(::traducirTipo) ?: "Todos")) +
                    if (allowFavorites) listOf("Favoritos" to if (filter.favoritesOnly) "Solo favoritos" else "Todos") else emptyList()
                summaries.forEach { (title, value) ->
                    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(value) },
                        trailingContent = { Text("›") }, modifier = Modifier.clickable { group = title }.heightIn(min = 48.dp))
                }
            }
            if (group == "Categoría") {
            Text("Categoría", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val categories = if (nationalOnly) pokedexCategories else
                    listOf(VarietyCategory.ALL, VarietyCategory.SPECIES) + pokedexCategories.drop(1)
                categories.forEach { category ->
                    FilterChip(selected = filter.category == category,
                        onClick = { onChange(filter.copy(category = category)) },
                        label = { Text(if (nationalOnly && category == VarietyCategory.ALL) "Nacional" else category.label, fontSize = 11.sp) })
                }
            }
            }
            if (group == "Grupo huevo") {
            Text("Grupo huevo", fontWeight = FontWeight.Bold)
            if (eggGroups.isEmpty()) Text("No disponible", fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = filter.eggGroup == null, onClick = { onChange(filter.copy(eggGroup = null)) }, label = { Text("Cualquier grupo huevo") })
                eggGroups.forEach { group ->
                    FilterChip(selected = filter.eggGroup == group.slug, onClick = { onChange(filter.copy(eggGroup = group.slug)) }, label = { Text(group.label) })
                }
            }
            }
            if (group == "Generación") {
            Text("Generación", fontWeight = FontWeight.Bold)
            Text("Generación de la especie, no de la forma regional.", style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = filter.generationId == null, onClick = { onChange(filter.copy(generationId = null)) }, label = { Text("Todas") })
                generations.forEachIndexed { i, region ->
                    FilterChip(selected = filter.generationId == i + 1, onClick = { onChange(filter.copy(generationId = i + 1)) }, label = { Text(region, fontSize = 11.sp) })
                }
            }
            }
            if (group == "Origen regional") {
            Text("Origen regional", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = filter.regionalOrigin == null, onClick = { onChange(filter.copy(regionalOrigin = null)) }, label = { Text("Todos") })
                listOf("alola", "galar", "hisui", "paldea").forEach { region ->
                    FilterChip(selected = filter.regionalOrigin == region, onClick = { onChange(filter.copy(regionalOrigin = region)) }, label = { Text(region.replaceFirstChar(Char::uppercase)) })
                }
            }
            }
            if (group == "Tipo") {
            Text("Tipo", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                allPokemonTypes.forEach { type ->
                    FilterChip(selected = (filter.type ?: "all") == type,
                        onClick = { onChange(filter.copy(type = type.takeUnless { it == "all" })) }, label = { Text(traducirTipo(type), fontSize = 11.sp) })
                }
            }
            }
            if (group == "Favoritos" && allowFavorites) FilterChip(selected = filter.favoritesOnly,
                onClick = { onChange(filter.copy(favoritesOnly = !filter.favoritesOnly)) }, label = { Text("⭐ Favoritos") })
            }
            if (group != null) TextButton(onClick = { group = null }) { Text("Confirmar selección") }
            else {
                TextButton(onClick = { filter = ExplorerFilter() }) { Text("Restablecer") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = { onApply(filter) }) { Text("Aplicar y cerrar") }
                }
            }
        }
    }
}
