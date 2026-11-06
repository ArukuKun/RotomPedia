package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.viewmodel.AuthState
import com.duoc.rotompedia.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onNavigateToLogin: () -> Unit, authViewModel: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val state by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(state) {
        when (state) {
            AuthState.REGISTRATION_SUCCESS -> {
                Toast.makeText(context, "¡Registro exitoso! Iniciando...", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState(); onRegisterSuccess()
            }
            AuthState.USER_EXISTS -> { error = "El nombre de usuario ya está registrado."; authViewModel.resetAuthState() }
            AuthState.EMAIL_EXISTS -> { error = "El correo electrónico ya está registrado."; authViewModel.resetAuthState() }
            AuthState.EMPTY_FIELDS -> { error = "Por favor, completa todos los campos requeridos."; authViewModel.resetAuthState() }
            else -> Unit
        }
    }
    RotomScaffold("Crear cuenta", onNavigateToLogin, actions = { RotomAppearanceAction() }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tu aventura con Rotom", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            RotomCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(name, { name = it; error = null }, label = { Text("Nombre de Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(email, { email = it; error = null }, label = { Text("Correo electrónico") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it; error = null }, label = { Text("Contraseña") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    Button({ authViewModel.registrarUsuario(name.trim(), email.trim(), password) }, enabled = state != AuthState.LOADING,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Registrarse") }
                    TextButton(onNavigateToLogin, Modifier.fillMaxWidth()) { Text("¿Ya tienes cuenta? Inicia sesión") }
                }
            }
        }
    }
}
