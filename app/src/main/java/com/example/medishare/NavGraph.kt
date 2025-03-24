package com.example.medishare

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medishare.ui.screens.HomeScreen
import com.example.medishare.ui.screens.LoginScreen
import com.example.medishare.ui.screens.ProfileScreen
import com.example.medishare.ui.screens.CreatePostScreen

sealed class Route(val route: String) {
    object Login : Route("login")
    object Home : Route("home")
    object Profile : Route("profile")
    object CreatePost : Route("create_post")
}

@Composable
fun NavGraph(
    startDestination: String = Route.Login.route,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Route.Home.route) {
            HomeScreen(
                onCreatePost = { navController.navigate(Route.CreatePost.route) },
                onProfileClick = { navController.navigate(Route.Profile.route) }
            )
        }
        
        composable(Route.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable(Route.CreatePost.route) {
            CreatePostScreen(
                onPostCreated = {
                    navController.navigateUp()
                },
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}