package com.duoc.rotompedia.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.model.PokemonData
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.PokemonVersusViewModel
import com.duoc.rotompedia.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonVersusScreen(
    versusViewModel: PokemonVersusViewModel,
    pokemonViewModel: PokemonViewModel,
    onNavigateBack: () -> Unit
) {
    val p1 by versusViewModel.pokemon1.collectAsState()
    val p2 by versusViewModel.pokemon2.collectAsState()
    val isLoadingP1 by versusViewModel.isLoadingP1.collectAsState()
    val isLoadingP2 by versusViewModel.isLoadingP2.collectAsState()
    val analysis by versusViewModel.matchupAnalysis.collectAsState()

    var selectingForSide by remember { mutableStateOf<Int?>(null) } // 1 for P1, 2 for P2

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Comparador Versus",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con las dos tarjetas enfrentadas y el badge VS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tarjeta P1
                    Box(modifier = Modifier.weight(1f)) {
                        VersusPokemonCard(
                            pokemon = p1,
                            isLoading = isLoadingP1,
                            accentColor = Color(0xFF61DAF6),
                            onChangeClick = { selectingForSide = 1 }
                        )
                    }

                    // Badge VS
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFF6D00), Color(0xFFD50000))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Tarjeta P2
                    Box(modifier = Modifier.weight(1f)) {
                        VersusPokemonCard(
                            pokemon = p2,
                            isLoading = isLoadingP2,
                            accentColor = Color(0xFFFF5252),
                            onChangeClick = { selectingForSide = 2 }
                        )
                    }
                }
            }

            // Dictamen táctico de Rotom
            if (analysis != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2430)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF61DAF6).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚡", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Veredicto Rotom",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF61DAF6)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = analysis!!.rotomVerdict,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Comparación de daño elemental directo
            if (analysis != null && p1 != null && p2 != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚔️ Eficacia Elemental Directa",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${p1!!.name.replaceFirstChar { it.uppercase() }} ➔ ${p2!!.name.replaceFirstChar { it.uppercase() }}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    DamageMultiplierBadge(multiplier = analysis!!.p1AgainstP2Multiplier)
                                }

                                Text(
                                    text = "⚡",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${p2!!.name.replaceFirstChar { it.uppercase() }} ➔ ${p1!!.name.replaceFirstChar { it.uppercase() }}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    DamageMultiplierBadge(multiplier = analysis!!.p2AgainstP1Multiplier)
                                }
                            }
                        }
                    }
                }
            }

            // Comparación gráfica de estadísticas base
            if (p1 != null && p2 != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📊 Comparación de Estadísticas Base",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            val p1Stats = p1!!.stats.associate { it.stat.name to it.baseStat }
                            val p2Stats = p2!!.stats.associate { it.stat.name to it.baseStat }

                            val statLabels = listOf(
                                "hp" to "PS",
                                "attack" to "Ataque",
                                "defense" to "Defensa",
                                "special-attack" to "Atq. Esp.",
                                "special-defense" to "Def. Esp.",
                                "speed" to "Velocidad"
                            )

                            statLabels.forEach { (statKey, label) ->
                                val val1 = p1Stats[statKey] ?: 0
                                val val2 = p2Stats[statKey] ?: 0
                                StatComparisonRow(
                                    label = label,
                                    val1 = val1,
                                    val2 = val2,
                                    color1 = Color(0xFF61DAF6),
                                    color2 = Color(0xFFFF5252)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Total BST
                            val bst1 = p1Stats.values.sum()
                            val bst2 = p2Stats.values.sum()
                            Divider(color = Color(0xFF2E2E2E), modifier = Modifier.padding(vertical = 4.dp))
                            StatComparisonRow(
                                label = "TOTAL BST",
                                val1 = bst1,
                                val2 = bst2,
                                maxExpected = 780,
                                color1 = Color(0xFF61DAF6),
                                color2 = Color(0xFFFF5252),
                                isTotal = true
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal selector de Pokémon
    if (selectingForSide != null) {
        val targetSide = selectingForSide!!
        PokemonPickerDialog(
            pokemonViewModel = pokemonViewModel,
            onDismiss = { selectingForSide = null },
            onPokemonSelected = { id, _, _, _, _, _, _, _, _, _, _ ->
                if (targetSide == 1) {
                    versusViewModel.loadPokemon1(id)
                } else {
                    versusViewModel.loadPokemon2(id)
                }
                selectingForSide = null
            }
        )
    }
}

@Composable
fun VersusPokemonCard(
    pokemon: PokemonData?,
    isLoading: Boolean,
    accentColor: Color,
    onChangeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChangeClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(28.dp))
                }
            } else if (pokemon != null) {
                val officialArt = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.id}.png"
                AsyncImage(
                    model = officialArt,
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { slot ->
                        BadgeTipo(tipo = slot.type.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2A2A2A)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Cambiar",
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cambiar",
                        fontSize = 11.sp,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DamageMultiplierBadge(multiplier: Float) {
    val (text, bgColor, textColor) = when {
        multiplier >= 4.0f -> Triple("4x Súper Eficaz", Color(0xFFD50000), Color.White)
        multiplier in 1.9f..3.9f -> Triple("2x Súper Eficaz", Color(0xFFFF6D00), Color.White)
        multiplier == 1.0f -> Triple("1x Normal", Color(0xFF37474F), Color.White)
        multiplier == 0.0f -> Triple("0x Inmune", Color(0xFF212121), Color.Gray)
        else -> Triple("${multiplier}x No muy eficaz", Color(0xFF1565C0), Color.White)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatComparisonRow(
    label: String,
    val1: Int,
    val2: Int,
    maxExpected: Int = 200,
    color1: Color,
    color2: Color,
    isTotal: Boolean = false
) {
    val animatedProgress1 by animateFloatAsState(
        targetValue = (val1.toFloat() / maxExpected).coerceIn(0.05f, 1f),
        animationSpec = tween(600),
        label = "p1_stat"
    )
    val animatedProgress2 by animateFloatAsState(
        targetValue = (val2.toFloat() / maxExpected).coerceIn(0.05f, 1f),
        animationSpec = tween(600),
        label = "p2_stat"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$val1",
                fontSize = if (isTotal) 14.sp else 12.sp,
                fontWeight = if (val1 >= val2) FontWeight.Black else FontWeight.Normal,
                color = if (val1 > val2) color1 else Color.LightGray
            )

            Text(
                text = label,
                fontSize = if (isTotal) 13.sp else 11.sp,
                fontWeight = if (isTotal) FontWeight.Black else FontWeight.Bold,
                color = if (isTotal) Color(0xFFFFD54F) else Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = "$val2",
                fontSize = if (isTotal) 14.sp else 12.sp,
                fontWeight = if (val2 >= val1) FontWeight.Black else FontWeight.Normal,
                color = if (val2 > val1) color2 else Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barras duales enfrentadas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTotal) 10.dp else 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra P1 (crece de derecha a izquierda o de izquierda a derecha)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress1)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        .background(if (val1 >= val2) color1 else color1.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Barra P2 (crece de izquierda a derecha)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress2)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(if (val2 >= val1) color2 else color2.copy(alpha = 0.5f))
                )
            }
        }
    }
}
