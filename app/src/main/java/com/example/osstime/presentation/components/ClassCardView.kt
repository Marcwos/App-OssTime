package com.example.osstime.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.ClassSession
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ClassCardView(
    classSession: ClassSession,
    navController: NavHostController
) {
    val tipoColor = when (classSession.type.uppercase()) {
        "NOGI" -> Color(0xFFB7CB76)
        "GI" -> Color(0xFF8AB5FF)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(140.dp)
            .clickable {
                val encodedId = URLEncoder.encode(classSession.id, StandardCharsets.UTF_8.toString())
                val encodedName = URLEncoder.encode(classSession.name, StandardCharsets.UTF_8.toString())
                val encodedType = URLEncoder.encode(classSession.type, StandardCharsets.UTF_8.toString())
                val encodedDate = URLEncoder.encode(classSession.date, StandardCharsets.UTF_8.toString())
                val encodedDescription = URLEncoder.encode(classSession.description, StandardCharsets.UTF_8.toString())
                val encodedTime = URLEncoder.encode(classSession.time, StandardCharsets.UTF_8.toString())
                val route = "class_detail/$encodedId/$encodedName/$encodedType/$encodedDate/$encodedDescription/$encodedTime"
                navController.navigate(route)
            },
        shape = RoundedCornerShape(20.dp),
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
                    text = classSession.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = classSession.time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

