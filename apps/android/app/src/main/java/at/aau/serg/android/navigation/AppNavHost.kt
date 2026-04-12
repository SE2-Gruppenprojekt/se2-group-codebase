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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import at.aau.serg.android.ui.screens.browselobbies.BrowsingLobbiesScreen
import at.aau.serg.android.ui.screens.browselobbies.LobbyBrowseItem


@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        route = "root",
        modifier = Modifier.padding(innerPadding)
    ) {

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
                onBrowseFancyLobbies = { navController.navigate("browsingLobbies") },
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
                isLoading = lobbyLoadState is LoadState.Loading,
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

        // second browse screen
        composable("browsingLobbies") {
            BrowsingLobbiesScreen(
                lobbies = listOf(
                    LobbyBrowseItem(
                        lobbyId = "A1B2C3",
                        hostId = "Miko",
                        currentPlayers = 3,
                        maxPlayers = 4,
                        turnTimerSeconds = 60,
                        startingCards = 7,
                        isOpen = true,
                        accentColor = Color(0xFF3B82F6)
                    ),
                    LobbyBrowseItem(
                        lobbyId = "D4E5F6",
                        hostId = "Sarah",
                        currentPlayers = 2,
                        maxPlayers = 6,
                        turnTimerSeconds = 90,
                        startingCards = 14,
                        isOpen = true,
                        accentColor = Color(0xFFA855F7)
                    ),
                    LobbyBrowseItem(
                        lobbyId = "G7H8J9",
                        hostId = "Alex",
                        currentPlayers = 1,
                        maxPlayers = 4,
                        turnTimerSeconds = 30,
                        startingCards = 7,
                        isOpen = true,
                        accentColor = Color(0xFF22C55E)
                    ),
                    LobbyBrowseItem(
                        lobbyId = "K1L2M3",
                        hostId = "David",
                        currentPlayers = 4,
                        maxPlayers = 4,
                        turnTimerSeconds = 45,
                        startingCards = 7,
                        isOpen = false,
                        accentColor = Color(0xFFF97316)
                    ),
                    LobbyBrowseItem(
                        lobbyId = "N4P5Q6",
                        hostId = "Emma",
                        currentPlayers = 0,
                        maxPlayers = 4,
                        turnTimerSeconds = 120,
                        startingCards = 10,
                        isOpen = true,
                        accentColor = Color(0xFFEC4899)
                    )
                ),
                onJoinLobby = { _ ->
                    // later real join logic
                },
                onCreateNewLobby = {
                    navController.navigate("createLobbyFancy")
                },
                onSettings = {
                    navController.navigate("settings")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("waitingRoom") {
            WaitingRoomScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate("settings") }
            )
        }

        composable("waitingRoom/{lobbyId}") { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId")!!
            val vm: LobbyViewModel = viewModel()

            WaitingRoomScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate("settings") },
                lobbyId = lobbyId,
                viewModel = vm
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
