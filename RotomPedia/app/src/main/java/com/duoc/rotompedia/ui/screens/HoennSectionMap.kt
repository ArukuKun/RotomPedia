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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.duoc.rotompedia.data.maps.*
import com.duoc.rotompedia.ui.maps.MapPoint
import com.duoc.rotompedia.ui.maps.MapViewport

/** An interactive GAME SECTION schematic, not a GPS map or an overlay on unsourced game art.
 * Sections have coordinates from the pinned pret/pokeemerald project and resolve only against
 * exact PokeAPI location slugs present in the audited Hoenn / Emerald pilot.
 */
@Composable
fun HoennSectionMap(locations: List<MapLocation>, selectedLocationId: Int?,
    onLocationClick: (MapLocation) -> Unit, modifier: Modifier = Modifier,
    matchingLocationIds: Set<Int>? = null, mapHeight: Dp = 214.dp) {
    val linked = remember(locations) { HoennSections.linked(locations) }
    var overlappingLocations by remember { mutableStateOf<List<MapLocation>>(emptyList()) }
    val sortedForPainting = remember(linked) {
        linked.sortedByDescending { it.first.width * it.first.height }
    }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var panX by rememberSaveable { mutableFloatStateOf(0f) }
    var panY by rememberSaveable { mutableFloatStateOf(0f) }
    fun currentViewport(): MapViewport? = if (size.width > 0 && size.height > 0) {
        MapViewport.create(size.width.toFloat(), size.height.toFloat(),
            HoennSections.COLUMNS.toFloat(), HoennSections.ROWS.toFloat(), zoom, panX, panY)
    } else null
    fun adopt(next: MapViewport) { zoom = next.zoom; panX = next.panX; panY = next.panY }
    fun focusSelection() {
        val bounds = linked.firstOrNull { it.second.id == selectedLocationId }?.first?.bounds ?: return
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
    val outline = MaterialTheme.colorScheme.outline
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Mapa regional de Hoenn · geometría del juego", style = MaterialTheme.typography.labelMedium)
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
            .semantics { contentDescription = "Plano esquemático de Hoenn. Puedes ampliar, arrastrar y tocar las secciones. También puedes seleccionar zonas desde la lista accesible inferior." }
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
                        val choices = HoennSections.hits(viewport, MapPoint(tap.x, tap.y), linked)
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
            val cellWidth = viewport.renderedWidth / HoennSections.COLUMNS
            val cellHeight = viewport.renderedHeight / HoennSections.ROWS
            val origin = viewport.toScreen(MapPoint(0f, 0f))
            // Light grid makes the true in-game section geometry readable; nothing is placed
            // over the unsourced Hoenn map PNG, and no geographical precision is promised.
            for (x in 0..HoennSections.COLUMNS) {
                val sx = origin.x + cellWidth * x
                drawLine(outline.copy(alpha = .12f), Offset(sx, origin.y),
                    Offset(sx, origin.y + viewport.renderedHeight), strokeWidth = 1f)
            }
            for (y in 0..HoennSections.ROWS) {
                val sy = origin.y + cellHeight * y
                drawLine(outline.copy(alpha = .12f), Offset(origin.x, sy),
                    Offset(origin.x + viewport.renderedWidth, sy), strokeWidth = 1f)
            }
            sortedForPainting.forEach { (section, location) ->
                val topLeft = viewport.toScreen(MapPoint(section.x / 28f, section.y / 15f))
                val rectangle = Size(section.width * cellWidth, section.height * cellHeight)
                val active = selectedLocationId == location.id
                val notMatching = matchingLocationIds != null && location.id !in matchingLocationIds
                val color = if (active) tertiary else if (notMatching) outline else primary
                drawRect(color.copy(alpha = if (active) .78f else if (notMatching) .08f else .38f),
                    topLeft = Offset(topLeft.x, topLeft.y), size = rectangle)
                drawRect(color.copy(alpha = if (active) .95f else if (notMatching) .22f else .85f),
                    topLeft = Offset(topLeft.x, topLeft.y), size = rectangle,
                    style = Stroke(width = if (active) 2.5.dp.toPx() else 1.dp.toPx()))
                // Landmarks remain distinguishable at default zoom without implying encounter data.
                if (!notMatching && section.width == 1 && section.height == 1) {
                    drawCircle(onSurface.copy(alpha = .78f), radius = 1.5.dp.toPx(),
                        center = Offset(topLeft.x + rectangle.width / 2, topLeft.y + rectangle.height / 2))
                }
            }
        }
        Text("Toca una zona, pellizca para ampliar o arrastra para recorrer Hoenn. " +
            "Plano de secciones verificadas (28 × 15), aún no ilustración final.",
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
