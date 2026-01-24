package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.osstime.data.repository.AuthRepositoryImpl
import com.example.osstime.data.repository.ClassRepositoryImpl
import com.example.osstime.data.repository.StudentRepositoryImpl
import com.example.osstime.presentation.components.TopBar
import com.example.osstime.presentation.components.ClassSection
import com.example.osstime.presentation.components.Title
import com.example.osstime.presentation.components.BottomNavigationBar
import com.example.osstime.presentation.viewmodel.AuthNavigation
import com.example.osstime.presentation.viewmodel.AuthViewModel
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
        HomeViewModel(ClassRepositoryImpl(), StudentRepositoryImpl())
    },
    authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepositoryImpl())
    }
) {
    // Estados del ViewModel - se actualizan automáticamente
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val tomorrowClasses by viewModel.tomorrowClasses.collectAsStateWithLifecycle()
    val upcomingClasses by viewModel.upcomingClasses.collectAsStateWithLifecycle()
    val recentStudents by viewModel.recentStudents.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val authNavigation by authViewModel.navigation.collectAsStateWithLifecycle()
    
    // Navegar al login cuando se cierra sesión
    LaunchedEffect(authNavigation) {
        if (authNavigation is AuthNavigation.ToLogin) {
            authViewModel.clearNavigation()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Cálculo derivado - solo se recalcula cuando cambian las clases
    val hasTodayClasses = remember { derivedStateOf { todayClasses.isNotEmpty() } }
    val hasTomorrowClasses = remember { derivedStateOf { tomorrowClasses.isNotEmpty() } }

    Scaffold(
        topBar = { 
            TopBar(
                onLogoutClick = { authViewModel.signOut() }
            ) 
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Title(text = "Hola Profesor Danager!")
            }

            // Mostrar loading solo si es necesario
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                // Clases de hoy
                if (hasTodayClasses.value) {
                    item {
                        Column {
                            Text(
                                text = "Clases del día hoy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Jueves 7/12/25",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    items(todayClasses) { classSession ->
                        UpcomingClassCard(
                            classSession = classSession,
                            onClick = {
                                navController.navigate(
                                    "class_detail/${classSession.id}/${classSession.name}/" +
                                    "${classSession.description.ifEmpty { "_" }}/" +
                                    "${classSession.date}/${classSession.time.ifEmpty { "_" }}"
                                )
                            }
                        )
                    }
                }

                // Clases de mañana
                if (hasTomorrowClasses.value) {
                    item {
                        Column {
                            Text(
                                text = "Clases del día de mañana",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Viernes 8/12/25",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    items(tomorrowClasses) { classSession ->
                        UpcomingClassCard(
                            classSession = classSession,
                            onClick = {
                                navController.navigate(
                                    "class_detail/${classSession.id}/${classSession.name}/" +
                                    "${classSession.description.ifEmpty { "_" }}/" +
                                    "${classSession.date}/${classSession.time.ifEmpty { "_" }}"
                                )
                            }
                        )
                    }
                }

                // Clases próximas
                if (upcomingClasses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Clases Próximas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(upcomingClasses) { classSession ->
                                UpcomingClassCard(
                                    classSession = classSession,
                                    onClick = {
                                        navController.navigate(
                                            "class_detail/${classSession.id}/${classSession.name}/" +
                                            "${classSession.description.ifEmpty { "_" }}/" +
                                            "${classSession.date}/${classSession.time.ifEmpty { "_" }}"
                                        )
                                    },
                                    modifier = Modifier.width(280.dp)
                                )
                            }
                        }
                    }
                }

                // Estudiantes recientes
                if (recentStudents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Estudiantes Recientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(recentStudents) { student ->
                        RecentStudentItem(student = student)
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingClassCard(
    classSession: com.example.osstime.domain.model.ClassSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = classSession.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (classSession.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = classSession.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = classSession.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (classSession.time.isNotEmpty()) {
                    Text(
                        text = classSession.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RecentStudentItem(
    student: com.example.osstime.domain.model.Student
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Estudiante",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (student.belt.isNotEmpty()) {
                    Text(
                        text = "Cinturón: ${student.belt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
