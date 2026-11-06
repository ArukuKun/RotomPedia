package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.data.items.IndexedItem
import com.duoc.rotompedia.data.items.ItemLabels
import com.duoc.rotompedia.data.reference.*
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.viewmodel.PokemonMovesViewModel
import com.duoc.rotompedia.util.traducirTipo
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect

internal val learningMethods = listOf("level-up", "egg", "machine", "tutor")

internal data class LearningMove(val id: Int, val label: String, val reference: ReferenceMove?, val relations: List<LearnRow>)

/** Preserve every learning relation, including unknown references and non-primary methods. */
internal fun learningMoves(index: ReferenceIndex, rows: List<LearnRow>, edition: String?, method: String,
    query: String, type: String?, category: String?): List<LearningMove> {
    val references = index.moves.associateBy { it.id }
    return rows.filter { it.versionGroup == edition && it.method == method }.groupBy { it.moveId }.mapNotNull { (id, links) ->
        val ref = references[id]
        val name = ref?.displayName ?: links.first().slug.split('-').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        if ((type != null && ref?.type != type) || (category != null && ref?.category != category) ||
            !ReferencePresentation.matches(query, name, ref?.nameEn.orEmpty())) null
        else LearningMove(id, name, ref, links)
    }.sortedWith(if (method == "level-up") compareBy<LearningMove> { it.relations.minOf(LearnRow::level) }.thenBy { PokemonFormCatalog.normalize(it.label) }
        else compareBy<LearningMove> { PokemonFormCatalog.normalize(it.label) }.thenBy { it.id })
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PokemonMovesScreen(model: PokemonMovesViewModel, pokemonLabel: String, onBack: () -> Unit,
    items: List<IndexedItem> = emptyList(), onItemClick: (Int) -> Unit = {}) {
    val loaded by model.data.collectAsState()
    val loading by model.loading.collectAsState()
    val failed by model.failed.collectAsState()
    val edition by model.edition.collectAsState()
    val query by model.query.collectAsState()
    val type by model.type.collectAsState()
    val category by model.category.collectAsState()
    val initialPage by model.page.collectAsState()
    val pager = rememberPagerState(initialPage = initialPage.coerceIn(0, 3), pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var filters by rememberSaveable { mutableStateOf(false) }
    var other by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect(model::setPage) }
    RotomScaffold("Movimientos", onBack, actions = {
        IconButton({ filters = true }, Modifier.size(48.dp)) { Icon(Icons.Default.FilterList, "Filtrar movimientos") }
        IconButton(model::refresh, Modifier.size(48.dp), enabled = !loading) { Icon(Icons.Default.Refresh, if (failed) "Reintentar" else "Actualizar") }
    }) {
        Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            Text(pokemonLabel, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp))
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (failed) Text("No se pudieron cargar los movimientos. Inténtalo de nuevo.", color = MaterialTheme.colorScheme.error)
            loaded?.let { data ->
                val extras = data.learnset.rows.filter { it.versionGroup == edition && it.method !in learningMethods }.map { it.method }.distinct().sorted()
                Row(Modifier.fillMaxWidth()) {
                    LearningEditionChoice(edition, ReferencePresentation.groups(data.index, data.learnset.rows), Modifier.weight(1f), model::setEdition)
                    if (extras.isNotEmpty()) IconButton({ other = true }, Modifier.size(48.dp)) {
                        Icon(Icons.Default.MoreHoriz, "Otros métodos")
                    }
                }
                SecondaryTabRow(selectedTabIndex = pager.currentPage) {
                    learningMethods.forEachIndexed { i, method ->
                        Tab(selected = pager.currentPage == i, onClick = { selectLearningPage(scope, pager, i) },
                            text = { Text(if (method == "machine") "MT/MO" else ReferencePresentation.method(method),
                                style = MaterialTheme.typography.labelMedium) }, modifier = Modifier.testTag("learn_tab_$method"))
                    }
                }
                HorizontalPager(pager, Modifier.weight(1f).testTag("learn_pager"), key = { learningMethods[it] }) { page ->
                    val method = learningMethods[page]
                    val rows = remember(data, edition, method, query, type, category) {
                        learningMoves(data.index, data.learnset.rows, edition, method, query, type, category)
                    }
                    key(model.pokemonId, edition, method) {
                        val scroll = rememberLazyListState()
                        LazyColumn(state = scroll, modifier = Modifier.fillMaxSize().testTag("learn_list_$method"),
                            verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                            if (edition == null) item { Text("Selecciona una edición.") }
                            else if (rows.isEmpty()) item { Text("No hay movimientos con estos filtros.") }
                            items(rows, key = { it.id }) { row ->
                                LearningMoveCard(row, data.index, edition, items) { selected = row.id }
                            }
                        }
                    }
                }
            }
        }
    }
    loaded?.let { data ->
        selected?.let { id -> data.index.moves.firstOrNull { it.id == id }?.let { move ->
            MoveReferenceSheet(move, data.index, items, { selected = null }, onItemClick, initialGroup = edition)
        } }
        if (filters) RotomFilterSheet({ filters = false }) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text("Filtros de movimientos", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(query, model::setQuery, label = { Text("Buscar movimiento") }, modifier = Modifier.fillMaxWidth())
                ItemChoice("Tipo", type, data.index.moves.map { it.type }.distinct().sorted(), ::traducirTipo, model::setType)
                ItemChoice("Categoría", category, listOf("physical", "special", "status"), ReferencePresentation::category, model::setCategory)
                TextButton({ filters = false }) { Text("Aplicar y cerrar") }
            }
        }
        if (other) RotomFilterSheet({ other = false }) {
            LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(16.dp).testTag("learn_other_list"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("Otros métodos", style = MaterialTheme.typography.titleLarge); TextButton({ other = false }) { Text("Cerrar") } }
                val extras = data.learnset.rows.filter { it.versionGroup == edition && it.method !in learningMethods }.map { it.method }.distinct().sorted()
                extras.forEach { method ->
                    item(key = "heading_$method") { Text(ReferencePresentation.method(method), fontWeight = FontWeight.Bold) }
                    items(learningMoves(data.index, data.learnset.rows, edition, method, query, type, category), key = { "$method/${it.id}" }) { row ->
                        LearningMoveCard(row, data.index, edition, items) { selected = row.id }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun selectLearningPage(scope: CoroutineScope, pager: PagerState, page: Int) {
    // Launch only in response to the click, outside nested composable/collection lambdas.
    scope.launch { pager.animateScrollToPage(page) }
}

@Composable
private fun LearningEditionChoice(edition: String?, editions: List<String>, modifier: Modifier, onChoose: (String?) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    val label = edition?.let(ItemLabels::version) ?: "Selecciona una edición"
    Box(modifier) {
        OutlinedButton({ open = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .semantics { contentDescription = "Juego / edición: $label" }) {
            Text(label, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Default.ExpandMore, null)
        }
        DropdownMenu(open, { open = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            DropdownMenuItem(text = { Text("Sin seleccionar") }, onClick = { onChoose(null); open = false })
            editions.forEach { version -> DropdownMenuItem(text = { Text(ItemLabels.version(version)) },
                onClick = { onChoose(version); open = false }) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LearningMoveCard(row: LearningMove, index: ReferenceIndex, edition: String?, items: List<IndexedItem>, onClick: () -> Unit) {
    RotomCard(Modifier.fillMaxWidth().testTag("learn_move_${row.id}"), onClick = if (row.reference == null) null else onClick) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(row.label, fontWeight = FontWeight.Bold)
            if (row.reference?.nameEs == null) Text("Inglés", style = MaterialTheme.typography.labelSmall)
            if (row.reference == null) Text("Ficha no disponible", style = MaterialTheme.typography.labelMedium)
            else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgeTipo(row.reference.type)
                    MoveCategoryBadge(row.reference.category)
                }
                Text("Potencia: ${row.reference.power ?: "—"} · Precisión: ${row.reference.accuracy?.let { "$it %" } ?: "—"}")
            }
            val levels = row.relations.filter { it.method == "level-up" }.map { it.level }.distinct().sorted()
            if (levels.isNotEmpty()) Text("Nivel: ${levels.joinToString()}", style = MaterialTheme.typography.labelMedium)
            if (row.relations.any { it.method == "machine" }) {
                val machineItems = index.machines.filter { it.moveId == row.id && it.versionGroup == edition }.mapNotNull { link ->
                    items.firstOrNull { it.itemId == link.itemId }?.name
                }.distinct()
                if (machineItems.isNotEmpty()) Text(machineItems.joinToString(" · "), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
