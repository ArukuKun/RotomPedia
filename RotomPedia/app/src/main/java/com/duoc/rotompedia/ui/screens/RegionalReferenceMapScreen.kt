package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.*
import com.duoc.rotompedia.data.RegionLocationData
import com.duoc.rotompedia.data.RouteLocation
import com.duoc.rotompedia.data.maps.*
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.ui.maps.*
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import kotlin.math.roundToInt

/** Same regional reference image for every edition. No unaudited pins or encounter filters. */
@Composable
fun MapViewerScreen(region: String, onNavigateBack: () -> Unit = {}, onPokemonClick: (Int) -> Unit = {}) {
    val context = LocalContext.current
    val title = MapRegionCatalogue.regions.firstOrNull { it.slug == region }?.name ?: region
    val imageId = remember(region) {
        context.resources.getIdentifier(if (region == "kanto") "kantomap" else "map_$region", "drawable", context.packageName)
    }
    val locations = remember(region) { RegionLocationData.getLocationsForRegion(region) }
    var selectedId by rememberSaveable(region) { mutableStateOf<String?>(null) }
    var query by rememberSaveable(region) { mutableStateOf("") }
    RotomScaffold("Mapa de $title", onNavigateBack, actions = { RotomAppearanceAction() }) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Cartografía anterior · reconstrucción pendiente", style = MaterialTheme.typography.labelMedium)
                Text("Imagen de referencia histórica; sus permisos y posiciones todavía requieren revisión. No se muestran pines como verificados.",
                    style = MaterialTheme.typography.bodySmall)
                if (imageId != 0) RegionalReferenceImage(imageId)
                else Text("Cartografía todavía no disponible para esta región.")
                OutlinedTextField(query, { query = it }, label = { Text("Buscar ubicación") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                Text("Ubicaciones del catálogo anterior", style = MaterialTheme.typography.titleMedium)
            }
            items(locations.filter { it.name.contains(query, ignoreCase = true) }, key = { it.id }) { location ->
                RotomCard(Modifier.fillMaxWidth(), onClick = { selectedId = location.id }) {
                    Text(location.name, Modifier.padding(16.dp))
                }
            }
            if (locations.isEmpty()) item {
                Text("El catálogo de ubicaciones por edición está pendiente. No significa que la región carezca de encuentros u objetos.")
            }
        }
        locations.firstOrNull { it.id == selectedId }?.let { location ->
            RegionalPendingLocationSheet(location, { selectedId = null }, onPokemonClick)
        }
    }
}

@Composable
private fun RegionalReferenceImage(imageId: Int) {
    val bitmap = ImageBitmap.imageResource(imageId)
    var size by remember { mutableStateOf(IntSize.Zero) }
    var zoom by rememberSaveable(imageId) { mutableFloatStateOf(1f) }
    var x by rememberSaveable(imageId) { mutableFloatStateOf(0f) }
    var y by rememberSaveable(imageId) { mutableFloatStateOf(0f) }
    fun viewport() = if (size.width > 0 && size.height > 0)
        MapViewport.create(size.width.toFloat(), size.height.toFloat(), bitmap.width.toFloat(), bitmap.height.toFloat(), zoom, x, y) else null
    fun adopt(v: MapViewport) { zoom = v.zoom; x = v.panX; y = v.panY }
    fun change(factor: Float) { viewport()?.let { adopt(it.zoomAt(factor, MapPoint(size.width / 2f, size.height / 2f))) } }
    val transform = rememberTransformableState { factor, pan, _ ->
        viewport()?.let { adopt(it.zoomAt(factor, MapPoint(size.width / 2f, size.height / 2f)).panBy(pan.x, pan.y)) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = { change(1.5f) }) { Text("Acercar") }
        TextButton(onClick = { change(1 / 1.5f) }) { Text("Alejar") }
        TextButton(onClick = { zoom = 1f; x = 0f; y = 0f }) { Text("Restablecer") }
    }
    Canvas(Modifier.fillMaxWidth().height(280.dp).background(MaterialTheme.colorScheme.surfaceVariant)
        .clipToBounds().testTag("regional_reference_map").onSizeChanged { size = it }
        .pointerInput(imageId) { detectTapGestures(onDoubleTap = { change(if (zoom > 1f) 1 / zoom else 2f) }) }
        .transformable(transform)) {
        viewport()?.let { v ->
            val origin = v.toScreen(MapPoint(0f, 0f))
            clipRect { drawImage(bitmap, dstOffset = IntOffset(origin.x.roundToInt(), origin.y.roundToInt()),
                dstSize = IntSize(v.renderedWidth.roundToInt().coerceAtLeast(1), v.renderedHeight.roundToInt().coerceAtLeast(1))) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionalPendingLocationSheet(location: RouteLocation, onDismiss: () -> Unit, onPokemonClick: (Int) -> Unit) {
    var version by rememberSaveable(location.id) { mutableIntStateOf(1) }
    var tab by rememberSaveable(location.id) { mutableIntStateOf(0) }
    var historical by remember { mutableStateOf(false) }
    if (historical) {
        // Preserve access to historical content without passing it off as edition-verified data.
        RotompediaTheme(legacy = true) {
            LocationDetailBottomSheet(location, { historical = false }, onPokemonClick)
        }
    } else ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 18.dp), contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(location.name, style = MaterialTheme.typography.titleLarge)
                if (location.region == "kanto") AtlasEditionSelector(AtlasEditions.kanto, version) { version = it }
                TabRow(tab) {
                    Tab(tab == 0, onClick = { tab = 0 }, text = { Text("Pokémon") })
                    Tab(tab == 1, onClick = { tab = 1 }, text = { Text("Objetos / NPC") })
                }
                Text(if (tab == 0) "Encuentros por edición pendientes de verificación. No se ofrecen métodos sin registros auditados."
                    else "Objetos y entregas de NPC por edición pendientes de documentar; no significa que no existan.")
                TextButton(onClick = { historical = true }) { Text("Consultar datos históricos no verificados") }
            }
        }
    }
}
