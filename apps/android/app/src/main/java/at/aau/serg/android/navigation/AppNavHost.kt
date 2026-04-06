package at.aau.serg.android.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        route = "root",
        modifier = Modifier.padding(innerPadding)
    ) {

        // HOME SCREEN
        composable("home") {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry("root")
            }
            val leaderboardVM: LeaderboardViewModel = viewModel(parentEntry)
            val state by leaderboardVM.loadState.collectAsState()

            HomeScreen(
                state = state,
                onCreateLobby = { navController.navigate("createLobby") },
                onBrowseLobbies = { navController.navigate("browseLobbies") },
                onShowLeaderboard = {
                    leaderboardVM.loadLeaderboard(
                        onSuccess = { navController.navigate("leaderboard") },
                        onError = { }
                    )
                },
                onSettings = { navController.navigate("settings") }
            )
        }

        // LEADERBOARD SCREEN
        composable("leaderboard") {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry("root")
            }
            val leaderboardVM: LeaderboardViewModel = viewModel(parentEntry)
            val players by leaderboardVM.players.collectAsState()

            LeaderboardScreen(
                players = players,
                onBack = { navController.popBackStack() }
            )
        }

        composable("createLobby") { Text("TODO: Create Lobby Screen") }
        composable("browseLobbies") { Text("TODO: Browse Lobbies Screen") }
        composable("settings") { Text("TODO: Settings Screen") }
    }
}
