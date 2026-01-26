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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.presentation.viewmodel.CreateTournamentUiState
import com.example.osstime.presentation.viewmodel.TournamentViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para crear un nuevo torneo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    navController: NavHostController,
    viewModel: TournamentViewModel,
    professorId: String?
) {
    var tournamentName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var modality by remember { mutableStateOf("") }
    var tournamentDate by remember { mutableStateOf("") }
    
    var expandedModality by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val modalityOptions = listOf("GI", "NOGI", "AMBAS")
    
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is CreateTournamentUiState.Loading
    
    val datePickerState = rememberDatePickerState()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateTournamentUiState.TournamentCreated -> {
                navController.popBackStack()
                viewModel.resetState()
            }
            is CreateTournamentUiState.Error -> {
                errorMessage = (uiState as CreateTournamentUiState.Error).message
            }
            else -> {}
        }
    }
    
    // Validación del formulario
    val isFormValid = tournamentName.isNotBlank() &&
            city.isNotBlank() &&
            modality.isNotBlank() &&
            tournamentDate.isNotBlank()
    
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
                            tournamentDate = dateFormat.format(calendar.time)
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
                title = { Text("Nuevo Torneo") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Información del Torneo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Mostrar error si existe
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Campo Nombre del Torneo
            OutlinedTextField(
                value = tournamentName,
                onValueChange = { tournamentName = it },
                label = { Text("Nombre del Torneo") },
                placeholder = { Text("Ej: Copa Nacional de BJJ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Campo Ciudad
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Ciudad") },
                placeholder = { Text("Ej: Ciudad de México") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Selector de Modalidad
            ExposedDropdownMenuBox(
                expanded = expandedModality,
                onExpandedChange = { expandedModality = !expandedModality }
            ) {
                OutlinedTextField(
                    value = modality,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Modalidad") },
                    placeholder = { Text("Selecciona modalidad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModality) },
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
                    expanded = expandedModality,
                    onDismissRequest = { expandedModality = false }
                ) {
                    modalityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                modality = option
                                expandedModality = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Campo Fecha
            OutlinedTextField(
                value = tournamentDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha del Torneo") },
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
            
            Spacer(Modifier.height(32.dp))
            
            // Botón Guardar
            Button(
                onClick = {
                    viewModel.createTournament(
                        name = tournamentName.trim(),
                        city = city.trim(),
                        modality = modality,
                        date = tournamentDate,
                        professorId = professorId
                    )
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Crear Torneo",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
