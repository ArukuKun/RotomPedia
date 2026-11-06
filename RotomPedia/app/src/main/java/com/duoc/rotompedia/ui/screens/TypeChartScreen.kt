package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.duoc.rotompedia.ui.components.RotomPageScaffold as Scaffold
import androidx.compose.material3.Text
import com.duoc.rotompedia.ui.components.RotomPageTopBar as TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.data.TIPO_NAMES_EN
import com.duoc.rotompedia.data.TYPE_CHART_MATRIX
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeChartScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabla de Tipos", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeaderCell(
                        text = "Defensa →\nAtaque ↓",
                        isColumnHeader = true
                    )

                    TIPO_NAMES_EN.forEach { tipo ->
                        HeaderCell(text = traducirTipo(tipo), typeColor = getTipoColor(tipo))
                    }
                }

                TIPO_NAMES_EN.forEachIndexed { rowIndex, tipoAtacante ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HeaderCell(
                            text = traducirTipo(tipoAtacante),
                            typeColor = getTipoColor(tipoAtacante),
                            isColumnHeader = true
                        )

                        TYPE_CHART_MATRIX[rowIndex].forEach { efectividad ->
                            EffectivenessCell(value = efectividad)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCell(text: String, typeColor: Color = MaterialTheme.colorScheme.surfaceVariant, isColumnHeader: Boolean = false) {
    val isCornerCell = isColumnHeader && text.contains("\n")
    val fontSize = if (isCornerCell) 11.sp else 13.sp

    Box(
        modifier = Modifier
            .size(width = 75.dp, height = 50.dp)
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(typeColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = if (typeColor == MaterialTheme.colorScheme.surfaceVariant) MaterialTheme.colorScheme.onSurface
                else if (typeColor.luminance() > 0.179f) Color.Black else Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EffectivenessCell(value: String) {
    val (text, bgColor) = when (value) {
        "2" -> "2x" to Color(0xFF4CAF50).copy(alpha = 0.7f)
        "0.5" -> "½" to Color(0xFFF44336).copy(alpha = 0.7f)
        "0" -> "0" to Color(0xFFBDBDBD).copy(alpha = 0.7f)
        else -> "" to MaterialTheme.colorScheme.background.copy(alpha = 0.0f)
    }

    Box(
        modifier = Modifier
            .size(width = 75.dp, height = 50.dp)
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
