package com.example.osstime.data.repository

import android.util.Log
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.domain.repository.ClassRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de clases usando Firebase Firestore.
 * Colección: classes
 * 
 * Actualizado para soportar nuevos campos: professorId, scheduleId
 * Mantiene retrocompatibilidad con clases antiguas que no tienen estos campos.
 */
class ClassRepositoryImpl : ClassRepository {

    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val classesCollection = firestore.collection("classes")
    
    companion object {
        private const val TAG = "ClassRepository"
    }

    override suspend fun getAllClasses(): List<ClassSession> {
        return try {
            val snapshot = classesCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toClassSession()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener clases", e)
            emptyList()
        }
    }

    override suspend fun getClassById(id: String): ClassSession? {
        return try {
            val doc = classesCollection.document(id).get().await()
            doc.toClassSession()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener clase: $id", e)
            null
        }
    }

    override suspend fun insertClass(classSession: ClassSession) {
        try {
            val classMap = classSession.toMap()
            classesCollection.document(classSession.id).set(classMap).await()
            Log.d(TAG, "Clase insertada: ${classSession.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al insertar clase", e)
            throw Exception("Error al insertar clase: ${e.message}")
        }
    }

    override suspend fun updateClass(classSession: ClassSession) {
        try {
            val classMap = classSession.toMap()
            classesCollection.document(classSession.id).set(classMap).await()
            Log.d(TAG, "Clase actualizada: ${classSession.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar clase", e)
            throw Exception("Error al actualizar clase: ${e.message}")
        }
    }

    override suspend fun deleteClass(id: String) {
        try {
            classesCollection.document(id).delete().await()
            Log.d(TAG, "Clase eliminada: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar clase", e)
            throw Exception("Error al eliminar clase: ${e.message}")
        }
    }

    override fun observeClasses(): Flow<List<ClassSession>> = callbackFlow {
        val listenerRegistration = classesCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando clases", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val classes = snapshot.documents.mapNotNull { doc ->
                        doc.toClassSession()
                    }
                    trySend(classes)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    // ========== Extensiones para conversión ==========
    
    /**
     * Convierte ClassSession a Map para Firestore.
     * Incluye professorId y scheduleId solo si no son null.
     */
    private fun ClassSession.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "type" to type,
            "date" to date,
            "description" to description,
            "time" to time
        )
        
        // Agregar nuevos campos solo si tienen valor (retrocompatibilidad)
        professorId?.let { map["professorId"] = it }
        scheduleId?.let { map["scheduleId"] = it }
        
        return map
    }

    /**
     * Convierte DocumentSnapshot a ClassSession.
     * Maneja clases antiguas que no tienen professorId/scheduleId (null).
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toClassSession(): ClassSession? {
        return try {
            ClassSession(
                id = getString("id") ?: id,
                name = getString("name") ?: "",
                type = getString("type") ?: "",
                date = getString("date") ?: "",
                description = getString("description") ?: "",
                time = getString("time") ?: "",
                professorId = getString("professorId"),  // Nuevo campo (nullable)
                scheduleId = getString("scheduleId")      // Nuevo campo (nullable)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a ClassSession", e)
            null
        }
    }
}
