package com.example.osstime.domain.repository

import androidx.paging.PagingSource
import com.example.osstime.domain.model.Student
import kotlinx.coroutines.flow.Flow

/**
 * Repository para manejar operaciones de estudiantes
 * Abstrae la fuente de datos (Room, API, etc.)
 */
interface StudentRepository {
    /**
     * Obtiene todos los estudiantes
     */
    suspend fun getAllStudents(): List<Student>

    /**
     * Obtiene un estudiante por ID
     */
    suspend fun getStudentById(id: String): Student?

    /**
     * Inserta un nuevo estudiante
     */
    suspend fun insertStudent(student: Student)

    /**
     * Actualiza un estudiante existente
     */
    suspend fun updateStudent(student: Student)

    /**
     * Elimina un estudiante
     */
    suspend fun deleteStudent(id: String)

    /**
     * Obtiene el contador total de estudiantes
     */
    suspend fun getStudentCount(): Int

    /**
     * PagingSource para paginación
     */
    fun getStudentsPagingSource(): PagingSource<Int, Student>

    /**
     * Observa cambios en los estudiantes (para Room con Flow)
     */
    fun observeStudents(): Flow<List<Student>>
}
