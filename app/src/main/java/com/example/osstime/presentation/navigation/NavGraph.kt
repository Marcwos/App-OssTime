package com.example.osstime.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        composable("attendance") { AttendanceScreen(navController) }
        composable("students") { StudentsScreen(navController) }
        composable("create_class") { CreateClassScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}