package com.example.osstime.data.repository

import android.util.Log
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.User
import com.example.osstime.domain.model.UserRole
import com.example.osstime.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de usuarios usando Firebase Firestore.
 * Colección: users (docId = uid)
 */
class UserRepositoryImpl : UserRepository {
    
    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "UserRepository"
    }
    
    override suspend fun getUserById(uid: String): User? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario: $uid", e)
            null
        }
    }
    
    override suspend fun createUser(user: User) {
        try {
            usersCollection.document(user.uid).set(user.toMap()).await()
            Log.d(TAG, "Usuario creado: ${user.uid}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear usuario", e)
            throw Exception("Error al crear usuario: ${e.message}")
        }
    }
    
    override suspend fun updateUser(user: User) {
        try {
            usersCollection.document(user.uid).set(user.toMap()).await()
            Log.d(TAG, "Usuario actualizado: ${user.uid}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar usuario", e)
            throw Exception("Error al actualizar usuario: ${e.message}")
        }
    }
    
    override suspend fun approveUser(uid: String) {
        try {
            usersCollection.document(uid).update("active", true).await()
            Log.d(TAG, "Usuario aprobado: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error al aprobar usuario", e)
            throw Exception("Error al aprobar usuario: ${e.message}")
        }
    }
    
    override suspend fun rejectUser(uid: String) {
        try {
            // Eliminar documento de Firestore
            usersCollection.document(uid).delete().await()
            Log.d(TAG, "Usuario rechazado y eliminado: $uid")
            // Nota: La cuenta de Firebase Auth permanece pero sin documento en Firestore
            // no podrá acceder. Para eliminar la cuenta Auth se necesitaría Cloud Functions.
        } catch (e: Exception) {
            Log.e(TAG, "Error al rechazar usuario", e)
            throw Exception("Error al rechazar usuario: ${e.message}")
        }
    }
    
    override fun observeProfessors(): Flow<List<User>> = callbackFlow {
        val listenerRegistration = usersCollection
            .whereEqualTo("role", UserRole.PROFESSOR.name)
            .orderBy("displayName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando profesores", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toUser() }
                    trySend(users)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override fun observePendingUsers(): Flow<List<User>> = callbackFlow {
        val listenerRegistration = usersCollection
            .whereEqualTo("role", UserRole.PROFESSOR.name)
            .whereEqualTo("active", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando usuarios pendientes", error)
                    Log.e(TAG, "Código de error: ${error.code}, Mensaje: ${error.message}")
                    // Si es un error de índice faltante, proporcionar mensaje más claro
                    if (error.message?.contains("index") == true) {
                        close(Exception("Se requiere un índice compuesto en Firestore. Ve a la consola de Firebase para crear el índice automáticamente."))
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toUser() }
                    Log.d(TAG, "Usuarios pendientes encontrados: ${users.size}")
                    trySend(users)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override fun observeActiveProfessors(): Flow<List<User>> = callbackFlow {
        val listenerRegistration = usersCollection
            .whereEqualTo("role", UserRole.PROFESSOR.name)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando profesores activos", error)
                    Log.e(TAG, "Código de error: ${error.code}, Mensaje: ${error.message}")
                    // Si es un error de índice faltante, proporcionar mensaje más claro
                    if (error.message?.contains("index") == true) {
                        close(Exception("Se requiere un índice compuesto en Firestore. Ve a la consola de Firebase para crear el índice automáticamente."))
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toUser() }
                    trySend(users)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override suspend fun getActiveProfessors(): List<User> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("role", UserRole.PROFESSOR.name)
                .whereEqualTo("active", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toUser() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener profesores activos", e)
            emptyList()
        }
    }
    
    // ========== Extensiones para conversión ==========
    
    private fun User.toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "role" to role.name,
            "active" to active,
            "createdAt" to createdAt
        )
    }
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        return try {
            val uid = getString("uid") ?: id
            val email = getString("email") ?: ""
            val displayName = getString("displayName") ?: ""
            val roleStr = getString("role") ?: "PROFESSOR"
            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.PROFESSOR }
            val active = getBoolean("active") ?: false
            val createdAt = getLong("createdAt") ?: 0L
            
            User(
                uid = uid,
                email = email,
                displayName = displayName,
                role = role,
                active = active,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a User", e)
            null
        }
    }
}
