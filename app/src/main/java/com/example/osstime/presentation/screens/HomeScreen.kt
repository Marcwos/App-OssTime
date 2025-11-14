package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.osstime.presentation.components.TopBar
import com.example.osstime.presentation.components.ClassSection
import com.example.osstime.presentation.components.Title

data class ClassInfo(
    val name: String,
    val description: String,
    val time: String,
    val tipo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    // 🔹 Datos falsos por ahora (para pruebas)
    val todayClass = listOf(
        ClassInfo("Pasada de media guardia", "Pase + finalización kata-gatame", "7:00 PM", "NOGI"),
    )
    val tomorrowClass = listOf(
        ClassInfo("Guardia Lazo", "Guardia Lazo + raspada", "7:00 PM", "GI"),
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
                classes = todayClass
            )

            Spacer(Modifier.height(24.dp))

            ClassSection(
                title = "Clases del día de mañana",
                date = "Viernes 8/12/25",
                classes = tomorrowClass
            )
        }
    }
}
