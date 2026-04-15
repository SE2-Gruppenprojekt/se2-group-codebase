package at.aau.serg.android.ui.screens.lobby

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import at.aau.serg.android.ui.screens.lobby.components.LobbyContent
import shared.models.lobby.domain.Lobby

@Composable
fun LobbyScreen(
    navController: NavHostController,
    viewModel: LobbyViewModel,
    lobbyId: String
) {
    val lobbyState by viewModel.lobby.collectAsState()

    // Lädt die Lobby-Daten beim ersten Laden
    LaunchedEffect(lobbyId) {
        viewModel.loadLobby(lobbyId)
    }

    // Hier rufst du die externe LobbyContent Composable auf
    LobbyContent(
        lobby = lobbyState,       // <- hier Lobby statt state
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
