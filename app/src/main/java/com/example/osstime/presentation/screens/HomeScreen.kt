package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.osstime.data.repository.ClassRepositoryImpl
import com.example.osstime.presentation.components.TopBar
import com.example.osstime.presentation.components.ClassSection
import com.example.osstime.presentation.components.Title
import com.example.osstime.presentation.components.BottomNavigationBar
import com.example.osstime.presentation.viewmodel.HomeViewModel

/**
 * HomeScreen optimizado con ViewModel y manejo eficiente de estados
 * - Usa ViewModel para separar lógica de negocio
 * - collectAsStateWithLifecycle para respetar el ciclo de vida
 * - derivedStateOf para cálculos derivados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(ClassRepositoryImpl())
    }
) {
    // Estados del ViewModel - se actualizan automáticamente
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val tomorrowClasses by viewModel.tomorrowClasses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Cálculo derivado - solo se recalcula cuando cambian las clases
    val hasTodayClasses = remember { derivedStateOf { todayClasses.isNotEmpty() } }
    val hasTomorrowClasses = remember { derivedStateOf { tomorrowClasses.isNotEmpty() } }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) }
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

            // Mostrar loading solo si es necesario
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                if (hasTodayClasses.value) {
                    ClassSection(
                        title = "Clases del día hoy",
                        date = "Jueves 7/12/25",
                        classes = todayClasses,
                        navController = navController
                    )

                    Spacer(Modifier.height(24.dp))
                }

                if (hasTomorrowClasses.value) {
                    ClassSection(
                        title = "Clases del día de mañana",
                        date = "Viernes 8/12/25",
                        classes = tomorrowClasses,
                        navController = navController
                    )
                }
            }
        }
    }
}
