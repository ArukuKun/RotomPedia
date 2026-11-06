package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.ui.components.*
import com.duoc.rotompedia.viewmodel.AuthState
import com.duoc.rotompedia.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel(), onNavigateToRegister: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberAccount by remember { mutableStateOf(false) }
    val state by authViewModel.authState.collectAsState()
    RotomScaffold("RotomPedia", actions = { RotomAppearanceAction() }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            RotomCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state == AuthState.INVALID_CREDENTIALS) Text("Usuario o contraseña incorrectos", color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(name, { name = it; if (state != AuthState.IDLE) authViewModel.resetAuthState() },
                        label = { Text("Nombre de Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it; if (state != AuthState.IDLE) authViewModel.resetAuthState() },
                        label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(rememberAccount, { rememberAccount = it }); Text("Recordar datos")
                    }
                    Button({ authViewModel.iniciarSesion(name.trim(), password, rememberAccount) },
                        enabled = state != AuthState.LOADING, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Iniciar Sesión") }
                    TextButton(onNavigateToRegister, Modifier.fillMaxWidth()) { Text("¿No tienes cuenta? Regístrate aquí") }
                }
            }
        }
    }
}
