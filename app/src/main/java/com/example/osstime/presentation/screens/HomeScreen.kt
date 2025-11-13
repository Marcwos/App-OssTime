package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.osstime.presentation.components.TopBar

data class ClassInfo(
    val name: String,
    val time: String,
    val students: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    // 🔹 Datos falsos por ahora (para pruebas)
    val classesToday = listOf(
        ClassInfo("Guard Passing", "7:00 PM", 14),
        ClassInfo("Raspe de Media Guardia", "8:30 PM", 10),
        ClassInfo("Defensas desde Montada", "9:15 PM", 8)
    )

    Scaffold(
        topBar = {
            TopBar(
                userName = "Jeremy",
                onProfileClick = { /* navController.navigate("profile") */ },
                onMenuClick = { /* abrir drawer */ }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Clases del día",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Lista horizontal de clases
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classesToday) { classInfo ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(130.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = classInfo.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Hora: ${classInfo.time}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Alumnos: ${classInfo.students}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Button(
                                onClick = {
                                    // Navegar a pantalla de asistencia
                                    // navController.navigate("attendance")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Marcar asistencia")
                            }
                        }
                    }
                }
            }
        }
    }
}
