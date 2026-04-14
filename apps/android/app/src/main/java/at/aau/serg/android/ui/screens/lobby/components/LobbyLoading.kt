package at.aau.serg.android.ui.screens.lobby.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LobbyLoading() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(12.dp))
    Text("Loading lobby...")
}
