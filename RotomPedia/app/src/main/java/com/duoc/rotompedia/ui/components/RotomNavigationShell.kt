package com.duoc.rotompedia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/** Owns only bottom navigation. Each destination owns its ONE top bar and reading surface. */
@Composable
fun RotomNavigationShell(navController: NavHostController, content: @Composable (PaddingValues) -> Unit) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    var activeTab by rememberSaveable { mutableStateOf("menu_principal_screen") }
    LaunchedEffect(route) {
        if (route != null && !route.startsWith("pokemon_detail/") && !route.startsWith("pokemon_moves/")) {
            activeTab = primaryNavRoute(route)
        }
    }
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
        if (route != null && route != "login" && route != "register") {
            RotomGlobalNavigationBar(activeTab) { target ->
                if (target != route) navController.navigate(target) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo("menu_principal_screen") { saveState = true }
                }
            }
        }
    }, containerColor = MaterialTheme.colorScheme.background, content = content)
}
