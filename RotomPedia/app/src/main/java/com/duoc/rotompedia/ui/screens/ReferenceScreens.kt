package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.duoc.rotompedia.data.PokemonMove
import com.duoc.rotompedia.data.PokemonMoveData
import com.duoc.rotompedia.data.items.IndexedItem
import com.duoc.rotompedia.data.items.ItemLabels
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.di.AppContainer
import com.duoc.rotompedia.util.traducirTipo
import kotlinx.coroutines.CancellationException
import coil.ImageLoader
import coil.imageLoader
import com.duoc.rotompedia.ui.components.MoveCategoryBadge
import com.duoc.rotompedia.ui.components.RotomCard
import com.duoc.rotompedia.ui.components.RotomPageScaffold as Scaffold
import com.duoc.rotompedia.ui.components.RotomPageTopBar as TopAppBar
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun resolvedLegacyMove(name: String?): PokemonMove? {
    val revision by PokemonMoveData.revision.collectAsState()
    return remember(name, revision) { name?.let(PokemonMoveData::findMove) }
}

@Composable
private fun SourceText(texts: List<ReferenceText>, group: String?, effectEs: String?, effectEn: String?) {
    val selected = ReferencePresentation.text(texts, group)
    selected?.let {
        Text("Descripción", fontWeight = FontWeight.Bold)
        Text(if (it.language == "es") it.text else "Inglés: ${it.text}")
    }
    val effect = effectEs?.takeIf(String::isNotBlank) ?: effectEn?.takeIf(String::isNotBlank)?.let { "Inglés: $it" }
    effect?.let { Text("Efecto", fontWeight = FontWeight.Bold); Text(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceBrowserScreen(abilities: Boolean, onBack: () -> Unit, onPokemonClick: (String) -> Unit,
    onItemClick: (Int) -> Unit, initialMoveId: Int = 0, initialAbilityId: Int = 0) {
    val container = AppContainer.get(LocalContext.current)
    var index by remember { mutableStateOf<ReferenceIndex?>(null) }
    var error by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var type by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<String?>(null) }
    var move by remember { mutableStateOf<ReferenceMove?>(null) }
    var ability by remember { mutableStateOf<ReferenceAbility?>(null) }
    var count by remember(query, type, category) { mutableIntStateOf(40) }
    val itemFlow = remember(container) { container.items.observeItems() }
    val items by itemFlow.collectAsState(initial = emptyList())
    val pokemonFlow = remember(container) { container.pokemon.observeExplorer() }
    val pokemon by pokemonFlow.collectAsState(initial = emptyList())
    LaunchedEffect(retry, initialMoveId, initialAbilityId) {
        error = false
        try {
            val loaded = container.moves.index()
            index = loaded
            container.items.ensureIndex()
            move = loaded.moves.firstOrNull { it.id == initialMoveId }
            ability = loaded.abilities.firstOrNull { it.id == initialAbilityId }
            if ((initialMoveId > 0 && move == null) || (initialAbilityId > 0 && ability == null)) error = true
        } catch (e: Exception) { if (e is CancellationException) throw e; error = true }
    }
    Scaffold(topBar = { TopAppBar(title = { Text(if (abilities) "Habilidades" else "Movimientos", maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = {
        IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Buscar por nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            val loaded = index
            if (error) { Text("No se pudo cargar el contenido."); TextButton({ retry++ }) { Text("Reintentar") } }
            if (loaded == null && !error) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (loaded != null) {
                if (!abilities) {
                    ItemChoice("Tipo", type, loaded.moves.map { it.type }.distinct().sorted(), ::traducirTipo) { type = it }
                    ItemChoice("Categoría", category, listOf("physical", "special", "status"), ReferencePresentation::category) { category = it }
                    val rows = remember(loaded, query, type, category) { ReferencePresentation.filterMoves(loaded, query, type, category) }
                    Text("${rows.size} movimientos · mostrando ${minOf(count, rows.size)}")
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rows.take(count), key = { it.id }) { row -> MoveCard(row, onClick = { move = row }) }
                        if (count < rows.size) item { TextButton({ count += 40 }) { Text("Cargar 40 más") } }
                    }
                } else {
                    val rows = remember(loaded, query) { loaded.abilities.filter { AbilityNames.matches(it, query) }.sortedBy { it.displayName } }
                    Text("${rows.size} habilidades · mostrando ${minOf(count, rows.size)}")
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rows.take(count), key = { it.id }) { row -> Card(Modifier.fillMaxWidth().clickable { ability = row }) {
                            Column(Modifier.padding(12.dp)) { Text(row.displayName, fontWeight = FontWeight.Bold) }
                        } }
                        if (count < rows.size) item { TextButton({ count += 40 }) { Text("Cargar 40 más") } }
                    }
                }
            }
        }
    }
    val loaded = index
    if (loaded != null) {
        move?.let { MoveReferenceSheet(it, loaded, items, { move = null }, onItemClick) }
        ability?.let { row -> AbilityReferenceSheet(row, loaded, pokemon.associate { it.pokemonId to "${it.numberLabel} · ${it.displayName}" }, { ability = null }, onPokemonClick) }
    }
}

@Composable
internal fun MoveCard(move: ReferenceMove, modifier: Modifier = Modifier, description: ReferenceText? = null,
    annotations: String? = null, onClick: (() -> Unit)?) {
    RotomCard(modifier.fillMaxWidth().then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(move.displayName, fontWeight = FontWeight.Bold)
            if (move.nameEs == null) Text("Inglés", fontSize = 11.sp)
            Text("${traducirTipo(move.type)} · ${ReferencePresentation.category(move.category)} · prioridad ${move.priority}", fontSize = 12.sp)
            MoveCategoryBadge(move.category)
            Text("Pot: ${move.power ?: "—"} · Prec: ${move.accuracy?.let { "$it%" } ?: "—"} · PP: ${move.pp ?: "—"}", fontSize = 12.sp)
            annotations?.let { Text(it, fontSize = 12.sp) }
            description?.let { Text(if (it.language == "es") it.text else "Inglés: ${it.text}", fontSize = 12.sp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoveReferenceSheet(move: ReferenceMove, index: ReferenceIndex, items: List<IndexedItem> = emptyList(),
    onDismiss: () -> Unit, onItemClick: ((Int) -> Unit)? = null, initialGroup: String? = null) {
    var group by rememberSaveable(move.id) { mutableStateOf(initialGroup) }
    val selectedGroup = group
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(move.displayName, style = MaterialTheme.typography.titleLarge)
            TextButton(onDismiss) { Text("Cerrar ficha") }
            MoveCard(move, onClick = null)
            val machineLinks = index.machines.filter { it.moveId == move.id }
            ItemChoice("Edición", selectedGroup, index.versions.sortedBy { it.order }.map { it.slug }.filter { g -> move.texts.any { it.versionGroup == g } || machineLinks.any { it.versionGroup == g } }, ItemLabels::version) { group = it }
            SourceText(move.texts, selectedGroup, move.effectEs, move.effectEn)
            if (selectedGroup != null) {
                val links = machineLinks.filter { it.versionGroup == selectedGroup }.distinctBy { it.itemId }
                if (links.isNotEmpty()) Text("MT / MO", fontWeight = FontWeight.Bold)
                links.forEach { link ->
                    val label = items.firstOrNull { it.itemId == link.itemId }?.name ?: "Ver objeto"
                    if (onItemClick == null) Text(label)
                    else TextButton({ onItemClick(link.itemId) }) { Text(label) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AbilityReferenceSheet(ability: ReferenceAbility, index: ReferenceIndex, pokemonLabels: Map<Int, String>,
    onDismiss: () -> Unit, onPokemonClick: (String) -> Unit) {
    var group by remember(ability.id) { mutableStateOf<String?>(null) }
    var count by remember(ability.id) { mutableIntStateOf(40) }
    val selectedGroup = group
    val visibleCount = count
    val summary = remember(index, ability.id) { AbilityPresentation.holders(index, ability.id) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(ability.displayName, style = MaterialTheme.typography.titleLarge)
            if (ability.nameEs == null) Text("Inglés")
            AbilityNames.revision(ability)?.let { revision ->
                Text("Antes de la novena generación: ${revision.previous}", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onDismiss) { Text("Cerrar ficha") }
            ItemChoice("Edición del texto", selectedGroup, ability.texts.map { it.versionGroup }.distinct(), ItemLabels::version) { group = it }
            SourceText(ability.texts, selectedGroup, ability.effectEs, ability.effectEn)
            AbilityHolderCounts(summary)
            if (summary.holders.isEmpty()) Text("No hay Pokémon para mostrar.")
            summary.holders.take(visibleCount).forEach { holder ->
                AbilityHolderRow(holder, pokemonLabels[holder.pokemonId] ?: "Nombre no disponible", onPokemonClick)
            }
            if (visibleCount < summary.total) TextButton({ count += 40 }) { Text("Cargar 40 más") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AbilityHolderCounts(summary: AbilityHolderSummary) {
    Text("Variedades asociadas · ${summary.total}", fontWeight = FontWeight.Bold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Normal: ${summary.normal}")
        Text("Oculta: ${summary.hidden}")
    }
    if (summary.both > 0) Text("Una variedad puede figurar en ambos roles.", style = MaterialTheme.typography.labelSmall)
}

@Composable
internal fun AbilityPokemonRow(link: AbilityLink, label: String, onPokemonClick: (String) -> Unit,
    imageLoader: ImageLoader = LocalContext.current.imageLoader) {
    AbilityHolderRow(AbilityHolder(link.pokemonId, if (link.hidden) emptyList() else listOf(link.slot),
        if (link.hidden) listOf(link.slot) else emptyList()), label, onPokemonClick, imageLoader)
}

@Composable
internal fun AbilityHolderRow(holder: AbilityHolder, label: String, onPokemonClick: (String) -> Unit,
    imageLoader: ImageLoader = LocalContext.current.imageLoader) {
    TextButton({ onPokemonClick(holder.pokemonId.toString()) }, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PokemonImage(pokemonId = holder.pokemonId, contentDescription = null, imageLoader = imageLoader,
                modifier = Modifier.size(40.dp).testTag("ability_sprite_${holder.pokemonId}_${holder.firstSlot}"))
            Spacer(Modifier.width(8.dp))
            Text("$label · ${holder.roleLabel}", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MoveLearnsetPane(pokemonId: Int, modifier: Modifier = Modifier, repository: MoveRepository? = null,
    onSelect: ((PokemonMove) -> Unit)? = null) {
    val repo = repository ?: AppContainer.get(LocalContext.current).moves
    var index by remember(repo) { mutableStateOf<ReferenceIndex?>(null) }
    var learnset by remember(pokemonId, repo) { mutableStateOf<LearnsetResult?>(null) }
    var error by remember(pokemonId, repo) { mutableStateOf<String?>(null) }
    var loading by remember(pokemonId, repo) { mutableStateOf(true) }
    var attempt by remember(pokemonId, repo) { mutableIntStateOf(0) }
    var group by remember(pokemonId) { mutableStateOf<String?>(null) }
    var method by remember(pokemonId) { mutableStateOf<String?>(null) }
    var query by remember(pokemonId) { mutableStateOf("") }
    var type by remember(pokemonId) { mutableStateOf<String?>(null) }
    var category by remember(pokemonId) { mutableStateOf<String?>(null) }
    var general by remember(pokemonId) { mutableStateOf(false) }
    var count by remember(pokemonId, group, method, query, type, category, general) { mutableIntStateOf(40) }
    var selected by remember(pokemonId) { mutableStateOf<ReferenceMove?>(null) }
    var showFilters by remember(pokemonId) { mutableStateOf(false) }
    LaunchedEffect(repo, pokemonId, attempt) {
        loading = true; error = null
        try { index = repo.index(); learnset = repo.learnset(pokemonId, refresh = attempt > 0) }
        catch (e: Exception) { if (e is CancellationException) throw e; error = "No se pudieron cargar los movimientos. Comprueba tu conexión e inténtalo de nuevo." }
        finally { loading = false }
    }
    val loaded = index
    val rows = learnset?.data?.rows.orEmpty()
    val learned = remember(rows, group, method) { ReferencePresentation.learned(rows, group, method) }
    val visible = remember(loaded, query, type, category, general, learned) {
        loaded?.let { ReferencePresentation.filterMoves(it, query, type, category).filter { move -> general || move.id in learned } }.orEmpty()
    }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item(key = "controls") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(query, { query = it }, label = { Text("Buscar movimiento") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                TextButton({ attempt++ }, enabled = !loading) { Text(if (error != null) "Reintentar" else "Actualizar") }
                if (loaded != null) {
                    ItemChoice("Edición de aprendizaje", group, ReferencePresentation.groups(loaded, rows), ItemLabels::version) { group = it; method = null }
                    ItemChoice("Método", method, rows.filter { it.versionGroup == group }.map { it.method }.distinct(), ReferencePresentation::method) { method = it }
                    TextButton({ showFilters = !showFilters }) { Text(if (showFilters) "Ocultar filtros" else "Filtros de tipo / categoría") }
                    if (showFilters) {
                        ItemChoice("Tipo", type, loaded.moves.map { it.type }.distinct().sorted(), ::traducirTipo) { type = it }
                        ItemChoice("Categoría", category, listOf("physical", "special", "status"), ReferencePresentation::category) { category = it }
                    }
                    Row {
                        Checkbox(general, { general = it })
                        Text("Catálogo general", fontSize = 11.sp)
                    }
                    Text("${visible.size} movimientos · mostrando ${minOf(count, visible.size)}", fontSize = 11.sp)
                    if (!general && group == null) Text("Selecciona una edición.", fontSize = 12.sp)
                    if (visible.isEmpty() && !loading && (general || group != null)) Text("Sin movimientos registrados con estos filtros.")
                }
            }
        }
        items(visible.take(count), key = { it.id }) { move ->
            val annotation = if (general) null else learned[move.id].orEmpty().joinToString(" · ") {
                "${ReferencePresentation.method(it.method)}${if (it.method == "level-up") ": nivel ${it.level}" else ""}"
            }
            MoveCard(move, description = ReferencePresentation.text(move.texts, group), annotations = annotation,
                onClick = { if (onSelect != null) onSelect(ReferencePresentation.legacy(move, group)) else selected = move })
            if (onSelect != null) TextButton({ selected = move }) { Text("Ver ficha de ${move.displayName}") }
        }
        if (count < visible.size) item { TextButton({ count += 40 }) { Text("Cargar 40 más") } }
    }
    if (loaded != null) selected?.let { MoveReferenceSheet(it, loaded, onDismiss = { selected = null }) }
}

@Composable
fun MovePickerDialog(slotIndex: Int, pokemonId: Int, onDismiss: () -> Unit, onMoveSelected: (PokemonMove) -> Unit, onClear: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(0.94f)) {
            Column(Modifier.padding(12.dp)) {
                Text("Elegir movimiento $slotIndex", style = MaterialTheme.typography.titleMedium)
                Row { TextButton(onDismiss) { Text("Cerrar") }; TextButton(onClear) { Text("Dejar vacío") } }
                MoveLearnsetPane(pokemonId, Modifier.weight(1f), onSelect = onMoveSelected)
            }
        }
    }
}

@Composable
fun PokemonAbilitiesSection(pokemonId: Int, onPokemonClick: (String) -> Unit) {
    val container = AppContainer.get(LocalContext.current)
    var selected by remember(pokemonId) { mutableStateOf<Pair<ReferenceAbility, ReferenceIndex>?>(null) }
    val pokemonFlow = remember(container) { container.pokemon.observeExplorer() }
    val pokemon by pokemonFlow.collectAsState(initial = emptyList())
    PokemonAbilitySummary(pokemonId, container.abilities) { ability, index -> selected = ability to index }
    selected?.let { (ability, index) ->
        AbilityReferenceSheet(ability, index,
            pokemon.associate { row -> row.pokemonId to "${row.numberLabel} · ${row.displayName}" },
            { selected = null }, onPokemonClick)
    }
}

@Composable
internal fun PokemonAbilitySummary(pokemonId: Int, repository: AbilityRepository,
    onChoose: (ReferenceAbility, ReferenceIndex) -> Unit) {
    var index by remember(pokemonId) { mutableStateOf<ReferenceIndex?>(null) }
    var links by remember(pokemonId) { mutableStateOf<List<AbilityLink>>(emptyList()) }
    var error by remember(pokemonId) { mutableStateOf(false) }
    var retry by remember(pokemonId) { mutableIntStateOf(0) }
    LaunchedEffect(pokemonId, repository, retry) {
        try {
            error = false
            index = null
            links = emptyList()
            val loaded = repository.index()
            val related = repository.forPokemon(pokemonId)
            require(related.all { link -> link.pokemonId == pokemonId && loaded.abilities.any { it.id == link.abilityId } })
            links = related.sortedBy { it.slot }
            index = loaded
        }
        catch (e: Exception) { if (e is CancellationException) throw e; error = true }
    }
    Text("Habilidades", fontWeight = FontWeight.SemiBold)
    if (error) { Text("No se pudieron consultar las habilidades."); TextButton({ retry++ }) { Text("Reintentar habilidades") } }
    val loaded = index
    if (loaded == null && !error) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (loaded != null) {
        if (links.isEmpty()) Text("No hay habilidades para mostrar.")
        links.forEach { link -> loaded.abilities.firstOrNull { it.id == link.abilityId }?.let { ability ->
            TextButton({ onChoose(ability, loaded) }, modifier = Modifier.fillMaxWidth()) {
                Text(ability.displayName + (if (link.hidden) " (oculta)" else "") +
                    (if (ability.nameEs == null) " · inglés" else ""), modifier = Modifier.fillMaxWidth())
            }
        } }
    }
}
