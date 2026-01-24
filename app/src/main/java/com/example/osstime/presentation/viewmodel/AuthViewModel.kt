package com.example.osstime.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osstime.domain.model.User
import com.example.osstime.domain.model.UserRole
import com.example.osstime.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados de UI para autenticación
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class PendingApproval(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * Estados de navegación post-login
 */
sealed class AuthNavigation {
    object None : AuthNavigation()
    object ToAdminHome : AuthNavigation()
    object ToProfessorHome : AuthNavigation()
    object ToPendingApproval : AuthNavigation()
    object ToLogin : AuthNavigation()
}

/**
 * ViewModel para autenticación de usuarios.
 * Maneja login, registro, logout y redirección según rol.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _navigation = MutableStateFlow<AuthNavigation>(AuthNavigation.None)
    val navigation: StateFlow<AuthNavigation> = _navigation.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    /**
     * Verifica si hay un usuario autenticado al iniciar la app.
     */
    fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _currentUser.value = user
                    handleUserNavigation(user)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verificando usuario actual", e)
            }
        }
    }
    
    /**
     * Inicia sesión con email y contraseña.
     */
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor complete todos los campos")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                val user = authRepository.signIn(email, password)
                _currentUser.value = user
                
                if (!user.active) {
                    // Usuario pendiente de aprobación
                    _uiState.value = AuthUiState.PendingApproval(user)
                    _navigation.value = AuthNavigation.ToPendingApproval
                    // Cerrar sesión ya que no puede usar la app
                    authRepository.signOut()
                } else {
                    _uiState.value = AuthUiState.Success(user)
                    handleUserNavigation(user)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en login", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }
    
    /**
     * Registra un nuevo usuario.
     * El usuario queda pendiente de aprobación por el Admin.
     */
    fun signUp(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor complete todos los campos")
            return
        }
        
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                val user = authRepository.signUp(email, password, displayName)
                _uiState.value = AuthUiState.PendingApproval(user)
                _navigation.value = AuthNavigation.ToPendingApproval
            } catch (e: Exception) {
                Log.e(TAG, "Error en registro", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Error al registrarse")
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _currentUser.value = null
                _uiState.value = AuthUiState.Idle
                _navigation.value = AuthNavigation.ToLogin
            } catch (e: Exception) {
                Log.e(TAG, "Error en logout", e)
            }
        }
    }
    
    /**
     * Determina la navegación según el rol del usuario.
     */
    private fun handleUserNavigation(user: User) {
        _navigation.value = when (user.role) {
            UserRole.ADMIN -> AuthNavigation.ToAdminHome
            UserRole.PROFESSOR -> AuthNavigation.ToProfessorHome
        }
    }
    
    /**
     * Limpia el estado de navegación después de navegar.
     */
    fun onNavigationHandled() {
        _navigation.value = AuthNavigation.None
    }
    
    /**
     * Alias para onNavigationHandled - limpia el estado de navegación.
     */
    fun clearNavigation() {
        onNavigationHandled()
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
    
    /**
     * Resetea el estado a Idle.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
    
    /**
     * Obtiene el ID del usuario actual.
     */
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }
}
