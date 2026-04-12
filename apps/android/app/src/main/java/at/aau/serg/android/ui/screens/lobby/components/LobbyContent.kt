package at.aau.serg.android.ui.screens.lobby.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.lobby.LobbyUiState

@Composable
fun LobbyContent(
    state: LobbyUiState,
    onLeaveLobby: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (state) {

            is LobbyUiState.Loading -> {
                LobbyLoading()
            }

            is LobbyUiState.Error -> {
                LobbyError(message = state.message)
            }

            is LobbyUiState.Success -> {
                LobbyDetails(
                    lobby = state.lobby,
                    onLeaveLobby = onLeaveLobby
                )
            }
        }
    }
}

@Composable
fun LobbyError(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error
    )
}
