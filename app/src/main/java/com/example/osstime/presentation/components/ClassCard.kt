package com.example.osstime.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.osstime.domain.model.ClassSession
import androidx.navigation.NavHostController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * ClassCard optimizado para minimizar recomposiciones
 * - Calcula valores fuera del árbol composable cuando es posible
 * - Usa remember para valores que no cambian
 */
@Composable
fun ClassCard(classSession: ClassSession, navController: NavHostController) {
    // Calcular color una sola vez y recordarlo
    val colorScheme = MaterialTheme.colorScheme
    val tipoColor = remember(classSession.type, colorScheme) {
        when (classSession.type.uppercase()) {
            "NOGI" -> Color(0xFFB7CB76)  // verde suave
            "GI" -> Color(0xFF8AB5FF)    // azul suave
            else -> colorScheme.primary
        }
    }
    
    // Pre-calcular la ruta de navegación
    val navigationRoute = remember(classSession) {
        val encodedId = URLEncoder.encode(classSession.id, StandardCharsets.UTF_8.toString())
        val encodedName = URLEncoder.encode(classSession.name, StandardCharsets.UTF_8.toString())
        val encodedType = URLEncoder.encode(classSession.type, StandardCharsets.UTF_8.toString())
        val encodedDate = URLEncoder.encode(classSession.date, StandardCharsets.UTF_8.toString())
        // Usar "_" como placeholder si description o time están vacíos para evitar parámetros vacíos en la URL
        val description = if (classSession.description.isBlank()) "_" else classSession.description
        val time = if (classSession.time.isBlank()) "_" else classSession.time
        val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
        val encodedTime = URLEncoder.encode(time, StandardCharsets.UTF_8.toString())
        "attendance/$encodedId/$encodedName/$encodedType/$encodedDate/$encodedDescription/$encodedTime"
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
                    text = classSession.name,
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
                        text = classSession.type,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = classSession.description,
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
                    text = classSession.time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Button(
                    onClick = {
                        navController.navigate(navigationRoute)
                    },
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
