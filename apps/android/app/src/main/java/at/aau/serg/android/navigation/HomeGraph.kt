package at.aau.serg.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import at.aau.serg.android.data.lobby.mapper.toBrowseItem
import at.aau.serg.android.datastore.core.DataStoreProvider
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.lobby.LobbiesUiState
import at.aau.serg.android.ui.screens.browselobbies.components.BrowsingLobbiesScreen
import at.aau.serg.android.ui.screens.createlobby.CreateLobbyScreen
import at.aau.serg.android.ui.screens.createlobby.NewLobbyScreen
import at.aau.serg.android.ui.screens.game.GameScreen
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardViewModel
import at.aau.serg.android.ui.screens.lobby.LobbyScreen
import at.aau.serg.android.ui.screens.lobby.LobbyViewModel
import at.aau.serg.android.ui.screens.auth.AuthScreen
import at.aau.serg.android.ui.screens.auth.AuthViewModel
import at.aau.serg.android.ui.screens.settings.SettingsScreen
import at.aau.serg.android.ui.screens.settings.SettingsViewModel
import at.aau.serg.android.ui.screens.waiting.WaitingRoomScreen
import at.aau.serg.android.ui.theme.ThemeState

fun NavGraphBuilder.homeGraph(navController: NavHostController) {

    navigation(
        startDestination = Routes.HOME_SCREEN,
        route = Routes.HOME
    ) {

        composable(Routes.HOME_SCREEN) {

            val parent = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }

            val leaderboardVM: LeaderboardViewModel = viewModel(parent)

            val state by leaderboardVM.loadState.collectAsState()

            HomeScreen(
                state = state,
                onNewLobbyScreen = {
                    navController.navigate(Routes.CREATE_LOBBY_FANCY)
                },
                onBrowseFancyLobbies = {
                    navController.navigate(Routes.BROWSING_LOBBIES)
                },
                onShowLeaderboard = {
                    leaderboardVM.loadLeaderboard(
                        onSuccess = { navController.navigate(Routes.LEADERBOARD) },
                        onError = {}
                    )
                },
                onSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {

            val dataStoreProvider =
                DataStoreProvider.getInstance(navController.context)

            val userStore = dataStoreProvider.userStore

            val user by userStore.data.collectAsState(
                initial = User.getDefaultInstance()
            )

            val vm: SettingsViewModel = viewModel()

            SettingsScreen(
                user = user,
                onChangeUsername = {
                    navController.navigate(Routes.CHANGE_USERNAME)
                },
                onBack = {
                    navController.popBackStack()
                },
                isDarkMode = ThemeState.isDarkMode.value,
                onToggleDarkMode = { ThemeState.isDarkMode.value = it },
                onLogout = {
                    vm.logout {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.CHANGE_USERNAME) {
            val vm: AuthViewModel = viewModel()
            AuthScreen(
                viewModel = vm,
                onContinue = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LEADERBOARD) {

            val parent = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }

            val vm: LeaderboardViewModel = viewModel(parent)
            val players by vm.players.collectAsState()

            LeaderboardScreen(
                players = players,
                onBack = { navController.popBackStack() }
            )
        }

        composable("${Routes.LOBBY}/{lobbyId}") {
            val vm: LobbyViewModel = viewModel()

            LobbyScreen(
                navController = navController,
                viewModel = vm,
                lobbyId = it.arguments?.getString("lobbyId")!!
            )
        }

        composable("${Routes.WAITING_ROOM}/{lobbyId}") {
            val vm: LobbyViewModel = viewModel()

            WaitingRoomScreen(
                onBack = { navController.popBackStack() },
                onGameStarted = { matchId ->
                    navController.navigate("${Routes.GAME}/$matchId")
                },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                lobbyId = it.arguments?.getString("lobbyId")!!,
                viewModel = vm
            )
        }

        composable("${Routes.GAME}/{matchId}") {
            GameScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.CREATE_LOBBY) {
            CreateLobbyScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }

        composable(Routes.CREATE_LOBBY_FANCY) {

            val parent = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }

            val lobbyVM: LobbyViewModel = viewModel(parent)

            val dataStoreProvider = remember {
                DataStoreProvider.getInstance(navController.context)
            }

            val userStore = dataStoreProvider.userStore

            val user by userStore.data.collectAsState(
                initial = User.getDefaultInstance()
            )

            NewLobbyScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                isLoading = false,
                onCreateLobby = { maxPlayers, isPrivate ->
                    lobbyVM.createLobby(
                        userId = user.uid,
                        displayName = user.displayName,
                        maxPlayers = maxPlayers,
                        isPrivate = isPrivate,
                        allowGuests = true,
                        onSuccess = {
                            navController.navigate("${Routes.WAITING_ROOM}/${it.lobbyId}")
                        },
                        onError = {}
                    )
                }
            )
        }

        composable(Routes.BROWSING_LOBBIES) {

            val parent = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }

            val lobbyVM: LobbyViewModel = viewModel(parent)

            val dataStoreProvider = remember {
                DataStoreProvider.getInstance(navController.context)
            }

            val userStore = dataStoreProvider.userStore

            val user by userStore.data.collectAsState(
                initial = User.getDefaultInstance()
            )

            val lobbySummaries by lobbyVM.lobbies.collectAsState()
            val loadState by lobbyVM.lobbiesState.collectAsState()

            LaunchedEffect(Unit) {
                lobbyVM.loadLobbies()
            }

            BrowsingLobbiesScreen(
                lobbies = lobbySummaries.map { it.toBrowseItem() },
                isLoading = loadState is LobbiesUiState.Loading,
                errorMessage = (loadState as? LobbiesUiState.Error)?.message,
                onJoinLobby = { lobbyId ->
                    lobbyVM.joinLobbyOrOpen(
                        lobbyId = lobbyId,
                        userId = user.uid,
                        displayName = user.displayName,
                        onSuccess = {
                            navController.navigate("${Routes.WAITING_ROOM}/${it.lobbyId}")
                        },
                        onError = {}
                    )
                },
                onCreateNewLobby = {
                    navController.navigate(Routes.CREATE_LOBBY_FANCY)
                },
                onSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
