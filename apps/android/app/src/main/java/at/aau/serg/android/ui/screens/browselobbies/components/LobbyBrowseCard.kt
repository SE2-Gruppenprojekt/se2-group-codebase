package at.aau.serg.android.ui.screens.browselobbies.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import at.aau.serg.android.ui.screens.browselobbies.LobbyBrowseItem

@Composable
fun LobbyBrowseCard(
    lobby: LobbyBrowseItem,
    onJoinLobby: (String) -> Unit
) {
    Card {
        Row {

            Column {
                Text("#${lobby.lobbyId}")
                Text("${lobby.turnTimerSeconds}s")
                Text("${lobby.startingCards} cards")
            }

            Column {
                Text("${lobby.currentPlayers}/${lobby.maxPlayers}")

                Button(
                    onClick = { onJoinLobby(lobby.lobbyId) },
                    enabled = lobby.isOpen
                ) {
                    Text(if (lobby.isOpen) "Join" else "Full")
                }
            }
        }
    }
}
