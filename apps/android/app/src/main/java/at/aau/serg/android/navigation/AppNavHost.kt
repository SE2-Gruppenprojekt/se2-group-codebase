package at.aau.serg.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onCreateLobby = { navController.navigate("createLobby") },
                onBrowseLobbies = { navController.navigate("browseLobbies") },
                onShowLeaderboard = { navController.navigate("leaderboard") },
                onSettings = { navController.navigate("settings") }
            )
        }

        composable("leaderboard") {
            LeaderboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

