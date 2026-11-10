package com.duoc.rotompedia

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.rotompedia.di.AppContainer
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import com.duoc.rotompedia.util.RotomAppearanceManager
import com.duoc.rotompedia.util.RotomThemeManager
import com.duoc.rotompedia.viewmodel.*
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RotomThemeManager.init(applicationContext)
        RotomAppearanceManager.init(applicationContext)
        setContent { RotompediaTheme { AppNavigationController() } }
    }
}

@Composable
fun MenuPrincipalScreen(navController: NavController, authViewModel: AuthViewModel) {
    RotomHomeScreen({ navController.navigate(it) }, { authViewModel.cerrarSesion() })
}

@Composable
fun AppNavigationController() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val container = remember { AppContainer.get(context) }
    val database = container.database
    val authFactory = remember { AuthViewModelFactory(application, database.usuarioDao(), container.sessions) }
    val pokemonFactory = remember {
        PokemonViewModelFactory(database.cachedPokemonDao(), container.sessions, container.favorites, container.pokemon)
    }
    val teamFactory = remember { TeamBuilderViewModelFactory(container.teams, container.sessions) }
    val versus = remember { PokemonVersusViewModel(database.cachedPokemonDao(), container.pokemon) }
    val auth: AuthViewModel = viewModel(factory = authFactory)
    val pokemon: PokemonViewModel = viewModel(factory = pokemonFactory)
    val teams: TeamBuilderViewModel = viewModel(factory = teamFactory)
    val activity = context as FragmentActivity
    val executor = ContextCompat.getMainExecutor(activity)
    val session by auth.userSession.collectAsState()
    LaunchedEffect(session) {
        when (session) {
            is UserSession.Active -> showBiometricPrompt(activity, executor, onSuccess = {
                navController.navigate("menu_principal_screen") { popUpTo("login") { inclusive = true } }
            }, onErrorOrCancel = { auth.cerrarSesion() })
            is UserSession.Inactive -> navController.navigate("login") { popUpTo(0) }
            is UserSession.Undetermined -> Unit
        }
    }
    LaunchedEffect(Unit) { auth.checkSession() }
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(auth, { navController.navigate("register") }) }
        composable("register") { RegisterScreen({ navController.popBackStack() }, { navController.popBackStack() }, auth) }
        composable("menu_principal_screen") { MenuPrincipalScreen(navController, auth) }
        composable("pokedex") {
            PokedexScreen({ navController.navigate("pokemon_detail/$it") }, { navController.navigateUp() }, pokemonFactory)
        }
        composable("pokemon_detail/{pokemonId}", arguments = listOf(navArgument("pokemonId") { type = NavType.StringType })) { entry ->
            entry.arguments?.getString("pokemonId")?.let { id ->
                PokemonDetailScreen(id, { navController.popBackStack() }, pokemonFactory,
                    onItemClick = { navController.navigate("competitive_items?slug=${android.net.Uri.encode(it)}") },
                    onPokemonClick = { navController.navigate("pokemon_detail/$it") },
                    onCatchRateClick = { navController.navigate("catch_rate_calculator?pokemonId=$it") },
                    onMovesClick = { navController.navigate("pokemon_moves/$it") })
            }
        }
        composable("pokemon_moves/{pokemonId}", arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("pokemonId") ?: 0
            if (id > 0) PokemonMovesRoute(id, { navController.popBackStack() }, { navController.navigate("competitive_items?itemId=$it") })
        }
        composable("type_chart_screen") { TypeChartScreen { navController.navigateUp() } }
        composable("map_feature_screen") { MapFeatureScreen(navController) }
        composable("gym_leaders") { GymLeadersScreen(navController) }
        composable("team_builder") { TeamBuilderScreen(teams, pokemon) { navController.navigateUp() } }
        composable("versus") { PokemonVersusScreen(versus, pokemon) { navController.navigateUp() } }
        composable("stat_calculator") { StatCalculatorScreen(pokemon, { navController.navigateUp() }) }
        composable("catch_rate_calculator?pokemonId={pokemonId}", arguments = listOf(navArgument("pokemonId") {
            type = NavType.IntType; defaultValue = 1
        })) { entry ->
            CatchRateCalculatorScreen(pokemon, { navController.navigateUp() }, initialPokemonId = entry.arguments?.getInt("pokemonId") ?: 1)
        }
        composable("moves_reference?moveId={moveId}", arguments = listOf(navArgument("moveId") { type = NavType.IntType; defaultValue = 0 })) { entry ->
            ReferenceBrowserScreen(false, { navController.navigateUp() }, { navController.navigate("pokemon_detail/$it") },
                { navController.navigate("competitive_items?itemId=$it") }, initialMoveId = entry.arguments?.getInt("moveId") ?: 0)
        }
        composable("abilities_reference?abilityId={abilityId}", arguments = listOf(navArgument("abilityId") { type = NavType.IntType; defaultValue = 0 })) { entry ->
            ReferenceBrowserScreen(true, { navController.navigateUp() }, { navController.navigate("pokemon_detail/$it") },
                { navController.navigate("competitive_items?itemId=$it") }, initialAbilityId = entry.arguments?.getInt("abilityId") ?: 0)
        }
        composable("competitive_items?slug={slug}&itemId={itemId}", arguments = listOf(
            navArgument("slug") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("itemId") { type = NavType.IntType; defaultValue = 0 }
        )) { entry ->
            CompetitiveItemsScreen(onNavigateBack = { navController.navigateUp() }, initialSlug = entry.arguments?.getString("slug"),
                initialItemId = entry.arguments?.getInt("itemId") ?: 0,
                onMoveClick = { navController.navigate("moves_reference?moveId=$it") },
                onPokemonClick = { navController.navigate("pokemon_detail/$it") })
        }
        composable("map_viewer/{region}", arguments = listOf(navArgument("region") { type = NavType.StringType })) { entry ->
            entry.arguments?.getString("region")?.let { region ->
                // Existing cartography is explicitly deferred, not presented as rebuilt or verified.
                RotompediaTheme(legacy = true) {
                    if (region == "hoenn") {
                        VerifiedHoennMapScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onPokemonClick = { navController.navigate("pokemon_detail/$it") }
                        )
                    } else {
                        MapViewerScreen(region, { navController.popBackStack() }, { navController.navigate("pokemon_detail/$it") })
                    }
                }
            }
        }
    }
}

private fun showBiometricPrompt(activity: FragmentActivity, executor: Executor,
    onSuccess: () -> Unit, onErrorOrCancel: () -> Unit = {}) {
    val manager = BiometricManager.from(activity)
    if (manager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
        onSuccess(); return
    }
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación Requerida")
        .setSubtitle("Usa tu huella para acceder a RotomPedia")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()
    BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result); onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString); onErrorOrCancel()
        }
    }).authenticate(info)
}
