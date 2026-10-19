package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.local.CachedPokemon
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.util.StatCalculator
import com.duoc.rotompedia.util.StatCalculator.Nature
import com.duoc.rotompedia.util.StatCalculator.StatSpread
import com.duoc.rotompedia.util.StatCalculator.StatType
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCalculatorScreen(
    pokemonViewModel: PokemonViewModel,
    onNavigateBack: () -> Unit,
    initialPokemonId: Int = 1
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    // Estado del Pokémon seleccionado
    var selectedId by remember { mutableStateOf(initialPokemonId) }
    var selectedName by remember { mutableStateOf("Bulbasaur") }
    var selectedSprite by remember { mutableStateOf("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png") }
    var selectedTypes by remember { mutableStateOf(listOf("grass", "poison")) }
    var baseStats by remember {
        mutableStateOf(StatSpread(hp = 45, atk = 49, def = 49, spa = 65, spd = 65, spe = 45))
    }

    // Modal de búsqueda de Pokémon
    var showPokemonPicker by remember { mutableStateOf(false) }

    // Parámetros de la build
    var level by remember { mutableStateOf(50) } // 50 (VGC) o 100
    var selectedNature by remember { mutableStateOf(StatCalculator.ALL_NATURES.first { it.nameEn == "Modest" }) }
    var showNatureDialog by remember { mutableStateOf(false) }

    // Reparto de EVs
    var evHp by remember { mutableStateOf(0) }
    var evAtk by remember { mutableStateOf(0) }
    var evDef by remember { mutableStateOf(0) }
    var evSpa by remember { mutableStateOf(252) }
    var evSpd by remember { mutableStateOf(4) }
    var evSpe by remember { mutableStateOf(252) }

    // Reparto de IVs (31 por defecto)
    var ivHp by remember { mutableStateOf(31) }
    var ivAtk by remember { mutableStateOf(31) }
    var ivDef by remember { mutableStateOf(31) }
    var ivSpa by remember { mutableStateOf(31) }
    var ivSpd by remember { mutableStateOf(31) }
    var ivSpe by remember { mutableStateOf(31) }

    // Total de EVs asignados
    val totalEvs = evHp + evAtk + evDef + evSpa + evSpd + evSpe
    val remainingEvs = 510 - totalEvs

    // Cálculo final en tiempo real
    val calculated = remember(baseStats, evHp, evAtk, evDef, evSpa, evSpd, evSpe, ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe, level, selectedNature) {
        StatCalculator.calculateAll(
            baseStats = baseStats,
            ivs = StatSpread(ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe),
            evs = StatSpread(evHp, evAtk, evDef, evSpa, evSpd, evSpe),
            level = level,
            nature = selectedNature,
            isShedinja = selectedId == 292
        )
    }

    // Cargar Pokémon inicial desde la base de datos o PokeAPI si cambia initialPokemonId
    LaunchedEffect(initialPokemonId) {
        val cached = pokemonViewModel.getCachedPokemonDirect(initialPokemonId)
        if (cached != null) {
            selectedId = cached.id
            selectedName = cached.name.replaceFirstChar { it.uppercase() }
            selectedSprite = cached.imageUrl
            selectedTypes = cached.types.split(",").filter { it.isNotBlank() }
            baseStats = StatSpread(cached.hp, cached.attack, cached.defense, cached.specialAttack, cached.specialDefense, cached.speed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calculadora de Stats (IV/EV)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val showdownText = StatCalculator.toShowdownFormat(
                            speciesName = selectedName,
                            level = level,
                            nature = selectedNature,
                            evs = StatSpread(evHp, evAtk, evDef, evSpa, evSpd, evSpe),
                            ivs = StatSpread(ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe)
                        )
                        clipboardManager.setText(AnnotatedString(showdownText))
                        Toast.makeText(context, "¡Build copiada al portapapeles para Showdown!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Copiar Showdown", tint = rotomForm.accentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141414))
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta de Identidad del Pokémon
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPokemonPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF262626)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedSprite,
                                contentDescription = selectedName,
                                modifier = Modifier.fillMaxSize(0.9f),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#$selectedId $selectedName",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Cambiar",
                                    tint = rotomForm.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                selectedTypes.forEach { tipo ->
                                    BadgeTipo(tipo = tipo)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "BST Base: ${baseStats.total} pts",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Selector de Nivel y Naturaleza
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nivel de Batalla", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = level == 50,
                                    onClick = { level = 50 },
                                    label = { Text("Nivel 50 (VGC)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = rotomForm.primaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = level == 100,
                                    onClick = { level = 100 },
                                    label = { Text("Nivel 100") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = rotomForm.primaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF2B2B2B))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNatureDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Naturaleza", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(
                                    text = selectedNature.displayName,
                                    fontSize = 12.sp,
                                    color = rotomForm.accentColor
                                )
                            }
                            OutlinedButton(
                                onClick = { showNatureDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = rotomForm.accentColor)
                            ) {
                                Text("Cambiar")
                            }
                        }
                    }
                }
            }

            // Presets Rápidos de Entrenamiento de EVs
            item {
                Text(
                    text = "Presets Competitivos Rápidos",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        PresetChip("Sweeper Físico (252 Atk / 252 Spe)") {
                            evHp = 4; evAtk = 252; evDef = 0; evSpa = 0; evSpd = 0; evSpe = 252
                            selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Jolly" }
                        }
                    }
                    item {
                        PresetChip("Sweeper Especial (252 SpA / 252 Spe)") {
                            evHp = 4; evAtk = 0; evDef = 0; evSpa = 252; evSpd = 0; evSpe = 252
                            selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Timid" }
                        }
                    }
                    item {
                        PresetChip("Bulky Físico (252 HP / 252 Def)") {
                            evHp = 252; evAtk = 0; evDef = 252; evSpa = 0; evSpd = 4; evSpe = 0
                            selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Impish" }
                        }
                    }
                    item {
                        PresetChip("Bulky Especial (252 HP / 252 SpD)") {
                            evHp = 252; evAtk = 0; evDef = 4; evSpa = 0; evSpd = 252; evSpe = 0
                            selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Careful" }
                        }
                    }
                    item {
                        PresetChip("Reset (0 EVs)") {
                            evHp = 0; evAtk = 0; evDef = 0; evSpa = 0; evSpd = 0; evSpe = 0
                        }
                    }
                }
            }

            // Barra de Puntos de Esfuerzo (EVs)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total EVs Usados:", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "$totalEvs / 510 EVs (${remainingEvs} restantes)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalEvs > 510) Color(0xFFFF5252) else Color(0xFF69F0AE)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = (totalEvs.toFloat() / 510f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (totalEvs > 510) Color(0xFFFF5252) else rotomForm.primaryColor,
                            trackColor = Color(0xFF2A2A2A)
                        )
                    }
                }
            }

            // Tabla de las 6 Estadísticas con Sliders
            item {
                Text(
                    text = "Ajuste de IVs (0-31) y EVs (0-252)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            item {
                StatRowControl(
                    label = "PS",
                    statType = StatType.HP,
                    baseStat = baseStats.hp,
                    finalStat = calculated.hp,
                    ev = evHp,
                    iv = ivHp,
                    nature = selectedNature,
                    onEvChange = { newEv -> if (totalEvs - evHp + newEv <= 510) evHp = newEv },
                    onIvChange = { ivHp = it }
                )
            }

            item {
                StatRowControl(
                    label = "Ataque",
                    statType = StatType.ATTACK,
                    baseStat = baseStats.atk,
                    finalStat = calculated.atk,
                    ev = evAtk,
                    iv = ivAtk,
                    nature = selectedNature,
                    onEvChange = { newEv -> if (totalEvs - evAtk + newEv <= 510) evAtk = newEv },
                    onIvChange = { ivAtk = it }
                )
            }

            item {
                StatRowControl(
                    label = "Defensa",
                    statType = StatType.DEFENSE,
                    baseStat = baseStats.def,
                    finalStat = calculated.def,
                    ev = evDef,
                    iv = ivDef,
                    nature = selectedNature,
                    onEvChange = { newEv -> if (totalEvs - evDef + newEv <= 510) evDef = newEv },
                    onIvChange = { ivDef = it }
                )
            }

            item {
                StatRowControl(
                    label = "At. Esp",
                    statType = StatType.SPECIAL_ATTACK,
                    baseStat = baseStats.spa,
                    finalStat = calculated.spa,
                    ev = evSpa,
                    iv = ivSpa,
                    nature = selectedNature,
                    onEvChange = { newEv -> if (totalEvs - evSpa + newEv <= 510) evSpa = newEv },
                    onIvChange = { ivSpa = it }
                )
            }

            item {
                StatRowControl(
                    label = "Def. Esp",
                    statType = StatType.SPECIAL_DEFENSE,
                    baseStat = baseStats.spd,
                    finalStat = calculated.spd,
                    ev = evSpd,
                    iv = ivSpd,
                    nature = selectedNature,
                    onEvChange = { newEv -> if (totalEvs - evSpd + newEv <= 510) evSpd = newEv },
                    onIvChange = { ivSpd = it }
                )
            }

            item {
                StatRowControl(
                    label = "Velocidad",
                    statType = StatType.SPEED,
                    baseStat = baseStats.spe,
                    finalStat = calculated.spe,
                    ev = evSpe,
                    iv = ivSpe,
                    nature = selectedNature,
                    onEvChange = { newEv -> if (totalEvs - evSpe + newEv <= 510) evSpe = newEv },
                    onIvChange = { ivSpe = it }
                )
            }

            // Resumen Total BST
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estadística Total (BST Calculado):",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${calculated.totalBST} pts",
                            color = rotomForm.accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Selección de Naturaleza
    if (showNatureDialog) {
        AlertDialog(
            onDismissRequest = { showNatureDialog = false },
            title = { Text("Seleccionar Naturaleza", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(StatCalculator.ALL_NATURES) { nature ->
                        val isSelected = nature == selectedNature
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) rotomForm.primaryColor.copy(alpha = 0.25f) else Color(0xFF262626))
                                .border(1.dp, if (isSelected) rotomForm.primaryColor else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedNature = nature
                                    showNatureDialog = false
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nature.nameEs,
                                color = if (isSelected) rotomForm.primaryColor else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (nature.increasedStat == null) "Neutra" else "+${nature.increasedStat.shortName} / -${nature.decreasedStat?.shortName}",
                                fontSize = 11.sp,
                                color = if (nature.increasedStat != null) Color(0xFF69F0AE) else Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNatureDialog = false }) {
                    Text("Cerrar", color = rotomForm.accentColor)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    // Diálogo Selector de Pokémon
    if (showPokemonPicker) {
        PokemonPickerDialog(
            pokemonViewModel = pokemonViewModel,
            onDismiss = { showPokemonPicker = false },
            onPokemonSelected = { id, name, sprite, tipo1, tipo2, hp, atk, def, spa, spd, spe ->
                selectedId = id
                selectedName = name
                selectedSprite = sprite
                selectedTypes = listOfNotNull(tipo1, tipo2)
                baseStats = StatSpread(hp, atk, def, spa, spd, spe)
                showPokemonPicker = false
            }
        )
    }
}

@Composable
fun PresetChip(text: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text, fontSize = 11.sp, color = Color.White) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF262626))
    )
}

@Composable
fun StatRowControl(
    label: String,
    statType: StatType,
    baseStat: Int,
    finalStat: Int,
    ev: Int,
    iv: Int,
    nature: Nature,
    onEvChange: (Int) -> Unit,
    onIvChange: (Int) -> Unit
) {
    val mult = nature.getMultiplier(statType)
    val statColor = when {
        mult > 1.0f -> Color(0xFF69F0AE) // Verde si sube
        mult < 1.0f -> Color(0xFFFF5252) // Rojo si baja
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = statColor, fontSize = 13.sp)
                    if (mult > 1.0f) Text(" ▲", color = Color(0xFF69F0AE), fontSize = 10.sp)
                    if (mult < 1.0f) Text(" ▼", color = Color(0xFFFF5252), fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "(Base: $baseStat)", fontSize = 10.sp, color = Color.Gray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Final: ", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "$finalStat",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = statColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Control de EV con Slider y botones rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "EV: $ev", fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.width(55.dp))
                Slider(
                    value = ev.toFloat(),
                    onValueChange = { onEvChange((it.toInt() / 4) * 4) },
                    valueRange = 0f..252f,
                    steps = 62,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = statColor,
                        activeTrackColor = statColor,
                        inactiveTrackColor = Color(0xFF2A2A2A)
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TinyStatButton("0") { onEvChange(0) }
                    TinyStatButton("4") { onEvChange(4) }
                    TinyStatButton("252") { onEvChange(252) }
                }
            }

            // Control de IV (31, 0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "IV: $iv / 31", fontSize = 10.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = iv == 31,
                        onClick = { onIvChange(31) },
                        label = { Text("31 (Máx)", fontSize = 9.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                    FilterChip(
                        selected = iv == 0,
                        onClick = { onIvChange(0) },
                        label = { Text("0 (Mín)", fontSize = 9.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TinyStatButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
