package at.aau.serg.android.ui.screens.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import at.aau.serg.android.ui.screens.lobby.components.LobbyContent


@Composable
fun LobbyScreen(
    navController: NavHostController,
    viewModel: LobbyViewModel,
    lobbyId: String
) {
    val lobbyState by viewModel.lobby.collectAsState()

    LaunchedEffect(lobbyId) {
        viewModel.loadLobby(lobbyId)
    }

    LobbyContent(
        lobbyState = lobbyState,
        onLeaveLobby = { id ->
            viewModel.leaveLobby(
                lobbyId = id,
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    )
}
