package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.duoc.rotompedia.data.PokemonMoveData
import com.duoc.rotompedia.data.local.EquipoConMiembros
import com.duoc.rotompedia.data.local.MiembroEquipo
import com.duoc.rotompedia.data.model.PokemonListItem
import com.duoc.rotompedia.util.StatCalculator
import com.duoc.rotompedia.util.StatCalculator.StatSpread
import com.duoc.rotompedia.util.StatCalculator.StatType
import com.duoc.rotompedia.util.TypeEffectiveness
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo
import com.duoc.rotompedia.viewmodel.PokemonViewModel
import com.duoc.rotompedia.viewmodel.TeamBuilderViewModel
import com.duoc.rotompedia.viewmodel.allPokemonTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
    var selectedAnalysisTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📊 Matriz Daño", "⚡ Resumen Equipo", "📈 Stats", "⚔️ Movimientos")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header de miembros y botón de exportar Showdown
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
                                    val moves = listOfNotNull(m.move1, m.move2, m.move3, m.move4)
                                    StatCalculator.toShowdownFormat(
                                        speciesName = m.nombre,
                                        level = m.level,
                                        nature = StatCalculator.getNatureByName(m.nature),
                                        evs = StatSpread(m.evHp, m.evAtk, m.evDef, m.evSpa, m.evSpd, m.evSpe),
                                        ivs = StatSpread(m.ivHp, m.ivAtk, m.ivDef, m.ivSpa, m.ivSpd, m.ivSpe),
                                        moves = moves
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

        // Selector de Pestañas de Análisis
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ScrollableTabRow(
                selectedTabIndex = selectedAnalysisTab,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color(0xFFFF6D00),
                edgePadding = 4.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedAnalysisTab]),
                        color = Color(0xFFFF6D00)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAnalysisTab == index,
                        onClick = { selectedAnalysisTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedAnalysisTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAnalysisTab == index) Color(0xFFFF6D00) else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        // Contenido según la pestaña seleccionada
        when (selectedAnalysisTab) {
            0 -> {
                // TAB 0: Matriz de Daño Recibido (Damage Taken Matrix)
                item {
                    TeamDamageTakenMatrix(members = equipoConMiembros.miembros)
                }
            }
            1 -> {
                // TAB 1: Resumen de Equipo (Cobertura Ofensiva y Vulnerabilidades Defensivas)
                item {
                    TeamMacroSummary(
                        analysis = analysis,
                        members = equipoConMiembros.miembros
                    )
                }
            }
            2 -> {
                // TAB 2: Estadísticas Base (BST & Stats)
                item {
                    TeamBaseStatsTab(members = equipoConMiembros.miembros)
                }
            }
            3 -> {
                // TAB 3: Movimientos asignados por miembro
                item {
                    TeamMovesTab(members = equipoConMiembros.miembros)
                }
            }
        }
    }
}

/**
 * Matriz de Daño Recibido por Tipo y Miembro (estilo GamePress / Screenshot 3).
 * Columnas: Pokémon 1 a 6 con sprite avatar.
 * Filas: 18 Tipos Pokémon con multiplicador x4 (Cyan), x2 (Verde), x1 (Gris), x0.5 (Rojo), x0.25 (Óxido), x0 (Negro).
 */
@Composable
fun TeamDamageTakenMatrix(members: List<MiembroEquipo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Tabla de Daño Recibido (Damage Taken)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Evalúa debilidades críticas x4 y resistencias para equilibrar el equipo.",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Scroll horizontal para garantizar visualización perfecta en cualquier pantalla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.widthIn(min = 340.dp)) {
                    // Header de la tabla: Cabeceras con Avatares de los 6 slots
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(68.dp)
                                .height(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tipo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        for (slot in 0 until 6) {
                            val m = members.find { it.slot == slot }
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(38.dp)
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (m != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2A2A2A))
                                            .border(1.dp, getTipoColor(m.tipo1), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = m.spriteUrl,
                                            contentDescription = m.nombre,
                                            modifier = Modifier.size(30.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF242424))
                                            .border(1.dp, Color(0xFF383838), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "?",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

                    // 18 filas: una por cada tipo atacante
                    TypeEffectiveness.ALL_TYPES.forEach { attackingType ->
                        val typeColor = getTipoColor(attackingType)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Columna 0: Badge del Tipo
                            Box(
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(typeColor)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = traducirTipo(attackingType),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Columnas 1 a 6: Celdas con Multiplicador de Daño
                            for (slot in 0 until 6) {
                                val m = members.find { it.slot == slot }
                                Box(
                                    modifier = Modifier
                                        .width(44.dp)
                                        .height(26.dp)
                                        .padding(horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (m != null) {
                                        val mult = TypeEffectiveness.getDualMultiplier(attackingType, m.tipo1, m.tipo2)
                                        val (text, bg, fg) = getMultiplierDisplay(mult)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = text,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fg
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF161616)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "-", fontSize = 11.sp, color = Color(0xFF383838))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Leyenda de Multiplicadores idéntica a la referencia
            Text("Guía de Multiplicadores:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendBadge(text = "4", label = "x4", bg = Color(0xFF00BCD4), fg = Color.White)
                LegendBadge(text = "2", label = "x2", bg = Color(0xFF2E7D32), fg = Color.White)
                LegendBadge(text = "1", label = "x1", bg = Color(0xFF232323), fg = Color.Gray)
                LegendBadge(text = "½", label = "x0.5", bg = Color(0xFFC62828), fg = Color.White)
                LegendBadge(text = "¼", label = "x0.25", bg = Color(0xFFBF360C), fg = Color.White)
                LegendBadge(text = "0", label = "Inmune", bg = Color(0xFF151515), fg = Color.White)
            }
        }
    }
}

/**
 * Pestaña de Resumen Macro del Equipo (Screenshots 1 & 2).
 */
@Composable
fun TeamMacroSummary(
    analysis: TypeEffectiveness.TeamAnalysisResult,
    members: List<MiembroEquipo>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Alerta de debilidades críticas si existen
        if (analysis.criticalWeaknesses.isNotEmpty()) {
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

        // Resumen de Cobertura Ofensiva
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

                if (analysis.uncoveredOffense.isNotEmpty() && members.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sin cobertura súper-eficaz contra: ${analysis.uncoveredOffense.joinToString(", ") { traducirTipo(it) }}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Matriz de Debilidades Defensivas del Equipo
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

/**
 * Pestaña de Estadísticas Base (BST & Stats).
 */
@Composable
fun TeamBaseStatsTab(members: List<MiembroEquipo>) {
    if (members.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Agrega miembros para comparar estadísticas del equipo.", color = Color.Gray, fontSize = 13.sp)
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Comparativa de Estadísticas Base (BST)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            members.sortedBy { it.slot }.forEach { m ->
                val bst = m.hp + m.attack + m.defense + m.specialAttack + m.specialDefense + m.speed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A2A))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = m.spriteUrl,
                        contentDescription = m.nombre,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = m.nombre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BST: $bst",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PS ${m.hp} | Atk ${m.attack} | Def ${m.defense} | SpA ${m.specialAttack} | SpD ${m.specialDefense} | Spe ${m.speed}",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            val avgBst = if (members.isNotEmpty()) {
                members.sumOf { (it.hp + it.attack + it.defense + it.specialAttack + it.specialDefense + it.speed) } / members.size
            } else 0
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Promedio BST del equipo: $avgBst",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF61DAF6)
            )
        }
    }
}

/**
 * Pestaña de Movimientos Asignados por Miembro.
 */
@Composable
fun TeamMovesTab(members: List<MiembroEquipo>) {
    if (members.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Agrega miembros para visualizar sus movimientos asignados.", color = Color.Gray, fontSize = 13.sp)
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Movimientos Asignados del Equipo (4 por Pokémon)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Toca cualquier Pokémon en los slots principales para editar o cambiar sus movimientos.",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))

            members.sortedBy { it.slot }.forEach { m ->
                val moves = listOfNotNull(m.move1, m.move2, m.move3, m.move4)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262626))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = m.spriteUrl,
                                contentDescription = m.nombre,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = m.nombre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${moves.size}/4 movimientos",
                            fontSize = 10.sp,
                            color = if (moves.size == 4) Color(0xFF69F0AE) else Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (moves.isEmpty()) {
                        Text(
                            text = "⚠️ Sin movimientos configurados. Toca la tarjeta del Pokémon para seleccionarlos.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    } else {
                        moves.chunked(2).forEach { rowMoves ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowMoves.forEach { moveName ->
                                    val moveInfo = PokemonMoveData.findMove(moveName)
                                    val moveType = moveInfo?.type ?: "normal"
                                    val catEmoji = moveInfo?.category?.emoji ?: "💥"
                                    val powerStr = moveInfo?.power?.toString() ?: "-"
                                    val accStr = moveInfo?.accuracy?.let { "$it%" } ?: "-"

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E1E1E))
                                            .border(1.dp, getTipoColor(moveType).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(6.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = moveName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(text = catEmoji, fontSize = 10.sp)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                BadgeTipo(tipo = moveType)
                                                Text(
                                                    text = "Pot $powerStr • Prec $accStr",
                                                    fontSize = 8.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowMoves.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Retorna el texto del multiplicador ("4", "2", "1", "½", "¼", "0") y sus colores de fondo y texto.
 */
fun getMultiplierDisplay(mult: Float): Triple<String, Color, Color> {
    return when {
        mult >= 3.9f -> Triple("4", Color(0xFF00BCD4), Color.White)   // Cyan x4 (debilidad crítica)
        mult in 1.9f..2.1f -> Triple("2", Color(0xFF2E7D32), Color.White) // Verde x2 (débil)
        mult in 0.9f..1.1f -> Triple("1", Color(0xFF232323), Color(0xFF757575)) // Gris x1 (neutro)
        mult in 0.45f..0.55f -> Triple("½", Color(0xFFC62828), Color.White) // Rojo oscuro x0.5 (resistente)
        mult in 0.2f..0.3f -> Triple("¼", Color(0xFFBF360C), Color.White) // Óxido x0.25 (doble resistencia)
        mult == 0.0f -> Triple("0", Color(0xFF151515), Color.White) // Negro x0 (inmune)
        else -> Triple(
            if (mult > 1f) String.format(Locale.US, "%.1f", mult) else "½",
            if (mult > 1f) Color(0xFF2E7D32) else Color(0xFFC62828),
            Color.White
        )
    }
}

@Composable
fun LegendBadge(text: String, label: String, bg: Color, fg: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
        }
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
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
            .height(134.dp)
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
                    val movesCount = listOfNotNull(miembro.move1, miembro.move2, miembro.move3, miembro.move4).size
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVs: $totalEvs/510",
                            fontSize = 8.sp,
                            color = if (totalEvs > 510) Color(0xFFFF5252) else Color(0xFF69F0AE)
                        )
                        Text(
                            text = "• ⚔️ $movesCount/4",
                            fontSize = 8.sp,
                            color = if (movesCount == 4) Color(0xFF69F0AE) else Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold
                        )
                    }
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

// Diálogo para buscar y seleccionar un Pokémon con filtro de tipos en tiempo real
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
    val selectedTypeFilter by pokemonViewModel.selectedTypeFilter.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isLoadingSelection by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        pokemonViewModel.updateTypeFilter("all")
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
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
                    IconButton(onClick = {
                        pokemonViewModel.updateTypeFilter("all")
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de búsqueda por nombre o número o tipo
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        pokemonViewModel.updateSearchQuery(it)
                    },
                    placeholder = { Text("Buscar por nombre, número o tipo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF6D00)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips horizontales para filtrar por Tipo Pokémon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allPokemonTypes.forEach { typeName ->
                        FilterChip(
                            selected = typeName == selectedTypeFilter,
                            onClick = {
                                pokemonViewModel.updateTypeFilter(typeName)
                            },
                            label = {
                                Text(
                                    traducirTipo(typeName),
                                    fontSize = 11.sp,
                                    fontWeight = if (typeName == selectedTypeFilter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (typeName == "all") Color(0xFFFF6D00) else getTipoColor(typeName),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2A2A2A),
                                labelColor = Color.LightGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = typeName == selectedTypeFilter,
                                borderColor = if (typeName == selectedTypeFilter) Color.White.copy(alpha = 0.6f) else Color(0xFF3E3E3E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                                                pokemonViewModel.updateTypeFilter("all")
                                                onPokemonSelected(
                                                    id, details.name,
                                                    details.sprites.urlImagen ?: spriteUrl,
                                                    t1, t2, hp, atk, def, spa, spd, spe
                                                )
                                            } catch (e: Exception) {
                                                pokemonViewModel.updateTypeFilter("all")
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
