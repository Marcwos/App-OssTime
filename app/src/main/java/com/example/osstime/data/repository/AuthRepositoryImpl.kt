package com.example.osstime.data.repository

import android.util.Log
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.User
import com.example.osstime.domain.model.UserRole
import com.example.osstime.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de autenticación usando Firebase Auth + Firestore.
 * - Firebase Auth: maneja email/password authentication
 * - Firestore users/{uid}: almacena datos adicionales del usuario (rol, active, etc.)
 */
class AuthRepositoryImpl : AuthRepository {
    
    private val auth: FirebaseAuth = FirebaseModule.getAuth()
    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    override suspend fun signIn(email: String, password: String): User {
        return try {
            // 1. Autenticar con Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user 
                ?: throw Exception("Error de autenticación: usuario no encontrado")
            
            Log.d(TAG, "Firebase Auth exitoso para: ${firebaseUser.uid}")
            
            // 2. Obtener datos del usuario desde Firestore
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            
            if (!userDoc.exists()) {
                // Usuario autenticado pero no existe en Firestore (caso raro)
                throw Exception("Usuario no registrado en el sistema. Contacte al administrador.")
            }
            
            val user = userDoc.toUser()
                ?: throw Exception("Error al leer datos del usuario")
            
            Log.d(TAG, "Usuario cargado: ${user.displayName}, rol: ${user.role}, activo: ${user.active}")
            
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error en signIn", e)
            throw Exception("Error al iniciar sesión: ${e.message}")
        }
    }
    
    override suspend fun signUp(email: String, password: String, displayName: String): User {
        return try {
            // 1. Crear cuenta en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("Error al crear cuenta")
            
            Log.d(TAG, "Cuenta Auth creada: ${firebaseUser.uid}")
            
            // 2. Crear documento en Firestore con active=false (pendiente de aprobación)
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = UserRole.PROFESSOR, // Por defecto, los nuevos usuarios son profesores
                active = false, // Pendiente de aprobación por Admin
                createdAt = System.currentTimeMillis()
            )
            
            usersCollection.document(user.uid).set(user.toMap()).await()
            
            Log.d(TAG, "Usuario creado en Firestore: ${user.uid}, pendiente de aprobación")
            
            // 3. Cerrar sesión inmediatamente (no puede usar la app hasta ser aprobado)
            auth.signOut()
            
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error en signUp", e)
            throw Exception("Error al registrarse: ${e.message}")
        }
    }
    
    override suspend fun signOut() {
        auth.signOut()
        Log.d(TAG, "Sesión cerrada")
    }
    
    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        
        return try {
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            userDoc.toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario actual", e)
            null
        }
    }
    
    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Obtener datos del usuario desde Firestore
                usersCollection.document(firebaseUser.uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toUser()
                        trySend(user)
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            }
        }
        
        auth.addAuthStateListener(authListener)
        
        awaitClose {
            auth.removeAuthStateListener(authListener)
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
    
    /**
     * Crea un admin por defecto si no existe ninguno.
     * Credenciales: admin@osstime.com / admin123
     * 
     * IMPORTANTE: Cambiar la contraseña después del primer login.
     */
    suspend fun createDefaultAdminIfNeeded() {
        try {
            // Verificar si ya existe algún admin
            val admins = usersCollection
                .whereEqualTo("role", "ADMIN")
                .whereEqualTo("active", true)
                .get()
                .await()
            
            if (admins.documents.isNotEmpty()) {
                Log.d(TAG, "Ya existe un admin activo, no se crea uno por defecto")
                return
            }
            
            Log.d(TAG, "No hay admin, creando admin por defecto...")
            
            val defaultEmail = "admin@osstime.com"
            val defaultPassword = "admin123"
            
            // Verificar si el usuario ya existe en Auth
            try {
                auth.signInWithEmailAndPassword(defaultEmail, defaultPassword).await()
                // Si llega aquí, el usuario existe en Auth
                val existingUser = auth.currentUser
                if (existingUser != null) {
                    // Crear/actualizar documento en Firestore
                    val adminUser = User(
                        uid = existingUser.uid,
                        email = defaultEmail,
                        displayName = "Administrador",
                        role = UserRole.ADMIN,
                        active = true,
                        createdAt = System.currentTimeMillis()
                    )
                    usersCollection.document(existingUser.uid).set(adminUser.toMap()).await()
                    auth.signOut()
                    Log.d(TAG, "Admin por defecto actualizado en Firestore")
                }
                return
            } catch (e: Exception) {
                // Usuario no existe, crearlo
                Log.d(TAG, "Creando nuevo usuario admin en Firebase Auth")
            }
            
            // Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(defaultEmail, defaultPassword).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Crear documento en Firestore
                val adminUser = User(
                    uid = firebaseUser.uid,
                    email = defaultEmail,
                    displayName = "Administrador",
                    role = UserRole.ADMIN,
                    active = true,
                    createdAt = System.currentTimeMillis()
                )
                
                usersCollection.document(firebaseUser.uid).set(adminUser.toMap()).await()
                
                // Cerrar sesión para que el admin tenga que loguearse
                auth.signOut()
                
                Log.d(TAG, "Admin por defecto creado: $defaultEmail / $defaultPassword")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando admin por defecto", e)
        }
    }
}
