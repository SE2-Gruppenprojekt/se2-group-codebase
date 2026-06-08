package at.aau.serg.android.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import at.aau.serg.android.ui.screens.game.GameEffect
import at.aau.serg.android.ui.screens.game.GameResultPlayerSummary
import at.aau.serg.android.ui.screens.game.GameResultScreen
import at.aau.serg.android.ui.screens.game.GameResultUiModel
import at.aau.serg.android.ui.screens.game.GameScreen
import at.aau.serg.android.ui.screens.game.GameUIEvent
import at.aau.serg.android.ui.screens.game.GameViewModel
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

            // DEBUG ONLY — remove before release
            Box(Modifier.fillMaxSize()) {
                HomeScreen(viewModel = vm)
                // DEBUG ONLY — remove before release
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { navController.navigate("debug_result_preview/winner") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                    ) {
                        Text("🏆 Test Result (Winner)", color = Color.White)
                    }
                    Button(
                        onClick = { navController.navigate("debug_result_preview/loser") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF444444))
                    ) {
                        Text("💀 Test Result (Loser)", color = Color.White)
                    }
                }
            }
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

        composable("${Routes.GAME}/{gameId}") {
            val gameId = it.arguments?.getString("gameId")!!
            val userStore = remember { provider.getStore<User>() }

            val vm: GameViewModel = viewModel(
                factory = GenericViewModelFactory { GameViewModel(userStore) }
            )

            LaunchedEffect(gameId) {
                vm.onUIEvent(GameUIEvent.OnLoadGame(gameId))
            }

            LaunchedEffect(Unit) {
                vm.effects.collect { effect ->
                    when (effect) {
                        GameEffect.NavigateToSettings ->
                            navController.navigate(Routes.SETTINGS)

                        GameEffect.NavigateBack ->
                            navController.popBackStack()

                        is GameEffect.NavigateToResult ->
                            navController.navigate("${Routes.GAME_RESULT}/${effect.winnerUserId}")
                    }
                }
            }

            GameScreen(viewModel = vm)
        }

        composable("${Routes.GAME_RESULT}/{winnerId}") { backStackEntry ->
            val winnerId = backStackEntry.arguments?.getString("winnerId").orEmpty()
            val userStore = remember { provider.getStore<User>() }

            // Reuse the GameViewModel from the game screen (still in backstack)
            // so the full result data (players, scores) is available
            val gameBackStackEntry = remember(backStackEntry) {
                navController.previousBackStackEntry
            }
            val vm: GameViewModel = if (gameBackStackEntry != null) {
                viewModel(gameBackStackEntry, factory = GenericViewModelFactory { GameViewModel(userStore) })
            } else {
                viewModel(factory = GenericViewModelFactory { GameViewModel(userStore) })
            }
            val uiState by vm.uiState.collectAsState()

            GameResultScreen(
                gameResult = uiState.gameResult,
                currentUserId = uiState.user?.uid,
                onNavigateHome = {
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(Routes.HOME_SCREEN) { inclusive = true }
                    }
                }
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

        // DEBUG ONLY — remove before release
        composable("debug_result_preview/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role")
            val fakeResult = GameResultUiModel(
                winnerUserId = "u1",
                players = listOf(
                    GameResultPlayerSummary(
                        userId = "u1", displayName = "ShadowNinja", score = 245,
                        finishPosition = 1, tilesPlayed = 50, meldsCreated = 8,
                        turnsCompleted = 47, pointsFromTiles = 291,
                        remainingTiles = 0, penaltyPoints = 0, isStillPlaying = false
                    ),
                    GameResultPlayerSummary(
                        userId = "u2", displayName = "TeamGameerfood", score = 127,
                        finishPosition = 2, tilesPlayed = 30, meldsCreated = 5,
                        turnsCompleted = 40, pointsFromTiles = 155,
                        remainingTiles = 14, penaltyPoints = 28, isStillPlaying = false
                    ),
                    GameResultPlayerSummary(
                        userId = "u3", displayName = "Player3", score = 89,
                        finishPosition = 3, tilesPlayed = 20, meldsCreated = 3,
                        turnsCompleted = 30, pointsFromTiles = 125,
                        remainingTiles = 18, penaltyPoints = 36, isStillPlaying = true
                    ),
                    GameResultPlayerSummary(
                        userId = "u4", displayName = "Player4", score = 63,
                        finishPosition = 4, tilesPlayed = 15, meldsCreated = 2,
                        turnsCompleted = 25, pointsFromTiles = 63,
                        remainingTiles = 22, penaltyPoints = 0, isStillPlaying = true
                    )
                ),
                matchDuration = "3:45",
                totalTurns = 47
            )
            GameResultScreen(
                gameResult = fakeResult,
                currentUserId = if (role == "winner") "u1" else "u2",
                onNavigateHome = { navController.popBackStack() }
            )
        }
    }
}
