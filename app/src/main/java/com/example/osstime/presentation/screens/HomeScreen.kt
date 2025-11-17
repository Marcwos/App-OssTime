package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.presentation.components.TopBar
import com.example.osstime.presentation.components.ClassSection
import com.example.osstime.presentation.components.Title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {

    // 🔹 Datos falsos por ahora (para pruebas)
    val todayClass = listOf(
        ClassSession(
            id = "1",
            name = "Pasada de media guardia",
            type = "NOGI",
            date = "Jueves 7/12/25",
            description = "Pase + finalización kata-gatame",
            time = "7:00 PM"
        ),
    )
    val tomorrowClass = listOf(
        ClassSession(
            id = "2",
            name = "Guardia Lazo",
            type = "GI",
            date = "Viernes 8/12/25",
            description = "Guardia Lazo + raspada",
            time = "7:00 PM"
        ),
    )

    Scaffold(
        topBar = { TopBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Title(
                text = "Hola Profesor Danager!"
            )

            ClassSection(
                title = "Clases del día hoy",
                date = "Jueves 7/12/25",
                classes = todayClass,
                navController = navController
            )

            Spacer(Modifier.height(24.dp))

            ClassSection(
                title = "Clases del día de mañana",
                date = "Viernes 8/12/25",
                classes = tomorrowClass,
                navController = navController
            )
        }
    }
}
