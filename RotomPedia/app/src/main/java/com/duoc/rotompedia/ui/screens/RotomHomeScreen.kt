package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.duoc.rotompedia.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.util.RotomForm
import com.duoc.rotompedia.util.RotomThemeManager

/** Real destinations: the home shortcut grid is not a replacement for any existing module. */
internal data class RotomDestination(val title: String, val route: String, val icon: ImageVector)
internal val homeDestinations = listOf(
    RotomDestination("Pokédex", "pokedex", Icons.Default.CatchingPokemon),
    RotomDestination("Mapas", "map_feature_screen", Icons.Default.Map),
    RotomDestination("Creador de equipos", "team_builder", Icons.Default.Groups),
    RotomDestination("Líderes y Alto Mando", "gym_leaders", Icons.Default.EmojiEvents),
    RotomDestination("Objetos", "competitive_items", Icons.Default.Backpack),
    RotomDestination("Habilidades", "abilities_reference", Icons.Default.AutoAwesome),
    RotomDestination("Tabla de tipos", "type_chart_screen", Icons.Default.GridView),
    RotomDestination("Todas las herramientas", "more_tools", Icons.Default.Apps)
)
internal val extraDestinations = listOf(
    RotomDestination("Movimientos", "moves_reference", Icons.Default.FlashOn),
    RotomDestination("Calculadora IV/EV", "stat_calculator", Icons.Default.Calculate),
    RotomDestination("Calculadora de captura", "catch_rate_calculator?pokemonId=1", Icons.Default.CatchingPokemon),
    RotomDestination("Comparador Pokémon", "versus", Icons.Default.CompareArrows)
)

private val DashboardInk = Color(0xFF193A46)
private val DashboardSecondary = Color(0xFF496B77)

@Composable
internal fun DashboardShortcut(destination: RotomDestination, onNavigate: (String) -> Unit) {
    Card(onClick = { onNavigate(destination.route) }, modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = DashboardInk),
        border = BorderStroke(1.dp, Color(0xFFCADDE0))) {
        Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = 104.dp).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(destination.icon, contentDescription = null, tint = Color(0xFF345966), modifier = Modifier.size(26.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(destination.title, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp, color = DashboardInk, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text("Abrir", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = DashboardSecondary)
            }
        }
    }
}

@Composable
fun RotomHomeScreen(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val form by RotomThemeManager.currentForm.collectAsState()
    var formsOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val tips = remember(form) { form.getTips() }
    var tip by rememberSaveable(form.name) { mutableIntStateOf(0) }
    RotomScaffold("Inicio", actions = { RotomAppearanceAction() }) {
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
            Text("Accesos a tus herramientas", color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp, bottom = 14.dp))
            LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize().testTag("rotom_home_destinations"),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)) {
                items(homeDestinations, key = { it.route }) { destination ->
                    DashboardShortcut(destination, onNavigate)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    RotomCard(Modifier.fillMaxWidth(), onClick = { tip = (tip + 1) % tips.size }) {
                        Row(Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(R.drawable.rotomfondo), contentDescription = null,
                                alignment = Alignment.TopCenter, contentScale = ContentScale.FillWidth,
                                modifier = Modifier.size(width = 94.dp, height = 78.dp)
                                    .clip(RoundedCornerShape(12.dp)))
                            Column(Modifier.weight(1f)) {
                                Text("Rotom te aconseja", color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(tips[tip.coerceIn(tips.indices)], color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 16.sp, maxLines = 4)
                                Text("Toca para otro consejo", color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp)
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("Rotom y preferencias", color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 9.dp))
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    RotomCard(Modifier.fillMaxWidth(), onClick = { formsOpen = true }) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Forma de Rotom", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Text(form.displayName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
                }
            }
        }
    }
    if (formsOpen) AlertDialog(onDismissRequest = { formsOpen = false }, title = { Text("Forma de Rotom") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RotomForm.entries.forEach { candidate ->
                OutlinedButton(onClick = { RotomThemeManager.setForm(context, candidate); formsOpen = false },
                    modifier = Modifier.fillMaxWidth(), border = BorderStroke(
                        if (candidate == form) 2.dp else 1.dp, MaterialTheme.colorScheme.outline)) {
                    Text("${candidate.iconEmoji} ${candidate.displayName}")
                }
            }
        }
    }, confirmButton = { TextButton(onClick = { formsOpen = false }) { Text("Cerrar") } })
}

/** Every remaining module remains reachable with no screen duplication or data changes. */
@Composable
fun RotomMoreToolsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val destinations = remember { listOf(homeDestinations[3], homeDestinations[4], homeDestinations[5],
        extraDestinations[0], homeDestinations[6], extraDestinations[1], extraDestinations[2], extraDestinations[3]) }
    RotomScaffold("Más herramientas", onBack) {
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
            Text("Todas tus herramientas Pokémon", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp, modifier = Modifier.padding(vertical = 18.dp))
            LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize().testTag("rotom_more_destinations"),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)) {
                items(destinations, key = { it.route }) { destination ->
                    DashboardShortcut(destination, onNavigate)
                }
            }
        }
    }
}
