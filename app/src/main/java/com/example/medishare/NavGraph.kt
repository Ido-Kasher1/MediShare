package com.example.pixelpeppers

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pixelpeppers.ui.screens.LoginScreen

@Composable
fun NavGraph(
    startDestination: String,
) {
    val navController = rememberNavController()
    val navigateGame: (Game) -> Unit =
        { game: Game -> navController.navigate(route = "${Route.Game.route}/${game.id}") }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(
            route = Route.OnboardingIntro.route
        ) {
            Onboarding(navigateToMenu = {navController.navigate(route = Route.Menu.route)})
        }
        composable(
            route = Route.Menu.route
        ) {
            MainMenu(
                onGameClick = navigateGame,
                onSearchClick = { navController.navigate(route = Route.Search.route) },
                onAccountClick = { navController.navigate(route = Route.Account.route) },
                returnToOnboarding= { navController.navigate(route=Route.OnboardingIntro.route)}
            )
        }
        composable(
            route = Route.Login.route
        ) {
            Login()
        }
        composable(
            route = Route.Search.route
        ) {
            Search(onGameClick = navigateGame)
        }
        composable(route = Route.LoginEmailPassword.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Menu.route) {
                        popUpTo(Route.LoginEmailPassword.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${Route.Game.route}/{gameID}"
        ) {
            GamePage(
                gameID = it.arguments?.getString("gameID")?.toInt() ?: 17000,
            )
        }
        composable(
            route = Route.Account.route
        ) {
            AccountScreen(
                routeToLogin = {navController.navigate(route = Route.Login.route)},
            )
        }
    }
}