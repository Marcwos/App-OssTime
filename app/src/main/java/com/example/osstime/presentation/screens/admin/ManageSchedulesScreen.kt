package com.example.osstime.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.Schedule
import com.example.osstime.domain.model.User
import com.example.osstime.presentation.viewmodel.AdminSchedulesViewModel
import com.example.osstime.presentation.viewmodel.SchedulesUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla de gestión de horarios para Admin.
 * - Muestra lista de horarios existentes
 * - FAB para crear nuevo horario
 * - Validación de solapamiento en servidor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSchedulesScreen(
    navController: NavHostController,
    viewModel: AdminSchedulesViewModel,
    currentUserId: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val professors by viewModel.professors.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }
    
    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar mensaje de éxito al crear
    LaunchedEffect(uiState) {
        when (uiState) {
            is SchedulesUiState.ScheduleCreated -> {
                showCreateDialog = false
                snackbarHostState.showSnackbar("Horario creado exitosamente")
                viewModel.resetState()
            }
            is SchedulesUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as SchedulesUiState.Error).message)
            }
            else -> {}
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Eliminar Horario") },
            text = { 
                Text("¿Estás seguro de que deseas eliminar el horario de ${scheduleToDelete?.professorName}?\n\n${scheduleToDelete?.startTime} - ${scheduleToDelete?.endTime}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scheduleToDelete?.let { viewModel.deleteSchedule(it.id) }
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para crear horario
    if (showCreateDialog) {
        CreateScheduleDialog(
            professors = professors,
            isSaving = isSaving,
            errorMessage = if (uiState is SchedulesUiState.Error) (uiState as SchedulesUiState.Error).message else null,
            onDismiss = { 
                showCreateDialog = false
                viewModel.resetState()
            },
            onCreate = { professorId, professorName, startTime, endTime ->
                viewModel.createSchedule(
                    professorId = professorId,
                    professorName = professorName,
                    startTime = startTime,
                    endTime = endTime,
                    createdBy = currentUserId
                )
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Horarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Crear Horario"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState is SchedulesUiState.Loading && schedules.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            schedules.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay horarios creados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Presiona + para crear uno",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onDelete = { scheduleToDelete = schedule }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.professorName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${schedule.startTime} - ${schedule.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScheduleDialog(
    professors: List<User>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var selectedProfessor by remember { mutableStateOf<User?>(null) }
    var professorDropdownExpanded by remember { mutableStateOf(false) }
    
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    
    // Time pickers
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // Start Time Picker
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Hora de inicio") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val amPm = if (hour < 12) "AM" else "PM"
                        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        startTime = String.format("%d:%02d %s", hour12, minute, amPm)
                        showStartTimePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // End Time Picker
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Hora de fin") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val amPm = if (hour < 12) "AM" else "PM"
                        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        endTime = String.format("%d:%02d %s", hour12, minute, amPm)
                        showEndTimePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Crear Nuevo Horario") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de profesor
                ExposedDropdownMenuBox(
                    expanded = professorDropdownExpanded,
                    onExpandedChange = { professorDropdownExpanded = !professorDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProfessor?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professorDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = professorDropdownExpanded,
                        onDismissRequest = { professorDropdownExpanded = false }
                    ) {
                        if (professors.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay profesores activos") },
                                onClick = { professorDropdownExpanded = false }
                            )
                        } else {
                            professors.forEach { professor ->
                                DropdownMenuItem(
                                    text = { Text(professor.displayName) },
                                    onClick = {
                                        selectedProfessor = professor
                                        professorDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Horas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora inicio") },
                        modifier = Modifier
                            .weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showStartTimePicker = true }) {
                                Icon(Icons.Filled.Schedule, "Seleccionar hora")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora fin") },
                        modifier = Modifier
                            .weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showEndTimePicker = true }) {
                                Icon(Icons.Filled.Schedule, "Seleccionar hora")
                            }
                        }
                    )
                }
                
                // Mensaje de error
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProfessor?.let { professor ->
                        onCreate(
                            professor.uid,
                            professor.displayName,
                            startTime,
                            endTime
                        )
                    }
                },
                enabled = !isSaving && 
                         selectedProfessor != null && 
                         startTime.isNotBlank() && 
                         endTime.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancelar")
            }
        }
    )
}
