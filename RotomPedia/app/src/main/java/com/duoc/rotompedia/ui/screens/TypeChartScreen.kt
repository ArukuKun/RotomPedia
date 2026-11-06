package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.TIPO_NAMES_EN
import com.duoc.rotompedia.data.TYPE_CHART_MATRIX
import com.duoc.rotompedia.ui.components.RotomPageScaffold as Scaffold
import com.duoc.rotompedia.ui.components.RotomPageTopBar as TopAppBar
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo

@Composable
fun TypeChartScreen(onNavigateBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Tabla de Tipos", maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = {
            IconButton(onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        })
    }) { padding -> TypeChartContent(Modifier.padding(padding).fillMaxSize().padding(8.dp)) }
}

/** Four panes, two shared offsets: neither heading can scroll out of its own axis. */
@Composable
internal fun TypeChartContent(modifier: Modifier = Modifier) {
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val width = (75 * fontScale).dp
    val height = (50 * fontScale).dp
    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(height)) {
            HeaderCell("Defensa →\nAtaque ↓", isColumnHeader = true,
                modifier = Modifier.testTag("type_corner"), width = width, height = height)
            Row(Modifier.weight(1f).horizontalScroll(horizontal).testTag("type_columns")) {
                TIPO_NAMES_EN.forEach { type ->
                    HeaderCell(traducirTipo(type), typeColor = getTipoColor(type), modifier = Modifier.testTag("type_column_$type"), width = width, height = height)
                }
            }
        }
        Row(Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.width(width).fillMaxHeight().verticalScroll(vertical).testTag("type_rows")) {
                TIPO_NAMES_EN.forEach { type ->
                    HeaderCell(traducirTipo(type), typeColor = getTipoColor(type), isColumnHeader = true,
                        modifier = Modifier.testTag("type_row_$type"), width = width, height = height)
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(vertical).horizontalScroll(horizontal).testTag("type_matrix")) {
                TIPO_NAMES_EN.forEachIndexed { row, _ ->
                    Row {
                        TYPE_CHART_MATRIX[row].forEachIndexed { column, value ->
                            EffectivenessCell(value, Modifier.testTag("type_cell_${row}_$column"), width, height)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCell(text: String, modifier: Modifier = Modifier, typeColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    isColumnHeader: Boolean = false, width: Dp = 75.dp, height: Dp = 50.dp) {
    val corner = isColumnHeader && text.contains("\n")
    Box(modifier.size(width, height).padding(1.dp).clip(RoundedCornerShape(8.dp)).background(typeColor).padding(4.dp),
        contentAlignment = Alignment.Center) {
        Text(text, fontSize = if (corner) 11.sp else 13.sp, fontWeight = FontWeight.Bold,
            color = if (typeColor == MaterialTheme.colorScheme.surfaceVariant) MaterialTheme.colorScheme.onSurface
                else if (typeColor.luminance() > 0.179f) Color.Black else Color.White,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun EffectivenessCell(value: String, modifier: Modifier = Modifier, width: Dp = 75.dp, height: Dp = 50.dp) {
    val (text, color) = when (value) {
        "2" -> "2x" to Color(0xFF4CAF50).copy(alpha = 0.7f)
        "0.5" -> "½" to Color(0xFFF44336).copy(alpha = 0.7f)
        "0" -> "0" to Color(0xFFBDBDBD).copy(alpha = 0.7f)
        else -> "" to Color.Transparent
    }
    Box(modifier.size(width, height).padding(1.dp).clip(RoundedCornerShape(8.dp)).background(color).padding(4.dp),
        contentAlignment = Alignment.Center) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
