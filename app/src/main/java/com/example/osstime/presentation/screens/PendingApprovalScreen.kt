package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.R
import com.example.osstime.presentation.viewmodel.AuthViewModel

/**
 * Pantalla que se muestra cuando el usuario está pendiente de aprobación.
 * - Informa al usuario que su cuenta está siendo revisada
 * - Permite cerrar sesión para intentar con otra cuenta
 */
@Composable
fun PendingApprovalScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.oss_tiime_letra_negro),
                contentDescription = "Logo de Oss Time",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp)
            )
            
            // Icono de espera
            Icon(
                imageVector = Icons.Filled.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Cuenta Pendiente de Aprobación",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Tu cuenta ha sido creada exitosamente, pero aún no ha sido aprobada por un administrador.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Qué sucede ahora?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Un administrador revisará tu solicitud y activará tu cuenta. " +
                               "Recibirás acceso completo una vez que tu cuenta sea aprobada.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botón para volver al login
            OutlinedButton(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("pending_approval") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio de sesión")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Puedes intentar iniciar sesión nuevamente después de que tu cuenta sea aprobada.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
