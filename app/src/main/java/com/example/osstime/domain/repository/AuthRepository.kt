package com.example.osstime.domain.repository

import com.example.osstime.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository para autenticación de usuarios.
 * Maneja Firebase Auth y lectura del documento de usuario para obtener rol.
 */
interface AuthRepository {
    
    /**
     * Inicia sesión con email y contraseña.
     * @return User si el login es exitoso y el usuario existe en Firestore
     * @throws Exception si el login falla o el usuario no existe en Firestore
     */
    suspend fun signIn(email: String, password: String): User
    
    /**
     * Registra un nuevo usuario con email y contraseña.
     * Crea el documento en Firestore con active=false (pendiente de aprobación).
     * @return User creado
     * @throws Exception si el registro falla
     */
    suspend fun signUp(email: String, password: String, displayName: String): User
    
    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun signOut()
    
    /**
     * Obtiene el usuario actualmente autenticado.
     * @return User si hay sesión activa, null si no
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Verifica si hay un usuario autenticado.
     */
    fun isUserLoggedIn(): Boolean
    
    /**
     * Obtiene el UID del usuario actual.
     * @return UID o null si no hay sesión
     */
    fun getCurrentUserId(): String?
    
    /**
     * Observa cambios en el estado de autenticación.
     * Emite User cuando hay sesión activa, null cuando no.
     */
    fun observeAuthState(): Flow<User?>
}
