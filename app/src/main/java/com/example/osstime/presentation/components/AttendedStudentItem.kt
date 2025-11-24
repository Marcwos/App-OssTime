package com.example.osstime.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.osstime.domain.model.Student

@Composable
fun AttendedStudentItem(student: Student) {
    val beltColor = when (student.belt.lowercase()) {
        "blanco" -> Color(0xFFF5F5F5)
        "azul" -> Color(0xFF2196F3)
        "morado", "púrpura" -> Color(0xFF9C27B0)
        "marrón", "café" -> Color(0xFF795548)
        "negro" -> Color(0xFF000000)
        "amarillo" -> Color(0xFFFFEB3B)
        "naranja" -> Color(0xFFFF9800)
        "verde" -> Color(0xFF4CAF50)
        else -> Color(0xFFE0E0E0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${student.firstName} ${student.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Box(
                modifier = Modifier
                    .background(
                        color = beltColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.belt,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (student.belt.lowercase() in listOf("blanco", "amarillo")) Color.Black else Color.White
                )
            }
        }
    }
}

