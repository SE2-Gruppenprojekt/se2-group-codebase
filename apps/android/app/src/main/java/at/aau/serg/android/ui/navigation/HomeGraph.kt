package at.aau.serg.android.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import at.aau.serg.android.core.datastore.DataStoreProvider
import at.aau.serg.android.core.datastore.getStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.util.GenericViewModelFactory
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.auth.AuthScreen
import at.aau.serg.android.ui.screens.auth.AuthViewModel
import at.aau.serg.android.ui.screens.game.GameScreen
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.home.HomeViewModel
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardScreen
import at.aau.serg.android.ui.screens.leaderboard.LeaderboardViewModel
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseScreen
import at.aau.serg.android.ui.screens.lobby.browse.toUi
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateViewModel
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateEffect
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateScreen
import at.aau.serg.android.ui.screens.lobby.main.LobbyViewModel
import at.aau.serg.android.ui.screens.lobby.waiting.WaitingRoomScreen
import at.aau.serg.android.ui.screens.settings.SettingsScreen
import at.aau.serg.android.ui.screens.settings.SettingsViewModel
import at.aau.serg.android.ui.theme.ThemeState

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    provider: DataStoreProvider
) {

    navigation(
        startDestination = Routes.HOME_SCREEN,
        route = Routes.HOME
    ) {

        composable(Routes.HOME_SCREEN) {

            val parent = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }

            val leaderboardVM: LeaderboardViewModel = viewModel(parent)

            val userStore = remember { provider.getStore<User>() }

            val homeVM: HomeViewModel = viewModel(
                factory = GenericViewModelFactory { HomeViewModel(userStore) }
            )

            HomeScreen(
                viewModel = homeVM,
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
            val userStore = remember { provider.getStore<User>() }

            val vm: SettingsViewModel = viewModel(
                factory = GenericViewModelFactory { SettingsViewModel(userStore as UserStore) }
            )

            SettingsScreen(
                viewModel = vm,
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
            val userStore = remember { provider.getStore<User>() }

            val vm: AuthViewModel = viewModel(
                factory = GenericViewModelFactory { AuthViewModel(userStore) }
            )
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

        composable("${Routes.WAITING_ROOM}/{lobbyId}") {
            val vm: LobbyViewModel = viewModel()

            WaitingRoomScreen(
                onBack = { navController.popBackStack() },
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

        composable(Routes.CREATE_LOBBY_FANCY) {
            val userStore = remember { provider.getStore<User>() }

            val vm: LobbyCreateViewModel = viewModel(
                factory = GenericViewModelFactory { LobbyCreateViewModel(userStore) }
            )

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        is LobbyCreateEffect.NavigateToWaitingRoom ->
                            navController.navigate("${Routes.WAITING_ROOM}/${effect.lobbyId}")
                    }
                }
            }

            LobbyCreateScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.BROWSING_LOBBIES) {

            val parent = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }

            val lobbyVM: LobbyViewModel = viewModel(parent)

            val userStore = remember { provider.getStore<User>() }

            val user by userStore.data.collectAsState(
                initial = User.getDefaultInstance()
            )

            val lobbies by lobbyVM.lobbies.collectAsState()
            val isLoading by lobbyVM.isLoadingLobbies.collectAsState()
            val errorMessage by lobbyVM.lobbiesError.collectAsState()

            LaunchedEffect(Unit) {
                lobbyVM.loadLobbies()
            }

            LobbyBrowseScreen(
                lobbies = lobbies.map { it.toUi() },
                isLoading = isLoading,
                errorMessage = errorMessage,
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
