package at.aau.serg.android.ui.screens.lobby.browse

import androidx.compose.ui.graphics.Color

data class LobbyBrowseItem(
    val lobbyId: String,
    val hostId: String,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val turnTimerSeconds: Int,
    val startingCards: Int,
    val isOpen: Boolean,
    val accentColor: Color
)
