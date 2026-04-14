package at.aau.serg.android.ui.screens.createlobby.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun CreateLobbyHeader() {
    Text(
        text = "Create Lobby",
        style = MaterialTheme.typography.headlineMedium
    )
}
