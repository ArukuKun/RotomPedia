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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.rotompedia.data.RouteLocation
import com.duoc.rotompedia.data.WildEncounter
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailBottomSheet(
    location: RouteLocation,
    onDismiss: () -> Unit,
    onPokemonClick: (Int) -> Unit
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = rotomForm.primaryColor)
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header: Emoji, Nombre y Región
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(rotomForm.primaryColor.copy(alpha = 0.2f))
                            .border(1.dp, rotomForm.primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = location.typeEmoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = location.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Región de ${location.region.replaceFirstChar { it.uppercase() }}",
                            fontSize = 12.sp,
                            color = rotomForm.accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción y Lore del lugar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
            ) {
                Text(
                    text = location.description,
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Objetos clave o recompensas en la ruta
            if (location.keyItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎒 Objetos Clave y Descubrimientos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = rotomForm.accentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    location.keyItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2A2A2A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3D3D3D))
                        ) {
                            Text(
                                text = item,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Encuentros Salvajes
            Text(
                text = "🐾 Pokémon Salvajes Disponibles (${location.wildEncounters.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (location.wildEncounters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Zona urbana protegida. No se registran encuentros de Pokémon salvajes.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(location.wildEncounters) { encounter ->
                        EncounterRowCard(
                            encounter = encounter,
                            onPokemonClick = { onPokemonClick(encounter.pokemonId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EncounterRowCard(
    encounter: WildEncounter,
    onPokemonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPokemonClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF202020)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF303030))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sprite del Pokémon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = encounter.spriteUrl,
                    contentDescription = encounter.pokemonName,
                    modifier = Modifier.fillMaxSize(0.9f),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${encounter.pokemonId} ${encounter.pokemonName}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    encounter.types.forEach { tipo ->
                        BadgeTipo(tipo = tipo)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Método y Niveles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${encounter.method.iconEmoji} ${encounter.method.displayName}",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Nv. ${encounter.minLevel}${if (encounter.maxLevel > encounter.minLevel) "-${encounter.maxLevel}" else ""}",
                        fontSize = 11.sp,
                        color = Color(0xFF69F0AE),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Porcentaje de aparición
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFF6D00).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "${encounter.chancePercent}%",
                        color = Color(0xFFFF6D00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver en Pokédex",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
