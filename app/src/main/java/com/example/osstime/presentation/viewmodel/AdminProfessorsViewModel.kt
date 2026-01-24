package com.example.osstime.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osstime.domain.model.User
import com.example.osstime.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Estados de UI para gestión de profesores
 */
sealed class ProfessorsUiState {
    object Loading : ProfessorsUiState()
    object Success : ProfessorsUiState()
    data class Error(val message: String) : ProfessorsUiState()
}

/**
 * ViewModel para la pantalla de gestión de profesores (Admin).
 * Permite ver, aprobar y rechazar profesores.
 */
class AdminProfessorsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "AdminProfessorsVM"
    }
    
    private val _uiState = MutableStateFlow<ProfessorsUiState>(ProfessorsUiState.Loading)
    val uiState: StateFlow<ProfessorsUiState> = _uiState.asStateFlow()
    
    // Profesores activos
    private val _activeProfessors = MutableStateFlow<List<User>>(emptyList())
    val activeProfessors: StateFlow<List<User>> = _activeProfessors.asStateFlow()
    
    // Usuarios pendientes de aprobación
    private val _pendingUsers = MutableStateFlow<List<User>>(emptyList())
    val pendingUsers: StateFlow<List<User>> = _pendingUsers.asStateFlow()
    
    // Contadores
    private val _activeCount = MutableStateFlow(0)
    val activeCount: StateFlow<Int> = _activeCount.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    // Estado de operación en progreso
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    init {
        observeActiveProfessors()
        observePendingUsers()
    }
    
    /**
     * Observa profesores activos en tiempo real.
     */
    private fun observeActiveProfessors() {
        viewModelScope.launch {
            userRepository.observeActiveProfessors()
                .catch { e ->
                    Log.e(TAG, "Error observando profesores activos", e)
                    _uiState.value = ProfessorsUiState.Error("Error al cargar profesores: ${e.message}")
                }
                .collect { professors ->
                    Log.d(TAG, "Profesores activos recibidos: ${professors.size}")
                    _activeProfessors.value = professors
                    _activeCount.value = professors.size
                    _uiState.value = ProfessorsUiState.Success
                }
        }
    }

    /**
     * Observa usuarios pendientes de aprobación en tiempo real.
     */
    private fun observePendingUsers() {
        viewModelScope.launch {
            userRepository.observePendingUsers()
                .catch { e ->
                    Log.e(TAG, "Error observando usuarios pendientes", e)
                }
                .collect { users ->
                    Log.d(TAG, "Usuarios pendientes recibidos: ${users.size}")
                    _pendingUsers.value = users
                    _pendingCount.value = users.size
                }
        }
    }
    
    /**
     * Aprueba un usuario pendiente (lo activa).
     */
    fun approveUser(uid: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                userRepository.approveUser(uid)
                Log.d(TAG, "Usuario aprobado: $uid")
            } catch (e: Exception) {
                Log.e(TAG, "Error al aprobar usuario", e)
                _uiState.value = ProfessorsUiState.Error("Error al aprobar: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Rechaza un usuario pendiente (lo elimina).
     */
    fun rejectUser(uid: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                userRepository.rejectUser(uid)
                Log.d(TAG, "Usuario rechazado: $uid")
            } catch (e: Exception) {
                Log.e(TAG, "Error al rechazar usuario", e)
                _uiState.value = ProfessorsUiState.Error("Error al rechazar: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is ProfessorsUiState.Error) {
            _uiState.value = ProfessorsUiState.Success
        }
    }
}
