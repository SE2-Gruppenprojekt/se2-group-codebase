package at.aau.serg.android.ui.screens.lobby.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shared.models.lobby.domain.Lobby

@Composable
fun LobbyDetails(
    lobby: Lobby,
    onLeaveLobby: (String) -> Unit
) {
    Text(
        text = "Lobby ID: ${lobby.lobbyId}",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onLeaveLobby(lobby.lobbyId) }
    ) {
        Text("Leave Lobby")
    }
}
