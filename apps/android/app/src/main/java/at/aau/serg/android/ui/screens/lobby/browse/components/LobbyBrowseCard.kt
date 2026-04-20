package at.aau.serg.android.ui.screens.lobby.browse.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseItem

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
                    enabled = lobby.isOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9D3CFF),
                        contentColor = Color.White
                    )
                ) {
                    Text(if (lobby.isOpen) "Join" else "Full")
                }
            }
        }
    }
}
