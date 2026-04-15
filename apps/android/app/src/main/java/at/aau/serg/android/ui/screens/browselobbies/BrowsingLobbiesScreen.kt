package at.aau.serg.android.ui.screens.browselobbies.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import at.aau.serg.android.ui.screens.browselobbies.LobbyBrowseItem


@Composable
fun BrowsingLobbiesScreen(
    lobbies: List<LobbyBrowseItem>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onJoinLobby: (String) -> Unit,
    onCreateNewLobby: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit
) {
    var lobbyIdInput by remember { mutableStateOf("") }

    val enteredLobbyId = lobbyIdInput.trim().uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        LobbyHeader(
            onBack = onBack,
            onSettings = onSettings
        )

        Spacer(Modifier.height(18.dp))

        DirectJoinSection(
            lobbyIdInput = lobbyIdInput,
            onLobbyIdChange = { lobbyIdInput = it },
            onJoin = { onJoinLobby(enteredLobbyId) },
            enabled = enteredLobbyId.isNotEmpty()
        )

        Spacer(Modifier.height(18.dp))

        LobbyListHeader(
            count = lobbies.size
        )

        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(lobbies) { lobby ->
                LobbyBrowseCard(
                    lobby = lobby,
                    onJoinLobby = onJoinLobby
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        CreateLobbyButton(
            onClick = onCreateNewLobby
        )
    }
}
