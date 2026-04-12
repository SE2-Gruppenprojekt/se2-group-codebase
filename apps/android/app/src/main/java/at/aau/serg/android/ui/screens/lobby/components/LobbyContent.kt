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
    lobbyState: Lobby?,
    onLeaveLobby: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (lobbyState) {
            null -> LobbyLoading()

            else -> LobbyDetails(
                lobby = lobbyState,
                onLeaveLobby = onLeaveLobby
            )
        }
    }
}
