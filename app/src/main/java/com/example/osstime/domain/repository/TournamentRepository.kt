package com.example.osstime.domain.repository

import com.example.osstime.domain.model.Tournament
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar torneos.
 */
interface TournamentRepository {
    /**
     * Obtiene todos los torneos.
     */
    suspend fun getAllTournaments(): List<Tournament>
    
    /**
     * Obtiene un torneo por su ID.
     */
    suspend fun getTournamentById(id: String): Tournament?
    
    /**
     * Inserta un nuevo torneo.
     */
    suspend fun insertTournament(tournament: Tournament)
    
    /**
     * Actualiza un torneo existente.
     */
    suspend fun updateTournament(tournament: Tournament)
    
    /**
     * Elimina un torneo por su ID.
     */
    suspend fun deleteTournament(id: String)
    
    /**
     * Observa cambios en tiempo real de los torneos.
     */
    fun observeTournaments(): Flow<List<Tournament>>
    
    /**
     * Obtiene los próximos torneos (fecha >= hoy).
     */
    fun observeUpcomingTournaments(): Flow<List<Tournament>>
}
