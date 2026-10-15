package com.duoc.rotompedia

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.rotompedia.data.local.AppDatabase
import com.duoc.rotompedia.ui.screens.*
import com.duoc.rotompedia.ui.theme.RotompediaTheme
import com.duoc.rotompedia.viewmodel.*
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RotompediaTheme {
                AppNavigationController()
            }
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(60.dp)
            .width(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2A2A2A),
            contentColor = Color(0xFF61DAF6)
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun MenuPrincipalScreen(navController: NavController, authViewModel: AuthViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.rotomfondo),
            contentDescription = "Fondo de la aplicación",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Menú Principal",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF61DAF6),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MenuButton(text = "Pokédex") { navController.navigate("pokedex") }
                Spacer(modifier = Modifier.width(16.dp))
                MenuButton(text = "Tabla de Tipos") { navController.navigate("type_chart_screen") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MenuButton(text = "Mapas") { navController.navigate("map_feature_screen") }
                Spacer(modifier = Modifier.width(16.dp))
                MenuButton(text = "Líderes") { navController.navigate("gym_leaders") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { authViewModel.cerrarSesion() }) {
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
fun AppNavigationController() {
    val navController = rememberNavController()

    // --- INICIO DE CAMBIOS ---
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application

    // Base de datos UNA vez
    val database = remember { AppDatabase.getDatabase(context) }

    // Factories
    val authViewModelFactory = remember {
        AuthViewModelFactory(application, database.usuarioDao())
    }

    val pokemonViewModelFactory = remember {
        PokemonViewModelFactory(database.favoritoDao())
    }

    // AuthViewModel con Factory
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val pokemonViewModel: PokemonViewModel = viewModel(factory = pokemonViewModelFactory)
    // --- FIN DE CAMBIOS ---

    val activity = LocalContext.current as FragmentActivity
    val executor = ContextCompat.getMainExecutor(activity)

    val userSession by authViewModel.userSession.collectAsState()

    LaunchedEffect(userSession) {
        when (val session = userSession) {
            is UserSession.Active -> {
                pokemonViewModel.setCurrentUser(session.username)
                showBiometricPrompt(
                    activity = activity,
                    executor = executor,
                    onSuccess = {
                        navController.navigate("menu_principal_screen") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onErrorOrCancel = {
                        authViewModel.cerrarSesion()
                    }
                )
            }
            is UserSession.Inactive -> {
                pokemonViewModel.setCurrentUser("")
                navController.navigate("login") {
                    popUpTo(0)
                }
            }
            is UserSession.Undetermined -> Unit
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("menu_principal_screen") {
            MenuPrincipalScreen(navController, authViewModel)
        }

        composable("pokedex") {
            PokedexScreen(
                onPokemonClick = { pokemonId ->
                    navController.navigate("pokemon_detail/$pokemonId")
                },
                onNavigateBack = { navController.navigateUp() },
                viewModelFactory = pokemonViewModelFactory
            )
        }

        composable(
            route = "pokemon_detail/{pokemonId}",
            arguments = listOf(navArgument("pokemonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getString("pokemonId")
            if (pokemonId != null) {
                PokemonDetailScreen(
                    pokemonId = pokemonId,
                    onNavigateBack = { navController.popBackStack() },
                    viewModelFactory = pokemonViewModelFactory,
                    onPokemonClick = { newPokemonId ->
                        navController.navigate("pokemon_detail/$newPokemonId")
                    }
                )
            }
        }

        composable("type_chart_screen") {
            TypeChartScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable("map_feature_screen") {
            MapFeatureScreen(navController)
        }

        composable("gym_leaders") {
            GymLeadersScreen(navController)
        }

        composable(
            "map_viewer/{region}",
            arguments = listOf(navArgument("region") { type = NavType.StringType })
        ) { backStackEntry ->
            val region = backStackEntry.arguments?.getString("region")
            if (region != null) {
                MapViewerScreen(
                    region = region,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    executor: Executor,
    onSuccess: () -> Unit,
    onErrorOrCancel: () -> Unit = {}
) {
    val biometricManager = BiometricManager.from(activity)
    if (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        != BiometricManager.BIOMETRIC_SUCCESS
    ) {
        onSuccess()
        return
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación Requerida")
        .setSubtitle("Usa tu huella para acceder a RotomPedia")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onErrorOrCancel()
            }
        }
    )

    biometricPrompt.authenticate(promptInfo)
}
