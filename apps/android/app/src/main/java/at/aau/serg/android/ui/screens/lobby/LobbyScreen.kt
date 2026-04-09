package at.aau.serg.android.ui.screens.lobby

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

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

    when (val lobby = lobbyState) {
        null -> Text("Loading lobby...")
        else -> {
            Column(Modifier.fillMaxSize()) {
                Text("Lobby ID: ${lobby.lobbyId}")

                Button(
                    onClick = {
                        viewModel.leaveLobby(
                            lobbyId = lobby.lobbyId,
                            onSuccess = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                ) {
                    Text("Leave Lobby")
                }
            }
        }
    }
}

