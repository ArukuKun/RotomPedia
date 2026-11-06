package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.util.RotomForm
import com.duoc.rotompedia.util.RotomThemeManager

private data class RotomDestination(val title: String, val route: String, val icon: ImageVector)
private val destinations = listOf(
    RotomDestination("Pokédex", "pokedex", Icons.Default.Search),
    RotomDestination("Equipos", "team_builder", Icons.Default.Groups),
    RotomDestination("Objetos", "competitive_items", Icons.Default.Backpack),
    RotomDestination("Movimientos", "moves_reference", Icons.Default.FlashOn),
    RotomDestination("Habilidades", "abilities_reference", Icons.Default.AutoAwesome),
    RotomDestination("Mapas", "map_feature_screen", Icons.Default.Map),
    RotomDestination("Calculadora IV/EV", "stat_calculator", Icons.Default.Calculate),
    RotomDestination("Ratio Captura", "catch_rate_calculator?pokemonId=1", Icons.Default.CatchingPokemon),
    RotomDestination("Tabla Tipos", "type_chart_screen", Icons.Default.GridView),
    RotomDestination("Líderes", "gym_leaders", Icons.Default.EmojiEvents),
    RotomDestination("Modo Versus", "versus", Icons.Default.CompareArrows)
)

@Composable
fun RotomHomeScreen(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val form by RotomThemeManager.currentForm.collectAsState()
    var formsOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val tips = remember(form) { form.getTips() }
    var tip by rememberSaveable(form.name) { mutableIntStateOf(0) }
    RotomScaffold("RotomPedia", actions = { RotomAppearanceAction() }) {
        Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            Text("¿Qué exploramos hoy?", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp))
            Text("Tu compañero durante la aventura.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
            LazyVerticalGrid(GridCells.Fixed(2), Modifier.weight(1f).testTag("rotom_home_destinations"),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)) {
                items(destinations, key = { it.route }) { destination ->
                    RotomCard(Modifier.fillMaxWidth().heightIn(min = 92.dp), onClick = { onNavigate(destination.route) }) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(destination.icon, null, tint = MaterialTheme.colorScheme.primary)
                            Text(destination.title, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    RotomCard(Modifier.fillMaxWidth(), onClick = { tip = (tip + 1) % tips.size }) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(form.displayName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text(tips[tip.coerceIn(tips.indices)])
                            Text("Toca para otro consejo", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    OutlinedButton({ formsOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("Forma: ${form.displayName}") }
                }
                item { TextButton(onLogout, Modifier.fillMaxWidth()) { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) } }
            }
        }
    }
    if (formsOpen) AlertDialog(onDismissRequest = { formsOpen = false }, title = { Text("Forma de Rotom") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RotomForm.entries.forEach { candidate ->
                OutlinedButton({ RotomThemeManager.setForm(context, candidate); formsOpen = false }, Modifier.fillMaxWidth(),
                    border = BorderStroke(if (candidate == form) 2.dp else 1.dp, MaterialTheme.colorScheme.outline)) {
                    Text("${candidate.iconEmoji} ${candidate.displayName}")
                }
            }
        }
    }, confirmButton = { TextButton({ formsOpen = false }) { Text("Cerrar") } })
}
