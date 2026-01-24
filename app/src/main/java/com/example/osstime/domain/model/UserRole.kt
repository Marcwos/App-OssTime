package com.example.osstime.domain.model

/**
 * Enum que define los roles de usuario en la aplicación.
 * - ADMIN: Administrador que gestiona profesores y horarios
 * - PROFESSOR: Profesor que crea clases y toma asistencia
 * 
 * Nota: Student no es un rol de usuario, es una entidad de datos (alumnos de la academia)
 */
enum class UserRole {
    ADMIN,
    PROFESSOR
}
