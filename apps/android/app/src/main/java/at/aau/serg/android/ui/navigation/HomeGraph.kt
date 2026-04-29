package at.aau.serg.android.ui.navigation

import androidx.compose.runtime.LaunchedEffect
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
import at.aau.serg.android.ui.screens.auth.AuthEffect
import at.aau.serg.android.ui.screens.auth.AuthMode
import at.aau.serg.android.ui.screens.auth.AuthScreen
import at.aau.serg.android.ui.screens.auth.AuthViewModel
import at.aau.serg.android.ui.screens.game.GameScreen
import at.aau.serg.android.ui.screens.home.HomeEffect
import at.aau.serg.android.ui.screens.home.HomeScreen
import at.aau.serg.android.ui.screens.home.HomeViewModel
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseEffect
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseScreen
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseViewModel
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateEffect
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateScreen
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateViewModel
import at.aau.serg.android.ui.screens.lobby.waiting.LobbyWaitingEffect
import at.aau.serg.android.ui.screens.lobby.waiting.LobbyWaitingEvent
import at.aau.serg.android.ui.screens.lobby.waiting.LobbyWaitingScreen
import at.aau.serg.android.ui.screens.lobby.waiting.LobbyWaitingViewModel
import at.aau.serg.android.ui.screens.settings.SettingsEffect
import at.aau.serg.android.ui.screens.settings.SettingsScreen
import at.aau.serg.android.ui.screens.settings.SettingsViewModel

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    provider: DataStoreProvider
) {

    navigation(
        startDestination = Routes.HOME_SCREEN,
        route = Routes.HOME
    ) {

        composable(Routes.HOME_SCREEN) {
            val userStore = remember { provider.getStore<User>() }

            val vm: HomeViewModel = viewModel(
                factory = GenericViewModelFactory { HomeViewModel(userStore) }
            )

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        HomeEffect.NavigateToCreate ->
                            navController.navigate(Routes.CREATE_LOBBY_FANCY)
                        HomeEffect.NavigateToBrowse ->
                            navController.navigate(Routes.BROWSING_LOBBIES)
                        HomeEffect.NavigateToSettings ->
                            navController.navigate(Routes.SETTINGS)

                    }
                }
            }

            HomeScreen(viewModel = vm)
        }


        composable(Routes.SETTINGS) {
            val userStore = remember { provider.getStore<User>() }

            val vm: SettingsViewModel = viewModel(
                factory = GenericViewModelFactory { SettingsViewModel(userStore as UserStore) }
            )

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        SettingsEffect.NavigateChangeUsername ->
                            navController.navigate(Routes.CHANGE_USERNAME)

                        SettingsEffect.NavigateBack ->
                            navController.popBackStack()

                        SettingsEffect.Logout -> {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    }
                }
            }

            SettingsScreen(viewModel = vm)
        }

        composable(Routes.CHANGE_USERNAME) {
            val userStore = remember { provider.getStore<User>() }

            val vm: AuthViewModel = viewModel(
                factory = GenericViewModelFactory { AuthViewModel(userStore) }
            )

            LaunchedEffect(Unit) {
                vm.setMode(AuthMode.ChangeUsername)
            }

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        is AuthEffect.NavigateBack ->
                            navController.popBackStack()

                        AuthEffect.NavigateContinue ->
                            navController.popBackStack()
                    }
                }
            }

            AuthScreen(viewModel = vm)
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

                        LobbyCreateEffect.NavigateToSettings ->
                            navController.navigate(Routes.SETTINGS)

                        LobbyCreateEffect.NavigateBack ->
                            navController.popBackStack()
                    }
                }
            }

            LobbyCreateScreen(viewModel = vm)
        }

        composable(Routes.BROWSING_LOBBIES) {
            val userStore = remember { provider.getStore<User>() }
            val vm: LobbyBrowseViewModel = viewModel(
                factory = GenericViewModelFactory { LobbyBrowseViewModel(userStore) }
            )

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        is LobbyBrowseEffect.NavigateToWaitingRoom -> {
                            navController.navigate("${Routes.WAITING_ROOM}/${effect.lobbyId}")
                        }
                        LobbyBrowseEffect.NavigateToCreate ->
                            navController.navigate(Routes.CREATE_LOBBY_FANCY)
                        LobbyBrowseEffect.NavigateToSettings ->
                            navController.navigate(Routes.SETTINGS)
                        LobbyBrowseEffect.NavigateBack ->
                            navController.popBackStack()
                    }
                }
            }

            LobbyBrowseScreen(viewModel = vm)
        }

        composable("${Routes.WAITING_ROOM}/{lobbyId}") {
            val lobbyId = it.arguments?.getString("lobbyId")!!
            val userStore = remember { provider.getStore<User>() }

            val vm: LobbyWaitingViewModel = viewModel(
                factory = GenericViewModelFactory { LobbyWaitingViewModel(userStore) }
            )

            LaunchedEffect(lobbyId) {
                vm.onEvent(LobbyWaitingEvent.OnLoadLobby(lobbyId))
            }

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        is LobbyWaitingEffect.NavigateToMatch ->
                            navController.navigate("${Routes.GAME}/${effect.matchId}")

                        LobbyWaitingEffect.NavigateToSettings ->
                            navController.navigate(Routes.SETTINGS)

                        LobbyWaitingEffect.NavigateBack ->
                            navController.popBackStack()
                    }
                }
            }

            LobbyWaitingScreen(viewModel = vm)
        }
    }
}
