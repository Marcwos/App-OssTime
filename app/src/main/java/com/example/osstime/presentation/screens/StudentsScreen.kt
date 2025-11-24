package com.example.osstime.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.osstime.domain.model.Student
import com.example.osstime.presentation.components.BottomNavigationBar
import com.example.osstime.presentation.components.StudentItem
import com.example.osstime.presentation.components.Title
import com.example.osstime.presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(navController: NavHostController) {
    // Lista de estudiantes (en el futuro vendrá de un ViewModel/Repository)
    val students = remember {
        listOf(
            Student(id = "1", firstName = "Luis Fernando", lastName = "Zambrano Ponce", belt = "Blanco"),
            Student(id = "2", firstName = "Daniel Alejandro", lastName = "Loor Vélez", belt = "Blanco"),
            Student(id = "3", firstName = "Kevin Matías", lastName = "Moreira Cedeño", belt = "Azul"),
            Student(id = "4", firstName = "Jesús Andrés", lastName = "Gómez Mantuano", belt = "Blanco"),
            Student(id = "5", firstName = "Carlos David", lastName = "Villamar Chancay", belt = "Morado"),
            Student(id = "6", firstName = "Jorge Sebastián", lastName = "Delgado Reyes", belt = "Blanco"),
            Student(id = "7", firstName = "Mario Esteban", lastName = "Mendoza Chávez", belt = "Marrón"),
            Student(id = "8", firstName = "Anthony Joel", lastName = "Cárdenas Palma", belt = "Blanco"),
            Student(id = "9", firstName = "Bryan Eduardo", lastName = "Quiroz Macías", belt = "Verde"),
            Student(id = "10", firstName = "Ángel Francisco", lastName = "Barreto Álava", belt = "Negro")
        )
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_student") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar estudiante",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(24.dp))
            
            Title(
                text = "Estudiantes"
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "${students.size} estudiantes registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay estudiantes registrados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Presiona el botón + para agregar uno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(students) { student ->
                        StudentItem(student = student)
                    }
                }
            }
        }
    }
}

