package com.duoc.rotompedia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.local.CachedPokemon
import com.duoc.rotompedia.util.CatchRateCalculator
import com.duoc.rotompedia.util.CatchRateCalculator.PokeBall
import com.duoc.rotompedia.util.CatchRateCalculator.StatusCondition
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchRateCalculatorScreen(
    pokemonViewModel: PokemonViewModel,
    onNavigateBack: () -> Unit,
    initialPokemonId: Int = 1
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    // Estado del Pokémon objetivo
    var selectedId by remember { mutableIntStateOf(initialPokemonId) }
    var selectedName by remember { mutableStateOf("Bulbasaur") }
    var selectedSprite by remember { mutableStateOf("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$initialPokemonId.png") }
    var selectedTypes by remember { mutableStateOf(listOf("grass", "poison")) }
    var targetSpeed by remember { mutableIntStateOf(45) }
    var baseCatchRate by remember { mutableIntStateOf(resolveBaseCatchRate(initialPokemonId)) }

    // Modal de selección de Pokémon
    var showPokemonPicker by remember { mutableStateOf(false) }

    // Cargar Pokémon inicial desde la base de datos local
    LaunchedEffect(initialPokemonId) {
        val cached = pokemonViewModel.getCachedPokemonDirect(initialPokemonId)
        if (cached != null) {
            selectedId = cached.id
            selectedName = cached.name.replaceFirstChar { it.uppercase() }
            selectedSprite = cached.imageUrl
            selectedTypes = cached.types.split(",").filter { it.isNotBlank() }
            targetSpeed = if (cached.speed > 0) cached.speed else 60
            baseCatchRate = resolveBaseCatchRate(cached.id)
        }
    }

    // Parámetros tácticos de captura
    var hpPercent by remember { mutableFloatStateOf(100f) }
    var selectedStatus by remember { mutableStateOf(StatusCondition.NONE) }
    var selectedBall by remember { mutableStateOf(PokeBall.ULTRA_BALL) }

    // Modificadores de entorno y combate
    var isFirstTurn by remember { mutableStateOf(false) }
    var isNightOrCave by remember { mutableStateOf(false) }
    var isAlreadyCaught by remember { mutableStateOf(false) }
    var turnsPassed by remember { mutableIntStateOf(1) }

    val isWaterOrBug = selectedTypes.any { it.equals("water", ignoreCase = true) || it.equals("bug", ignoreCase = true) }

    // Cálculo reactivo
    val catchResult = remember(
        baseCatchRate, hpPercent, selectedBall, selectedStatus,
        isFirstTurn, isNightOrCave, isWaterOrBug, isAlreadyCaught,
        turnsPassed, targetSpeed
    ) {
        CatchRateCalculator.calculate(
            baseCatchRate = baseCatchRate,
            maxHp = 100,
            currentHp = hpPercent.roundToInt().coerceIn(1, 100),
            ball = selectedBall,
            status = selectedStatus,
            isFirstTurn = isFirstTurn,
            isNightOrCave = isNightOrCave,
            isWaterOrBug = isWaterOrBug,
            isAlreadyCaught = isAlreadyCaught,
            turnsPassed = turnsPassed,
            targetBaseSpeed = targetSpeed
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Calculadora de Captura", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Fórmulas Oficiales Gen 8 / 9",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
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
                    IconButton(onClick = {
                        hpPercent = 100f
                        selectedStatus = StatusCondition.NONE
                        selectedBall = PokeBall.ULTRA_BALL
                        isFirstTurn = false
                        isNightOrCave = false
                        isAlreadyCaught = false
                        turnsPassed = 1
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0F1115))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ================= 1. CABECERA: POKÉMON OBJETIVO =================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E26)),
                    border = BorderStroke(1.5.dp, rotomForm.primaryColor.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF262A36)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = selectedSprite,
                                    contentDescription = selectedName,
                                    modifier = Modifier.size(68.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "#$selectedId - ${selectedName.replaceFirstChar { it.uppercase() }}",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedTypes.forEach { type ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = getTipoColor(type).copy(alpha = 0.85f)
                                        ) {
                                            Text(
                                                text = traducirTipo(type),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ratio base: $baseCatchRate • Vel: $targetSpeed",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Button(
                                onClick = { showPokemonPicker = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = rotomForm.primaryColor)
                            ) {
                                Text("Cambiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ratio base editable
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ratio de captura base:",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = baseCatchRate == 3,
                                    onClick = { baseCatchRate = 3 },
                                    label = { Text("3 (Legendario)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = baseCatchRate == 45,
                                    onClick = { baseCatchRate = 45 },
                                    label = { Text("45 (Inicial)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = baseCatchRate == 255,
                                    onClick = { baseCatchRate = 255 },
                                    label = { Text("255 (Común)", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // ================= 2. DASHBOARD DE RESULTADO TÁCTICO =================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151820)),
                    border = BorderStroke(2.dp, getProbColor(catchResult.singleThrowProbPercent))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PROBABILIDAD POR LANZAMIENTO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = String.format("%.1f%%", catchResult.singleThrowProbPercent),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = getProbColor(catchResult.singleThrowProbPercent)
                        )

                        Text(
                            text = "~${String.format("%.1f", catchResult.expectedThrows)} ${selectedBall.displayName}s estimadas para captura",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Métricas en 3, 5 y 10 lanzamientos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatPill(title = "En 3 tiros", value = String.format("%.0f%%", catchResult.prob3ThrowsPercent))
                            StatPill(title = "En 5 tiros", value = String.format("%.0f%%", catchResult.prob5ThrowsPercent))
                            StatPill(title = "En 10 tiros", value = String.format("%.0f%%", catchResult.prob10ThrowsPercent))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Consejo Rotom
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = rotomForm.primaryColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = catchResult.rotomTip,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.95f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // ================= 3. SALUD DEL POKÉMON (PS %) =================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E26))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Salud Restante: ${hpPercent.roundToInt()}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Barra de vida gráfica
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF333845))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(hpPercent / 100f)
                                        .background(getHpBarColor(hpPercent))
                                )
                            }
                        }

                        Slider(
                            value = hpPercent,
                            onValueChange = { hpPercent = it },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = getHpBarColor(hpPercent),
                                activeTrackColor = getHpBarColor(hpPercent)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetHpButton(label = "100%", onClick = { hpPercent = 100f }, selected = hpPercent == 100f)
                            PresetHpButton(label = "50%", onClick = { hpPercent = 50f }, selected = hpPercent == 50f)
                            PresetHpButton(label = "20%", onClick = { hpPercent = 20f }, selected = hpPercent == 20f)
                            PresetHpButton(
                                label = "✨ 1 PS (Falso Tortazo)",
                                onClick = { hpPercent = 1f },
                                selected = hpPercent == 1f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ================= 4. PROBLEMA DE ESTADO =================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E26))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Problema de Estado:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(StatusCondition.values()) { status ->
                                val isSelected = selectedStatus == status
                                Surface(
                                    onClick = { selectedStatus = status },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) rotomForm.primaryColor.copy(alpha = 0.25f) else Color(0xFF262A36),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) rotomForm.primaryColor else Color(0xFF383E4E)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(status.emoji, fontSize = 14.sp)
                                        Text(
                                            text = status.displayName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (status.multiplier > 1.0f) {
                                            Text(
                                                text = "${status.multiplier}x",
                                                color = rotomForm.primaryColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================= 5. SELECCIÓN DE POKÉ BALL =================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E26))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Poké Ball Utilizada:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PokeBall.values()) { ball ->
                                val isSelected = selectedBall == ball
                                Surface(
                                    onClick = { selectedBall = ball },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) rotomForm.primaryColor.copy(alpha = 0.25f) else Color(0xFF242834),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) rotomForm.primaryColor else Color(0xFF333845)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(ball.emoji, fontSize = 16.sp)
                                            Text(
                                                text = ball.displayName,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ball.description,
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================= 6. CONDICIONES TÁCTICAS DEL COMBATE =================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E26))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Condiciones del Combate:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ConditionToggleRow(
                            title = "Primer turno del combate",
                            subtitle = "Activa bonus x5.0 de la Quick Ball",
                            checked = isFirstTurn,
                            onCheckedChange = { isFirstTurn = it }
                        )

                        HorizontalDivider(color = Color(0xFF2B303D), modifier = Modifier.padding(vertical = 8.dp))

                        ConditionToggleRow(
                            title = "Es de noche o dentro de una cueva",
                            subtitle = "Activa bonus x3.0 de la Ocaso Ball",
                            checked = isNightOrCave,
                            onCheckedChange = { isNightOrCave = it }
                        )

                        HorizontalDivider(color = Color(0xFF2B303D), modifier = Modifier.padding(vertical = 8.dp))

                        ConditionToggleRow(
                            title = "Especie ya registrada en la Pokédex",
                            subtitle = "Activa bonus x3.5 de la Acopio Ball",
                            checked = isAlreadyCaught,
                            onCheckedChange = { isAlreadyCaught = it }
                        )

                        HorizontalDivider(color = Color(0xFF2B303D), modifier = Modifier.padding(vertical = 8.dp))

                        // Slider de turnos para Turno Ball
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Turnos transcurridos: $turnsPassed", fontSize = 13.sp, color = Color.White)
                                Text("Aumenta el poder de la Turno Ball hasta x4.0", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Slider(
                            value = turnsPassed.toFloat(),
                            onValueChange = { turnsPassed = it.roundToInt() },
                            valueRange = 1f..15f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = rotomForm.primaryColor,
                                activeTrackColor = rotomForm.primaryColor
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal para cambiar el Pokémon objetivo
    if (showPokemonPicker) {
        val cachedPokemonList: List<CachedPokemon> by pokemonViewModel.allCachedPokemon.collectAsState(initial = emptyList())
        var searchQuery by remember { mutableStateOf("") }

        val filteredList = remember(searchQuery, cachedPokemonList) {
            if (searchQuery.isBlank()) cachedPokemonList
            else cachedPokemonList.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.id.toString() == searchQuery.trim()
            }
        }

        AlertDialog(
            onDismissRequest = { showPokemonPicker = false },
            title = { Text("Seleccionar Pokémon Objetivo", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(380.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por nombre o #ID...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filteredList) { pkm ->
                            Surface(
                                onClick = {
                                    selectedId = pkm.id
                                    selectedName = pkm.name
                                    selectedSprite = pkm.imageUrl
                                    selectedTypes = pkm.types.split(",").filter { it.isNotBlank() }
                                    targetSpeed = if (pkm.speed > 0) pkm.speed else 60
                                    baseCatchRate = resolveBaseCatchRate(pkm.id)
                                    showPokemonPicker = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF232733),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = pkm.imageUrl,
                                        contentDescription = pkm.name,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "#${pkm.id} - ${pkm.name.replaceFirstChar { it.uppercase() }}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Vel: ${pkm.speed} • Ratio: ${resolveBaseCatchRate(pkm.id)}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPokemonPicker = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF222633),
        border = BorderStroke(1.dp, Color(0xFF333848))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun PresetHpButton(
    label: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF333D4F) else Color(0xFF222633),
        border = BorderStroke(1.dp, if (selected) Color(0xFF61DAF6) else Color(0xFF383E50)),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color(0xFF61DAF6) else Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConditionToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun getProbColor(prob: Float): Color {
    return when {
        prob >= 75f -> Color(0xFF4CAF50)
        prob >= 40f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getHpBarColor(percent: Float): Color {
    return when {
        percent > 50f -> Color(0xFF4CAF50)
        percent > 20f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

/**
 * Tabla canónica simplificada de ratios base de captura según el ID o arquetipo.
 */
private fun resolveBaseCatchRate(pokemonId: Int): Int {
    return when (pokemonId) {
        // Legendarios Kanto & Clásicos
        144, 145, 146, 150, 151, 243, 244, 245, 249, 250, 251,
        377, 378, 379, 380, 381, 382, 383, 384, 385, 386 -> 3

        // Iniciales (Bulbasaur, Charmander, Squirtle, etc.)
        1, 2, 3, 4, 5, 6, 7, 8, 9,
        152, 153, 154, 155, 156, 157, 158, 159, 160,
        252, 253, 254, 255, 256, 257, 258, 259, 260 -> 45

        // Especies raras / semilegendarias
        131, 143, 147, 148, 149, 246, 247, 248, 374, 375, 376 -> 45

        // Especies comunes
        10, 11, 13, 14, 16, 19, 129, 265, 266, 267 -> 255

        // Especies medias
        25, 35, 39, 133 -> 190

        else -> 120
    }
}
