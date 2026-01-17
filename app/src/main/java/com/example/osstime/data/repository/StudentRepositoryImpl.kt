package com.example.osstime.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.Student
import com.example.osstime.domain.repository.StudentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de estudiantes usando Firebase Firestore
 */
class StudentRepositoryImpl : StudentRepository {

    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val studentsCollection = firestore.collection("students")

    override suspend fun getAllStudents(): List<Student> {
        return try {
            val snapshot = studentsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toStudent()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getStudentById(id: String): Student? {
        return try {
            val doc = studentsCollection.document(id).get().await()
            doc.toStudent()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insertStudent(student: Student) {
        try {
            val studentMap = student.toMap()
            // Usar el ID del estudiante como ID del documento
            studentsCollection.document(student.id).set(studentMap).await()
            // Log para depuración (remover en producción)
            android.util.Log.d("StudentRepository", "Estudiante guardado: ${student.id}")
        } catch (e: Exception) {
            android.util.Log.e("StudentRepository", "Error al insertar estudiante", e)
            throw Exception("Error al insertar estudiante: ${e.message}")
        }
    }

    override suspend fun updateStudent(student: Student) {
        try {
            val studentMap = student.toMap()
            studentsCollection.document(student.id).set(studentMap).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar estudiante: ${e.message}")
        }
    }

    override suspend fun deleteStudent(id: String) {
        try {
            studentsCollection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar estudiante: ${e.message}")
        }
    }

    override suspend fun getStudentCount(): Int {
        return try {
            val snapshot = studentsCollection.get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    override fun getStudentsPagingSource(): PagingSource<Int, Student> {
        // Para Firestore, se recomienda usar FirestorePagingSource
        // Por ahora retornamos un PagingSource básico
        return object : PagingSource<Int, Student>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Student> {
                return try {
                    val students = getAllStudents()
                    LoadResult.Page(
                        data = students,
                        prevKey = null,
                        nextKey = null
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, Student>): Int? {
                // Retornar null para indicar que no hay una clave de actualización
                // Esto es apropiado cuando cargamos todos los datos de una vez
                return null
            }
        }
    }

    override fun observeStudents(): Flow<List<Student>> = callbackFlow {
        val listenerRegistration = studentsCollection
            .orderBy("firstName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val students = snapshot.documents.mapNotNull { doc ->
                        doc.toStudent()
                    }
                    trySend(students)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    // Extensiones para convertir entre Student y Map/DocumentSnapshot
    private fun Student.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "firstName" to firstName,
            "lastName" to lastName,
            "belt" to belt
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toStudent(): Student? {
        return try {
            Student(
                id = getString("id") ?: id,
                firstName = getString("firstName") ?: "",
                lastName = getString("lastName") ?: "",
                belt = getString("belt") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}

