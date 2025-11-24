package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.presentation.components.BottomNavigationBar
import com.example.osstime.presentation.components.ClassCardView
import com.example.osstime.presentation.components.Title
import com.example.osstime.presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassScreen(navController: NavHostController) {
    // Lista de todas las clases (combinando las de hoy y mañana)
    val allClasses = remember {
        listOf(
            ClassSession(
                id = "1",
                name = "Pasada de media guardia",
                type = "NOGI",
                date = "Jueves 7/12/25",
                description = "Pase + finalización kata-gatame",
                time = "7:00 PM"
            ),
            ClassSession(
                id = "2",
                name = "Guardia Lazo",
                type = "GI",
                date = "Viernes 8/12/25",
                description = "Guardia Lazo + raspada",
                time = "7:00 PM"
            )
        )
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_class_form") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar clase",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(24.dp))
            
            Title(
                text = "Clases"
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "${allClasses.size} clases registradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (allClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay clases registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Presiona el botón + para agregar una",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allClasses) { classSession ->
                        ClassCardView(
                            classSession = classSession,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

