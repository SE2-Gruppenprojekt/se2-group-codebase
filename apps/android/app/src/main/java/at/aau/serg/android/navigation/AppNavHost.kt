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
import at.aau.serg.android.ui.screens.browselobbies.BrowseLobbiesScreen
import at.aau.serg.android.ui.screens.createlobby.CreateLobbyScreen
import at.aau.serg.android.ui.screens.createlobby.NewLobbyScreen
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardViewModel
import at.aau.serg.android.ui.screens.lobby.LobbyScreen
import at.aau.serg.android.ui.screens.lobby.LobbyViewModel
import at.aau.serg.android.ui.screens.settings.SettingsScreen
import at.aau.serg.android.ui.screens.waiting.WaitingRoomScreen
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.lobby.LobbyUiState

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
                    // create lobby directly from backend
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
                onSettings = { navController.navigate("settings") },
                onWaitingRoom = { navController.navigate("waitingRoom") },
                onNewLobbyScreen = { navController.navigate("createLobbyFancy") }
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

        // simple create lobby screen
        composable("createLobby") {
            CreateLobbyScreen(
                onBack = { navController.popBackStack() },
                onCreate = { lobbyName ->
                    // later Backend Call
                    navController.popBackStack()
                }
            )
        }

        // fancy create lobby screen
        composable("createLobbyFancy") {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry("root")
            }
            val lobbyVM: LobbyViewModel = viewModel(parentEntry)
            val lobbyLoadState by lobbyVM.loadState.collectAsState()

            NewLobbyScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate("settings") },
                onCreateLobby = { maxPlayers, isPrivate ->
                    lobbyVM.createLobby(
                        displayName = "Host",
                        maxPlayers = maxPlayers,
                        isPrivate = isPrivate,
                        allowGuests = true,
                        onSuccess = { lobby ->
                            LobbyUiState.lobbyName.value = "New Lobby"
                            LobbyUiState.maxPlayers.intValue = lobby.settings.maxPlayers
                            LobbyUiState.roomCode.value = lobby.lobbyId.take(6).uppercase()
                            navController.navigate("waitingRoom/${lobby.lobbyId}")
                        },
                        onError = { }
                    )
                }
            )
        }

        // browse lobbies screen
        composable("browseLobbies") {
            BrowseLobbiesScreen(
                lobbies = listOf("Lobby A", "Lobby B", "Lobby C"), // dummy data
                onJoin = { lobby ->
                    // later Backend Join
                },
                onBack = { navController.popBackStack() }
            )
        }

        // waiting room screen
        composable("waitingRoom") {
            WaitingRoomScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate("settings") }
            )
        }

        // settings screen
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
