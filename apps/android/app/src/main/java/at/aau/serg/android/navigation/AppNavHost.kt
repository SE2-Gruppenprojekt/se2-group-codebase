package at.aau.serg.android.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import shared.models.lobby.response.LobbyListItemResponse


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
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry("root")
            }
            val lobbyVM: LobbyViewModel = viewModel(parentEntry)
            val lobbySummaries by lobbyVM.lobbies.collectAsState()
            val browseLoadState by lobbyVM.loadState.collectAsState()

            LaunchedEffect(Unit) {
                lobbyVM.loadLobbies()
            }

            BrowsingLobbiesScreen(
                lobbies = lobbySummaries.map { it.toBrowseItem() },
                isLoading = browseLoadState is LoadState.Loading && lobbySummaries.isEmpty(),
                errorMessage = (browseLoadState as? LoadState.Error)?.message,
                onJoinLobby = { lobbyId ->
                    lobbyVM.joinLobbyOrOpen(
                        lobbyId = lobbyId,
                        onSuccess = { joinedLobby ->
                            navController.navigate("waitingRoom/${joinedLobby.lobbyId}")
                        },
                        onError = { }
                    )
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
                onGameStarted = { /* TODO: navController.navigate("game/$matchId") if Game-Screen finished */ },
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

private fun LobbyListItemResponse.toBrowseItem(): LobbyBrowseItem {
    return LobbyBrowseItem(
        lobbyId = lobbyId,
        hostId = hostUserId,
        currentPlayers = currentPlayerCount,
        maxPlayers = maxPlayers,
        // The current backend lobby list does not expose these fields yet.
        turnTimerSeconds = 60,
        startingCards = 7,
        isOpen = status == "OPEN" && currentPlayerCount < maxPlayers,
        accentColor = lobbyAccentColor(lobbyId)
    )
}

private fun lobbyAccentColor(lobbyId: String): Color {
    val palette = listOf(
        Color(0xFF3B82F6),
        Color(0xFFA855F7),
        Color(0xFF22C55E),
        Color(0xFFF97316),
        Color(0xFFEC4899),
        Color(0xFF06B6D4),
        Color(0xFFEAB308)
    )
    return palette[(lobbyId.hashCode() and Int.MAX_VALUE) % palette.size]
}
