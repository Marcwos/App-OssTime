package com.example.osstime.domain.model

data class Attendance(
    val studentId: String,
    val classId: String,
    val present: Boolean,
)