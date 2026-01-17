package com.example.osstime.data.repository

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
 * Implementación del repositorio de clases usando Firebase Firestore
 */
class ClassRepositoryImpl : ClassRepository {

    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val classesCollection = firestore.collection("classes")

    override suspend fun getAllClasses(): List<ClassSession> {
        return try {
            val snapshot = classesCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toClassSession()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getClassById(id: String): ClassSession? {
        return try {
            val doc = classesCollection.document(id).get().await()
            doc.toClassSession()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insertClass(classSession: ClassSession) {
        try {
            val classMap = classSession.toMap()
            classesCollection.document(classSession.id).set(classMap).await()
        } catch (e: Exception) {
            throw Exception("Error al insertar clase: ${e.message}")
        }
    }

    override suspend fun updateClass(classSession: ClassSession) {
        try {
            val classMap = classSession.toMap()
            classesCollection.document(classSession.id).set(classMap).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar clase: ${e.message}")
        }
    }

    override suspend fun deleteClass(id: String) {
        try {
            classesCollection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar clase: ${e.message}")
        }
    }

    override fun observeClasses(): Flow<List<ClassSession>> = callbackFlow {
        val listenerRegistration = classesCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
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

    // Extensiones para convertir entre ClassSession y Map/DocumentSnapshot
    private fun ClassSession.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "type" to type,
            "date" to date,
            "description" to description,
            "time" to time
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toClassSession(): ClassSession? {
        return try {
            ClassSession(
                id = getString("id") ?: id,
                name = getString("name") ?: "",
                type = getString("type") ?: "",
                date = getString("date") ?: "",
                description = getString("description") ?: "",
                time = getString("time") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
