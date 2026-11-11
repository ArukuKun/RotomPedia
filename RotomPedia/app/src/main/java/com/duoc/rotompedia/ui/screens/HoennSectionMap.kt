package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import com.duoc.rotompedia.R
import com.duoc.rotompedia.ui.maps.HoennAtlasProjection
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.duoc.rotompedia.data.maps.*
import com.duoc.rotompedia.ui.maps.MapPoint
import com.duoc.rotompedia.ui.maps.MapViewport

/** Licensed community atlas, with approximate section hit areas projected separately from art.
 * Original section geometry and exact pilot IDs remain intact; overlaps require explicit choice.
 */
@Composable
fun HoennSectionMap(locations: List<MapLocation>, selectedLocationId: Int?,
    onLocationClick: (MapLocation) -> Unit, modifier: Modifier = Modifier,
    matchingLocationIds: Set<Int>? = null, mapHeight: Dp = 214.dp) {
    val linked = remember(locations) { HoennSections.linked(locations) }
    var overlappingLocations by remember { mutableStateOf<List<MapLocation>>(emptyList()) }
    val bitmap = ImageBitmap.imageResource(R.drawable.hoenn_atlas_cc0)
    var size by remember { mutableStateOf(IntSize.Zero) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var panX by rememberSaveable { mutableFloatStateOf(0f) }
    var panY by rememberSaveable { mutableFloatStateOf(0f) }
    fun currentViewport(): MapViewport? = if (size.width > 0 && size.height > 0) {
        MapViewport.create(size.width.toFloat(), size.height.toFloat(),
            HoennAtlasProjection.WIDTH, HoennAtlasProjection.HEIGHT, zoom, panX, panY)
    } else null
    fun adopt(next: MapViewport) { zoom = next.zoom; panX = next.panX; panY = next.panY }
    fun focusSelection() {
        val section = linked.firstOrNull { it.second.id == selectedLocationId }?.first ?: return
        val bounds = HoennAtlasProjection.bounds(section)
        currentViewport()?.let { adopt(it.focusOn(bounds)) }
    }
    LaunchedEffect(selectedLocationId, linked, size) {
        if (selectedLocationId != null && size.width > 0 && size.height > 0) focusSelection()
    }
    val transform = rememberTransformableState { zoomDelta, drag, _ ->
        currentViewport()?.let { current ->
            val next = current.zoomAt(zoomDelta, MapPoint(size.width / 2f, size.height / 2f))
                .panBy(drag.x, drag.y)
            adopt(next)
        }
    }
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Atlas de Hoenn · cartografía provisional", style = MaterialTheme.typography.labelMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End) {
                IconButton(onClick = {
                    currentViewport()?.let { adopt(it.zoomAt(1.5f,
                        MapPoint(size.width / 2f, size.height / 2f))) }
                }) { Icon(Icons.Default.Add, "Acercar") }
                IconButton(onClick = {
                    currentViewport()?.let { adopt(it.zoomAt(1f / 1.5f,
                        MapPoint(size.width / 2f, size.height / 2f))) }
                }) { Icon(Icons.Default.Remove, "Alejar") }
                if (selectedLocationId != null) {
                    TextButton(onClick = { focusSelection() }) { Text("Centrar zona") }
                }
                IconButton(onClick = { zoom = 1f; panX = 0f; panY = 0f }) {
                    Icon(Icons.Default.Refresh, "Restablecer vista")
                }
        }
        Canvas(Modifier.fillMaxWidth().height(mapHeight)
            .background(surface, RoundedCornerShape(12.dp))
            .clipToBounds().testTag("hoenn_atlas")
            .semantics { contentDescription = "Atlas de Hoenn. Pellizca para ampliar y arrastra para recorrer. Selección aproximada por sectores; también puedes elegir una zona desde Buscar zonas." }
            .onSizeChanged { newSize ->
                size = newSize
                // A rotation or width change may reduce the permitted pan offset.
                if (newSize.width > 0 && newSize.height > 0) {
                    val corrected = currentViewport()
                    if (corrected != null) adopt(corrected)
                }
            }
            .pointerInput(linked, size) {
                detectTapGestures { tap ->
                    currentViewport()?.let { viewport ->
                        val choices = HoennAtlasProjection.hits(viewport, MapPoint(tap.x, tap.y), linked)
                        when (choices.size) {
                            0 -> Unit
                            1 -> onLocationClick(choices.first())
                            else -> overlappingLocations = choices
                        }
                    }
                }
            }
            .transformable(transform)) {
            val viewport = currentViewport() ?: return@Canvas
            val origin = viewport.toScreen(MapPoint(0f, 0f))
            clipRect {
                drawImage(bitmap, dstOffset = IntOffset(origin.x.roundToInt(), origin.y.roundToInt()),
                    dstSize = IntSize(viewport.renderedWidth.roundToInt().coerceAtLeast(1),
                        viewport.renderedHeight.roundToInt().coerceAtLeast(1)))
                // No grid, filled sections or persistent marker cloud over the landscape.
                linked.forEach { (section, location) ->
                    if (location.id == selectedLocationId || matchingLocationIds?.contains(location.id) == true) {
                        val center = HoennAtlasProjection.fromGrid(section.x + section.width / 2f,
                            section.y + section.height / 2f)
                        val point = viewport.toScreen(center)
                        drawCircle(primary, radius = if (location.id == selectedLocationId) 8.dp.toPx() else 4.dp.toPx(),
                            center = Offset(point.x, point.y), style = Stroke(2.dp.toPx()))
                    }
                }
            }
        }
        Text("Mapa recreado por Ztash · CC0. Selección aproximada por sectores; " +
            "usa Buscar zonas para elegir una ubicación exacta. No es la cartografía final.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (overlappingLocations.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { overlappingLocations = emptyList() },
            title = { Text("Hay varias zonas aquí") },
            text = {
                Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                    Text("Selecciona qué ruta o lugar quieres consultar.",
                        style = MaterialTheme.typography.bodySmall)
                    overlappingLocations.forEach { location ->
                        TextButton(onClick = {
                            overlappingLocations = emptyList()
                            onLocationClick(location)
                        }) {
                            Text(location.names["es"] ?: location.names["en"] ?: location.slug)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { overlappingLocations = emptyList() }) { Text("Cancelar") }
            }
        )
    }
}
