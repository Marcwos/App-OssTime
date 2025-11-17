package com.example.osstime.domain.model

data class ClassSession(
    val id: String,
    val name: String,
    val type: String,
    val date: String,
    val description: String = "",
    val time: String = "",
)

