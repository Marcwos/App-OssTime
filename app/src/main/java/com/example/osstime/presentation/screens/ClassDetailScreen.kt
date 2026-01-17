package com.example.osstime.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.osstime.data.repository.AttendanceRepositoryImpl
import com.example.osstime.data.repository.StudentRepositoryImpl
import com.example.osstime.domain.model.Attendance
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.presentation.components.StudentAttendanceItem
import com.example.osstime.presentation.components.Title
import com.example.osstime.presentation.viewmodel.StudentsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    navController: NavHostController,
    classSession: ClassSession,
    viewModel: StudentsViewModel = viewModel {
        StudentsViewModel(StudentRepositoryImpl())
    }
) {
    // Cargar todos los estudiantes desde Firestore
    val allStudents by viewModel.students.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // Estado de asistencia para cada estudiante
    val attendanceState = remember {
        mutableStateMapOf<String, Boolean>()
    }
    
    // Estado de guardado
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    
    // Cargar asistencias previas para esta clase
    LaunchedEffect(classSession.id) {
        try {
            val attendanceRepo = AttendanceRepositoryImpl()
            val savedAttendances = attendanceRepo.getAttendanceByClassId(classSession.id)
            
            // Inicializar con las asistencias guardadas
            savedAttendances.forEach { attendance ->
                attendanceState[attendance.studentId] = attendance.present
            }
            
            android.util.Log.d("ClassDetailScreen", "Asistencias cargadas: ${savedAttendances.size}")
        } catch (e: Exception) {
            android.util.Log.e("ClassDetailScreen", "Error al cargar asistencias", e)
        }
    }
    
    // Inicializar estudiantes que no tienen asistencia como ausentes
    LaunchedEffect(allStudents) {
        allStudents.forEach { student ->
            if (!attendanceState.containsKey(student.id)) {
                attendanceState[student.id] = false
            }
        }
    }
    
    // Contar presentes
    val presentCount = attendanceState.values.count { it }
    
    val tipoColor = when (classSession.type.uppercase()) {
        "NOGI" -> Color(0xFFB7CB76)
        "GI" -> Color(0xFF8AB5FF)
        else -> MaterialTheme.colorScheme.primary
    }
    
    // Función para guardar asistencia
    fun saveAttendance() {
        isSaving = true
        saveMessage = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val attendanceRepo = AttendanceRepositoryImpl()
                
                // Guardar la asistencia de cada estudiante
                attendanceState.forEach { (studentId, isPresent) ->
                    val attendance = Attendance(
                        studentId = studentId,
                        classId = classSession.id,
                        present = isPresent
                    )
                    attendanceRepo.saveAttendance(attendance)
                }
                
                android.util.Log.d("ClassDetailScreen", "Asistencia guardada para ${attendanceState.size} estudiantes")
                saveMessage = "Asistencia guardada exitosamente"
                
                // Esperar un momento y navegar de vuelta
                delay(1500)
                navController.popBackStack()
            } catch (e: Exception) {
                android.util.Log.e("ClassDetailScreen", "Error al guardar asistencia", e)
                saveMessage = "Error al guardar: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tomar Asistencia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (allStudents.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { saveAttendance() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar asistencia",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mostrar mensaje de guardado
            saveMessage?.let { message ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.contains("exitosamente"))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.contains("exitosamente"))
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Header con información de la clase
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = classSession.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = classSession.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = tipoColor,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = classSession.type,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = classSession.date,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = classSession.time,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Sección de estudiantes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Title(
                            text = "Lista de Asistencia"
                        )
                        Text(
                            text = "$presentCount de ${allStudents.size} presentes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Mostrar loading
            if (isLoading && allStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (allStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No hay estudiantes registrados",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Agrega estudiantes desde la pantalla de Estudiantes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                items(allStudents) { student ->
                    StudentAttendanceItem(
                        student = student,
                        isPresent = attendanceState[student.id] ?: false,
                        onToggle = { isPresent ->
                            attendanceState[student.id] = isPresent
                        }
                    )
                }
            }
        }
    }
}

