package com.example.osstime.domain.repository

import com.example.osstime.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gestión de horarios en Firestore.
 * Colección: schedules
 */
interface ScheduleRepository {
    
    /**
     * Crea un nuevo horario.
     */
    suspend fun createSchedule(schedule: Schedule)
    
    /**
     * Actualiza un horario existente.
     */
    suspend fun updateSchedule(schedule: Schedule)
    
    /**
     * Elimina un horario.
     */
    suspend fun deleteSchedule(scheduleId: String)
    
    /**
     * Desactiva un horario (soft delete).
     */
    suspend fun deactivateSchedule(scheduleId: String)
    
    /**
     * Obtiene un horario por ID.
     */
    suspend fun getScheduleById(scheduleId: String): Schedule?
    
    /**
     * Obtiene todos los horarios.
     */
    suspend fun getAllSchedules(): List<Schedule>
    
    /**
     * Obtiene horarios de un profesor específico.
     */
    suspend fun getSchedulesByProfessor(professorId: String): List<Schedule>
    
    /**
     * Obtiene horarios activos de un profesor.
     */
    suspend fun getActiveSchedulesByProfessor(professorId: String): List<Schedule>
    
    /**
     * Observa todos los horarios.
     */
    fun observeSchedules(): Flow<List<Schedule>>
    
    /**
     * Observa horarios activos de un profesor.
     */
    fun observeActiveSchedulesByProfessor(professorId: String): Flow<List<Schedule>>
}
