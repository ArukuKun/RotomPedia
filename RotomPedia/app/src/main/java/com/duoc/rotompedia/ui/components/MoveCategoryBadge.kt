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
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.data.reference.ReferencePresentation

@Composable
fun MoveCategoryBadge(category: String) {
    val icon = when (category) {
        "physical" -> Icons.Default.FlashOn
        "special" -> Icons.Default.Waves
        else -> Icons.Default.RadioButtonUnchecked
    }
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(ReferencePresentation.category(category), style = MaterialTheme.typography.labelMedium)
        }
    }
}
