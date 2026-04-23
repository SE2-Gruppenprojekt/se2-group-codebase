package at.aau.serg.android.ui.screens.lobby.browse.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LobbyItem(
    lobby: String,
    onJoin: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = lobby,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(onClick = { onJoin(lobby) }) {
                Text("Join")
            }
        }
    }
}
