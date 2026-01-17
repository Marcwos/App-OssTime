package com.example.osstime.data.repository

import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.Attendance
import com.example.osstime.domain.repository.AttendanceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de asistencias usando Firebase Firestore
 */
class AttendanceRepositoryImpl : AttendanceRepository {

    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val attendanceCollection = firestore.collection("attendances")

    override suspend fun saveAttendance(attendance: Attendance) {
        try {
            // Usar classId + studentId como ID del documento para evitar duplicados
            val docId = "${attendance.classId}_${attendance.studentId}"
            val attendanceMap = attendance.toMap()
            attendanceCollection.document(docId).set(attendanceMap).await()
            android.util.Log.d("AttendanceRepository", "Asistencia guardada: $docId")
        } catch (e: Exception) {
            android.util.Log.e("AttendanceRepository", "Error al guardar asistencia", e)
            throw Exception("Error al guardar asistencia: ${e.message}")
        }
    }

    override suspend fun getAttendanceByClassId(classId: String): List<Attendance> {
        return try {
            val snapshot = attendanceCollection
                .whereEqualTo("classId", classId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toAttendance()
            }
        } catch (e: Exception) {
            android.util.Log.e("AttendanceRepository", "Error al obtener asistencias", e)
            emptyList()
        }
    }

    override suspend fun getAttendanceByStudentId(studentId: String): List<Attendance> {
        return try {
            val snapshot = attendanceCollection
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toAttendance()
            }
        } catch (e: Exception) {
            android.util.Log.e("AttendanceRepository", "Error al obtener asistencias del estudiante", e)
            emptyList()
        }
    }

    override fun observeAttendanceByClassId(classId: String): Flow<List<Attendance>> = callbackFlow {
        val listenerRegistration = attendanceCollection
            .whereEqualTo("classId", classId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val attendances = snapshot.documents.mapNotNull { doc ->
                        doc.toAttendance()
                    }
                    trySend(attendances)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun deleteAttendanceByClassId(classId: String) {
        try {
            val snapshot = attendanceCollection
                .whereEqualTo("classId", classId)
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            android.util.Log.d("AttendanceRepository", "Asistencias eliminadas para clase: $classId")
        } catch (e: Exception) {
            android.util.Log.e("AttendanceRepository", "Error al eliminar asistencias", e)
            throw Exception("Error al eliminar asistencias: ${e.message}")
        }
    }

    // Extensiones para convertir entre Attendance y Map/DocumentSnapshot
    private fun Attendance.toMap(): Map<String, Any> {
        return mapOf(
            "studentId" to studentId,
            "classId" to classId,
            "present" to present
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAttendance(): Attendance? {
        return try {
            Attendance(
                studentId = getString("studentId") ?: "",
                classId = getString("classId") ?: "",
                present = getBoolean("present") ?: false
            )
        } catch (e: Exception) {
            android.util.Log.e("AttendanceRepository", "Error al parsear asistencia", e)
            null
        }
    }
}
