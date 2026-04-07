package at.aau.serg.android.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import at.aau.serg.android.util.findActivity
import at.aau.serg.android.ui.state.LoadState   // ← NEW IMPORT

@Composable
fun HomeScreen(
    state: LoadState,   // ← UPDATED TYPE
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
        // Loading indicator
        if (state is LoadState.Loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }

        // Error message
        if (state is LoadState.Error) {
            Text(
                text = state.message,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
        }

        Button(onClick = onCreateLobby) {
            Text("Create Lobby")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onBrowseLobbies) {
            Text("Browse Lobbies")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onShowLeaderboard,
            enabled = state !is LoadState.Loading   // ← UPDATED
        ) {
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
