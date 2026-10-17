package com.duoc.rotompedia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.rotompedia.R
import com.duoc.rotompedia.viewmodel.AuthState
import com.duoc.rotompedia.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToRegister: () -> Unit
) {
    var nombreUsuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var recordarDatos by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.rotomfondo),
            contentDescription = "Fondo de la aplicación",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay oscuro sutil para garantizar legibilidad óptima
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
            // Tarjeta contenedora con alto contraste y fondo oscuro sólido
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF61DAF6).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡ RotomPedia",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF61DAF6),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensaje de error si las credenciales fallan
                    if (authState == AuthState.INVALID_CREDENTIALS) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            color = Color(0xFF3E1A1A),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                        ) {
                            Text(
                                text = "⚠️ Usuario o contraseña incorrectos",
                                color = Color(0xFFFF8A80),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Campo de Usuario con alto contraste
                    OutlinedTextField(
                        value = nombreUsuario,
                        onValueChange = {
                            nombreUsuario = it
                            if (authState != AuthState.IDLE) authViewModel.resetAuthState()
                        },
                        label = { Text("Nombre de Usuario", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF61DAF6)
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
                            focusedBorderColor = Color(0xFF61DAF6),
                            unfocusedBorderColor = Color(0xFF555555),
                            focusedLabelColor = Color(0xFF61DAF6),
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            cursorColor = Color(0xFF61DAF6)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo de Contraseña con alto contraste
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = {
                            contrasena = it
                            if (authState != AuthState.IDLE) authViewModel.resetAuthState()
                        },
                        label = { Text("Contraseña", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF61DAF6)
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2A2A2A),
                            unfocusedContainerColor = Color(0xFF2A2A2A),
                            focusedBorderColor = Color(0xFF61DAF6),
                            unfocusedBorderColor = Color(0xFF555555),
                            focusedLabelColor = Color(0xFF61DAF6),
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            cursorColor = Color(0xFF61DAF6)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkbox de recordar datos
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = recordarDatos,
                            onCheckedChange = { recordarDatos = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF61DAF6),
                                uncheckedColor = Color.LightGray,
                                checkmarkColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Recordar datos",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Iniciar Sesión
                    Button(
                        onClick = { authViewModel.iniciarSesion(nombreUsuario.trim(), contrasena, recordarDatos) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF61DAF6),
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Navegar a Registro
                    Row(
                        modifier = Modifier.clickable { onNavigateToRegister() },
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿No tienes cuenta? ",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Regístrate aquí",
                            color = Color(0xFF61DAF6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}