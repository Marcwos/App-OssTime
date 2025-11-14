package com.example.osstime.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.osstime.presentation.screens.ClassInfo

@Composable
fun ClassCard(info: ClassInfo) {

    val tipoColor = when (info.tipo.uppercase()) {
        "NOGI" -> Color(0xFFB7CB76)  // verde suave
        "GI" -> Color(0xFF8AB5FF)    // azul suave
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),  // bordes más grandes, estilo mockup
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = tipoColor,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = info.tipo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = info.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.DarkGray
                )
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = info.time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Button(
                    onClick = { /* asistencia */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF7D96F)   // amarillo mockup
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Asistencia", color = Color.Black)
                }
            }
        }
    }
}
