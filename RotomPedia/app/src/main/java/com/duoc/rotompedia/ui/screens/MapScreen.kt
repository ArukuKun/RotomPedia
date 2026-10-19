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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFeatureScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapas Regionales") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD11527),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.rotomfondo),
                contentDescription = "Fondo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Capa semitransparente para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecciona una Región",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF61DAF6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                RegionSelectionContent(navController = navController)
            }
        }
    }
}

@Composable
fun RegionSelectionContent(navController: NavController) {
    val regions = listOf(
        "Kanto" to "kanto",
        "Johto" to "johto",
        "Hoenn" to "hoenn",
        "Sinnoh" to "sinnoh",
        "Teselia (Unova)" to "teselia",
        "Kalos" to "kalos",
        "Alola" to "alola",
        "Galar" to "galar",
        "Paldea" to "paldea"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(regions) { (displayName, internalName) ->
            Card(
                onClick = { navController.navigate("map_viewer/$internalName") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF23272E).copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF61DAF6),
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
    var showPins by remember { mutableStateOf(true) }

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
                                "${locations.size} puntos interactivos",
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
                            text = "Kanto cuenta con radar completo. ¡Rutas de $displayName en próxima expansión!",
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
