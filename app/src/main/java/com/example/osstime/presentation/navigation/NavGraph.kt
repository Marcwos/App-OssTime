package com.example.osstime.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.data.repository.AuthRepositoryImpl
import com.example.osstime.data.repository.ClassRepositoryImpl
import com.example.osstime.data.repository.ScheduleRepositoryImpl
import com.example.osstime.data.repository.UserRepositoryImpl
import com.example.osstime.domain.model.ClassSession
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.example.osstime.domain.model.Student
import com.example.osstime.presentation.screens.HomeScreen
import com.example.osstime.presentation.screens.LoginScreen
import com.example.osstime.presentation.screens.RegisterScreen
import com.example.osstime.presentation.screens.PendingApprovalScreen
import com.example.osstime.presentation.screens.AttendanceScreen
import com.example.osstime.presentation.screens.StudentsScreen
import com.example.osstime.presentation.screens.CreateClassScreen
import com.example.osstime.presentation.screens.CreateClassFormScreen
import com.example.osstime.presentation.screens.CreateStudentScreen
import com.example.osstime.presentation.screens.ProfileScreen
import com.example.osstime.presentation.screens.ClassDetailScreen
import com.example.osstime.presentation.screens.admin.AdminHomeScreen
import com.example.osstime.presentation.screens.admin.ManageProfessorsScreen
import com.example.osstime.presentation.screens.admin.ManageSchedulesScreen
import com.example.osstime.presentation.viewmodel.AdminProfessorsViewModel
import com.example.osstime.presentation.viewmodel.AdminSchedulesViewModel
import com.example.osstime.presentation.viewmodel.AuthViewModel
import com.example.osstime.presentation.viewmodel.CreateClassViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavGraph(navController: NavHostController) {
    // Crear repositorios (sin parámetros - usan FirebaseModule internamente)
    val userRepository = remember { UserRepositoryImpl() }
    val scheduleRepository = remember { ScheduleRepositoryImpl() }
    val authRepository = remember { AuthRepositoryImpl() }
    val classRepository = remember { ClassRepositoryImpl() }
    
    // Crear admin por defecto si no existe
    LaunchedEffect(Unit) {
        authRepository.createDefaultAdminIfNeeded()
    }
    
    // Crear ViewModels
    val authViewModel = remember { AuthViewModel(authRepository) }
    val professorsViewModel = remember { AdminProfessorsViewModel(userRepository) }
    val schedulesViewModel = remember { AdminSchedulesViewModel(scheduleRepository, userRepository) }
    val createClassViewModel = remember { CreateClassViewModel(classRepository, scheduleRepository) }
    
    // Obtener auth para el userId actual
    val auth = remember { FirebaseModule.getAuth() }
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // ===== AUTHENTICATION ROUTES =====
        composable("login") { 
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            ) 
        }
        
        composable("register") { 
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            ) 
        }
        
        composable("pending_approval") { 
            PendingApprovalScreen(
                navController = navController,
                authViewModel = authViewModel
            ) 
        }
        
        // ===== ADMIN ROUTES =====
        composable("admin_home") { 
            AdminHomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                professorsViewModel = professorsViewModel
            ) 
        }
        
        composable("admin_professors") { 
            ManageProfessorsScreen(
                navController = navController,
                viewModel = professorsViewModel
            ) 
        }
        
        composable("admin_schedules") { 
            ManageSchedulesScreen(
                navController = navController,
                viewModel = schedulesViewModel,
                currentUserId = auth.currentUser?.uid ?: ""
            ) 
        }
        
        // ===== PROFESSOR ROUTES =====
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
            val rawDescription = URLDecoder.decode(backStackEntry.arguments?.getString("classDescription") ?: "", StandardCharsets.UTF_8.toString())
            val classDescription = if (rawDescription == "_") "" else rawDescription
            val rawTime = URLDecoder.decode(backStackEntry.arguments?.getString("classTime") ?: "", StandardCharsets.UTF_8.toString())
            val classTime = if (rawTime == "_") "" else rawTime
            
            val classSession = ClassSession(
                id = classId,
                name = className,
                type = classType,
                date = classDate,
                description = classDescription,
                time = classTime
            )
            
            ClassDetailScreen(navController, classSession)
        }
        
        composable("students") { StudentsScreen(navController) }
        composable("create_student") { CreateStudentScreen(navController) }
        composable("create_class") { CreateClassScreen(navController) }
        
        // Create class form - con soporte para professorId
        composable("create_class_form") { 
            CreateClassFormScreen(
                navController = navController,
                professorId = auth.currentUser?.uid,
                createClassViewModel = createClassViewModel
            ) 
        }
        
        // Ruta alternativa sin ViewModel (modo legacy/admin)
        composable("create_class_form_legacy") { 
            CreateClassFormScreen(navController = navController) 
        }
        
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
            val rawDescription = URLDecoder.decode(backStackEntry.arguments?.getString("classDescription") ?: "", StandardCharsets.UTF_8.toString())
            val classDescription = if (rawDescription == "_") "" else rawDescription
            val rawTime = URLDecoder.decode(backStackEntry.arguments?.getString("classTime") ?: "", StandardCharsets.UTF_8.toString())
            val classTime = if (rawTime == "_") "" else rawTime
            
            val classSession = ClassSession(
                id = classId,
                name = className,
                type = classType,
                date = classDate,
                description = classDescription,
                time = classTime
            )
            
            ClassDetailScreen(navController, classSession)
        }
        
        composable("profile") { ProfileScreen(navController) }
    }
}