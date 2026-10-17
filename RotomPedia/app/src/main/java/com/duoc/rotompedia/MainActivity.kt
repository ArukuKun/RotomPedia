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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import com.duoc.rotompedia.util.RotomForm
import com.duoc.rotompedia.util.RotomThemeManager

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RotomThemeManager.init(applicationContext)
        setContent {
            RotompediaTheme {
                AppNavigationController()
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    containerColor: Color = Color(0xFF2A2A2A),
    contentColor: Color = Color(0xFF61DAF6),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .width(165.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MenuPrincipalScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val rotomForm by RotomThemeManager.currentForm.collectAsState()
    val tips = remember(rotomForm) { rotomForm.getTips() }
    var currentTipIndex by remember { mutableStateOf(0) }
    var showThemeDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RotomPedia",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = rotomForm.accentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Widget interactivo de Rotom con diálogo / Tips
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentTipIndex = (currentTipIndex + 1) % tips.size
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E).copy(alpha = 0.92f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, rotomForm.primaryColor.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(rotomForm.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = rotomForm.iconEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = rotomForm.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = rotomForm.primaryColor
                            )
                            Text(
                                text = "Toca para otro tip",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tips.getOrElse(currentTipIndex) { tips.first() },
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fila 1: Pokedex y Tabla de Tipos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MenuButton(
                    text = "Pokédex",
                    contentColor = rotomForm.accentColor
                ) { navController.navigate("pokedex") }
                Spacer(modifier = Modifier.width(14.dp))
                MenuButton(
                    text = "Tabla Tipos",
                    contentColor = rotomForm.accentColor
                ) { navController.navigate("type_chart_screen") }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila 2: Mapas y Líderes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MenuButton(
                    text = "Mapas",
                    contentColor = rotomForm.accentColor
                ) { navController.navigate("map_feature_screen") }
                Spacer(modifier = Modifier.width(14.dp))
                MenuButton(
                    text = "Líderes",
                    contentColor = rotomForm.accentColor
                ) { navController.navigate("gym_leaders") }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila 3: Constructor de Equipos y Comparador Versus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MenuButton(
                    text = "Equipos",
                    contentColor = rotomForm.primaryColor
                ) { navController.navigate("team_builder") }
                Spacer(modifier = Modifier.width(14.dp))
                MenuButton(
                    text = "Modo Versus",
                    contentColor = rotomForm.primaryColor
                ) { navController.navigate("versus") }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selector de Forma de Rotom
            OutlinedButton(
                onClick = { showThemeDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, rotomForm.accentColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = rotomForm.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Forma: ${rotomForm.displayName}",
                    color = rotomForm.accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = { authViewModel.cerrarSesion() }) {
                Text("Cerrar Sesión", color = Color(0xFFFF5252), fontSize = 13.sp)
            }
        }
    }

    // Diálogo de Selección de Forma Rotom
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Cambiar Forma de Rotom",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RotomForm.values().forEach { form ->
                        val isSelected = form == rotomForm
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) form.primaryColor.copy(alpha = 0.25f) else Color(0xFF2A2A2A))
                                .border(
                                    1.dp,
                                    if (isSelected) form.primaryColor else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    RotomThemeManager.setForm(context, form)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = form.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = form.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) form.primaryColor else Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = form.subtitle,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cerrar", color = rotomForm.accentColor)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
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
        PokemonViewModelFactory(database.favoritoDao(), database.cachedPokemonDao())
    }

    val teamBuilderViewModelFactory = remember {
        TeamBuilderViewModelFactory(database.equipoDao(), database.cachedPokemonDao())
    }

    val versusViewModel = remember {
        PokemonVersusViewModel(database.cachedPokemonDao())
    }

    // ViewModels
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val pokemonViewModel: PokemonViewModel = viewModel(factory = pokemonViewModelFactory)
    val teamBuilderViewModel: TeamBuilderViewModel = viewModel(factory = teamBuilderViewModelFactory)
    // --- FIN DE CAMBIOS ---

    val activity = LocalContext.current as FragmentActivity
    val executor = ContextCompat.getMainExecutor(activity)

    val userSession by authViewModel.userSession.collectAsState()

    LaunchedEffect(userSession) {
        when (val session = userSession) {
            is UserSession.Active -> {
                pokemonViewModel.setCurrentUser(session.username)
                teamBuilderViewModel.setCurrentUser(session.username)
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
                teamBuilderViewModel.setCurrentUser("")
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

        composable("team_builder") {
            TeamBuilderScreen(
                teamBuilderViewModel = teamBuilderViewModel,
                pokemonViewModel = pokemonViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("versus") {
            PokemonVersusScreen(
                versusViewModel = versusViewModel,
                pokemonViewModel = pokemonViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
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
