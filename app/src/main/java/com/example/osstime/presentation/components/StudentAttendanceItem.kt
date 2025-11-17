package com.example.osstime.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.osstime.domain.model.Student

@Composable
fun StudentAttendanceItem(
    student: Student,
    isPresent: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isPresent) Color(0xFFAEDFA5) else Color(0xFFE3E3E3),
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre estudiante
        Text(
            text = "${student.firstName} ${student.lastName}",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Toggle de asistencia
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(12.dp))
                .clickable { onToggle(!isPresent) }
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isPresent) "Presente" else "Ausente",
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
