package at.aau.serg.android.ui.screens.browselobbies

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.browselobbies.components.*

@Composable
fun BrowseLobbiesScreen(
    lobbies: List<String>,
    onJoin: (String) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // HEADER COMPONENT
        BrowseLobbiesHeader()

        Spacer(Modifier.height(16.dp))

        // CONTENT
        if (lobbies.isEmpty()) {
            LobbyEmptyState()
        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lobbies) { lobby ->
                    LobbyItem(
                        lobby = lobby,
                        onJoin = onJoin
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
