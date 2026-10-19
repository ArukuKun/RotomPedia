package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.duoc.rotompedia.data.local.EquipoConMiembros
import com.duoc.rotompedia.data.local.MiembroEquipo
import com.duoc.rotompedia.data.model.PokemonListItem
import com.duoc.rotompedia.util.TypeEffectiveness
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import com.duoc.rotompedia.viewmodel.TeamBuilderViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.duoc.rotompedia.util.StatCalculator
import com.duoc.rotompedia.util.StatCalculator.StatSpread
import com.duoc.rotompedia.util.StatCalculator.StatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBuilderScreen(
    teamBuilderViewModel: TeamBuilderViewModel,
    pokemonViewModel: PokemonViewModel,
    onNavigateBack: () -> Unit
) {
    val equipos by teamBuilderViewModel.equipos.collectAsState()
    val selectedEquipoId by teamBuilderViewModel.selectedEquipoId.collectAsState()
    val selectedEquipo by teamBuilderViewModel.selectedEquipo.collectAsState()
    val analysis by teamBuilderViewModel.teamAnalysis.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var slotToSelectPokemon by remember { mutableStateOf<Int?>(null) }
    var memberToEditStats by remember { mutableStateOf<MiembroEquipo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedEquipoId == null) "Constructor de Equipos" else selectedEquipo?.equipo?.nombre ?: "Mi Equipo",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedEquipoId != null) {
                            teamBuilderViewModel.seleccionarEquipo(null)
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (selectedEquipoId != null) {
                        IconButton(onClick = {
                            selectedEquipoId?.let { teamBuilderViewModel.eliminarEquipo(it) }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Equipo",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        },
        floatingActionButton = {
            if (selectedEquipoId == null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color(0xFFFF6D00),
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Equipo")
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedEquipoId == null) {
                // Lista de equipos
                if (equipos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡Aún no tienes ningún equipo!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pulsa el botón + para armar tu primer equipo de 6 Pokémon y analizar sus debilidades.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(equipos, key = { it.equipo.id }) { item ->
                            TeamListItemCard(
                                item = item,
                                onClick = { teamBuilderViewModel.seleccionarEquipo(item.equipo.id) },
                                onDelete = { teamBuilderViewModel.eliminarEquipo(item.equipo.id) }
                            )
                        }
                    }
                }
            } else {
                // Detalle del equipo seleccionado
                val equipo = selectedEquipo
                if (equipo != null) {
                    TeamDetailContent(
                        equipoConMiembros = equipo,
                        analysis = analysis,
                        onSlotClick = { slot ->
                            val currentMember = equipo.miembros.find { it.slot == slot }
                            if (currentMember != null) {
                                memberToEditStats = currentMember
                            } else {
                                slotToSelectPokemon = slot
                            }
                        },
                        onRemoveMember = { slot ->
                            teamBuilderViewModel.eliminarMiembroPorSlot(equipo.equipo.id, slot)
                        }
                    )
                }
            }
        }
    }

    // Diálogo editor de estadísticas competitivas del miembro (Sprint 3)
    if (memberToEditStats != null) {
        val currentMember = memberToEditStats!!
        MemberStatEditorDialog(
            miembro = currentMember,
            onDismiss = { memberToEditStats = null },
            onSave = { updated ->
                teamBuilderViewModel.actualizarMiembro(updated)
                memberToEditStats = null
            },
            onChangePokemon = { slot ->
                memberToEditStats = null
                slotToSelectPokemon = slot
            }
        )
    }

    // Diálogo para crear equipo
    if (showCreateDialog) {
        var teamName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuevo Equipo Pokémon", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Nombre del equipo") },
                    placeholder = { Text("Ej: Aventura Paldea, Competitivo...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF6D00)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank()) {
                            teamBuilderViewModel.crearEquipo(teamName.trim())
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
                ) {
                    Text("Crear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF242424)
        )
    }

    // Diálogo selector de Pokémon para un slot
    if (slotToSelectPokemon != null) {
        val targetSlot = slotToSelectPokemon!!
        val currentEq = selectedEquipo
        if (currentEq != null) {
            PokemonPickerDialog(
                pokemonViewModel = pokemonViewModel,
                onDismiss = { slotToSelectPokemon = null },
                onPokemonSelected = { pokemonId, name, spriteUrl, tipo1, tipo2, hp, atk, def, spa, spd, spe ->
                    teamBuilderViewModel.agregarPokemonAlSlot(
                        equipoId = currentEq.equipo.id,
                        slot = targetSlot,
                        pokemonId = pokemonId,
                        nombre = name,
                        spriteUrl = spriteUrl,
                        tipo1 = tipo1,
                        tipo2 = tipo2,
                        hp = hp, atk = atk, def = def, spa = spa, spd = spd, spe = spe
                    )
                    slotToSelectPokemon = null
                }
            )
        }
    }
}

@Composable
fun TeamListItemCard(
    item: EquipoConMiembros,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.equipo.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${item.miembros.size} / 6",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF6D00)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila con los avatares de los 6 slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (slot in 0 until 6) {
                    val miembro = item.miembros.find { it.slot == slot }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2A2A))
                            .border(
                                1.dp,
                                if (miembro != null) getTipoColor(miembro.tipo1) else Color(0xFF3A3A3A),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (miembro != null) {
                            AsyncImage(
                                model = miembro.spriteUrl,
                                contentDescription = miembro.nombre,
                                modifier = Modifier.fillMaxSize(0.9f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = "+",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamDetailContent(
    equipoConMiembros: EquipoConMiembros,
    analysis: TypeEffectiveness.TeamAnalysisResult,
    onSlotClick: (Int) -> Unit,
    onRemoveMember: (Int) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grid de los 6 slots
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Miembros (${equipoConMiembros.miembros.size}/6)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (equipoConMiembros.miembros.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val showdown = equipoConMiembros.miembros
                                .sortedBy { it.slot }
                                .joinToString("\n\n") { m ->
                                    StatCalculator.toShowdownFormat(
                                        speciesName = m.nombre,
                                        level = m.level,
                                        nature = StatCalculator.getNatureByName(m.nature),
                                        evs = StatSpread(m.evHp, m.evAtk, m.evDef, m.evSpa, m.evSpd, m.evSpe),
                                        ivs = StatSpread(m.ivHp, m.ivAtk, m.ivDef, m.ivSpa, m.ivSpd, m.ivSpe)
                                    )
                                }
                            clipboardManager.setText(AnnotatedString(showdown))
                            Toast.makeText(context, "¡Equipo copiado para Showdown!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF61DAF6))
                    ) {
                        Text("Exportar Showdown", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Renderizamos los 6 slots en pares de 2 por fila
        for (row in 0 until 3) {
            val slot1 = row * 2
            val slot2 = row * 2 + 1
            val m1 = equipoConMiembros.miembros.find { it.slot == slot1 }
            val m2 = equipoConMiembros.miembros.find { it.slot == slot2 }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TeamSlotCard(
                            slotIndex = slot1,
                            miembro = m1,
                            onClick = { onSlotClick(slot1) },
                            onRemove = { onRemoveMember(slot1) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TeamSlotCard(
                            slotIndex = slot2,
                            miembro = m2,
                            onClick = { onSlotClick(slot2) },
                            onRemove = { onRemoveMember(slot2) }
                        )
                    }
                }
            }
        }

        // Sección de Análisis de Coberturas
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚡ Análisis de Coberturas y Debilidades",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF61DAF6)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Alerta de debilidades críticas si existen
        if (analysis.criticalWeaknesses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠️ Alerta de Debilidad Crítica",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tres o más miembros son débiles al tipo: ${analysis.criticalWeaknesses.joinToString(", ") { traducirTipo(it) }} sin ninguna resistencia que lo cubra.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Resumen de Cobertura Ofensiva
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Cobertura Ofensiva Super-Eficaz: ${analysis.offensiveCoverage.size} / 18 tipos",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OptTypeChipsRow(types = analysis.offensiveCoverage.toList())

                    if (analysis.uncoveredOffense.isNotEmpty() && equipoConMiembros.miembros.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin cobertura súper-eficaz contra: ${analysis.uncoveredOffense.joinToString(", ") { traducirTipo(it) }}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Matriz de Debilidades Defensivas del Equipo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Vulnerabilidad Defensiva por Tipo",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mostrar resumen de tipos agrupados
                    analysis.defenseSummary.chunked(3).forEach { chunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            chunk.forEach { item ->
                                val typeColor = getTipoColor(item.type)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2A2A2A))
                                        .border(
                                            1.dp,
                                            if (item.weakCount >= 2) Color(0xFFFF5252) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = traducirTipo(item.type),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = typeColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (item.weakCount > 0) {
                                                Text(
                                                    text = "🔴 ${item.weakCount}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFFF5252),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (item.resistCount + item.immuneCount > 0) {
                                                Text(
                                                    text = "🟢 ${item.resistCount + item.immuneCount}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF69F0AE),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (item.weakCount == 0 && item.resistCount + item.immuneCount == 0) {
                                                Text(
                                                    text = "⚪ 0",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamSlotCard(
    slotIndex: Int,
    miembro: MiembroEquipo?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (miembro != null) Color(0xFF242424) else Color(0xFF1C1C1C)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (miembro != null) getTipoColor(miembro.tipo1).copy(alpha = 0.6f) else Color(0xFF333333)
        )
    ) {
        if (miembro != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = miembro.spriteUrl,
                        contentDescription = miembro.nombre,
                        modifier = Modifier.size(46.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = miembro.nombre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Nv.${miembro.level} • ${miembro.nature}",
                        fontSize = 9.sp,
                        color = Color(0xFF61DAF6),
                        fontWeight = FontWeight.SemiBold
                    )
                    val totalEvs = miembro.evHp + miembro.evAtk + miembro.evDef + miembro.evSpa + miembro.evSpd + miembro.evSpe
                    Text(
                        text = "EVs: $totalEvs/510",
                        fontSize = 8.sp,
                        color = if (totalEvs > 510) Color(0xFFFF5252) else Color(0xFF69F0AE)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BadgeTipo(tipo = miembro.tipo1)
                        miembro.tipo2?.let { BadgeTipo(tipo = it) }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color(0xFFFF6D00),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Slot ${slotIndex + 1}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BadgeTipo(tipo: String) {
    val color = getTipoColor(tipo)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.85f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = traducirTipo(tipo),
            fontSize = 9.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptTypeChipsRow(types: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        types.forEach { t ->
            BadgeTipo(tipo = t)
        }
    }
}

// Diálogo para buscar y seleccionar un Pokémon
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPickerDialog(
    pokemonViewModel: PokemonViewModel,
    onDismiss: () -> Unit,
    onPokemonSelected: (
        pokemonId: Int,
        nombre: String,
        spriteUrl: String,
        tipo1: String,
        tipo2: String?,
        hp: Int,
        atk: Int,
        def: Int,
        spa: Int,
        spd: Int,
        spe: Int
    ) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val pokedexList by pokemonViewModel.filteredPokemonList.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isLoadingSelection by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elige un Pokémon",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        pokemonViewModel.updateSearchQuery(it)
                    },
                    placeholder = { Text("Buscar por nombre o número...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF6D00)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingSelection) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF6D00))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pokedexList) { item ->
                            val id = item.url.split("/").dropLast(1).last().toIntOrNull() ?: 1
                            val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2A2A2A))
                                    .clickable {
                                        isLoadingSelection = true
                                        coroutineScope.launch {
                                            try {
                                                val details = withContext(Dispatchers.IO) {
                                                    com.duoc.rotompedia.data.remote.PokemonApi.service.getPokemon(id)
                                                }
                                                val t1 = details.types.getOrNull(0)?.type?.name ?: "normal"
                                                val t2 = details.types.getOrNull(1)?.type?.name
                                                val hp = details.stats.find { it.stat.name == "hp" }?.baseStat ?: 0
                                                val atk = details.stats.find { it.stat.name == "attack" }?.baseStat ?: 0
                                                val def = details.stats.find { it.stat.name == "defense" }?.baseStat ?: 0
                                                val spa = details.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0
                                                val spd = details.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0
                                                val spe = details.stats.find { it.stat.name == "speed" }?.baseStat ?: 0

                                                onPokemonSelected(
                                                    id, details.name,
                                                    details.sprites.urlImagen ?: spriteUrl,
                                                    t1, t2, hp, atk, def, spa, spd, spe
                                                )
                                            } catch (e: Exception) {
                                                // Fallback con datos mínimos
                                                onPokemonSelected(
                                                    id, item.name, spriteUrl,
                                                    "normal", null,
                                                    0, 0, 0, 0, 0, 0
                                                )
                                            } finally {
                                                isLoadingSelection = false
                                            }
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = spriteUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier.size(44.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "#${id.toString().padStart(3, '0')} ${item.name}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
