package com.example.osstime.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osstime.domain.model.Tournament
import com.example.osstime.domain.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Estados de UI para crear torneo
 */
sealed class CreateTournamentUiState {
    object Idle : CreateTournamentUiState()
    object Loading : CreateTournamentUiState()
    object TournamentCreated : CreateTournamentUiState()
    data class Error(val message: String) : CreateTournamentUiState()
}

/**
 * ViewModel para gestionar torneos.
 */
class TournamentViewModel(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "TournamentViewModel"
    }
    
    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()
    
    private val _upcomingTournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val upcomingTournaments: StateFlow<List<Tournament>> = _upcomingTournaments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _uiState = MutableStateFlow<CreateTournamentUiState>(CreateTournamentUiState.Idle)
    val uiState: StateFlow<CreateTournamentUiState> = _uiState.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        observeTournaments()
        observeUpcomingTournaments()
    }
    
    /**
     * Observa todos los torneos en tiempo real
     */
    private fun observeTournaments() {
        viewModelScope.launch {
            _isLoading.value = true
            tournamentRepository.observeTournaments()
                .catch { e ->
                    Log.e(TAG, "Error observing tournaments", e)
                    val errorMessage = when {
                        e.message?.contains("PERMISSION_DENIED") == true -> 
                            "Error de permisos: Verifica las reglas de Firebase"
                        e.message?.contains("index") == true -> 
                            "Se requiere un índice compuesto. Ve a la consola de Firebase para crearlo."
                        else -> "Error al cargar torneos: ${e.message}"
                    }
                    _error.value = errorMessage
                    _isLoading.value = false
                    _tournaments.value = emptyList() // Asegurar lista vacía en caso de error
                }
                .collect { tournamentList ->
                    Log.d(TAG, "Torneos recibidos: ${tournamentList.size}")
                    _tournaments.value = tournamentList
                    _isLoading.value = false
                    _error.value = null // Limpiar error si la carga es exitosa
                }
        }
    }
    
    /**
     * Observa los próximos torneos (fecha >= hoy)
     */
    private fun observeUpcomingTournaments() {
        viewModelScope.launch {
            tournamentRepository.observeUpcomingTournaments()
                .catch { e ->
                    Log.e(TAG, "Error observing upcoming tournaments", e)
                    // No mostrar error aquí para no interferir con la UI principal
                    _upcomingTournaments.value = emptyList()
                }
                .collect { tournamentList ->
                    Log.d(TAG, "Próximos torneos: ${tournamentList.size}")
                    _upcomingTournaments.value = tournamentList.take(2) // Máximo 2
                }
        }
    }
    
    /**
     * Crea un nuevo torneo
     */
    fun createTournament(
        name: String,
        city: String,
        modality: String,
        date: String,
        professorId: String?
    ) {
        if (name.isBlank()) {
            _uiState.value = CreateTournamentUiState.Error("Ingrese el nombre del torneo")
            return
        }
        if (city.isBlank()) {
            _uiState.value = CreateTournamentUiState.Error("Ingrese la ciudad")
            return
        }
        if (modality.isBlank()) {
            _uiState.value = CreateTournamentUiState.Error("Seleccione una modalidad")
            return
        }
        if (date.isBlank()) {
            _uiState.value = CreateTournamentUiState.Error("Seleccione la fecha del torneo")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = CreateTournamentUiState.Loading
            try {
                val tournament = Tournament(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    city = city.trim(),
                    modality = modality,
                    date = date,
                    professorId = professorId
                )
                tournamentRepository.insertTournament(tournament)
                Log.d(TAG, "Torneo creado: ${tournament.id}")
                _uiState.value = CreateTournamentUiState.TournamentCreated
            } catch (e: Exception) {
                Log.e(TAG, "Error creating tournament", e)
                _uiState.value = CreateTournamentUiState.Error("Error al crear torneo: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un torneo
     */
    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            try {
                tournamentRepository.deleteTournament(tournamentId)
                Log.d(TAG, "Torneo eliminado: $tournamentId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting tournament", e)
                _error.value = "Error al eliminar torneo: ${e.message}"
            }
        }
    }
    
    /**
     * Resetea el estado de UI
     */
    fun resetState() {
        _uiState.value = CreateTournamentUiState.Idle
    }
    
    /**
     * Limpia el error
     */
    fun clearError() {
        _error.value = null
    }
}
