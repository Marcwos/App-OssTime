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
import com.example.osstime.data.repository.TournamentRepositoryImpl
import com.example.osstime.presentation.components.TopBar
import com.example.osstime.presentation.components.ClassSection
import com.example.osstime.presentation.components.Title
import com.example.osstime.presentation.components.BottomNavigationBar
import com.example.osstime.presentation.components.TournamentCard
import com.example.osstime.presentation.viewmodel.AuthNavigation
import com.example.osstime.presentation.viewmodel.AuthViewModel
import com.example.osstime.presentation.viewmodel.HomeViewModel
import com.example.osstime.presentation.viewmodel.TournamentViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    },
    tournamentViewModel: TournamentViewModel = viewModel {
        TournamentViewModel(TournamentRepositoryImpl())
    }
) {
    // Estados del ViewModel - se actualizan automáticamente
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val tomorrowClasses by viewModel.tomorrowClasses.collectAsStateWithLifecycle()
    val upcomingClasses by viewModel.upcomingClasses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val authNavigation by authViewModel.navigation.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val upcomingTournaments by tournamentViewModel.upcomingTournaments.collectAsStateWithLifecycle()
    
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
                Title(text = "Hola ${currentUser?.displayName ?: "Profesor"}!")
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
                            val todayFormatted = remember {
                                val calendar = Calendar.getInstance()
                                val dateFormat = SimpleDateFormat("EEEE d/MM/yy", Locale("es", "ES"))
                                dateFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
                            }
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    items(todayClasses) { classSession ->
                        UpcomingClassCard(
                            classSession = classSession,
                            onClick = {
                                val encodedId = URLEncoder.encode(classSession.id, StandardCharsets.UTF_8.toString())
                                val encodedName = URLEncoder.encode(classSession.name, StandardCharsets.UTF_8.toString())
                                val encodedType = URLEncoder.encode(classSession.type, StandardCharsets.UTF_8.toString())
                                val encodedDate = URLEncoder.encode(classSession.date, StandardCharsets.UTF_8.toString())
                                val description = if (classSession.description.isBlank()) "_" else classSession.description
                                val time = if (classSession.time.isBlank()) "_" else classSession.time
                                val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
                                val encodedTime = URLEncoder.encode(time, StandardCharsets.UTF_8.toString())
                                navController.navigate(
                                    "class_detail/$encodedId/$encodedName/$encodedType/$encodedDate/$encodedDescription/$encodedTime"
                                )
                            }
                        )
                    }
                }

                // Próximo Objetivo (Torneos)
                if (upcomingTournaments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Próximo Objetivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Solo mostrar el primer torneo próximo
                    item {
                        TournamentCard(tournament = upcomingTournaments.first())
                    }
                }
                
                // Espacio al final para scroll
                item {
                    Spacer(modifier = Modifier.height(24.dp))
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
    // Color del badge según el tipo
    val typeColor = when (classSession.type.uppercase()) {
        "NOGI" -> androidx.compose.ui.graphics.Color(0xFFB7CB76)
        "GI" -> androidx.compose.ui.graphics.Color(0xFF8AB5FF)
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Fila superior: Nombre de la clase y tipo (GI/NOGI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classSession.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // Badge del tipo (GI/NOGI)
                if (classSession.type.isNotEmpty()) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        color = typeColor
                    ) {
                        Text(
                            text = classSession.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Descripción
            if (classSession.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = classSession.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Hora (sin fecha)
            if (classSession.time.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = classSession.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
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
