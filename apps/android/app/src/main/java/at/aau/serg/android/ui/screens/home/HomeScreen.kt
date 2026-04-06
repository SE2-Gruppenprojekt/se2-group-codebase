package at.aau.serg.android.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import at.aau.serg.android.util.findActivity

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCreateLobby: () -> Unit,
    onBrowseLobbies: () -> Unit,
    onShowLeaderboard: () -> Unit,
    onSettings: () -> Unit
) {
    val activity = LocalContext.current.findActivity()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onCreateLobby) {
            Text("Create Lobby")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onBrowseLobbies) {
            Text("Browse Lobbies")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onShowLeaderboard) {
            Text("Leaderboard")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onSettings) {
            Text("Settings")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = { activity?.finish() }) {
            Text("Exit")
        }
    }
}
