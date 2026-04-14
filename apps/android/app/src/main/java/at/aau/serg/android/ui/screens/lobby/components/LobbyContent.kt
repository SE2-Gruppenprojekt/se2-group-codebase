package at.aau.serg.android.ui.screens.lobby.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import shared.models.lobby.domain.Lobby

@Composable
fun LobbyContent(
    lobby: Lobby?,
    onLeaveLobby: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lobby == null) {
            LobbyLoadingContent()
        } else {
            LobbyDetailsContent(
                lobby = lobby,
                onLeaveLobby = onLeaveLobby
            )
        }
    }
}

@Composable
fun LobbyErrorContent(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
fun LobbyLoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Loading lobby…", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator()
    }
}

@Composable
fun LobbyDetailsContent(
    lobby: Lobby,
    onLeaveLobby: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lobby: ${lobby.lobbyId}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Players: ${lobby.players.joinToString { it.displayName }}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onLeaveLobby(lobby.lobbyId) }
        ) {
            Text("Leave Lobby")
        }
    }
}
