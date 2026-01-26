package com.example.osstime.domain.model

/**
 * Modelo de torneo.
 * Colección Firestore: tournaments
 * 
 * @param id ID único del torneo (UUID)
 * @param name Nombre del torneo
 * @param city Ciudad donde se realiza
 * @param modality Modalidad: "GI", "NOGI" o "AMBAS"
 * @param date Fecha del torneo (formato: "dd/MM/yyyy")
 * @param professorId ID del profesor que creó el torneo
 * @param createdAt Timestamp de creación
 */
data class Tournament(
    val id: String,
    val name: String,
    val city: String,
    val modality: String,
    val date: String,
    val professorId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
