package com.example.medishare

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medishare.ui.screens.HomeScreen
import com.example.medishare.ui.screens.LoginScreen
import com.example.medishare.ui.screens.ProfileScreen
import androidx.compose.runtime.getValue
import com.example.medishare.ui.screens.CreatePostScreen
import com.example.medishare.ui.viewmodels.AuthViewModel

sealed class Route(val route: String) {
    object Login : Route("login")
    object Home : Route("home")
    object Profile : Route("profile")
    object CreatePost : Route("create_post")
}

@Composable
fun NavGraph(
    startDestination: String = Route.Login.route,
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if(authState is com.example.medishare.ui.viewmodels.AuthState.Unauthenticated) {
            Log.d("NavGraph", "User is unauthenticated, navigating to login screen")
            navController.navigate(Route.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
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
                authViewModel = authViewModel,
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