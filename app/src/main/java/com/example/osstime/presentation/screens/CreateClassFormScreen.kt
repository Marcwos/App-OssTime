package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.Schedule
import com.example.osstime.presentation.viewmodel.CreateClassViewModel
import com.example.osstime.presentation.viewmodel.CreateClassUiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para crear una nueva clase.
 * 
 * @param navController Controlador de navegación
 * @param professorId ID del profesor logueado (null si es admin o modo legacy)
 * @param createClassViewModel ViewModel para gestionar horarios del profesor (opcional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassFormScreen(
    navController: NavHostController,
    professorId: String? = null,
    createClassViewModel: CreateClassViewModel? = null
) {
    var className by remember { mutableStateOf("") }
    var classType by remember { mutableStateOf("") }
    var classDate by remember { mutableStateOf("") }
    var classDescription by remember { mutableStateOf("") }
    var classTime by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para el selector de horario y tipo
    var expandedSchedule by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    
    val classTypes = listOf("GI", "NOGI")

    // Estados del ViewModel si está disponible
    val professorSchedules by createClassViewModel?.professorSchedules?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val isLoadingSchedules by createClassViewModel?.isLoadingSchedules?.collectAsState()
        ?: remember { mutableStateOf(false) }
    val viewModelUiState by createClassViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(CreateClassUiState.Idle) }

    // Cargar horarios del profesor si aplica
    LaunchedEffect(professorId) {
        if (professorId != null && createClassViewModel != null) {
            createClassViewModel.loadProfessorSchedules(professorId)
        }
    }

    // Manejar éxito desde el ViewModel
    LaunchedEffect(viewModelUiState) {
        when (viewModelUiState) {
            is CreateClassUiState.ClassCreated -> {
                navController.popBackStack()
                createClassViewModel?.resetState()
            }

            is CreateClassUiState.Error -> {
                errorMessage = (viewModelUiState as CreateClassUiState.Error).message
            }

            else -> {}
        }
    }

    // Estados para los pickers
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Validación del formulario - solo para profesores
    val isFormValid = className.isNotBlank() &&
            classType.isNotBlank() &&
            classDate.isNotBlank() &&
            classTime.isNotBlank() &&
            selectedSchedule != null &&
            professorId != null

    // Función para guardar la clase
    fun saveClass() {
        if (!isFormValid || isLoading) return

        // Usar el ViewModel del profesor
        createClassViewModel?.selectSchedule(selectedSchedule!!)
        createClassViewModel?.createClass(
            name = className.trim(),
            date = classDate.trim(),
            time = classTime.trim(),
            type = classType,
            description = classDescription.trim(),
            professorId = professorId
        )
    }

    // DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            classDate = dateFormat.format(calendar.time)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Clase") },
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
        }
    ) { padding ->
        // Si no es profesor (es admin), mostrar mensaje informativo
        if (professorId == null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Solo los profesores pueden crear clases",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Como administrador, tu rol es gestionar horarios para los profesores. Los profesores crearán clases dentro de los horarios que les asignes.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Información de la Clase",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))

            // Selector de Horario
            Text(
                text = "Horario Asignado",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoadingSchedules) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            ExposedDropdownMenuBox(
                expanded = expandedSchedule,
                onExpandedChange = { expandedSchedule = !expandedSchedule }
            ) {
                OutlinedTextField(
                    value = selectedSchedule?.let {
                        "${it.startTime} - ${it.endTime}"
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar horario") },
                    placeholder = { Text("Elige tu horario asignado") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = professorSchedules.isEmpty() && !isLoadingSchedules
                )
                ExposedDropdownMenu(
                    expanded = expandedSchedule,
                    onDismissRequest = { expandedSchedule = false }
                ) {
                    if (professorSchedules.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No tienes horarios asignados") },
                            onClick = { expandedSchedule = false }
                        )
                    } else {
                        professorSchedules.forEach { schedule ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${schedule.startTime} - ${schedule.endTime}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedSchedule = schedule
                                    expandedSchedule = false
                                }
                            )
                        }
                    }
                }
            }

            if (professorSchedules.isEmpty() && !isLoadingSchedules) {
                Text(
                    text = "Contacta al administrador para que te asigne un horario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Campo Nombre
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Nombre de la clase") },
                placeholder = { Text("Ej: Pasada de media guardia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Tipo de clase - El profesor lo selecciona
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = classType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de clase") },
                    placeholder = { Text("Selecciona el tipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    classTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                classType = type
                                expandedType = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Campo Fecha con DatePicker
            OutlinedTextField(
                value = classDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha") },
                placeholder = { Text("Selecciona una fecha") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Campo Hora - Diferente para profesores y admin
            if (professorId != null) {
                // Para profesores: mostrar horarios disponibles como tarjetas
                Text(
                    text = "Horario de Clase",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (selectedSchedule != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Hora de inicio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedSchedule!!.startTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(
                                    text = "Hora de fin",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedSchedule!!.endTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Auto-asignar la hora de inicio como hora de la clase
                    LaunchedEffect(selectedSchedule) {
                        selectedSchedule?.let {
                            classTime = it.startTime
                        }
                    }
                } else {
                    Text(
                        text = "Primero selecciona un horario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Campo Descripción
                OutlinedTextField(
                    value = classDescription,
                    onValueChange = { classDescription = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Pase + finalización kata-gatame") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(32.dp))

                // Mostrar mensaje de error si existe
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Botón Guardar
                Button(
                    onClick = { saveClass() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Guardar Clase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

