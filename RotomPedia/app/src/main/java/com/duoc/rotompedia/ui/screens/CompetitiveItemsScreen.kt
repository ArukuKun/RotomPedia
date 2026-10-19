package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.CompetitiveItem
import com.duoc.rotompedia.data.CompetitiveItemData
import com.duoc.rotompedia.data.ItemCategory
import com.duoc.rotompedia.util.RotomThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitiveItemsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ItemCategory.ALL) }

    val filteredItems = remember(searchQuery, selectedCategory) {
        CompetitiveItemData.items.filter { item ->
            val matchesCategory = selectedCategory == ItemCategory.ALL || item.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    item.nameEs.contains(searchQuery, ignoreCase = true) ||
                    item.nameEn.contains(searchQuery, ignoreCase = true) ||
                    item.effectDescription.contains(searchQuery, ignoreCase = true) ||
                    item.competitiveUsage.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Objetos Competitivos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Meta VGC & Singles • Gen IX",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD11527),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0F1115))
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Buscar objeto, efecto o estrategia...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = rotomForm.primaryColor
                    )
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = rotomForm.primaryColor,
                    unfocusedBorderColor = Color(0xFF2E3342),
                    focusedContainerColor = Color(0xFF191D26),
                    unfocusedContainerColor = Color(0xFF191D26),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Selector horizontal de categorías
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ItemCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) rotomForm.primaryColor else Color(0xFF1E222D),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) rotomForm.primaryColor else Color(0xFF2C3240)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(category.emoji, fontSize = 13.sp)
                            Text(
                                text = category.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contador de resultados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredItems.size} objetos encontrados",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedCategory != ItemCategory.ALL || searchQuery.isNotBlank()) {
                    TextButton(
                        onClick = {
                            selectedCategory = ItemCategory.ALL
                            searchQuery = ""
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Limpiar filtros", fontSize = 11.sp, color = rotomForm.primaryColor)
                    }
                }
            }

            // Lista de objetos
            if (filteredItems.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        CompetitiveItemCard(
                            item = item,
                            onCopy = {
                                val textToCopy = "${item.nameEs} (${item.nameEn}): ${item.effectDescription}\nUso: ${item.competitiveUsage}"
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "Copiado: ${item.nameEs}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron objetos",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Intenta buscar por otro término o cambiar de categoría.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompetitiveItemCard(
    item: CompetitiveItem,
    onCopy: () -> Unit
) {
    val rotomForm by RotomThemeManager.currentForm.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171A22)),
        border = BorderStroke(1.dp, getCategoryColor(item.category).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Icono, Nombres y Categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(item.category).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.emoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nameEs,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${item.nameEn} • Gen ${item.generation}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chip de categoría
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getCategoryColor(item.category).copy(alpha = 0.16f)
            ) {
                Text(
                    text = "${item.category.emoji} ${item.category.displayName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(item.category),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Descripción del efecto
            Text(
                text = item.effectDescription,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.92f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Estrategia competitiva / Rotom Tip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF222633),
                border = BorderStroke(1.dp, Color(0xFF33384A))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.competitiveUsage,
                        fontSize = 11.sp,
                        color = Color(0xFF61DAF6),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

private fun getCategoryColor(category: ItemCategory): Color {
    return when (category) {
        ItemCategory.CHOICE -> Color(0xFFE53935)
        ItemCategory.DEFENSIVE -> Color(0xFF1E88E5)
        ItemCategory.OFFENSIVE -> Color(0xFFFF9800)
        ItemCategory.UTILITY -> Color(0xFF8E24AA)
        ItemCategory.BERRY -> Color(0xFF4CAF50)
        ItemCategory.WEATHER -> Color(0xFF00ACC1)
        ItemCategory.ALL -> Color(0xFF61DAF6)
    }
}
