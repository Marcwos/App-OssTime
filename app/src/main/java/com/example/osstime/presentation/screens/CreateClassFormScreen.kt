package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.osstime.data.repository.ClassRepositoryImpl
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassFormScreen(
    navController: NavHostController
) {
    var className by remember { mutableStateOf("") }
    var classType by remember { mutableStateOf("") }
    var classDate by remember { mutableStateOf("") }
    var classDescription by remember { mutableStateOf("") }
    var classTime by remember { mutableStateOf("") }
    
    val classTypes = listOf("GI", "NOGI")
    var expandedType by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isFormValid = className.isNotBlank() && 
                     classType.isNotBlank() && 
                     classDate.isNotBlank() && 
                     classTime.isNotBlank()

    // Función para guardar la clase
    fun saveClass() {
        if (!isFormValid || isLoading) return
        
        isLoading = true
        errorMessage = null
        
        // Generar ID único para la clase
        val classId = UUID.randomUUID().toString()
        
        val classSession = ClassSession(
            id = classId,
            name = className.trim(),
            type = classType,
            date = classDate.trim(),
            description = classDescription.trim(),
            time = classTime.trim()
        )
        
        // Guardar usando el repositorio en una coroutine
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val repository = ClassRepositoryImpl()
                repository.insertClass(classSession)
                android.util.Log.d("CreateClassForm", "Clase guardada: ${classSession.name}")
                // Esperar un momento para que se complete la operación
                delay(1000)
                isLoading = false
                // Navegar de vuelta solo si no hay error
                navController.popBackStack()
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error al guardar: ${e.message ?: "Error desconocido"}"
                android.util.Log.e("CreateClassForm", "Error al guardar clase", e)
            }
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
            
            // Campo Tipo (Dropdown)
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
            
            // Campo Fecha
            OutlinedTextField(
                value = classDate,
                onValueChange = { classDate = it },
                label = { Text("Fecha") },
                placeholder = { Text("Ej: Jueves 7/12/25") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Campo Hora
            OutlinedTextField(
                value = classTime,
                onValueChange = { classTime = it },
                label = { Text("Hora") },
                placeholder = { Text("Ej: 7:00 PM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
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

