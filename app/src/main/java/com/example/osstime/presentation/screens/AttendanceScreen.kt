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
    classInfo: ClassSession
) {
    val classSession = classInfo
    
    val defaultStudents = listOf(
        Student(id = "1", firstName = "Luis Fernando", lastName = "Zambrano Ponce", belt = "Blanco"),
        Student(id = "2", firstName = "Daniel Alejandro", lastName = "Loor Vélez", belt = "Blanco"),
        Student(id = "3", firstName = "Kevin Matías", lastName = "Moreira Cedeño", belt = "Azul"),
        Student(id = "4", firstName = "Jesús Andrés", lastName = "Gómez Mantuano", belt = "Blanco"),
        Student(id = "5", firstName = "Carlos David", lastName = "Villamar Chancay", belt = "Amarillo"),
        Student(id = "6", firstName = "Jorge Sebastián", lastName = "Delgado Reyes", belt = "Naranja"),
        Student(id = "7", firstName = "Mario Esteban", lastName = "Mendoza Chávez", belt = "Azul"),
        Student(id = "8", firstName = "Anthony Joel", lastName = "Cárdenas Palma", belt = "Blanco"),
        Student(id = "9", firstName = "Bryan Eduardo", lastName = "Quiroz Macías", belt = "Verde"),
        Student(id = "10", firstName = "Ángel Francisco", lastName = "Barreto Álava", belt = "Azul")
    )

    
    val attendanceState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            defaultStudents.forEach { put(it.id, false) } // Por defecto todos AUSENTES
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
            items(defaultStudents) { student ->
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
