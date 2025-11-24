package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.domain.model.Student
import com.example.osstime.presentation.components.StudentAttendanceItem
import com.example.osstime.presentation.components.Title

@Composable
fun AttendanceScreen(
    navController: NavHostController,
    classInfo: ClassSession,
    students: List<Student>
) {
    val classSession = classInfo

    
    val attendanceState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            students.forEach { put(it.id, false) } // Por defecto todos AUSENTES
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(Modifier.height(30.dp))
        Title(
            text = "Asistencia BJJ",
        )

        Text(
            text = classSession.date,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(8.dp))

        // Nombre + tipo de clase
        Text(
            text = "${classSession.name} • ${classSession.type}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Estudiantes",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(students) { student ->
                StudentAttendanceItem(
                    student = student,
                    isPresent = attendanceState[student.id] == true,
                    onToggle = { isPresent ->
                        attendanceState[student.id] = isPresent
                    }
                )
            }
        }

        Button(
            onClick = {
                // Aquí guardas la asistencia (luego supabase o api)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar asistencia")
        }
    }
}
