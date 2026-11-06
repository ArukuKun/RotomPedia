package com.duoc.rotompedia.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.reference.ReferencePresentation
import com.duoc.rotompedia.ui.theme.RotomColors
import com.duoc.rotompedia.util.getTipoColor
import com.duoc.rotompedia.util.traducirTipo

/** Shared by the catalogue, learning rows, editor and full move sheet. */
@Composable
fun MoveTypeBadge(type: String) {
    val color = getTipoColor(type)
    Surface(color = color, contentColor = RotomColors.inkOn(color), shape = RoundedCornerShape(6.dp)) {
        Box(Modifier.widthIn(min = 80.dp).heightIn(min = 32.dp).padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center) {
            Text(traducirTipo(type), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MoveCategoryBadge(category: String) {
    val icon = when (category) {
        "physical" -> Icons.Default.FlashOn
        "special" -> Icons.Default.Waves
        else -> Icons.Default.RadioButtonUnchecked
    }
    val color = when (category) {
        "physical" -> Color(0xFFAD3528)
        "special" -> Color(0xFF245C9B)
        "status" -> Color(0xFF55515F)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val ink = RotomColors.inkOn(color)
    Surface(color = color, contentColor = ink, shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, ink.copy(alpha = 0.25f))) {
        Row(Modifier.heightIn(min = 32.dp).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(ReferencePresentation.category(category), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
