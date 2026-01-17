package com.example.osstime.domain.repository

import com.example.osstime.domain.model.Attendance
import kotlinx.coroutines.flow.Flow

/**
 * Repository para manejar operaciones de asistencia
 */
interface AttendanceRepository {
    /**
     * Guarda o actualiza la asistencia de un estudiante en una clase
     */
    suspend fun saveAttendance(attendance: Attendance)
    
    /**
     * Obtiene la asistencia de una clase específica
     */
    suspend fun getAttendanceByClassId(classId: String): List<Attendance>
    
    /**
     * Obtiene la asistencia de un estudiante específico
     */
    suspend fun getAttendanceByStudentId(studentId: String): List<Attendance>
    
    /**
     * Observa cambios en la asistencia de una clase
     */
    fun observeAttendanceByClassId(classId: String): Flow<List<Attendance>>
    
    /**
     * Elimina la asistencia de una clase
     */
    suspend fun deleteAttendanceByClassId(classId: String)
}
