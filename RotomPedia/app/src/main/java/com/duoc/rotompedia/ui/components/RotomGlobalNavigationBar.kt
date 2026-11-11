package com.duoc.rotompedia.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal data class MainNavDestination(val label: String, val route: String, val icon: ImageVector)

internal val mainNavDestinations = listOf(
    MainNavDestination("Inicio", "menu_principal_screen", Icons.Default.Home),
    MainNavDestination("Pokédex", "pokedex", Icons.Default.CatchingPokemon),
    MainNavDestination("Mapas", "map_feature_screen", Icons.Default.Map),
    MainNavDestination("Equipos", "team_builder", Icons.Default.Groups),
    MainNavDestination("Más", "more_tools", Icons.Default.Menu)
)

internal fun primaryNavRoute(route: String?): String = when {
    route == "menu_principal_screen" -> "menu_principal_screen"
    route == "pokedex" || route?.startsWith("pokemon_detail/") == true || route?.startsWith("pokemon_moves/") == true -> "pokedex"
    route == "map_feature_screen" || route?.startsWith("map_viewer/") == true -> "map_feature_screen"
    route == "team_builder" -> "team_builder"
    else -> "more_tools"
}

@Composable
fun RotomGlobalNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, contentColor = Color(0xFF254652),
        tonalElevation = 0.dp, modifier = Modifier.testTag("rotom_global_nav")) {
        val active = primaryNavRoute(currentRoute)
        mainNavDestinations.forEach { item ->
            val selected = active == item.route
            NavigationBarItem(selected = selected, onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1) }, alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFAE3A2D),
                    selectedTextColor = Color(0xFFAE3A2D),
                    indicatorColor = Color(0xFFFFE9E3),
                    unselectedIconColor = Color(0xFF345A66),
                    unselectedTextColor = Color(0xFF345A66)))
        }
    }
}
