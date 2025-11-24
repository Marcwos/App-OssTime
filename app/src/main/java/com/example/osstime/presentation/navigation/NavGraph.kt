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
import com.example.osstime.presentation.screens.HomeScreen
import com.example.osstime.presentation.screens.LoginScreen
import com.example.osstime.presentation.screens.AttendanceScreen
import com.example.osstime.presentation.screens.StudentsScreen
import com.example.osstime.presentation.screens.CreateClassScreen
import com.example.osstime.presentation.screens.ProfileScreen

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
            
            AttendanceScreen(navController, classSession)
        }
        composable("students") { StudentsScreen(navController) }
        composable("create_class") { CreateClassScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}