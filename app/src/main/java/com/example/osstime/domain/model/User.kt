package com.example.osstime.domain.model

/**
 * Modelo de usuario autenticado en la aplicación.
 * Colección Firestore: users (docId = uid)
 * 
 * @param uid ID único del usuario (mismo que Firebase Auth uid)
 * @param email Correo electrónico del usuario
 * @param displayName Nombre para mostrar del usuario
 * @param role Rol del usuario (ADMIN o PROFESSOR)
 * @param active Si el usuario está activo (false = pendiente de aprobación por Admin)
 * @param createdAt Timestamp de creación en formato ISO o millis
 */
data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val active: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
