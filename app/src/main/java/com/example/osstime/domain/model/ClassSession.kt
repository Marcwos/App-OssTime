package com.example.osstime.domain.model

/**
 * Modelo de sesión de clase.
 * Colección Firestore: classes
 * 
 * @param id ID único de la clase (UUID)
 * @param name Nombre de la clase
 * @param type Tipo de clase: "GI" o "NOGI"
 * @param date Fecha de la clase (formato: "dd/MM/yyyy")
 * @param description Descripción opcional
 * @param time Hora de la clase (formato: "h:mm AM/PM")
 * @param professorId ID del profesor que creó la clase (nuevo campo - nullable para retrocompatibilidad)
 * @param scheduleId ID del horario asociado (nuevo campo - nullable para retrocompatibilidad)
 */
data class ClassSession(
    val id: String,
    val name: String,
    val type: String,
    val date: String,
    val description: String = "",
    val time: String = "",
    val professorId: String? = null,  // Nuevo: ID del profesor
    val scheduleId: String? = null    // Nuevo: ID del horario
)

