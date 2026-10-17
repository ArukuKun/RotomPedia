package com.duoc.rotompedia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.R
import com.duoc.rotompedia.viewmodel.AuthState
import com.duoc.rotompedia.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.REGISTRATION_SUCCESS -> {
                Toast.makeText(context, "¡Registro exitoso! Iniciando...", Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
                onRegisterSuccess()
            }
            AuthState.USER_EXISTS -> {
                errorMessage = "El nombre de usuario ya está registrado."
                authViewModel.resetAuthState()
            }
            AuthState.EMAIL_EXISTS -> {
                errorMessage = "El correo electrónico ya está registrado."
                authViewModel.resetAuthState()
            }
            AuthState.EMPTY_FIELDS -> {
                errorMessage = "Por favor, completa todos los campos requeridos."
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.rotomfondo),
            contentDescription = "Fondo de la aplicación",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay oscuro sutil para garantizar contraste y legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF6D00).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡ Registro Entrenador",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6D00),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Crea tu cuenta local de RotomPedia",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensaje de alerta de error si existe
                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            color = Color(0xFF3E1A1A),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                color = Color(0xFFFF8A80),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Campo de Nombre de Usuario
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorMessage = null
                        },
                        label = { Text("Nombre de Usuario", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2A2A2A),
                            unfocusedContainerColor = Color(0xFF2A2A2A),
                            focusedBorderColor = Color(0xFFFF6D00),
                            unfocusedBorderColor = Color(0xFF555555),
                            focusedLabelColor = Color(0xFFFF6D00),
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            cursorColor = Color(0xFFFF6D00)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo de Correo Electrónico
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            errorMessage = null
                        },
                        label = { Text("Correo Electrónico", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2A2A2A),
                            unfocusedContainerColor = Color(0xFF2A2A2A),
                            focusedBorderColor = Color(0xFFFF6D00),
                            unfocusedBorderColor = Color(0xFF555555),
                            focusedLabelColor = Color(0xFFFF6D00),
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            cursorColor = Color(0xFFFF6D00)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo de Contraseña
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = {
                            contrasena = it
                            errorMessage = null
                        },
                        label = { Text("Contraseña", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00)
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2A2A2A),
                            unfocusedContainerColor = Color(0xFF2A2A2A),
                            focusedBorderColor = Color(0xFFFF6D00),
                            unfocusedBorderColor = Color(0xFF555555),
                            focusedLabelColor = Color(0xFFFF6D00),
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            cursorColor = Color(0xFFFF6D00)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de Registro
                    Button(
                        onClick = {
                            authViewModel.registrarUsuario(nombre.trim(), correo.trim(), contrasena)
                        },
                        enabled = authState != AuthState.LOADING,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6D00),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (authState == AuthState.LOADING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Registrarse",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Navegar a Iniciar Sesión
                    Row(
                        modifier = Modifier.clickable { onNavigateToLogin() },
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? ",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Inicia sesión",
                            color = Color(0xFFFF6D00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
