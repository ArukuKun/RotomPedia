package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import com.duoc.rotompedia.data.local.MiembroEquipo
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.util.StatCalculator
import com.duoc.rotompedia.util.StatCalculator.StatSpread
import com.duoc.rotompedia.util.StatCalculator.StatType

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
            border = androidx.compose.foundation.BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.6f))
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
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BadgeTipo(tipo = miembro.tipo1)
                                miembro.tipo2?.let { BadgeTipo(tipo = it) }
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = {
                            val showdown = StatCalculator.toShowdownFormat(
                                speciesName = miembro.nombre,
                                level = level,
                                nature = selectedNature,
                                evs = StatSpread(evHp, evAtk, evDef, evSpa, evSpd, evSpe),
                                ivs = StatSpread(ivHp, ivAtk, ivDef, ivSpa, ivSpd, ivSpe)
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

                // Contenido Scrolleable
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
                                    .weight(1f)
                                    .clickable { showNaturePicker = true },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Naturaleza", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = selectedNature.nameEs,
                                        fontWeight = FontWeight.Bold,
                                        color = rotomForm.accentColor,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (selectedNature.increasedStat != null)
                                            "+${selectedNature.increasedStat?.shortName} / -${selectedNature.decreasedStat?.shortName}"
                                        else "Neutra",
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    // Presets rápidos
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                PresetChip("Sweeper Físico (252 Atk/Spe)") {
                                    evHp = 4; evAtk = 252; evDef = 0; evSpa = 0; evSpd = 0; evSpe = 252
                                    selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Jolly" }
                                }
                            }
                            item {
                                PresetChip("Sweeper Especial (252 SpA/Spe)") {
                                    evHp = 4; evAtk = 0; evDef = 0; evSpa = 252; evSpd = 0; evSpe = 252
                                    selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Timid" }
                                }
                            }
                            item {
                                PresetChip("Bulky Defensivo (252 HP/Def)") {
                                    evHp = 252; evAtk = 0; evDef = 252; evSpa = 0; evSpd = 4; evSpe = 0
                                    selectedNature = StatCalculator.ALL_NATURES.first { it.nameEn == "Impish" }
                                }
                            }
                            item {
                                PresetChip("Reset (0 EVs)") {
                                    evHp = 0; evAtk = 0; evDef = 0; evSpa = 0; evSpd = 0; evSpe = 0
                                }
                            }
                        }
                    }

                    // Total EVs
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("EVs Usados:", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = "$totalEvs / 510 (${remainingEvs} libres)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalEvs > 510) Color(0xFFFF5252) else Color(0xFF69F0AE)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (totalEvs.toFloat() / 510f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (totalEvs > 510) Color(0xFFFF5252) else rotomForm.primaryColor,
                                    trackColor = Color(0xFF2A2A2A)
                                )
                            }
                        }
                    }

                    // 6 Estadísticas con Sliders
                    item {
                        StatRowControl(
                            label = "PS",
                            statType = StatType.HP,
                            baseStat = baseStats.hp,
                            finalStat = calculated.hp,
                            ev = evHp,
                            iv = ivHp,
                            nature = selectedNature,
                            onEvChange = { if (totalEvs - evHp + it <= 510) evHp = it },
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
                            onEvChange = { if (totalEvs - evAtk + it <= 510) evAtk = it },
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
                            onEvChange = { if (totalEvs - evDef + it <= 510) evDef = it },
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
                            onEvChange = { if (totalEvs - evSpa + it <= 510) evSpa = it },
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
                            onEvChange = { if (totalEvs - evSpd + it <= 510) evSpd = it },
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
                            onEvChange = { if (totalEvs - evSpe + it <= 510) evSpe = it },
                            onIvChange = { ivSpe = it }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total BST Calculado:", color = Color.Gray, fontSize = 12.sp)
                            Text("${calculated.totalBST} pts", color = rotomForm.accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Footer de Acciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1C))
                        .padding(12.dp),
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
                                ivSpe = ivSpe
                            )
                            onSave(updated)
                            Toast.makeText(context, "¡Estadísticas guardadas!", Toast.LENGTH_SHORT).show()
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
}
