package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.MoveCategory
import com.duoc.rotompedia.data.PokemonMove
import com.duoc.rotompedia.data.PokemonMoveData
import com.duoc.rotompedia.data.local.MiembroEquipo
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.util.StatCalculator
import com.duoc.rotompedia.util.StatCalculator.StatSpread
import com.duoc.rotompedia.util.StatCalculator.StatType
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberStatEditorDialog(
    miembro: MiembroEquipo,
    onDismiss: () -> Unit,
    onSave: (MiembroEquipo) -> Unit,
    onChangePokemon: (slot: Int) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Stats (IV/EV), 1: Movimientos (4)

    var level by remember { mutableStateOf(miembro.level) }
    var selectedNature by remember { mutableStateOf(StatCalculator.getNatureByName(miembro.nature)) }
    var showNaturePicker by remember { mutableStateOf(false) }

    var evHp by remember { mutableStateOf(miembro.evHp) }
    var evAtk by remember { mutableStateOf(miembro.evAtk) }
    var evDef by remember { mutableStateOf(miembro.evDef) }
    var evSpa by remember { mutableStateOf(miembro.evSpa) }
    var evSpd by remember { mutableStateOf(miembro.evSpd) }
    var evSpe by remember { mutableStateOf(miembro.evSpe) }

    var ivHp by remember { mutableStateOf(miembro.ivHp) }
    var ivAtk by remember { mutableStateOf(miembro.ivAtk) }
    var ivDef by remember { mutableStateOf(miembro.ivDef) }
    var ivSpa by remember { mutableStateOf(miembro.ivSpa) }
    var ivSpd by remember { mutableStateOf(miembro.ivSpd) }
    var ivSpe by remember { mutableStateOf(miembro.ivSpe) }

    // Movimientos del Pokémon
    var move1 by remember { mutableStateOf(miembro.move1) }
    var move2 by remember { mutableStateOf(miembro.move2) }
    var move3 by remember { mutableStateOf(miembro.move3) }
    var move4 by remember { mutableStateOf(miembro.move4) }
    var slotPickingMove by remember { mutableStateOf<Int?>(null) } // 1, 2, 3 o 4

    val totalEvs = evHp + evAtk + evDef + evSpa + evSpd + evSpe
    val remainingEvs = 510 - totalEvs

    val baseStats = remember(miembro) {
        StatSpread(
            hp = miembro.hp,
            atk = miembro.attack,
            def = miembro.defense,
            spa = miembro.specialAttack,
            spd = miembro.specialDefense,
            spe = miembro.speed
        )
    }

    val calculated = remember(baseStats, evHp, evAtk, evDef, evSpa, evSpd, evSpe, ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe, level, selectedNature) {
        StatCalculator.calculateAll(
            baseStats = baseStats,
            ivs = StatSpread(ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe),
            evs = StatSpread(evHp, evAtk, evDef, evSpa, evSpd, evSpe),
            level = level,
            nature = selectedNature,
            isShedinja = miembro.pokemonId == 292
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            border = BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header del Diálogo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1C))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = miembro.spriteUrl,
                            contentDescription = miembro.nombre,
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = miembro.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BadgeTipo(tipo = miembro.tipo1)
                                miembro.tipo2?.let { BadgeTipo(tipo = it) }
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = {
                            val activeMoves = listOfNotNull(move1, move2, move3, move4)
                            val showdown = StatCalculator.toShowdownFormat(
                                speciesName = miembro.nombre,
                                level = level,
                                nature = selectedNature,
                                evs = StatSpread(evHp, evAtk, evDef, evSpa, evSpd, evSpe),
                                ivs = StatSpread(ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe),
                                moves = activeMoves
                            )
                            clipboardManager.setText(AnnotatedString(showdown))
                            Toast.makeText(context, "¡Build copiada a Showdown!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Exportar", tint = rotomForm.accentColor)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                        }
                    }
                }

                // Pestañas: Stats (IV/EV) vs Movimientos
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1C1C1C),
                    contentColor = rotomForm.primaryColor
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "📊 Stats (IV/EV)",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) rotomForm.primaryColor else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            val moveCount = listOfNotNull(move1, move2, move3, move4).size
                            Text(
                                "⚔️ Movimientos ($moveCount/4)",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) rotomForm.primaryColor else Color.Gray
                            )
                        }
                    )
                }

                // ==================== CONTENIDO DE PESTAÑA 0: STATS (IV/EV) ====================
                if (selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Controles de Nivel y Naturaleza
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Selector de Nivel
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Nivel", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = level == 50,
                                                onClick = { level = 50 },
                                                label = { Text("50 (VGC)", fontSize = 10.sp) },
                                                modifier = Modifier.height(28.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = rotomForm.primaryColor,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                            FilterChip(
                                                selected = level == 100,
                                                onClick = { level = 100 },
                                                label = { Text("100", fontSize = 10.sp) },
                                                modifier = Modifier.height(28.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = rotomForm.primaryColor,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }

                                // Selector de Naturaleza
                                Card(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clickable { showNaturePicker = true },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF333333))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Naturaleza", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${selectedNature.nameEs} (${selectedNature.nameEn})",
                                            color = rotomForm.accentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        val inc = selectedNature.increasedStat
                                        val dec = selectedNature.decreasedStat
                                        val modText = if (inc != null) {
                                            "+${inc.shortName} / -${dec?.shortName}"
                                        } else "Neutra"
                                        Text(modText, fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }

                        // Presets rápidos de reparto de EVs
                        item {
                            Text("Repartos Rápidos (Presets)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    PresetChip("Sweeper Físico") {
                                        evHp = 4; evAtk = 252; evDef = 0; evSpa = 0; evSpd = 0; evSpe = 252
                                    }
                                }
                                item {
                                    PresetChip("Sweeper Especial") {
                                        evHp = 4; evAtk = 0; evDef = 0; evSpa = 252; evSpd = 0; evSpe = 252
                                    }
                                }
                                item {
                                    PresetChip("Bulky Defensivo") {
                                        evHp = 252; evAtk = 0; evDef = 252; evSpa = 0; evSpd = 4; evSpe = 0
                                    }
                                }
                                item {
                                    PresetChip("Bulky Especial") {
                                        evHp = 252; evAtk = 0; evDef = 4; evSpa = 0; evSpd = 252; evSpe = 0
                                    }
                                }
                                item {
                                    PresetChip("Limpiar") {
                                        evHp = 0; evAtk = 0; evDef = 0; evSpa = 0; evSpd = 0; evSpe = 0
                                    }
                                }
                            }
                        }

                        // Barra de límite de EVs (510)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EVs Asignados: $totalEvs / 510",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalEvs > 510) Color(0xFFFF5252) else Color.White
                                )
                                Text(
                                    text = "Restantes: $remainingEvs",
                                    fontSize = 11.sp,
                                    color = if (remainingEvs < 0) Color(0xFFFF5252) else Color(0xFF69F0AE)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (totalEvs / 510f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (totalEvs > 510) Color(0xFFFF5252) else rotomForm.primaryColor,
                                trackColor = Color(0xFF2A2A2A),
                            )
                        }

                        // Filas de Estadísticas: HP, Atk, Def, SpA, SpD, Spe
                        item {
                            StatRowSlider(
                                statType = StatType.HP,
                                base = baseStats.hp,
                                calculated = calculated.hp,
                                ev = evHp,
                                iv = ivHp,
                                onEvChange = { evHp = it },
                                onIvChange = { ivHp = it }
                            )
                        }
                        item {
                            StatRowSlider(
                                statType = StatType.ATTACK,
                                base = baseStats.atk,
                                calculated = calculated.atk,
                                ev = evAtk,
                                iv = ivAtk,
                                onEvChange = { evAtk = it },
                                onIvChange = { ivAtk = it },
                                isBoosted = selectedNature.increasedStat == StatType.ATTACK,
                                isReduced = selectedNature.decreasedStat == StatType.ATTACK
                            )
                        }
                        item {
                            StatRowSlider(
                                statType = StatType.DEFENSE,
                                base = baseStats.def,
                                calculated = calculated.def,
                                ev = evDef,
                                iv = ivDef,
                                onEvChange = { evDef = it },
                                onIvChange = { ivDef = it },
                                isBoosted = selectedNature.increasedStat == StatType.DEFENSE,
                                isReduced = selectedNature.decreasedStat == StatType.DEFENSE
                            )
                        }
                        item {
                            StatRowSlider(
                                statType = StatType.SPECIAL_ATTACK,
                                base = baseStats.spa,
                                calculated = calculated.spa,
                                ev = evSpa,
                                iv = ivSpa,
                                onEvChange = { evSpa = it },
                                onIvChange = { ivSpa = it },
                                isBoosted = selectedNature.increasedStat == StatType.SPECIAL_ATTACK,
                                isReduced = selectedNature.decreasedStat == StatType.SPECIAL_ATTACK
                            )
                        }
                        item {
                            StatRowSlider(
                                statType = StatType.SPECIAL_DEFENSE,
                                base = baseStats.spd,
                                calculated = calculated.spd,
                                ev = evSpd,
                                iv = ivSpd,
                                onEvChange = { evSpd = it },
                                onIvChange = { ivSpd = it },
                                isBoosted = selectedNature.increasedStat == StatType.SPECIAL_DEFENSE,
                                isReduced = selectedNature.decreasedStat == StatType.SPECIAL_DEFENSE
                            )
                        }
                        item {
                            StatRowSlider(
                                statType = StatType.SPEED,
                                base = baseStats.spe,
                                calculated = calculated.spe,
                                ev = evSpe,
                                iv = ivSpe,
                                onEvChange = { evSpe = it },
                                onIvChange = { ivSpe = it },
                                isBoosted = selectedNature.increasedStat == StatType.SPEED,
                                isReduced = selectedNature.decreasedStat == StatType.SPEED
                            )
                        }
                    }
                } else {
                    // ==================== CONTENIDO DE PESTAÑA 1: MOVIMIENTOS (MOVESET) ====================
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Moveset de Combate (4 Movimientos)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Configura los 4 ataques para cálculos de cobertura y exportación a Cobblemon/Showdown.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Slot 1
                        item {
                            MoveSlotCard(
                                slotIndex = 1,
                                moveName = move1,
                                onClick = { slotPickingMove = 1 },
                                onClear = { move1 = null }
                            )
                        }

                        // Slot 2
                        item {
                            MoveSlotCard(
                                slotIndex = 2,
                                moveName = move2,
                                onClick = { slotPickingMove = 2 },
                                onClear = { move2 = null }
                            )
                        }

                        // Slot 3
                        item {
                            MoveSlotCard(
                                slotIndex = 3,
                                moveName = move3,
                                onClick = { slotPickingMove = 3 },
                                onClear = { move3 = null }
                            )
                        }

                        // Slot 4
                        item {
                            MoveSlotCard(
                                slotIndex = 4,
                                moveName = move4,
                                onClick = { slotPickingMove = 4 },
                                onClear = { move4 = null }
                            )
                        }
                    }
                }

                // Barra Inferior: Acciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1C))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onChangePokemon(miembro.slot) }) {
                        Text("Cambiar Pokémon", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val updated = miembro.copy(
                                level = level,
                                nature = selectedNature.nameEs,
                                evHp = evHp,
                                evAtk = evAtk,
                                evDef = evDef,
                                evSpa = evSpa,
                                evSpd = evSpd,
                                evSpe = evSpe,
                                ivHp = ivHp,
                                ivAtk = ivAtk,
                                ivDef = ivDef,
                                ivSpa = ivSpa,
                                ivSpd = ivSpd,
                                ivSpe = ivSpe,
                                move1 = move1,
                                move2 = move2,
                                move3 = move3,
                                move4 = move4
                            )
                            onSave(updated)
                            Toast.makeText(context, "¡Build y movimientos guardados!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = rotomForm.primaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal para cambiar Naturaleza
    if (showNaturePicker) {
        AlertDialog(
            onDismissRequest = { showNaturePicker = false },
            title = { Text("Elegir Naturaleza", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(StatCalculator.ALL_NATURES) { nature ->
                        val isSelected = nature == selectedNature
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) rotomForm.primaryColor.copy(alpha = 0.25f) else Color(0xFF262626))
                                .clickable {
                                    selectedNature = nature
                                    showNaturePicker = false
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(nature.nameEs, color = if (isSelected) rotomForm.primaryColor else Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                if (nature.increasedStat != null) "+${nature.increasedStat.shortName}/-${nature.decreasedStat?.shortName}" else "Neutra",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNaturePicker = false }) {
                    Text("Cerrar", color = rotomForm.accentColor)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    // Modal para seleccionar movimiento de un slot
    if (slotPickingMove != null) {
        val currentSlot = slotPickingMove!!
        MovePickerDialog(
            slotIndex = currentSlot,
            onDismiss = { slotPickingMove = null },
            onMoveSelected = { selectedMove ->
                when (currentSlot) {
                    1 -> move1 = selectedMove.nameEn
                    2 -> move2 = selectedMove.nameEn
                    3 -> move3 = selectedMove.nameEn
                    4 -> move4 = selectedMove.nameEn
                }
                slotPickingMove = null
            },
            onClear = {
                when (currentSlot) {
                    1 -> move1 = null
                    2 -> move2 = null
                    3 -> move3 = null
                    4 -> move4 = null
                }
                slotPickingMove = null
            }
        )
    }
}

@Composable
private fun MoveSlotCard(
    slotIndex: Int,
    moveName: String?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()
    val moveObj = remember(moveName) { moveName?.let { PokemonMoveData.findMove(it) } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (moveObj != null) Color(0xFF222630) else Color(0xFF1A1A1A)
        ),
        border = BorderStroke(
            1.dp,
            if (moveObj != null) getTipoColor(moveObj.type).copy(alpha = 0.6f) else Color(0xFF333333)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (moveObj != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Mov $slotIndex: ${moveObj.nameEs}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${moveObj.nameEn})",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BadgeTipo(tipo = moveObj.type)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = moveObj.category.color.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, moveObj.category.color)
                        ) {
                            Text(
                                text = "${moveObj.category.emoji} ${moveObj.category.displayName}",
                                color = moveObj.category.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "Pot: ${moveObj.power ?: "-"} • Prec: ${moveObj.accuracy?.let { "$it%" } ?: "-"} • PP: ${moveObj.pp}",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                    if (moveObj.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = moveObj.description,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = rotomForm.primaryColor)
                    Column {
                        Text(
                            text = "Movimiento $slotIndex: (Vacío)",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Toca para asignar un movimiento del catálogo",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo de selección de movimientos con búsqueda en vivo, filtro por categoría y tipo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovePickerDialog(
    slotIndex: Int,
    onDismiss: () -> Unit,
    onMoveSelected: (PokemonMove) -> Unit,
    onClear: () -> Unit
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MoveCategory?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }

    val filteredMoves = remember(searchQuery, selectedCategory, selectedType) {
        PokemonMoveData.allMoves.filter { m ->
            val matchesCategory = selectedCategory == null || m.category == selectedCategory
            val matchesType = selectedType == null || m.type.equals(selectedType, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    m.nameEs.contains(searchQuery, ignoreCase = true) ||
                    m.nameEn.contains(searchQuery, ignoreCase = true) ||
                    m.description.contains(searchQuery, ignoreCase = true) ||
                    m.type.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesType && matchesSearch
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            border = BorderStroke(1.dp, rotomForm.primaryColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elegir Movimiento $slotIndex",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, tipo...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = rotomForm.primaryColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = rotomForm.primaryColor,
                        unfocusedBorderColor = Color(0xFF333333)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtro por Categoría
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Todas", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == MoveCategory.PHYSICAL,
                        onClick = { selectedCategory = if (selectedCategory == MoveCategory.PHYSICAL) null else MoveCategory.PHYSICAL },
                        label = { Text("💥 Físico", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == MoveCategory.SPECIAL,
                        onClick = { selectedCategory = if (selectedCategory == MoveCategory.SPECIAL) null else MoveCategory.SPECIAL },
                        label = { Text("🔮 Especial", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == MoveCategory.STATUS,
                        onClick = { selectedCategory = if (selectedCategory == MoveCategory.STATUS) null else MoveCategory.STATUS },
                        label = { Text("🛡️ Estado", fontSize = 10.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Conteo y Quitar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredMoves.size} movimientos disponibles",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    TextButton(onClick = onClear) {
                        Text("Dejar Vacío", color = Color(0xFFFF5252), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Lista de movimientos
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredMoves) { move ->
                        Surface(
                            onClick = { onMoveSelected(move) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF242730),
                            border = BorderStroke(1.dp, getTipoColor(move.type).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(move.nameEs, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("(${move.nameEn})", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        BadgeTipo(tipo = move.type)
                                        Text(
                                            text = "${move.category.emoji} ${move.category.displayName}",
                                            color = move.category.color,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Pot: ${move.power ?: "-"} • Prec: ${move.accuracy?.let { "$it%" } ?: "-"} • PP: ${move.pp}",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (move.description.isNotBlank()) {
                                        Text(move.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
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
private fun StatRowSlider(
    statType: StatType,
    base: Int,
    calculated: Int,
    ev: Int,
    iv: Int,
    onEvChange: (Int) -> Unit,
    onIvChange: (Int) -> Unit,
    isBoosted: Boolean = false,
    isReduced: Boolean = false
) {
    val statColor = when (statType) {
        StatType.HP -> Color(0xFFFF5252)
        StatType.ATTACK -> Color(0xFFFF7043)
        StatType.DEFENSE -> Color(0xFFFFCA28)
        StatType.SPECIAL_ATTACK -> Color(0xFF42A5F5)
        StatType.SPECIAL_DEFENSE -> Color(0xFF66BB6A)
        StatType.SPEED -> Color(0xFFEC407A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = statType.shortName,
                        fontWeight = FontWeight.Bold,
                        color = statColor,
                        fontSize = 13.sp
                    )
                    Text("Base: $base", fontSize = 10.sp, color = Color.Gray)
                    if (isBoosted) Text("(+10%)", fontSize = 10.sp, color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold)
                    if (isReduced) Text("(-10%)", fontSize = 10.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Toggle IV 31 vs 0
                    FilterChip(
                        selected = iv == 31,
                        onClick = { onIvChange(if (iv == 31) 0 else 31) },
                        label = { Text("IV: $iv", fontSize = 9.sp) },
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        text = "$calculated",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Slider de EV (0..252 en múltiplos de 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Slider(
                    value = ev.toFloat(),
                    onValueChange = { onEvChange((it.toInt() / 4) * 4) },
                    valueRange = 0f..252f,
                    steps = 62,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = statColor,
                        activeTrackColor = statColor
                    )
                )

                Text(
                    text = "EV: $ev",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.width(50.dp)
                )
            }
        }
    }
}
