package com.example.osstime.domain.model

/**
 * Modelo de horario asignado a un profesor.
 * Colección Firestore: schedules
 * 
 * @param id ID único del horario (UUID)
 * @param professorId ID del profesor asignado a este horario
 * @param professorName Nombre del profesor (para mostrar en UI sin hacer join)
 * @param startTime Hora de inicio (formato: "HH:mm" o "h:mm AM/PM")
 * @param endTime Hora de fin (formato: "HH:mm" o "h:mm AM/PM")
 * @param active Si el horario está activo
 * @param createdAt Timestamp de creación
 * @param createdBy UID del admin que creó el horario
 */
data class Schedule(
    val id: String,
    val professorId: String,
    val professorName: String = "",
    val startTime: String,
    val endTime: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)
