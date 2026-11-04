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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.items.*
import com.duoc.rotompedia.di.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitiveItemsScreen(onNavigateBack: () -> Unit, onPokemonClick: (String) -> Unit = {}, initialSlug: String? = null,
    itemRepository: ItemRepository? = null, initialItemId: Int = 0, onMoveClick: (Int) -> Unit = {}) {
    val repository = itemRepository ?: AppContainer.get(LocalContext.current).items
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var filter by remember { mutableStateOf(ItemFilter()) }
    var selected by remember { mutableStateOf<IndexedItem?>(null) }
    var reference by remember { mutableStateOf<com.duoc.rotompedia.data.reference.ReferenceIndex?>(null) }
    val localContext = LocalContext.current
    LaunchedEffect(repository, retry, initialSlug, initialItemId) {
        loading = true; error = null
        try {
            repository.ensureIndex()
            if (initialItemId > 0) {
                selected = repository.observeItems().first().firstOrNull { it.itemId == initialItemId }
                if (selected == null) error = "Objeto con ID desconocido. No se sustituye por otro."
            } else if (initialSlug != null) {
                selected = repository.resolve(initialSlug)
                if (selected == null) error = "No se pudo resolver el objeto solicitado. Puedes buscarlo en el catálogo."
            }
        } catch (e: Exception) { if (e is CancellationException) throw e; error = "No se pudo preparar el índice de objetos." }
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
    val result by remember(repository) {
        combine(repository.observeItems(), filteredFlow) { rows, filters -> ItemPresentation.filter(rows, filters) }
            .flowOn(Dispatchers.Default)
    }.collectAsState(initial = emptyList())
    Scaffold(topBar = {
        TopAppBar(title = { Text("Enciclopedia de objetos") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(filter.query, { filter = filter.copy(query = it) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre, categoría o ID del objeto") }, singleLine = true)
            ItemChoice("Categoría oficial", filter.category, all.map { it.category }.distinct().sorted(), ItemLabels::category) {
                filter = filter.copy(category = it)
            }
            ItemChoice("Atributo documentado", filter.attribute, all.flatMap { it.attributeList }.distinct().sorted(), ItemLabels::attribute) {
                filter = filter.copy(attribute = it)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(filter.editorialOnly, { filter = filter.copy(editorialOnly = it) })
                Text("Con consejo editorial revisado", fontSize = 12.sp)
                TextButton({ filter = ItemFilter() }) { Text("Limpiar") }
            }
            Text("${result.size} / ${all.size} objetos · índice local", fontSize = 12.sp)
            Text("Categorías y atributos no garantizan disponibilidad o uso en todos los juegos.", fontSize = 11.sp)
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton({ retry++ }) { Text("Reintentar índice") } }
            if (!loading && result.isEmpty() && error == null) Text("Sin objetos con estos filtros.")
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(result, key = { it.itemId }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { selected = item }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ItemSprite(item.slug, modifier = Modifier.size(48.dp))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text("${ItemLabels.category(item.category)} · ID objeto ${item.itemId}", fontSize = 12.sp)
                                if (item.nameLanguage != "es") Text("Nombre español no disponible (${item.nameLanguage})", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    selected?.let { item ->
        ItemDetailSheet(item, repository, onDismiss = { selected = null }, onPokemonClick = onPokemonClick, pokemonLabels = pokemonLabels,
            reference = reference, onMoveClick = onMoveClick)
    }
}

@Composable
internal fun ItemChoice(label: String, selected: String?, choices: List<String>, display: (String) -> String, onChoose: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton({ open = true }) { Text("$label: ${selected?.let(display) ?: "Sin seleccionar"}") }
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
    onMoveClick: (Int) -> Unit = {}) {
    var data by remember(item.itemId) { mutableStateOf<ItemDetailResult?>(null) }
    var loading by remember(item.itemId) { mutableStateOf(true) }
    var error by remember(item.itemId) { mutableStateOf<String?>(null) }
    var retry by remember(item.itemId) { mutableIntStateOf(0) }
    var group by remember(item.itemId) { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(item.itemId, retry) {
        loading = true; error = null
        try { data = repository.detail(item.itemId, refresh = retry > 0) }
        catch (e: Exception) { if (e is CancellationException) throw e; error = "Detalle no disponible. No hay una ficha guardada utilizable; comprueba la conexión y reintenta." }
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
                Column(Modifier.weight(1f)) { Text(item.name, style = MaterialTheme.typography.titleLarge); Text("${item.slug} · ID objeto ${item.itemId}", fontSize = 12.sp) }
                TextButton(onDismiss) { Text("Cerrar") }
            }
            Text(ItemLabels.category(item.category))
            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            currentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton({ retry++ }) { Text("Actualizar / reintentar ficha") }
            currentData?.let { loaded ->
                val detail = loaded.detail
                Text(if (loaded.offline) "No se pudo actualizar: mostrando copia guardada." else "Ficha disponible en caché local.", fontSize = 12.sp)
                Text("Fuente: PokeAPI · item/${detail.id} · guardado ${java.time.Instant.ofEpochMilli(loaded.fetchedAt)}", fontSize = 11.sp)
                val machines = reference?.machines.orEmpty().filter { it.itemId == item.itemId }
                val groups = (detail.descriptions.map { it.versionGroup.name } + detail.prices.map { it.versionGroup.name } + machines.map { it.versionGroup }).distinct().sorted()
                ItemChoice("Edición de descripción / precio", group, groups, ItemLabels::version) { group = it }
                val description = ItemPresentation.description(detail, group)
                Text("Descripción de juego", style = MaterialTheme.typography.titleMedium)
                Text(when {
                    group == null -> "Selecciona una edición para consultar su texto y precio."
                    detail.name == "shuca-berry" && group == "x-y" -> "Texto original de X/Y conocido como inconsistente: se oculta para no atribuir un efecto incorrecto. Consulta el efecto revisado o una edición diferente."
                    description == null -> "Sin descripción española o inglesa para esta edición."
                    description.language == "es" -> description.text
                    else -> "Sin traducción española para esta edición. Original en inglés: ${description.text}"
                })
                val effect = ItemPresentation.effect(detail)
                Text("Efecto mecánico · fuente sin edición específica", style = MaterialTheme.typography.titleMedium)
                if (detail.name == "shuca-berry") {
                    Text(ItemEditorial.shucaEffect)
                    Text(ItemEditorial.shucaSource, fontSize = 11.sp)
                } else Text(when (effect?.language) {
                    "es" -> effect.text
                    "en" -> "Sin efecto español verificado. Original en inglés: ${effect.text}"
                    else -> "Sin efecto documentado en español o inglés."
                })
                Text("Atributos documentados", style = MaterialTheme.typography.titleMedium)
                Text(detail.attributes.map { ItemLabels.attribute(it.name) }.joinToString(" · ").ifBlank { "Sin atributos documentados; no implica que no se pueda usar o llevar." })
                Text("Precio por edición", style = MaterialTheme.typography.titleMedium)
                if (item.category == "all-machines") {
                    Text("Movimiento de la MT / MO · depende de la edición", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    if (reference == null) Text("Referencia de relaciones no disponible.")
                    else if (group == null) Text("Selecciona una edición para consultar el movimiento relacionado.")
                    else {
                        val links = machines.filter { it.versionGroup == group }.distinctBy { it.moveId }
                        if (links.isEmpty()) Text("Sin relación documentada para esta edición.")
                        links.forEach { link -> TextButton({ onMoveClick(link.moveId) }) { Text(reference.moves.firstOrNull { it.id == link.moveId }?.displayName ?: "Movimiento ID ${link.moveId}") } }
                    }
                }
                val prices = detail.prices.filter { it.versionGroup.name == group }
                Text(if (prices.isEmpty()) "Sin precio contextual documentado para la edición seleccionada. No se asume que sea gratis." else prices.joinToString("\n") {
                    "Compra: ${it.purchasePrice?.toString() ?: "sin dato"} · venta: ${it.sellPrice?.toString() ?: "sin dato"} · moneda: ${it.currency.name}"
                })
                ItemEditorial.notes[item.slug]?.let {
                    Text("Consejo editorial de RotomPedia · no oficial", style = MaterialTheme.typography.titleMedium)
                    Text(it)
                }
                TextButton({
                    val text = "${item.name} (${item.slug})\nFuente: PokeAPI item/${item.itemId}\nEdición: ${group?.let(ItemLabels::version) ?: "no seleccionada"}\n" +
                        (description?.let { "Texto (${it.language}): ${it.text}" } ?: "Sin descripción seleccionada")
                    clipboard.setText(AnnotatedString(text))
                }) { Text("Copiar ficha con procedencia") }
                if (detail.holders.isNotEmpty()) {
                    Text("Pokémon que pueden llevarlo en encuentros", style = MaterialTheme.typography.titleMedium)
                    Text("No es una lista de Pokémon compatibles para equiparlo. Condiciones registradas por juego.", fontSize = 12.sp)
                    detail.holders.forEach { holder ->
                        val id = Regex("https://pokeapi.co/api/v2/pokemon/([1-9][0-9]*)/?").matchEntire(holder.pokemon.url)?.groupValues?.get(1)
                        if (id != null) TextButton({ onPokemonClick(id) }) {
                            Text(pokemonLabels[id.toInt()] ?: "ID técnico $id · ${holder.pokemon.name}")
                        }
                        Text(holder.versions.joinToString(" · ") { "${it.version.name}: ${it.rarity}%" }, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
