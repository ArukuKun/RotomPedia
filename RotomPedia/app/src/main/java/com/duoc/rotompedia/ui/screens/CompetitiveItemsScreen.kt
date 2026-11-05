package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.di.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

internal val ItemFilterSaver = listSaver<ItemFilter, Any>(
    save = { listOf(it.query, it.category.orEmpty(), it.attribute.orEmpty(), it.editorialOnly, it.pocketId ?: 0) },
    restore = { ItemFilter(it[0] as String, (it[1] as String).ifBlank { null }, (it[2] as String).ifBlank { null },
        it[3] as Boolean, (it[4] as Int).takeUnless { id -> id == 0 }) })

private data class ItemBrowserResult(val filter: ItemFilter, val pockets: ItemPocketIndex,
    val rows: List<IndexedItem>, val total: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitiveItemsScreen(onNavigateBack: () -> Unit, onPokemonClick: (String) -> Unit = {}, initialSlug: String? = null,
    itemRepository: ItemRepository? = null, initialItemId: Int = 0, onMoveClick: (Int) -> Unit = {}) {
    val repository = itemRepository ?: AppContainer.get(LocalContext.current).items
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var filter by rememberSaveable(stateSaver = ItemFilterSaver) { mutableStateOf(ItemFilter()) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val pocketFlow = remember(repository) { MutableStateFlow(ItemPocketIndex()) }
    val pockets by pocketFlow.collectAsState()
    var selected by remember { mutableStateOf<IndexedItem?>(null) }
    var reference by remember { mutableStateOf<com.duoc.rotompedia.data.reference.ReferenceIndex?>(null) }
    val localContext = LocalContext.current
    LaunchedEffect(repository, retry, initialSlug, initialItemId) {
        loading = true; error = null
        try {
            pocketFlow.value = repository.pocketIndex()
            if (filter.pocketId != null && filter.pocketId != ItemPocketIndex.UNKNOWN &&
                pocketFlow.value.pockets.none { it.id == filter.pocketId }) filter = filter.copy(pocketId = null)
            if (initialItemId > 0) {
                selected = repository.observeItems().first().firstOrNull { it.itemId == initialItemId }
                if (selected == null) error = "No se encontró el objeto."
            } else if (initialSlug != null) {
                selected = repository.resolve(initialSlug)
                if (selected == null) error = "No se encontró el objeto. Puedes buscarlo en el catálogo."
            }
        } catch (e: Exception) { if (e is CancellationException) throw e; error = "No se pudieron cargar los objetos." }
        finally { loading = false }
    }
    LaunchedEffect(itemRepository) {
        if (itemRepository == null) try { reference = AppContainer.get(localContext).moves.index() }
        catch (e: Exception) { if (e is CancellationException) throw e }
    }
    val itemsFlow = remember(repository) { repository.observeItems() }
    val all by itemsFlow.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val pokemonFlow = remember(itemRepository) {
        if (itemRepository == null) AppContainer.get(context).pokemon.observeExplorer() else flowOf(emptyList())
    }
    val pokemon by pokemonFlow.collectAsState(initial = emptyList())
    val pokemonLabels = remember(pokemon) { pokemon.associate { it.pokemonId to "${it.numberLabel} · ${it.displayName}" } }
    val filteredFlow = remember(repository) { MutableStateFlow(ItemFilter()) }
    LaunchedEffect(filter) { filteredFlow.value = filter }
    val browserResult by remember(repository) {
        combine(repository.observeItems(), filteredFlow, pocketFlow) { rows, filters, metadata ->
            ItemBrowserResult(filters, metadata, ItemPresentation.filter(rows, filters, metadata), rows.size)
        }
             .flowOn(Dispatchers.Default)
    }.collectAsState(initial = null)
    val currentResult = browserResult?.takeIf { it.filter == filter && it.pockets == pockets }
    val result = currentResult?.rows.orEmpty()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Objetos", maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        }, actions = {
            ItemFilterAction(listOf(filter.category != null, filter.attribute != null, filter.editorialOnly).count { it }) { showFilters = true }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(filter.query, { filter = filter.copy(query = it) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar objeto") }, singleLine = true)
            ItemPocketTabs(pockets, if (loading) emptyList() else all, filter.pocketId) { pocket ->
                filter = filter.copy(pocketId = pocket, category = filter.category.takeIf {
                    pocket == null || (pockets.pocket(it.orEmpty())?.id ?: ItemPocketIndex.UNKNOWN) == pocket
                })
            }
            val summary = listOfNotNull(filter.category?.let(ItemLabels::category), filter.attribute?.let(ItemLabels::attribute),
                "Consejos de Rotom".takeIf { filter.editorialOnly })
            if (summary.isNotEmpty()) Text(summary.joinToString(" · "), style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${result.size} / ${currentResult?.total ?: all.size} objetos", fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton({ filter = ItemFilter() }) { Text("Limpiar") }
            }
            if (loading || (currentResult == null && error == null)) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton({ retry++ }) { Text("Reintentar") } }
            if (!loading && currentResult != null && result.isEmpty() && error == null) Text("Sin objetos con estos filtros.")
            if (!loading && currentResult != null) key(filter) {
            LazyColumn(Modifier.weight(1f).testTag("item_list"), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(result, key = { it.itemId }) { item ->
                    Card(Modifier.fillMaxWidth().testTag("item_${item.itemId}").clickable { selected = item }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ItemSprite(item.slug, modifier = Modifier.size(48.dp))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text(ItemLabels.category(item.category), fontSize = 12.sp)
                                if (item.nameLanguage == "en") Text("Inglés", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            }
        }
    }
    selected?.let { item ->
        ItemDetailSheet(item, repository, onDismiss = { selected = null }, onPokemonClick = onPokemonClick, pokemonLabels = pokemonLabels,
            reference = reference, onMoveClick = onMoveClick, pockets = pockets)
    }
    if (showFilters) ModalBottomSheet(onDismissRequest = { showFilters = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.85f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Filtros de objetos", style = MaterialTheme.typography.titleMedium)
            val categories = all.filter { filter.pocketId == null ||
                (pockets.pocket(it.category)?.id ?: ItemPocketIndex.UNKNOWN) == filter.pocketId }.map { it.category }.distinct().sorted()
            ItemChoice("Categoría", filter.category, categories, ItemLabels::category) { filter = filter.copy(category = it) }
            ItemChoice("Característica", filter.attribute, all.flatMap { it.attributeList }.distinct().sorted(), ItemLabels::attribute) {
                filter = filter.copy(attribute = it)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(filter.editorialOnly, { filter = filter.copy(editorialOnly = it) })
                Text("Con consejos de Rotom", modifier = Modifier.weight(1f))
            }
            TextButton({ showFilters = false }) { Text("Aplicar y cerrar") }
        }
    }
}

@Composable
internal fun ItemPocketTabs(pockets: ItemPocketIndex, items: List<IndexedItem>, selected: Int?, onChoose: (Int?) -> Unit) {
    val order = listOf(3, 2, 5, 4, 8, 1, 7, 6)
    LazyRow(Modifier.fillMaxWidth().testTag("item_pockets"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected == null, { onChoose(null) }, label = { Text("Todos") }, modifier = Modifier.testTag("pocket_all")) }
        items(pockets.pockets.sortedWith(compareBy<ItemPocket> { order.indexOf(it.id).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE }.thenBy { it.id }), key = { it.id }) { pocket ->
            FilterChip(selected == pocket.id, { onChoose(pocket.id) }, label = { Text(pocket.label) }, modifier = Modifier.testTag("pocket_${pocket.id}"))
        }
        if (items.any { pockets.pocket(it.category) == null }) item {
            FilterChip(selected == ItemPocketIndex.UNKNOWN, { onChoose(ItemPocketIndex.UNKNOWN) },
                label = { Text("Otros") }, modifier = Modifier.testTag("pocket_unknown"))
        }
    }
}

@Composable
internal fun ItemChoice(label: String, selected: String?, choices: List<String>, display: (String) -> String, onChoose: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        val value = selected?.let(display) ?: "Sin seleccionar"
        OutlinedCard(onClick = { open = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$label: $value" }, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ExpandMore, null)
            }
        }
        DropdownMenu(open, { open = false }, modifier = Modifier.heightIn(max = 300.dp)) {
            DropdownMenuItem(text = { Text("Sin seleccionar") }, onClick = { onChoose(null); open = false })
            choices.forEach { choice -> DropdownMenuItem(text = { Text(display(choice)) }, onClick = { onChoose(choice); open = false }) }
        }
    }
}

@Composable
internal fun ItemSprite(slug: String, modifier: Modifier = Modifier, declared: String? = null) {
    val url = ItemPresentation.sprite(slug, declared)
    var failed by remember(url) { mutableStateOf(false) }
    var retry by remember(url) { mutableIntStateOf(0) }
    Box(modifier.clickable(enabled = failed) { failed = false; retry++ }, contentAlignment = Alignment.Center) {
        if (url == null || failed) Text("Sin\nsprite", fontSize = 10.sp)
        else key(url, retry) { AsyncImage(url, "Sprite del objeto $slug", modifier = Modifier.fillMaxSize(), onError = { failed = true }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemDetailSheet(item: IndexedItem, repository: ItemRepository, onDismiss: () -> Unit, onPokemonClick: (String) -> Unit,
    pokemonLabels: Map<Int, String> = emptyMap(), reference: com.duoc.rotompedia.data.reference.ReferenceIndex? = null,
    onMoveClick: (Int) -> Unit = {}, pockets: ItemPocketIndex = ItemPocketIndex()) {
    var data by remember(item.itemId) { mutableStateOf<ItemDetailResult?>(null) }
    var loading by remember(item.itemId) { mutableStateOf(true) }
    var error by remember(item.itemId) { mutableStateOf<String?>(null) }
    var retry by remember(item.itemId) { mutableIntStateOf(0) }
    var group by remember(item.itemId) { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(item.itemId, retry) {
        loading = true; error = null
        try { data = repository.detail(item.itemId, refresh = retry > 0) }
        catch (e: Exception) { if (e is CancellationException) throw e; error = "No se pudo cargar la ficha. Comprueba tu conexión e inténtalo de nuevo." }
        finally { loading = false }
    }
    // Read loading/detail state in the owning composition before the sheet's
    // separate window, so asynchronous completion also updates its content.
    val currentData = data
    val isLoading = loading
    val currentError = error
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).verticalScroll(rememberScrollState()).padding(20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ItemSprite(item.slug, modifier = Modifier.size(64.dp), declared = currentData?.detail?.sprites?.default)
                Column(Modifier.weight(1f)) { Text(item.name, style = MaterialTheme.typography.titleLarge) }
                TextButton(onDismiss) { Text("Cerrar") }
            }
            Text(ItemLabels.category(item.category))
            pockets.pocket(item.category)?.let { Text("Bolsillo: ${it.label}", style = MaterialTheme.typography.labelSmall) }
            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            currentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton({ retry++ }, enabled = !isLoading) { Text(if (currentError != null) "Reintentar" else "Actualizar") }
            currentData?.let { loaded ->
                val detail = loaded.detail
                val machines = reference?.machines.orEmpty().filter { it.itemId == item.itemId }
                val groups = (detail.descriptions.map { it.versionGroup.name } + detail.prices.map { it.versionGroup.name } + machines.map { it.versionGroup }).distinct().sorted()
                if (groups.isNotEmpty()) ItemChoice("Edición", group, groups, ItemLabels::version) { group = it }
                val description = ItemPresentation.description(detail, group)
                description?.let {
                    Text("Descripción", style = MaterialTheme.typography.titleMedium)
                    Text(if (it.language == "es") it.text else "Inglés: ${it.text}")
                }
                val effect = ItemPresentation.effect(detail)
                if (detail.name == "shuca-berry") {
                    Text("Efecto", style = MaterialTheme.typography.titleMedium)
                    Text(ItemEditorial.shucaEffect)
                } else effect?.let {
                    Text("Efecto", style = MaterialTheme.typography.titleMedium)
                    Text(if (it.language == "es") it.text else "Inglés: ${it.text}")
                }
                if (detail.attributes.isNotEmpty()) {
                    Text("Características", style = MaterialTheme.typography.titleMedium)
                    Text(detail.attributes.joinToString(" · ") { ItemLabels.attribute(it.name) })
                }
                if (item.category == "all-machines") {
                    if (reference != null && group != null) {
                        val links = machines.filter { it.versionGroup == group }.distinctBy { it.moveId }
                        if (links.isNotEmpty()) Text("Movimiento", fontWeight = FontWeight.Bold)
                        links.forEach { link -> TextButton({ onMoveClick(link.moveId) }) { Text(reference.moves.firstOrNull { it.id == link.moveId }?.displayName ?: "Ver movimiento") } }
                    }
                }
                val prices = detail.prices.filter { it.versionGroup.name == group }
                if (prices.isNotEmpty()) {
                    Text("Precio", style = MaterialTheme.typography.titleMedium)
                    Text(prices.joinToString("\n") {
                        "Compra: ${it.purchasePrice?.toString() ?: "—"} · venta: ${it.sellPrice?.toString() ?: "—"} · ${it.currency.name}"
                    })
                }
                ItemEditorial.notes[item.slug]?.let {
                    Text("Consejo de Rotom", style = MaterialTheme.typography.titleMedium)
                    Text(it)
                }
                TextButton({
                    val text = "${item.name}\n${group?.let(ItemLabels::version).orEmpty()}\n" +
                        (description?.let { if (it.language == "es") it.text else "Inglés: ${it.text}" }.orEmpty())
                    clipboard.setText(AnnotatedString(text))
                }) { Text("Copiar ficha") }
                if (detail.holders.isNotEmpty()) {
                    Text("Lo llevan en encuentros", style = MaterialTheme.typography.titleMedium)
                    detail.holders.forEach { holder ->
                        val id = Regex("https://pokeapi.co/api/v2/pokemon/([1-9][0-9]*)/?").matchEntire(holder.pokemon.url)?.groupValues?.get(1)
                        if (id != null) TextButton({ onPokemonClick(id) }) {
                            Text(pokemonLabels[id.toInt()] ?: com.duoc.rotompedia.data.PokemonFormCatalog.displayName(holder.pokemon.name))
                        }
                        Text(holder.versions.joinToString(" · ") { "${it.version.name}: ${it.rarity}%" }, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
