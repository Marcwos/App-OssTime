package com.example.osstime.domain.repository

import com.example.osstime.domain.model.ClassSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository para manejar operaciones de clases
 * Abstrae la fuente de datos (Room, API, etc.)
 */
interface ClassRepository {
    /**
     * Obtiene todas las clases
     */
    suspend fun getAllClasses(): List<ClassSession>

    /**
     * Obtiene una clase por ID
     */
    suspend fun getClassById(id: String): ClassSession?

    /**
     * Inserta una nueva clase
     */
    suspend fun insertClass(classSession: ClassSession)

    /**
     * Actualiza una clase existente
     */
    suspend fun updateClass(classSession: ClassSession)

    /**
     * Elimina una clase
     */
    suspend fun deleteClass(id: String)

    /**
     * Observa cambios en las clases (para Room con Flow)
     */
    fun observeClasses(): Flow<List<ClassSession>>
}
