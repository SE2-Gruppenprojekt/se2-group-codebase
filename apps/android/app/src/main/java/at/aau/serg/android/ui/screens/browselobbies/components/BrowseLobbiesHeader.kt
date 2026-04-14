package at.aau.serg.android.ui.screens.browselobbies.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BrowseLobbiesHeader() {
    Text(
        text = "Browse Lobbies",
        style = MaterialTheme.typography.headlineMedium
    )
}
