package com.example.osstime.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.domain.model.Schedule
import com.example.osstime.domain.repository.ClassRepository
import com.example.osstime.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Estados de UI para crear clase
 */
sealed class CreateClassUiState {
    object Idle : CreateClassUiState()
    object Loading : CreateClassUiState()
    object ClassCreated : CreateClassUiState()
    data class Error(val message: String) : CreateClassUiState()
}

/**
 * ViewModel para la pantalla de crear clase del Profesor.
 * Permite seleccionar un horario asignado y crear una clase.
 */
class CreateClassViewModel(
    private val classRepository: ClassRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "CreateClassVM"
    }
    
    private val _uiState = MutableStateFlow<CreateClassUiState>(CreateClassUiState.Idle)
    val uiState: StateFlow<CreateClassUiState> = _uiState.asStateFlow()
    
    // Horarios del profesor logueado
    private val _professorSchedules = MutableStateFlow<List<Schedule>>(emptyList())
    val professorSchedules: StateFlow<List<Schedule>> = _professorSchedules.asStateFlow()
    
    // Horario seleccionado
    private val _selectedSchedule = MutableStateFlow<Schedule?>(null)
    val selectedSchedule: StateFlow<Schedule?> = _selectedSchedule.asStateFlow()
    
    // Estado de carga de horarios
    private val _isLoadingSchedules = MutableStateFlow(true)
    val isLoadingSchedules: StateFlow<Boolean> = _isLoadingSchedules.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    /**
     * Carga los horarios asignados a un profesor.
     * 
     * @param professorId UID del profesor logueado
     */
    fun loadProfessorSchedules(professorId: String) {
        viewModelScope.launch {
            _isLoadingSchedules.value = true
            try {
                val schedules = scheduleRepository.getSchedulesByProfessor(professorId)
                _professorSchedules.value = schedules.filter { it.active }
                Log.d(TAG, "Horarios del profesor cargados: ${schedules.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando horarios del profesor", e)
                _uiState.value = CreateClassUiState.Error("Error al cargar horarios: ${e.message}")
            } finally {
                _isLoadingSchedules.value = false
            }
        }
    }
    
    /**
     * Selecciona un horario para la clase.
     */
    fun selectSchedule(schedule: Schedule) {
        _selectedSchedule.value = schedule
        Log.d(TAG, "Horario seleccionado: ${schedule.id}")
    }
    
    /**
     * Limpia la selección de horario.
     */
    fun clearScheduleSelection() {
        _selectedSchedule.value = null
    }
    
    /**
     * Crea una nueva clase.
     * 
     * @param date Fecha de la clase (dd/MM/yyyy)
     * @param time Hora de la clase (h:mm AM/PM)
     * @param type Tipo de clase (GI/NOGI)
     * @param professorId UID del profesor (si está logueado como profesor)
     */
    fun createClass(
        date: String,
        time: String,
        type: String,
        professorId: String? = null
    ) {
        // Validaciones
        if (date.isBlank()) {
            _uiState.value = CreateClassUiState.Error("Seleccione una fecha")
            return
        }
        if (time.isBlank()) {
            _uiState.value = CreateClassUiState.Error("Seleccione una hora")
            return
        }
        if (type.isBlank()) {
            _uiState.value = CreateClassUiState.Error("Seleccione el tipo de clase")
            return
        }
        
        viewModelScope.launch {
            _isSaving.value = true
            _uiState.value = CreateClassUiState.Loading
            
            try {
                val selectedSch = _selectedSchedule.value
                
                // Validar que la fecha esté dentro del rango del horario seleccionado
                if (selectedSch != null) {
                    if (!isDateWithinScheduleRange(date, selectedSch)) {
                        _uiState.value = CreateClassUiState.Error(
                            "La fecha debe estar dentro del rango del horario: ${selectedSch.startDate} - ${selectedSch.endDate}"
                        )
                        _isSaving.value = false
                        return@launch
                    }
                }
                
                val classSession = ClassSession(
                    id = UUID.randomUUID().toString(),
                    name = "", // Se llena desde CreateClassFormScreen
                    date = date,
                    time = time,
                    type = type,
                    professorId = professorId,
                    scheduleId = selectedSch?.id
                )
                
                classRepository.insertClass(classSession)
                
                Log.d(TAG, "Clase creada: ${classSession.id}")
                _uiState.value = CreateClassUiState.ClassCreated
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear clase", e)
                _uiState.value = CreateClassUiState.Error("Error al crear clase: ${e.message}")
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    /**
     * Valida que la fecha de la clase esté dentro del rango del horario.
     */
    private fun isDateWithinScheduleRange(classDate: String, schedule: Schedule): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val classParsed = dateFormat.parse(classDate) ?: return false
            val startParsed = dateFormat.parse(schedule.startDate) ?: return false
            val endParsed = dateFormat.parse(schedule.endDate) ?: return false
            
            classParsed >= startParsed && classParsed <= endParsed
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando fechas", e)
            true // Si hay error en parseo, permitir (el admin corregirá)
        }
    }
    
    /**
     * Limpia el estado.
     */
    fun resetState() {
        _uiState.value = CreateClassUiState.Idle
    }
    
    /**
     * Limpia todo el formulario.
     */
    fun clearForm() {
        _selectedSchedule.value = null
        _uiState.value = CreateClassUiState.Idle
    }
}
