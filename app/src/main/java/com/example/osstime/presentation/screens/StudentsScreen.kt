package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.presentation.components.BottomNavigationBar
import com.example.osstime.presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Vista de Estudiantes",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Aquí podrás gestionar y crear estudiantes.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

