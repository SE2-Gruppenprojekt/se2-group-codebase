package at.aau.serg.android.ui.screens.waiting.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoomInfoCard(
    roomCode: String,
    joinedCount: Int,
    maxPlayers: Int,
    onCopy: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text("Room Code")

                Row {
                    Text(roomCode)

                    IconButton(onClick = onCopy) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                }
            }

            Column {
                Text("Players")
                Text("$joinedCount/$maxPlayers")
            }
        }
    }
}
