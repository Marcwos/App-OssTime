package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.R
import com.example.osstime.presentation.viewmodel.AuthNavigation
import com.example.osstime.presentation.viewmodel.AuthUiState
import com.example.osstime.presentation.viewmodel.AuthViewModel

/**
 * LoginScreen con autenticación Firebase.
 * - Valida credenciales con Firebase Auth
 * - Navega según rol (Admin/Professor)
 * - Muestra estado de aprobación pendiente
 */
@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val uiState by authViewModel.uiState.collectAsState()
    val navigation by authViewModel.navigation.collectAsState()
    
    // Manejar navegación según rol
    LaunchedEffect(navigation) {
        when (navigation) {
            is AuthNavigation.ToAdminHome -> {
                navController.navigate("admin_home") {
                    popUpTo("login") { inclusive = true }
                }
                authViewModel.clearNavigation()
            }
            is AuthNavigation.ToProfessorHome -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                authViewModel.clearNavigation()
            }
            is AuthNavigation.ToPendingApproval -> {
                navController.navigate("pending_approval") {
                    popUpTo("login") { inclusive = true }
                }
                authViewModel.clearNavigation()
            }
            is AuthNavigation.ToLogin -> {
                // Ya estamos en login
                authViewModel.clearNavigation()
            }
            AuthNavigation.None -> { /* No hacer nada */ }
        }
    }
    
    // Verificar si hay sesión activa al inicio
    LaunchedEffect(Unit) {
        authViewModel.checkCurrentUser()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.oss_tiime_letra_negro),
                contentDescription = "Logo de Oss Time",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña con toggle de visibilidad
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Mostrar error si existe
            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Botón Iniciar Sesión
            Button(
                onClick = { 
                    authViewModel.signIn(email.trim(), password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Iniciar sesión")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Link para registrarse
            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
