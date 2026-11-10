package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.index.IndexEnvelope
import com.duoc.rotompedia.data.index.IndexPayload
import com.duoc.rotompedia.data.index.IndexValidator
import com.duoc.rotompedia.data.maps.*
import com.duoc.rotompedia.di.AppContainer
import com.duoc.rotompedia.ui.components.RotomCard
import com.duoc.rotompedia.ui.components.RotomScaffold
import com.duoc.rotompedia.ui.components.RotomAppearanceAction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * First data-backed map entry: Hoenn / Emerald only. The old background image is explicitly
 * illustrative and has NO interactive positions until cartographic rights and mappings are audited.
 * Source encounters are authoritative for this edition; objects and unlisted games are NOT inferred.
 *
 * The suppression is for the known lint false positive ProduceStateDoesNotAssignValue
 * (issuetracker.google.com/issues/349411310): AGP 8.12.3 flags even the canonical
 * `produceState(0) { value = 1 }`, although `value = …` is present in both producers below.
 */
@Suppress("ProduceStateDoesNotAssignValue")
@Composable
fun VerifiedHoennMapScreen(onNavigateBack: () -> Unit, onPokemonClick: (Int) -> Unit) {
    val context = LocalContext.current
    val container = remember(context) { AppContainer.get(context) }
    var reload by remember { mutableIntStateOf(0) }
    var loadFailed by remember { mutableStateOf(false) }
    val catalogue by produceState<LocationCatalogue?>(null, reload) {
        val loaded = try {
            container.hoennPilot.catalogue()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            loadFailed = true
            null
        }
        value = loaded
    }
    val labels by produceState<Map<Int, LocationPokemonLabel>>(emptyMap()) {
        val loaded = try {
            withContext(Dispatchers.IO) {
                val json = context.assets.open("pokemon-index.json").bufferedReader().use { it.readText() }
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val envelope = requireNotNull(moshi.adapter(IndexEnvelope::class.java).fromJson(json))
                val payload = requireNotNull(moshi.adapter(IndexPayload::class.java).fromJson(envelope.payloadJson))
                IndexValidator.validate(envelope, payload)
                LocationPokemonNames.fromNationalIndex(payload)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()
        }
        value = loaded
    }
    var query by rememberSaveable { mutableStateOf("") }
    var withEncounters by rememberSaveable { mutableStateOf(true) }
    var categoryName by rememberSaveable { mutableStateOf(HoennEncounterCategory.ALL.name) }
    var showExplorer by rememberSaveable { mutableStateOf(false) }
    var mapFullscreen by rememberSaveable { mutableStateOf(false) }
    var selectedLocationId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedAreaId by rememberSaveable { mutableStateOf<Int?>(null) }

    RotomScaffold("Mapa de Hoenn", onNavigateBack, actions = { RotomAppearanceAction() }) {
        val current = catalogue
        if (current == null) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                if (!loadFailed) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Verificando los encuentros de Esmeralda…")
                } else {
                    Text("No se pudo abrir el catálogo de Esmeralda. Los datos no se mostrarán sin validar.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { loadFailed = false; reload++ }) { Text("Reintentar") }
                }
            }
        } else {
            val pilot = current.pilot
            val locations = remember(pilot) { pilot.locations.associateBy { it.id } }
            val methods = remember(pilot) { pilot.methods.associateBy { it.id } }
            val conditions = remember(pilot) { pilot.conditions.associateBy { it.id } }
            val search = remember(current, labels) { HoennAreaSearch(current, labels) }
            val category = HoennEncounterCategory.entries.firstOrNull { it.name == categoryName }
                ?: HoennEncounterCategory.ALL
            val filtered = remember(search, query, withEncounters, category) {
                search.filter(query, category, withEncounters)
            }
            val supportedCategories = remember(pilot) {
                val sourcedMethods = pilot.encounters.map { it.methodId }.toSet()
                HoennEncounterCategory.entries.filter { categoryOption ->
                    categoryOption == HoennEncounterCategory.ALL || pilot.methods.any { method ->
                        method.id in sourcedMethods && categoryOption.includes(method.slug)
                    }
                }
            }
            Column(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Explora Hoenn", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text("Toca una ruta, ciudad o cueva. El mapa pertenece a Hoenn; " +
                    "los encuentros cambian según la edición (ahora verificada: Esmeralda).", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showExplorer = !showExplorer }) {
                        Text(if (showExplorer) "Ocultar lista" else "Buscar zonas")
                    }
                    TextButton(onClick = { mapFullscreen = true }) { Text("Ampliar mapa") }
                }
                val highlightedLocationId = selectedLocationId
                    ?: selectedAreaId?.let { id -> pilot.areas.firstOrNull { it.id == id }?.locationId }
                val matchingLocationIds = if (showExplorer) filtered.map { it.area.locationId }.toSet() else null
                val selectMapLocation: (MapLocation) -> Unit = { location ->
                    mapFullscreen = false
                    selectedAreaId = null
                    val matchingAreas = if (showExplorer) {
                        filtered.filter { it.area.locationId == location.id }.map { it.area }
                    } else {
                        pilot.areas.filter { it.locationId == location.id }
                    }
                    if (matchingAreas.size == 1) {
                        selectedLocationId = null
                        selectedAreaId = matchingAreas.first().id
                    } else {
                        selectedLocationId = location.id
                    }
                }
                HoennSectionMap(pilot.locations, highlightedLocationId, selectMapLocation,
                    Modifier.fillMaxWidth(), matchingLocationIds, if (showExplorer) 205.dp else 340.dp)
                if (mapFullscreen) {
                    Dialog(onDismissRequest = { mapFullscreen = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)) {
                        Surface(Modifier.fillMaxSize().padding(8.dp), shape = RoundedCornerShape(16.dp)) {
                            BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp)) {
                                val availableHeight = (maxHeight - 135.dp).coerceAtLeast(120.dp)
                                Column(Modifier.fillMaxSize()) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("Región de Hoenn", style = MaterialTheme.typography.titleLarge)
                                        TextButton(onClick = { mapFullscreen = false }) { Text("Cerrar") }
                                    }
                                    HoennSectionMap(pilot.locations, highlightedLocationId, selectMapLocation,
                                        Modifier.fillMaxWidth(), matchingLocationIds, availableHeight)
                                }
                            }
                        }
                    }
                }
                if (showExplorer) {
                        OutlinedTextField(query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar zona o Pokémon") }, singleLine = true)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(supportedCategories, key = { it.name }) { option ->
                            FilterChip(selected = category == option, onClick = { categoryName = option.name },
                                label = { Text(option.title) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = withEncounters, onCheckedChange = { withEncounters = it })
                        Text("Solo zonas con registros", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${filtered.size} de ${pilot.areas.size} áreas declaradas · ${category.title}",
                        style = MaterialTheme.typography.labelSmall)
                    if (query.isNotBlank() || category != HoennEncounterCategory.ALL || !withEncounters) {
                        TextButton(onClick = {
                            query = ""
                            categoryName = HoennEncounterCategory.ALL.name
                            withEncounters = true
                        }) { Text("Limpiar búsqueda y filtros") }
                    }
                    if (filtered.isEmpty()) Text("Sin resultados verificados con estos filtros.",
                        style = MaterialTheme.typography.bodySmall)
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.area.id }) { result ->
                            val area = result.area
                            RotomCard(onClick = { selectedAreaId = area.id }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(areaDisplay(area, locations[area.locationId]), fontWeight = FontWeight.SemiBold,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(if (result.recordCount > 0) "${result.varietyCount} variedades · ${result.recordCount} registros"
                                    else "Sin registros en la fuente para Esmeralda (no significa área sin Pokémon)",
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    Text("Selecciona una zona del mapa para consultar Pokémon y objetos por edición. " +
                        "La opción de búsqueda está disponible sin llenar el mapa de etiquetas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            selectedLocationId?.let { id ->
                pilot.locations.firstOrNull { it.id == id }?.let { location ->
                    HoennLocationAreasSheet(location,
                        if (showExplorer) filtered.filter { it.area.locationId == id }.map { it.area }
                        else pilot.areas.filter { it.locationId == id },
                        current, onDismiss = { selectedLocationId = null },
                        onAreaClick = { selectedLocationId = null; selectedAreaId = it })
                }
            }
            selectedAreaId?.let { id ->
                pilot.areas.firstOrNull { it.id == id }?.let { area ->
                    HoennAreaSheet(area, locations[area.locationId], current.area(id, pilot.versionId),
                        search.encountersForArea(id, if (showExplorer) query else "",
                            if (showExplorer) category else HoennEncounterCategory.ALL),
                        methods, conditions, labels,
                        if (showExplorer) category else HoennEncounterCategory.ALL,
                        if (showExplorer) query else "", onDismiss = { selectedAreaId = null },
                        onPokemonClick = { selectedAreaId = null; onPokemonClick(it) })
                }
            }
        }
    }
}

private fun areaDisplay(area: MapArea, location: MapLocation?): String {
    val base = location?.names?.get("es") ?: location?.names?.get("en") ?: location?.slug.orEmpty()
    val detail = area.names["es"] ?: area.names["en"] ?: area.identifier
    return if (detail.isBlank() || detail.equals(base, true) || detail.equals(location?.names?.get("en"), true)) base
    else "$base · $detail"
}

private fun methodDisplay(method: MapEncounterMethod?): String = AtlasEncounterMethods.displayName(
    method?.slug.orEmpty(), method?.names?.get("es") ?: method?.names?.get("en")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoennAreaSheet(area: MapArea, location: MapLocation?, data: AreaEncounters,
    filtered: List<MapEncounter>, methods: Map<Int, MapEncounterMethod>,
    conditions: Map<Int, MapEncounterCondition>, labels: Map<Int, LocationPokemonLabel>,
    category: HoennEncounterCategory, query: String,
    onDismiss: () -> Unit, onPokemonClick: (Int) -> Unit) {
    // The edition is a property of encounter data, not of the Hoenn map itself.
    var selectedTab by rememberSaveable(area.id) { mutableIntStateOf(0) }
    val families = remember(filtered, methods) {
        AtlasEncounterFamily.entries.mapNotNull { family ->
            val byMethod = filtered.filter { row ->
                AtlasEncounterMethods.family(methods[row.methodId]?.slug.orEmpty()) == family
            }.groupBy { it.methodId }.toSortedMap()
            if (byMethod.isEmpty()) null else family to byMethod
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item(key = "atlas_header") {
                Text(areaDisplay(area, location), style = MaterialTheme.typography.titleLarge)
                Text("Región de Hoenn · encuentros según edición", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, enabled = false, label = { Text("Pokémon Esmeralda") })
                    Text("Única edición verificada actualmente", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = { Text("Pokémon") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = { Text("Objetos / NPC") })
                }
                Spacer(Modifier.height(12.dp))
                if (selectedTab == 0) {
                    Text("${filtered.size} registros · ${category.title}",
                        style = MaterialTheme.typography.bodySmall)
                    if (query.isNotBlank()) Text("Filtro de búsqueda: ${query.trim()}",
                        style = MaterialTheme.typography.labelSmall)
                    Text("Cada método tiene sus propios encuentros. Los porcentajes son pesos " +
                        "de slot, no una tasa global de captura.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (selectedTab == 0) {
                if (filtered.isEmpty()) item(key = "atlas_empty_encounters") {
                    Text(if (data.coverage != AreaCoverage.AVAILABLE)
                        "No hay registros de encuentros verificados para esta zona y edición. " +
                            "Eso no demuestra que no existan Pokémon aquí."
                        else "No hay resultados con los filtros activos. Prueba otra búsqueda o método.",
                        style = MaterialTheme.typography.bodySmall)
                }
                families.forEach { (family, byMethod) ->
                    item(key = "family_${family.name}") {
                        Spacer(Modifier.height(4.dp))
                        Text(family.title, style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    byMethod.forEach { (methodId, encounters) ->
                        item(key = "method_$methodId") {
                            Text(methodDisplay(methods[methodId]), fontWeight = FontWeight.SemiBold)
                            data.methodRates.firstOrNull { it.methodId == methodId }?.let { rate ->
                                Text("Frecuencia de activación del método: ${rate.rate}%",
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        items(encounters, key = { it.id }) { encounter ->
                            val pokemon = labels[encounter.pokemonId]
                            RotomCard(onClick = { onPokemonClick(encounter.pokemonId) },
                                modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(pokemon?.let {
                                        "#${it.nationalNumber.toString().padStart(3, '0')} · ${it.name}"
                                    } ?: "Variedad de ID interno ${encounter.pokemonId}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold)
                                    val levels = if (encounter.minLevel == encounter.maxLevel)
                                        "${encounter.minLevel}" else "${encounter.minLevel}–${encounter.maxLevel}"
                                    Text("Nv. $levels" + (encounter.chance?.let { " · Peso de slot: $it%" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall)
                                    encounter.conditionIds.forEach { id ->
                                        conditions[id]?.let { condition ->
                                            val value = condition.names["es"] ?: condition.names["en"] ?: condition.slug
                                            Text("Condición: $value",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item(key = "atlas_no_items") {
                    Text("Objetos y regalos de NPC", style = MaterialTheme.typography.titleMedium)
                    Text("Todavía no hay una fuente auditada de objetos para esta zona " +
                        "y edición. No significa que la zona no tenga objetos.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Cuando se incorporen datos verificables, separaremos objetos " +
                        "recogidos, ocultos, regalos de NPC, recompensas y eventos, " +
                        "cada uno con su edición y forma de obtención.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoennLocationAreasSheet(location: MapLocation, areas: List<MapArea>,
    catalogue: LocationCatalogue, onDismiss: () -> Unit, onAreaClick: (Int) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            Text(location.names["es"] ?: location.names["en"] ?: location.slug,
                style = MaterialTheme.typography.titleLarge)
            Text("Subáreas del mapa de Hoenn · sus datos dependen de la edición.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
        }
        LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (areas.isEmpty()) item {
                Text("No hay subáreas que coincidan con los filtros actuales en esta ubicación. " +
                    "Puedes limpiar la búsqueda para revisar su cobertura.")
            }
            items(areas, key = { it.id }) { area ->
                val encounter = catalogue.area(area.id, catalogue.pilot.versionId)
                RotomCard(onClick = { onAreaClick(area.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(areaDisplay(area, location), fontWeight = FontWeight.SemiBold)
                        Text(if (encounter.coverage == AreaCoverage.AVAILABLE)
                            "${encounter.encounters.size} registros verificados"
                            else "Sin registros disponibles en la fuente",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
