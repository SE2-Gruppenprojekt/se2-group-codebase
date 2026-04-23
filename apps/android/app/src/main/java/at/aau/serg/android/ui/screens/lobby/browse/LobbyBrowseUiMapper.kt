package at.aau.serg.android.ui.screens.lobby.browse

import androidx.compose.ui.graphics.Color
import shared.models.lobby.response.LobbyListItemResponse

private fun accentColorForLobby(id: String): Color {
    val palette = listOf(
        Color(0xFF3B82F6),
        Color(0xFFA855F7),
        Color(0xFF22C55E),
        Color(0xFFF97316),
        Color(0xFFEC4899),
        Color(0xFF06B6D4),
        Color(0xFFEAB308)
    )
    return palette[(id.hashCode() and Int.MAX_VALUE) % palette.size]
}

fun LobbyListItemResponse.toUi(): LobbyBrowseItem =
    LobbyBrowseItem(
        lobbyId = lobbyId,
        hostId = hostUserId,
        currentPlayers = currentPlayerCount,
        maxPlayers = maxPlayers,
        turnTimerSeconds = 60,
        startingCards = 7,
        isOpen = status == "OPEN" && currentPlayerCount < maxPlayers,
        accentColor = accentColorForLobby(lobbyId)
    )
