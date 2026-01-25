package com.example.osstime.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osstime.domain.model.Schedule
import com.example.osstime.domain.model.User
import com.example.osstime.domain.repository.ScheduleRepository
import com.example.osstime.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Estados de UI para gestión de horarios
 */
sealed class SchedulesUiState {
    object Idle : SchedulesUiState()
    object Loading : SchedulesUiState()
    object Success : SchedulesUiState()
    data class Error(val message: String) : SchedulesUiState()
    object ScheduleCreated : SchedulesUiState()
}

/**
 * ViewModel para la pantalla de gestión de horarios (Admin).
 * Permite crear, ver y gestionar horarios de profesores.
 */
class AdminSchedulesViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "AdminSchedulesVM"
    }
    
    private val _uiState = MutableStateFlow<SchedulesUiState>(SchedulesUiState.Loading)
    val uiState: StateFlow<SchedulesUiState> = _uiState.asStateFlow()
    
    // Lista de todos los horarios
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()
    
    // Lista de profesores activos (para selector)
    private val _professors = MutableStateFlow<List<User>>(emptyList())
    val professors: StateFlow<List<User>> = _professors.asStateFlow()
    
    // Estado de guardado
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    init {
        observeSchedules()
        loadProfessors()
    }
    
    /**
     * Observa horarios en tiempo real.
     */
    private fun observeSchedules() {
        viewModelScope.launch {
            scheduleRepository.observeSchedules()
                .catch { e ->
                    Log.e(TAG, "Error observando horarios", e)
                    _uiState.value = SchedulesUiState.Error("Error al cargar horarios: ${e.message}")
                }
                .collect { scheduleList ->
                    Log.d(TAG, "Horarios recibidos: ${scheduleList.size}")
                    _schedules.value = scheduleList
                    _uiState.value = SchedulesUiState.Success
                }
        }
    }
    
    /**
     * Carga la lista de profesores activos.
     */
    private fun loadProfessors() {
        viewModelScope.launch {
            try {
                val professorsList = userRepository.getActiveProfessors()
                _professors.value = professorsList
                Log.d(TAG, "Profesores cargados: ${professorsList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando profesores", e)
            }
        }
    }
    
    /**
     * Crea un nuevo horario.
     * Solo define horas de inicio y fin para un profesor.
     * El profesor elegirá las fechas y tipo cuando cree sus clases.
     * 
     * @param professorId ID del profesor
     * @param professorName Nombre del profesor (para mostrar)
     * @param startTime Hora inicio (h:mm AM/PM)
     * @param endTime Hora fin (h:mm AM/PM)
     * @param createdBy UID del admin que crea el horario
     */
    fun createSchedule(
        professorId: String,
        professorName: String,
        startTime: String,
        endTime: String,
        createdBy: String
    ) {
        // Validaciones básicas
        if (professorId.isBlank()) {
            _uiState.value = SchedulesUiState.Error("Seleccione un profesor")
            return
        }
        if (startTime.isBlank() || endTime.isBlank()) {
            _uiState.value = SchedulesUiState.Error("Seleccione las horas")
            return
        }
        
        viewModelScope.launch {
            _isSaving.value = true
            _uiState.value = SchedulesUiState.Loading
            
            try {
                val schedule = Schedule(
                    id = java.util.UUID.randomUUID().toString(),
                    professorId = professorId,
                    professorName = professorName,
                    startTime = startTime,
                    endTime = endTime,
                    active = true,
                    createdAt = System.currentTimeMillis(),
                    createdBy = createdBy
                )
                
                scheduleRepository.createSchedule(schedule)
                
                Log.d(TAG, "Horario creado: ${schedule.id}")
                _uiState.value = SchedulesUiState.ScheduleCreated
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear horario", e)
                _uiState.value = SchedulesUiState.Error(e.message ?: "Error al crear horario")
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    /**
     * Desactiva un horario (soft delete).
     */
    fun deactivateSchedule(scheduleId: String) {
        viewModelScope.launch {
            try {
                scheduleRepository.deactivateSchedule(scheduleId)
                Log.d(TAG, "Horario desactivado: $scheduleId")
            } catch (e: Exception) {
                Log.e(TAG, "Error al desactivar horario", e)
                _uiState.value = SchedulesUiState.Error("Error al desactivar: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un horario permanentemente.
     */
    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            try {
                scheduleRepository.deleteSchedule(scheduleId)
                Log.d(TAG, "Horario eliminado: $scheduleId")
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar horario", e)
                _uiState.value = SchedulesUiState.Error("Error al eliminar: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error/éxito.
     */
    fun resetState() {
        _uiState.value = SchedulesUiState.Idle
    }
    
    /**
     * Recarga la lista de profesores.
     */
    fun refreshProfessors() {
        loadProfessors()
    }
}
