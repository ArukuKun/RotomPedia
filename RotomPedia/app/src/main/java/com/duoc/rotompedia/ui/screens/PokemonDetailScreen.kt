// PokemonDetailScreen.kt
package com.duoc.rotompedia.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.viewmodel.EvolutionState
import com.duoc.rotompedia.data.PokemonFormCatalog
import com.duoc.rotompedia.viewmodel.PokemonFormsState
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.viewmodel.PokemonDetailState
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import com.duoc.rotompedia.data.model.StatInfo
import com.duoc.rotompedia.viewmodel.TypeRelationsState
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.util.RotomCommentaryEngine
import com.duoc.rotompedia.viewmodel.EvolutionStage
import com.duoc.rotompedia.viewmodel.PokemonViewModelFactory

@Composable
private fun getStatColor(value: Int): Color {
    return when {
        value < 50 -> Color(0xFFF44336)
        value < 80 -> Color(0xFFFF9800)
        value < 110 -> Color(0xFF4CAF50)
        else -> Color(0xFF2196F3)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: String,
    onNavigateBack: () -> Unit,
    viewModelFactory: PokemonViewModelFactory,
    onItemClick: (String) -> Unit = {},
    onPokemonClick: (String) -> Unit = {},
    onCatchRateClick: (Int) -> Unit = {}
) {
    val pokemonViewModel: PokemonViewModel = viewModel(factory = viewModelFactory)
    var selectedPokemonId by rememberSaveable(pokemonId) { mutableStateOf(pokemonId) }

    LaunchedEffect(selectedPokemonId) {
        pokemonViewModel.loadPokemonDetails(selectedPokemonId)
        pokemonViewModel.loadEvolutionChain(selectedPokemonId)
    }

    val detailState by pokemonViewModel.pokemonDetailState.collectAsState()
    val evolutionState by pokemonViewModel.evolutionState.collectAsState()
    val typeRelationsState by pokemonViewModel.typeRelationsState.collectAsState()

    // Cargar el estado de favorito de manera reactiva
    val pokemonIdInt = selectedPokemonId.toIntOrNull() ?: 0
    val favorites by pokemonViewModel.favoritePokemonIds.collectAsState()
    val isFavorite = pokemonIdInt in favorites

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (detailState) {
                            is PokemonDetailState.Success -> {
                                val pokemon = (detailState as PokemonDetailState.Success).pokemonData
                                "${nationalNumberLabel(pokemonViewModel, pokemon.id)} · ${PokemonFormCatalog.displayName(pokemon.name)}"
                            }
                            else -> "Detalle Pokémon"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E222B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color(0xFF61DAF6)
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (detailState is PokemonDetailState.Success && pokemonIdInt > 0) {
                        val pokemonName = (detailState as PokemonDetailState.Success).pokemonData.name

                        IconButton(
                            onClick = {
                                pokemonViewModel.toggleFavorite(
                                    pokemonId = pokemonIdInt,
                                    pokemonName = pokemonName,
                                    isCurrentlyFavorite = isFavorite
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Eliminar de favoritos" else "Añadir a favoritos",
                                tint = if (isFavorite) Color(0xFFFFD700) else Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (val currentState = detailState) {
                is PokemonDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PokemonDetailState.Success -> {
                    LaunchedEffect(key1 = currentState.pokemonData.types) {
                        pokemonViewModel.loadTypeRelations(currentState.pokemonData.types)
                    }

                    PokemonDetailContent(
                        pokemon = currentState.pokemonData,
                        evolutionState = evolutionState,
                        typeRelationsState = typeRelationsState,
                        pokemonViewModel = pokemonViewModel,
                        onFormClick = { selectedPokemonId = it.toString() },
                        onPokemonClick = onPokemonClick,
                        onItemClick = onItemClick,
                        onCatchRateClick = onCatchRateClick
                    )
                }
                is PokemonDetailState.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PokemonDetailContent(
    pokemon: PokemonData,
    evolutionState: EvolutionState,
    typeRelationsState: TypeRelationsState,
    pokemonViewModel: PokemonViewModel,
    onItemClick: (String) -> Unit = {},
    onFormClick: (Int) -> Unit = {},
    onPokemonClick: (String) -> Unit = {},
    onCatchRateClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var isShiny by remember(pokemon.id) { mutableStateOf(false) }
    var isPlayingCry by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val speciesId = pokemon.species?.url?.trimEnd('/')?.substringAfterLast('/')?.toIntOrNull()
        ?: PokemonFormCatalog.find(pokemon.name)?.speciesId ?: pokemon.id
    val availableForms by pokemonViewModel.formVarieties.collectAsState()
    val formsState by pokemonViewModel.pokemonFormsState.collectAsState()
    LaunchedEffect(speciesId) {
        pokemonViewModel.loadFormVarieties(speciesId)
    }

    DisposableEffect(pokemon.id) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlayingCry = false
        }
    }

    val displayNumber = nationalNumberLabel(pokemonViewModel, pokemon.id)
    val indexedEntries by pokemonViewModel.explorerEntries.collectAsState()
    val selectedEntry = indexedEntries.firstOrNull { it.pokemonId == pokemon.id }
    val speciesCategory by pokemonViewModel.speciesCategory.collectAsState()
    val displayName = PokemonFormCatalog.displayName(pokemon.name)
    val displayTypes = pokemon.types.map { it.type.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            PokemonImage(
                pokemonId = pokemon.id,
                shiny = isShiny,
                style = com.duoc.rotompedia.data.images.PokemonImageStyle.ARTWORK,
                sprites = pokemon.sprites,
                contentDescription = "Imagen de $displayName",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Fila de acciones interactivas: Toggle Variocolor y Rugido Oficial
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            run {
                // Botón Variocolor (Shiny)
                FilterChip(
                    selected = isShiny,
                    onClick = { isShiny = !isShiny },
                    label = {
                        Text(
                            text = if (isShiny) "✨ Variocolor" else "Normal",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Alternar Variocolor",
                            modifier = Modifier.size(16.dp),
                            tint = if (isShiny) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF263238),
                        selectedLabelColor = Color(0xFFFFD700)
                    )
                )
            }

            // Botón Rugido (Cry)
            FilledTonalButton(
                onClick = {
                    try {
                        mediaPlayer?.release()
                        val cryUrl = pokemon.cries?.latest
                            ?: "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/${pokemon.id}.ogg"

                        val player = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                            setDataSource(cryUrl)
                            prepareAsync()
                            setOnPreparedListener { mp ->
                                isPlayingCry = true
                                mp.start()
                            }
                            setOnCompletionListener {
                                isPlayingCry = false
                            }
                            setOnErrorListener { _, _, _ ->
                                isPlayingCry = false
                                true
                            }
                        }
                        mediaPlayer = player
                    } catch (e: Exception) {
                        isPlayingCry = false
                    }
                },
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isPlayingCry) Color(0xFFD11527) else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isPlayingCry) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Rugido Pokémon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlayingCry) "Rugiendo..." else "Rugido",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Botón Ratio de Captura
            FilledTonalButton(
                onClick = { onCatchRateClick(speciesId) },
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF263238),
                    contentColor = Color(0xFF61DAF6)
                )
            ) {
                Text("🎯", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Captura",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "$displayNumber - $displayName",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            displayTypes.forEach { originalType ->
                val translatedType = traducirTipo(originalType)
                TypeChip(originalType = originalType, translatedType = translatedType)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Widget Interactivo de Comentarios RotomDex
        val hpStat = pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0
        val atkStat = pokemon.stats.find { it.stat.name == "attack" }?.baseStat ?: 0
        val defStat = pokemon.stats.find { it.stat.name == "defense" }?.baseStat ?: 0
        val spaStat = pokemon.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0
        val spdStat = pokemon.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0
        val speStat = pokemon.stats.find { it.stat.name == "speed" }?.baseStat ?: 0

        RotomDialogueBox(
            pokemonId = pokemon.id,
            pokemonName = displayName,
            types = displayTypes,
            hp = hpStat,
            attack = atkStat,
            defense = defStat,
            specialAttack = spaStat,
            specialDefense = spdStat,
            speed = speStat
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Descripción de la especie (no específica de esta variedad)",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (evolutionState is EvolutionState.Success) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text(
                    text = if (selectedEntry?.isDefault == true) evolutionState.description else
                        "No hay una descripción verificada para esta variedad. PokeAPI ofrece textos de la especie, que pueden referirse a otra forma; por eso no se muestran aquí.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (evolutionState is EvolutionState.Error) {
            Text(evolutionState.message, color = MaterialTheme.colorScheme.error)
        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        PokemonBiologyCard(pokemon, speciesId,
            selectedEntry?.speciesName ?: pokemon.species?.name?.replaceFirstChar { it.uppercase() } ?: "—",
            speciesCategory, onRetryCategory = { pokemonViewModel.loadEvolutionChain(pokemon.id.toString()) }) {
            PokemonAbilitiesSection(pokemonId = pokemon.id, onPokemonClick = onPokemonClick)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Estadísticas Base",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            val totalBst = hpStat + atkStat + defStat + spaStat + spdStat + speStat
            Text(
                text = "BST: $totalBst",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF61DAF6)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val statsToRender = pokemon.stats

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                statsToRender.forEach { statInfo ->
                    StatRow(statInfo = statInfo)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Relaciones de Tipo",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Box(Modifier.padding(16.dp)) { TypeRelationsView(state = typeRelationsState) }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Línea Evolutiva",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (evolutionState) {
            is EvolutionState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is EvolutionState.Success -> {
                EvolutionChainColumn(
                    chain = evolutionState.evolutionChain,
                    currentPokemonId = speciesId.toString(),
                    onPokemonClick = onPokemonClick,
                    onItemClick = onItemClick
                )
            }
            is EvolutionState.Error -> {
                Text(
                    text = evolutionState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Formas", fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        if (formsState is PokemonFormsState.Loading) {
            CircularProgressIndicator(Modifier.size(24.dp))
        }
        if (formsState is PokemonFormsState.Error) {
            Text((formsState as PokemonFormsState.Error).message, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { pokemonViewModel.loadFormVarieties(speciesId) }) { Text("Reintentar") }
        }
        if (availableForms.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                items(availableForms, key = { it.name }) { form ->
                    val selected = form.id == pokemon.id
                    Card(
                        onClick = { if (!selected) onFormClick(form.id) },
                        modifier = Modifier.width(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (selected) 2.dp else 1.dp,
                            if (selected) Color(0xFF61DAF6) else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            PokemonImage(pokemonId = form.id, savedUrl = form.spriteUrl, contentDescription = form.formDisplayName,
                                modifier = Modifier.size(96.dp))
                            Text(form.formDisplayName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text(if (selected) "✓ Seleccionada" else if (form.isDefault) "Predeterminada" else
                                PokemonFormCatalog.find(form.name)?.formTag ?: "Variante", fontSize = 12.sp,
                                color = if (selected) Color(0xFF61DAF6) else MaterialTheme.colorScheme.onSurfaceVariant)
                            form.types.forEach { type -> Text(traducirTipo(type), fontSize = 12.sp) }
                        }
                    }
                }
            }
        } else if (formsState is PokemonFormsState.Success) {
            Text("Esta especie no tiene otras variedades de combate en PokeAPI.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(24.dp))
        PokemonMovesSection(pokemonId = pokemon.id)
        PokemonLocationsSection(pokemonId = pokemon.id)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TypeChip(originalType: String, translatedType: String) {
    val typeColor = when (originalType) {
        "grass" -> Color(0xFF78C850)
        "fire" -> Color(0xFFF08030)
        "water" -> Color(0xFF6890F0)
        "bug" -> Color(0xFFA8B820)
        "normal" -> Color(0xFFA8A878)
        "poison" -> Color(0xFFA040A0)
        "electric" -> Color(0xFFF8D030)
        "ground" -> Color(0xFFE0C068)
        "fairy" -> Color(0xFFEE99AC)
        "fighting" -> Color(0xFFC03028)
        "psychic" -> Color(0xFFF85888)
        "rock" -> Color(0xFFB8A038)
        "ghost" -> Color(0xFF705898)
        "ice" -> Color(0xFF98D8D8)
        "dragon" -> Color(0xFF7038F8)
        "dark" -> Color(0xFF705848)
        "steel" -> Color(0xFFB8B8D0)
        "flying" -> Color(0xFFA890F0)
        else -> Color.Gray
    }
    Box(
        modifier = Modifier
            .background(typeColor, CircleShape)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = translatedType.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatRow(statInfo: StatInfo) {
    val statName = when (statInfo.stat.name) {
        "hp" -> "PS"
        "attack" -> "Ataque"
        "defense" -> "Defensa"
        "special-attack" -> "At. Esp."
        "special-defense" -> "Def. Esp."
        "speed" -> "Velocidad"
        else -> statInfo.stat.name.replaceFirstChar { it.uppercase() }
    }

    val statColor = getStatColor(statInfo.baseStat)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName,
            modifier = Modifier.width(80.dp),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = statInfo.baseStat.toString(),
            modifier = Modifier.width(40.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { statInfo.baseStat / 255f },
            modifier = Modifier
                .height(12.dp)
                .clip(CircleShape),
            color = statColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun EvolutionChainColumn(
    chain: List<EvolutionStage>,
    onItemClick: (String) -> Unit = {},
    currentPokemonId: String = "",
    onPokemonClick: (String) -> Unit = {}
) {
    if (chain.size <= 1) {
        Text(
            text = "Este Pokémon no evoluciona.",
            color = MaterialTheme.colorScheme.onBackground
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chain.forEachIndexed { index, evolutionStage ->
            val pokemonId = evolutionStage.id
            val isCurrent = pokemonId == currentPokemonId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                    .clickable { onPokemonClick(pokemonId) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (index > 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Evoluciona",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }

                PokemonImage(
                    pokemonId = pokemonId.toIntOrNull() ?: 0,
                    contentDescription = "Imagen de ${evolutionStage.name}",
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${pokemonId.toString().padStart(3, '0')} ${evolutionStage.name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "● Actual",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    evolutionStage.itemSlugs.forEach { slug ->
                        TextButton(onClick = { onItemClick(slug) }) { Text("Consultar objeto de evolución · $slug", fontSize = 12.sp) }
                    }
                    if (!evolutionStage.isBaseForm) {
                        Text(
                            text = "Desde ${chain.firstOrNull { it.id == evolutionStage.evolvesFromId }?.name ?: "etapa anterior"}: ${evolutionStage.evolutionRequirement}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Forma Base",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeRelationsView(state: TypeRelationsState) {
    when (state) {
        is TypeRelationsState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        is TypeRelationsState.Success -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.superWeakAgainst.isNotEmpty()) {
                    Text(
                        text = "Superdébil contra (4x):",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.superWeakAgainst.forEach { typeName ->
                            TypeChip(originalType = typeName, translatedType = traducirTipo(typeName))
                        }
                    }
                }

                if (state.weakAgainst.isNotEmpty()) {
                    Text(
                        text = "Débil contra (2x):",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.weakAgainst.forEach { typeName ->
                            TypeChip(originalType = typeName, translatedType = traducirTipo(typeName))
                        }
                    }
                }

                if (state.immuneAgainst.isNotEmpty()) {
                    Text(
                        text = "Inmune a (0x):",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575),
                        fontSize = 14.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.immuneAgainst.forEach { typeName ->
                            TypeChip(originalType = typeName, translatedType = traducirTipo(typeName))
                        }
                    }
                }

                if (state.resistantAgainst.isNotEmpty()) {
                    Text(
                        text = "Resistente a (½x):",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32),
                        fontSize = 14.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.resistantAgainst.forEach { typeName ->
                            TypeChip(originalType = typeName, translatedType = traducirTipo(typeName))
                        }
                    }
                }

                if (state.superResistantAgainst.isNotEmpty()) {
                    Text(
                        text = "Superresistente a (¼x):",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        fontSize = 14.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.superResistantAgainst.forEach { typeName ->
                            TypeChip(originalType = typeName, translatedType = traducirTipo(typeName))
                        }
                    }
                }

                if (state.strongAgainst.isNotEmpty()) {
                    Text(
                        text = "Efectivo con STAB contra (2x):",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.strongAgainst.forEach { typeName ->
                            TypeChip(originalType = typeName, translatedType = traducirTipo(typeName))
                        }
                    }
                }
            }
        }
        is TypeRelationsState.Error -> {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun RotomDialogueBox(
    pokemonId: Int,
    pokemonName: String,
    types: List<String>,
    hp: Int = 0,
    attack: Int = 0,
    defense: Int = 0,
    specialAttack: Int = 0,
    specialDefense: Int = 0,
    speed: Int = 0,
    modifier: Modifier = Modifier
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()
    val comments = remember(pokemonId, pokemonName, types, hp, attack, defense, specialAttack, specialDefense, speed, rotomForm) {
        RotomCommentaryEngine.getComments(
            pokemonId = pokemonId,
            pokemonName = pokemonName,
            types = types,
            hp = hp,
            attack = attack,
            defense = defense,
            specialAttack = specialAttack,
            specialDefense = specialDefense,
            speed = speed,
            rotomForm = rotomForm
        )
    }
    var currentIndex by remember(pokemonId) { mutableStateOf(0) }
    val isDevFav = remember(pokemonId) { RotomCommentaryEngine.isDeveloperFavorite(pokemonId) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (comments.isNotEmpty()) {
                    currentIndex = (currentIndex + 1) % comments.size
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDevFav) Color(0xFF231F15) else Color(0xFF1E1E1E).copy(alpha = 0.95f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isDevFav) 1.5.dp else 1.dp,
            if (isDevFav) Color(0xFFFFD700) else rotomForm.primaryColor.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isDevFav) Color(0xFFFFD700).copy(alpha = 0.2f) else rotomForm.primaryColor.copy(alpha = 0.2f))
                    .border(1.dp, if (isDevFav) Color(0xFFFFD700) else rotomForm.primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (isDevFav) "✨" else rotomForm.iconEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDevFav) "✨ RotomDex • Favorito del Dev" else "RotomDex (${rotomForm.displayName})",
                        fontWeight = FontWeight.Bold,
                        color = if (isDevFav) Color(0xFFFFD700) else rotomForm.primaryColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Toca para más 💬",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comments.getOrElse(currentIndex) { comments.firstOrNull() ?: "" },
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
