package com.example.osstime.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.osstime.domain.model.ClassSession
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.example.osstime.domain.model.Student
import com.example.osstime.presentation.screens.HomeScreen
import com.example.osstime.presentation.screens.LoginScreen
import com.example.osstime.presentation.screens.AttendanceScreen
import com.example.osstime.presentation.screens.StudentsScreen
import com.example.osstime.presentation.screens.CreateClassScreen
import com.example.osstime.presentation.screens.CreateClassFormScreen
import com.example.osstime.presentation.screens.CreateStudentScreen
import com.example.osstime.presentation.screens.ProfileScreen
import com.example.osstime.presentation.screens.ClassDetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable(
            route = "attendance/{classId}/{className}/{classType}/{classDate}/{classDescription}/{classTime}",
            arguments = listOf(
                navArgument("classId") { type = NavType.StringType },
                navArgument("className") { type = NavType.StringType },
                navArgument("classType") { type = NavType.StringType },
                navArgument("classDate") { type = NavType.StringType },
                navArgument("classDescription") { type = NavType.StringType },
                navArgument("classTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = URLDecoder.decode(backStackEntry.arguments?.getString("classId") ?: "", StandardCharsets.UTF_8.toString())
            val className = URLDecoder.decode(backStackEntry.arguments?.getString("className") ?: "", StandardCharsets.UTF_8.toString())
            val classType = URLDecoder.decode(backStackEntry.arguments?.getString("classType") ?: "", StandardCharsets.UTF_8.toString())
            val classDate = URLDecoder.decode(backStackEntry.arguments?.getString("classDate") ?: "", StandardCharsets.UTF_8.toString())
            val classDescription = URLDecoder.decode(backStackEntry.arguments?.getString("classDescription") ?: "", StandardCharsets.UTF_8.toString())
            val classTime = URLDecoder.decode(backStackEntry.arguments?.getString("classTime") ?: "", StandardCharsets.UTF_8.toString())
            
            val classSession = ClassSession(
                id = classId,
                name = className,
                type = classType,
                date = classDate,
                description = classDescription,
                time = classTime
            )
            
            // Lista de estudiantes (en el futuro vendrá de un ViewModel/Repository compartido)
            val students = listOf(
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
            
            AttendanceScreen(navController, classSession, students)
        }
        composable("students") { StudentsScreen(navController) }
        composable("create_student") { CreateStudentScreen(navController) }
        composable("create_class") { CreateClassScreen(navController) }
        composable("create_class_form") { CreateClassFormScreen(navController) }
        composable(
            route = "class_detail/{classId}/{className}/{classType}/{classDate}/{classDescription}/{classTime}",
            arguments = listOf(
                navArgument("classId") { type = NavType.StringType },
                navArgument("className") { type = NavType.StringType },
                navArgument("classType") { type = NavType.StringType },
                navArgument("classDate") { type = NavType.StringType },
                navArgument("classDescription") { type = NavType.StringType },
                navArgument("classTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = URLDecoder.decode(backStackEntry.arguments?.getString("classId") ?: "", StandardCharsets.UTF_8.toString())
            val className = URLDecoder.decode(backStackEntry.arguments?.getString("className") ?: "", StandardCharsets.UTF_8.toString())
            val classType = URLDecoder.decode(backStackEntry.arguments?.getString("classType") ?: "", StandardCharsets.UTF_8.toString())
            val classDate = URLDecoder.decode(backStackEntry.arguments?.getString("classDate") ?: "", StandardCharsets.UTF_8.toString())
            val classDescription = URLDecoder.decode(backStackEntry.arguments?.getString("classDescription") ?: "", StandardCharsets.UTF_8.toString())
            val classTime = URLDecoder.decode(backStackEntry.arguments?.getString("classTime") ?: "", StandardCharsets.UTF_8.toString())
            
            val classSession = ClassSession(
                id = classId,
                name = className,
                type = classType,
                date = classDate,
                description = classDescription,
                time = classTime
            )
            
            // Simulación de estudiantes que asistieron (en el futuro vendrá de la base de datos)
            // Por ahora, simulamos que algunos estudiantes asistieron según el ID de la clase
            val allStudents = listOf(
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
            
            // Simulamos que para la clase 1 asistieron los primeros 7, y para la clase 2 los últimos 5
            val attendedStudents = if (classId == "1") {
                allStudents.take(7)
            } else {
                allStudents.takeLast(5)
            }
            
            ClassDetailScreen(navController, classSession, attendedStudents)
        }
        composable("profile") { ProfileScreen(navController) }
    }
}