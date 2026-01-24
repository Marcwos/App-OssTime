package com.example.osstime.domain.repository

import com.example.osstime.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gestión de usuarios en Firestore.
 * Colección: users (docId = uid)
 */
interface UserRepository {
    
    /**
     * Obtiene un usuario por su UID.
     */
    suspend fun getUserById(uid: String): User?
    
    /**
     * Crea un nuevo usuario en Firestore.
     */
    suspend fun createUser(user: User)
    
    /**
     * Actualiza un usuario existente.
     */
    suspend fun updateUser(user: User)
    
    /**
     * Aprueba un usuario pendiente (establece active=true).
     */
    suspend fun approveUser(uid: String)
    
    /**
     * Rechaza/elimina un usuario pendiente.
     */
    suspend fun rejectUser(uid: String)
    
    /**
     * Observa todos los usuarios con rol PROFESSOR.
     */
    fun observeProfessors(): Flow<List<User>>
    
    /**
     * Observa usuarios pendientes de aprobación (active=false).
     */
    fun observePendingUsers(): Flow<List<User>>
    
    /**
     * Observa usuarios activos con rol PROFESSOR.
     */
    fun observeActiveProfessors(): Flow<List<User>>
    
    /**
     * Obtiene la lista de profesores activos.
     */
    suspend fun getActiveProfessors(): List<User>
}
