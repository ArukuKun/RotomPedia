package com.duoc.rotompedia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

private const val MAIN_OWNER = "rotompedia.mainOwner"

/** Home is the anchor, never a saved child stack to restore. */
fun NavController.navigateMainDestination(target: String) {
    if (currentDestination?.route == target) return
    if (target == "menu_principal_screen") {
        popBackStack(target, false, saveState = true)
    } else navigate(target) {
        launchSingleTop = true
        restoreState = true
        popUpTo("menu_principal_screen") { saveState = true }
    }
}

/** Owns only bottom navigation. Each destination owns its ONE top bar and reading surface. */
@Composable
fun RotomNavigationShell(navController: NavHostController, content: @Composable (PaddingValues) -> Unit) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            val current = controller.currentBackStackEntry ?: return@OnDestinationChangedListener
            if (!current.savedStateHandle.contains(MAIN_OWNER)) {
                val previous = controller.previousBackStackEntry
                val inherited = previous?.savedStateHandle?.get<String>(MAIN_OWNER)
                val owner = if (mainNavDestinations.any { it.route == destination.route } ||
                    previous?.destination?.route == "menu_principal_screen" || inherited == null) {
                    primaryNavRoute(destination.route)
                } else inherited
                current.savedStateHandle[MAIN_OWNER] = owner
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }
    // Ownership travels with each saved back-stack entry, not a second remembered active tab.
    val activeTab = entry?.savedStateHandle?.get<String>(MAIN_OWNER) ?: primaryNavRoute(route)
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
        if (route != null && route != "login" && route != "register") {
            RotomGlobalNavigationBar(activeTab) { target ->
                navController.navigateMainDestination(target)
            }
        }
    }, containerColor = MaterialTheme.colorScheme.background, content = content)
}
