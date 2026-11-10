package com.duoc.rotompedia.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import com.duoc.rotompedia.data.maps.MapRegionCatalogue
import com.duoc.rotompedia.data.maps.LastMapRegionStore
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.duoc.rotompedia.R
import com.duoc.rotompedia.data.RegionLocationData
import com.duoc.rotompedia.data.RouteLocation
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.ui.components.RotomScaffold
import com.duoc.rotompedia.ui.components.RotomCard
import com.duoc.rotompedia.ui.components.RotomAppearanceAction

/**
 * Regional-map entry point.
 * - Never invent a default map on first use: show the region selector (Kanto first).
 * - On later visits resume the last region chosen by the user.
 * - Back from a viewer always goes to the selector, avoiding an automatic-resume loop.
 * - No new NavHost destinations or account/Room changes are required.
 */
@Composable
fun MapFeatureScreen(navController: NavController) {
    val context = LocalContext.current
    val history = remember(context) { LastMapRegionStore(context.applicationContext) }
    var openRegion by rememberSaveable { mutableStateOf(history.lastOpenedRegion()) }
    val showSelector = { openRegion = null }

    // Internal back returns to the region selector; the selector's back exits Mapas.
    BackHandler(enabled = openRegion != null, onBack = showSelector)

    if (openRegion != null) {
        key(openRegion) {
            if (openRegion == "hoenn") {
                VerifiedHoennMapScreen(
                    onNavigateBack = showSelector,
                    onPokemonClick = { navController.navigate("pokemon_detail/$it") }
                )
            } else {
                // Other regions retain the previous viewer until their atlas is rebuilt.
                RotompediaTheme(legacy = true) {
                    MapViewerScreen(
                        region = requireNotNull(openRegion),
                        onNavigateBack = showSelector,
                        onPokemonClick = { navController.navigate("pokemon_detail/$it") }
                    )
                }
            }
        }
    } else {
        RotomScaffold("Mapas Regionales", { navController.navigateUp() },
            actions = { RotomAppearanceAction() }) {
            Column(Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Elige una región",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text(
                    text = "Cada región tiene su mapa. Los encuentros y objetos se consultan por edición dentro de cada zona.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                RegionSelectionContent(onRegionSelected = { region ->
                    // Saving happens only after a user chooses an available region.
                    history.rememberRegion(region)
                    openRegion = region
                })
            }
        }
    }
}

@Composable
fun RegionSelectionContent(onRegionSelected: (String) -> Unit) {
    val regions = MapRegionCatalogue.regions
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(regions, key = { it.slug }) { region ->
            RotomCard(
                onClick = if (!region.available) null else { { onRegionSelected(region.slug) } },
                modifier = Modifier.fillMaxWidth().heightIn(min = if (region.slug == "kanto") 84.dp else 72.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = region.name,
                        fontSize = if (region.slug == "kanto") 20.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = when (region.slug) {
                            "kanto" -> "Primera región · mapa clásico"
                            "hoenn" -> "Piloto interactivo"
                            "hisui" -> "En preparación"
                            else -> "Mapa anterior · por rehacer"
                        },
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewerScreen(
    region: String,
    onNavigateBack: () -> Unit = {},
    onPokemonClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    val resourceName = when (region) {
        "kanto" -> "kantomap"
        else -> "map_$region"
    }
    val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)

    val displayName = when (region.lowercase()) {
        "teselia" -> "Teselia (Unova)"
        else -> region.replaceFirstChar { it.uppercase() }
    }

    // Datos de rutas y ubicaciones interactivas
    val locations = remember(region) { RegionLocationData.getLocationsForRegion(region) }
    var selectedLocation by remember { mutableStateOf<RouteLocation?>(null) }
    var showPins by remember { mutableStateOf(false) } // Legacy coordinates have not been audited

    // Estados para zoom y paneo
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        if (newScale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mapa de $displayName", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (locations.isNotEmpty()) {
                            Text(
                                "${locations.size} puntos históricos sin verificar",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (locations.isNotEmpty()) {
                        IconButton(onClick = { showPins = !showPins }) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = if (showPins) "Ocultar Pines" else "Mostrar Pines",
                                tint = if (showPins) rotomForm.primaryColor else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (scale > 1f) {
                        IconButton(onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        }) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "Restablecer Zoom",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD11527),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0F1115))
        ) {
            val containerWidth = maxWidth
            val containerHeight = maxHeight

            if (resourceId != 0) {
                val painter = painterResource(id = resourceId)
                val intrinsicSize = painter.intrinsicSize
                val imageRatio = if (intrinsicSize.height > 0f) intrinsicSize.width / intrinsicSize.height else 1.136f
                val containerRatio = if (containerHeight.value > 0f) containerWidth.value / containerHeight.value else 1f

                // Dimensiones exactas calculadas con ContentScale.Fit
                val (renderedWidth, renderedHeight) = if (imageRatio > containerRatio) {
                    val rw = containerWidth
                    val rh = containerWidth / imageRatio
                    rw to rh
                } else {
                    val rh = containerHeight
                    val rw = containerHeight * imageRatio
                    rw to rh
                }

                // Viewport del mapa con soporte para zoom y paneo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        }
                        .transformable(state = transformState),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(renderedWidth, renderedHeight)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Mapa de $displayName",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        // Pines interactivos colocados en coordenadas relativas
                        if (showPins && locations.isNotEmpty()) {
                            locations.forEach { location ->
                                val pinX = renderedWidth * location.relativeX
                                val pinY = renderedHeight * location.relativeY

                                Box(
                                    modifier = Modifier
                                        .offset(x = pinX - 16.dp, y = pinY - 16.dp)
                                ) {
                                    MapPinMarker(
                                        location = location,
                                        onClick = { selectedLocation = location }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mapa de '$displayName' no encontrado.",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Carrusel inferior de rutas / radar Rotom
            if (locations.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF16181F).copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.45f)),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("📡", fontSize = 13.sp)
                                Text(
                                    text = "Radar Rotom • ${locations.size} Ubicaciones",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = rotomForm.primaryColor
                                )
                            }
                            Text(
                                text = if (scale > 1f) "Zoom ${(scale * 100).toInt()}%" else "Toca para explorar",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(locations) { loc ->
                                Surface(
                                    onClick = { selectedLocation = loc },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedLocation?.id == loc.id) rotomForm.primaryColor.copy(alpha = 0.25f) else Color(0xFF222631),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selectedLocation?.id == loc.id) rotomForm.primaryColor else Color(0xFF333845)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = loc.typeEmoji, fontSize = 13.sp)
                                        Text(
                                            text = loc.name,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (loc.wildEncounters.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = rotomForm.primaryColor.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "${loc.wildEncounters.size} pkmn",
                                                    color = rotomForm.primaryColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Banner informativo para regiones que aún no tienen rutas cargadas
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161616).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color(0xFF61DAF6).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚡", fontSize = 16.sp)
                        Text(
                            text = "Cartografía histórica: encuentros y pines de $displayName pendientes de verificar.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet al hacer clic en un punto de interés
    selectedLocation?.let { loc ->
        LocationDetailBottomSheet(
            location = loc,
            onDismiss = { selectedLocation = null },
            onPokemonClick = { pokemonId ->
                selectedLocation = null
                onPokemonClick(pokemonId)
            }
        )
    }
}

/**
 * Marcador de pin interactivo con pulso animado para el radar del mapa regional.
 */
@Composable
private fun MapPinMarker(
    location: RouteLocation,
    onClick: () -> Unit
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF1E222B).copy(alpha = 0.92f),
        border = BorderStroke(2.dp, rotomForm.primaryColor),
        shadowElevation = 6.dp,
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = location.typeEmoji,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
