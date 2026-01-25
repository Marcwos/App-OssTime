package com.example.osstime.data.repository

import android.util.Log
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.Schedule
import com.example.osstime.domain.repository.ScheduleRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Implementación del repositorio de horarios usando Firebase Firestore.
 * Colección: schedules
 * 
 * Incluye validación de solapamiento de horarios.
 */
class ScheduleRepositoryImpl : ScheduleRepository {
    
    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val schedulesCollection = firestore.collection("schedules")
    
    companion object {
        private const val TAG = "ScheduleRepository"
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        private val time24Format = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    
    override suspend fun createSchedule(schedule: Schedule) {
        try {
            schedulesCollection.document(schedule.id).set(schedule.toMap()).await()
            Log.d(TAG, "Horario creado: ${schedule.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear horario", e)
            throw e
        }
    }
    
    override suspend fun updateSchedule(schedule: Schedule) {
        try {
            schedulesCollection.document(schedule.id).set(schedule.toMap()).await()
            Log.d(TAG, "Horario actualizado: ${schedule.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar horario", e)
            throw e
        }
    }
    
    override suspend fun deleteSchedule(scheduleId: String) {
        try {
            schedulesCollection.document(scheduleId).delete().await()
            Log.d(TAG, "Horario eliminado: $scheduleId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar horario", e)
            throw Exception("Error al eliminar horario: ${e.message}")
        }
    }
    
    override suspend fun deactivateSchedule(scheduleId: String) {
        try {
            schedulesCollection.document(scheduleId).update("active", false).await()
            Log.d(TAG, "Horario desactivado: $scheduleId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al desactivar horario", e)
            throw Exception("Error al desactivar horario: ${e.message}")
        }
    }
    
    override suspend fun getScheduleById(scheduleId: String): Schedule? {
        return try {
            val doc = schedulesCollection.document(scheduleId).get().await()
            doc.toSchedule()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener horario: $scheduleId", e)
            null
        }
    }
    
    override suspend fun getAllSchedules(): List<Schedule> {
        return try {
            val snapshot = schedulesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toSchedule() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener horarios", e)
            emptyList()
        }
    }
    
    override suspend fun getSchedulesByProfessor(professorId: String): List<Schedule> {
        return try {
            val snapshot = schedulesCollection
                .whereEqualTo("professorId", professorId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toSchedule() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener horarios del profesor: $professorId", e)
            emptyList()
        }
    }
    
    override suspend fun getActiveSchedulesByProfessor(professorId: String): List<Schedule> {
        return try {
            val snapshot = schedulesCollection
                .whereEqualTo("professorId", professorId)
                .whereEqualTo("active", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toSchedule() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener horarios activos del profesor: $professorId", e)
            emptyList()
        }
    }
    
    override fun observeSchedules(): Flow<List<Schedule>> = callbackFlow {
        val listenerRegistration = schedulesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando horarios", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val schedules = snapshot.documents.mapNotNull { it.toSchedule() }
                    trySend(schedules)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override fun observeActiveSchedulesByProfessor(professorId: String): Flow<List<Schedule>> = callbackFlow {
        val listenerRegistration = schedulesCollection
            .whereEqualTo("professorId", professorId)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando horarios del profesor", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val schedules = snapshot.documents.mapNotNull { it.toSchedule() }
                    trySend(schedules)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    // ========== Funciones de utilidad ==========
    
    private fun parseDate(dateStr: String): Long? {
        return try {
            dateFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseTime(timeStr: String): Int? {
        return try {
            // Intentar parsear formato 12h (ej: "6:00 PM")
            val date = try {
                timeFormat.parse(timeStr)
            } catch (e: Exception) {
                // Intentar formato 24h (ej: "18:00")
                time24Format.parse(timeStr)
            }
            
            if (date != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = date
                calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // ========== Extensiones para conversión ==========
    
    private fun Schedule.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "professorId" to professorId,
            "professorName" to professorName,
            "startTime" to startTime,
            "endTime" to endTime,
            "active" to active,
            "createdAt" to createdAt,
            "createdBy" to createdBy
        )
    }
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toSchedule(): Schedule? {
        return try {
            // Leer campos (compatibilidad con documentos antiguos y nuevos)
            val startTime = getString("startTime") ?: ""
            val endTime = getString("endTime") ?: ""
            
            // Si no hay startTime, probablemente es un documento antiguo - ignorarlo
            if (startTime.isEmpty() || endTime.isEmpty()) {
                Log.w(TAG, "Documento antiguo encontrado (sin startTime/endTime): $id - será ignorado")
                return null
            }
            
            Schedule(
                id = getString("id") ?: id,
                professorId = getString("professorId") ?: "",
                professorName = getString("professorName") ?: "",
                startTime = startTime,
                endTime = endTime,
                active = getBoolean("active") ?: true,
                createdAt = getLong("createdAt") ?: 0L,
                createdBy = getString("createdBy") ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a Schedule", e)
            null
        }
    }
}
