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
     * @throws Exception si hay solapamiento con otro horario
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
     * Verifica si existe un horario que se solape con el rango dado.
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @param startTime Hora inicio
     * @param endTime Hora fin
     * @param excludeScheduleId ID de horario a excluir (para edición)
     * @return true si hay solapamiento
     */
    suspend fun hasOverlappingSchedule(
        startDate: String,
        endDate: String,
        startTime: String,
        endTime: String,
        excludeScheduleId: String? = null
    ): Boolean
    
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
     * Obtiene horarios activos de un profesor que incluyan una fecha específica.
     * @param professorId ID del profesor
     * @param date Fecha a verificar (formato: "dd/MM/yyyy")
     */
    suspend fun getSchedulesForDate(professorId: String, date: String): List<Schedule>
    
    /**
     * Observa todos los horarios.
     */
    fun observeSchedules(): Flow<List<Schedule>>
    
    /**
     * Observa horarios activos de un profesor.
     */
    fun observeActiveSchedulesByProfessor(professorId: String): Flow<List<Schedule>>
}
