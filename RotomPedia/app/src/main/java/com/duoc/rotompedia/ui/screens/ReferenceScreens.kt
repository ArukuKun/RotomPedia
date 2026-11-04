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
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun resolvedLegacyMove(name: String?): PokemonMove? {
    val revision by PokemonMoveData.revision.collectAsState()
    return remember(name, revision) { name?.let(PokemonMoveData::findMove) }
}

@Composable
private fun SourceText(texts: List<ReferenceText>, group: String?, effectEs: String?, effectEn: String?) {
    Text("Descripción por edición", fontWeight = FontWeight.Bold)
    val selected = ReferencePresentation.text(texts, group)
    Text(when {
        group == null -> "Selecciona una edición. No se combinan textos de juegos distintos."
        selected == null -> "Sin texto español o inglés documentado para esta edición."
        selected.language == "es" -> selected.text
        else -> "Sin traducción española para esta edición. Original en inglés: ${selected.text}"
    })
    Text("Efecto de referencia · no histórico por edición", fontWeight = FontWeight.Bold)
    Text(effectEs?.takeIf(String::isNotBlank) ?: effectEn?.takeIf(String::isNotBlank)?.let { "Sin efecto español verificado. Original en inglés: $it" } ?: "Sin efecto documentado en español o inglés.")
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
    Scaffold(topBar = { TopAppBar(title = { Text(if (abilities) "Habilidades" else "Movimientos") }, navigationIcon = {
        IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Buscar nombre, alias o ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            val loaded = index
            if (error) { Text("Referencia no disponible o identidad desconocida."); TextButton({ retry++ }) { Text("Reintentar") } }
            if (loaded == null && !error) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (loaded != null) {
                Text("Datos locales PokeAPI · revisión ${loaded.revision.take(12)}", fontSize = 11.sp)
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
                    val rows = remember(loaded, query) { loaded.abilities.filter { ReferencePresentation.matches(query, it.displayName, it.nameEn, it.slug, it.id.toString()) }.sortedBy { it.displayName } }
                    Text("${rows.size} habilidades · mostrando ${minOf(count, rows.size)}")
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rows.take(count), key = { it.id }) { row -> Card(Modifier.fillMaxWidth().clickable { ability = row }) {
                            Column(Modifier.padding(12.dp)) { Text(row.displayName, fontWeight = FontWeight.Bold); Text("ID habilidad ${row.id} · ${if (row.mainSeries) "Serie principal" else "Fuera de serie principal"}", fontSize = 12.sp) }
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
    Card(modifier.fillMaxWidth().then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(move.displayName, fontWeight = FontWeight.Bold)
            if (move.nameEs == null) Text("Nombre español no disponible · ${move.slug}", fontSize = 11.sp)
            Text("${traducirTipo(move.type)} · ${ReferencePresentation.category(move.category)} · prioridad ${move.priority}", fontSize = 12.sp)
            Text("Pot: ${move.power ?: "—"} · Prec: ${move.accuracy?.let { "$it%" } ?: "—"} · PP: ${move.pp ?: "—"}", fontSize = 12.sp)
            annotations?.let { Text(it, fontSize = 12.sp) }
            description?.let { Text(if (it.language == "es") it.text else "Original en inglés: ${it.text}", fontSize = 12.sp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoveReferenceSheet(move: ReferenceMove, index: ReferenceIndex, items: List<IndexedItem> = emptyList(),
    onDismiss: () -> Unit, onItemClick: ((Int) -> Unit)? = null) {
    var group by remember(move.id) { mutableStateOf<String?>(null) }
    val selectedGroup = group
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(move.displayName, style = MaterialTheme.typography.titleLarge)
            Text("${move.slug} · ID movimiento ${move.id}", fontSize = 12.sp)
            TextButton(onDismiss) { Text("Cerrar ficha") }
            MoveCard(move, onClick = null)
            Text("Valores base actuales de la fuente. Elegir edición filtra descripción y MT, no reconstruye estadísticas históricas.", fontSize = 11.sp)
            val machineLinks = index.machines.filter { it.moveId == move.id }
            ItemChoice("Edición", selectedGroup, index.versions.sortedBy { it.order }.map { it.slug }.filter { g -> move.texts.any { it.versionGroup == g } || machineLinks.any { it.versionGroup == g } }, ItemLabels::version) { group = it }
            SourceText(move.texts, selectedGroup, move.effectEs, move.effectEn)
            Text("Objetos MT / MO relacionados", fontWeight = FontWeight.Bold)
            if (selectedGroup == null) Text("Selecciona una edición para consultar sus MT/MO.")
            else {
                val links = machineLinks.filter { it.versionGroup == selectedGroup }.distinctBy { it.itemId }
                if (links.isEmpty()) Text("Sin relación MT/MO documentada para esta edición.")
                links.forEach { link ->
                    val label = items.firstOrNull { it.itemId == link.itemId }?.name ?: "Objeto ID ${link.itemId}"
                    if (onItemClick == null) Text("$label · ficha disponible en la enciclopedia de objetos")
                    else TextButton({ onItemClick(link.itemId) }) { Text(label) }
                }
            }
            Text("Fuente: CSV oficiales PokeAPI · ${index.revision.take(12)}. Sin certificación de legalidad competitiva.", fontSize = 11.sp)
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
    val links = index.pokemonAbilities.filter { it.abilityId == ability.id }.sortedBy { it.pokemonId }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(ability.displayName, style = MaterialTheme.typography.titleLarge)
            Text("${ability.slug} · ID habilidad ${ability.id}")
            if (ability.nameEs == null) Text("Nombre español no disponible.")
            TextButton(onDismiss) { Text("Cerrar ficha") }
            ItemChoice("Edición del texto", selectedGroup, ability.texts.map { it.versionGroup }.distinct(), ItemLabels::version) { group = it }
            SourceText(ability.texts, selectedGroup, ability.effectEs, ability.effectEn)
            Text("Pokémon asociados · ${links.size}", fontWeight = FontWeight.Bold)
            Text("Relaciones actuales de la fuente por variedad y slot, no historial de habilidades por juego. La edición seleccionada solo filtra texto.", fontSize = 11.sp)
            if (links.isEmpty()) Text("Sin relación documentada en el índice nacional incluido; no implica inexistencia en todos los juegos.")
            links.take(visibleCount).forEach { link ->
                TextButton({ onPokemonClick(link.pokemonId.toString()) }) {
                    Text("${pokemonLabels[link.pokemonId] ?: "ID técnico ${link.pokemonId}"} · ${if (link.hidden) "Oculta" else "Normal"} · slot ${link.slot}")
                }
            }
            if (visibleCount < links.size) TextButton({ count += 40 }) { Text("Cargar 40 más") }
            Text("Fuente: PokeAPI · ${index.revision.take(12)}", fontSize = 11.sp)
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
        catch (e: Exception) { if (e is CancellationException) throw e; error = "No se pudo consultar el aprendizaje de esta variedad. Sin copia utilizable, se necesita conexión." }
        finally { loading = false }
    }
    val loaded = index
    val rows = learnset?.data?.rows.orEmpty()
    val learned = remember(rows, group, method) { ReferencePresentation.learned(rows, group, method) }
    val visible = remember(loaded, query, type, category, general, learned) {
        loaded?.let { ReferencePresentation.filterMoves(it, query, type, category).filter { move -> general || move.id in learned } }.orEmpty()
    }
    val unknown = learned.keys - loaded?.moves.orEmpty().map { it.id }.toSet()
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item(key = "controls") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(query, { query = it }, label = { Text("Buscar movimiento") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                TextButton({ attempt++ }) { Text("Actualizar / reintentar aprendizaje") }
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
                        Text("Catálogo general (no acredita aprendizaje)", fontSize = 11.sp)
                    }
                    if (unknown.isNotEmpty()) Text("Movimientos no incluidos aún en la referencia: ${unknown.sorted().joinToString()}. No se inventan sus datos.", fontSize = 11.sp)
                    if (learnset?.offline == true) Text("No se pudo actualizar: copia guardada del aprendizaje.", fontSize = 11.sp)
                    learnset?.let { Text("Fuente: PokeAPI pokemon/${it.data.pokemonId} · guardado ${java.time.Instant.ofEpochMilli(it.fetchedAt)}", fontSize = 10.sp) }
                    Text("${visible.size} movimientos · mostrando ${minOf(count, visible.size)}. Sin certificación de legalidad.", fontSize = 11.sp)
                    if (!general && group == null) Text("Selecciona una edición; no se mezclan generaciones.", fontSize = 12.sp)
                    if (visible.isEmpty() && !loading && (general || group != null)) Text("Sin movimientos registrados con estos filtros.")
                }
            }
        }
        items(visible.take(count), key = { it.id }) { move ->
            val annotation = if (general) "Catálogo general, no aprendizaje verificado" else learned[move.id].orEmpty().joinToString(" · ") {
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
    var expanded by remember(pokemonId) { mutableStateOf(false) }
    var index by remember(pokemonId) { mutableStateOf<ReferenceIndex?>(null) }
    var links by remember(pokemonId) { mutableStateOf<List<AbilityLink>>(emptyList()) }
    var error by remember(pokemonId) { mutableStateOf(false) }
    var retry by remember(pokemonId) { mutableIntStateOf(0) }
    var selected by remember(pokemonId) { mutableStateOf<ReferenceAbility?>(null) }
    val pokemonFlow = remember(container) { container.pokemon.observeExplorer() }
    val pokemon by pokemonFlow.collectAsState(initial = emptyList())
    LaunchedEffect(pokemonId, expanded, retry) {
        if (expanded) try { error = false; index = container.abilities.index(); links = container.abilities.forPokemon(pokemonId) }
        catch (e: Exception) { if (e is CancellationException) throw e; error = true }
    }
    Text("Habilidades", style = MaterialTheme.typography.titleLarge)
    TextButton({ expanded = !expanded }) { Text(if (expanded) "Ocultar habilidades" else "Consultar habilidades") }
    if (!expanded) return
    if (error) { Text("No se pudieron consultar las habilidades."); TextButton({ retry++ }) { Text("Reintentar habilidades") } }
    val loaded = index
    if (loaded == null && !error) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (loaded != null) {
        Text("Relación actual por variedad; no historial de habilidades por edición.", fontSize = 11.sp)
        if (links.isEmpty() && !error) Text("Sin habilidades documentadas para esta variedad en el índice incluido.")
        links.forEach { link -> loaded.abilities.firstOrNull { it.id == link.abilityId }?.let { ability ->
            TextButton({ selected = ability }) { Text("${ability.displayName} · ${if (link.hidden) "Oculta" else "Normal"} · slot ${link.slot}") }
        } }
        selected?.let { AbilityReferenceSheet(it, loaded, pokemon.associate { row -> row.pokemonId to "${row.numberLabel} · ${row.displayName}" }, { selected = null }, onPokemonClick) }
    }
}
