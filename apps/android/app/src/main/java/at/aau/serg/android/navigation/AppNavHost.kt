package at.aau.serg.android.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardViewModel
import at.aau.serg.android.ui.screens.lobby.LobbyScreen
import at.aau.serg.android.ui.screens.lobby.LobbyViewModel

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
            val lobbyVM: LobbyViewModel = viewModel(parentEntry)
            val state by leaderboardVM.loadState.collectAsState()

            HomeScreen(
                state = state,
                onCreateLobby = {
                    lobbyVM.createLobby(
                        onSuccess = { lobby ->
                            // Navigate with ONLY the lobbyId
                            navController.navigate("lobby/${lobby.lobbyId}")
                        },
                        onError = { /* show error */ }
                    )
                },
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

        // LOBBY SCREEN — loads lobby inside the screen
        composable("lobby/{lobbyId}") { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId")!!
            val vm: LobbyViewModel = viewModel()

            LobbyScreen(
                navController = navController,
                viewModel = vm,
                lobbyId = lobbyId
            )
        }

        // PLACEHOLDER SCREENS
        composable("browseLobbies") { Text("TODO: Browse Lobbies Screen") }
        composable("settings") { Text("TODO: Settings Screen") }
    }
}
