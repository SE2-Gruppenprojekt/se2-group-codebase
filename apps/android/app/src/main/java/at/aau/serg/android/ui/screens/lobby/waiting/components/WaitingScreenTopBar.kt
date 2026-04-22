package at.aau.serg.android.ui.screens.lobby.waiting.components

import androidx.compose.runtime.Composable
import at.aau.serg.android.ui.components.TopBar

@Composable
fun WaitingScreenTopBar(
    lobbyName: String,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    TopBar(
        subtitle = lobbyName,
        onBack = onBack,
        onSettings = onSettings
    )
}
