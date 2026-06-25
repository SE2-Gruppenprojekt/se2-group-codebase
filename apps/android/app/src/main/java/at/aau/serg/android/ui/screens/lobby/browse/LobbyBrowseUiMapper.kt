package at.aau.serg.android.ui.screens.lobby.browse

import androidx.compose.ui.graphics.Color
import at.aau.serg.android.ui.theme.AccentBlue
import at.aau.serg.android.ui.theme.AccentPurple
import shared.models.lobby.response.LobbyListItemResponse

private val lobbyAccentPalette = listOf(AccentPurple, AccentBlue)

fun LobbyListItemResponse.toUiOrNull(index: Int = 0): LobbyBrowseItem? {
    if (lobbyId.toIntOrNull() == null) return null
    if (status != "OPEN") return null
    if (currentPlayerCount >= maxPlayers) return null

    return LobbyBrowseItem(
        lobbyId = lobbyId,
        hostId = hostUserId,
        currentPlayers = currentPlayerCount,
        maxPlayers = maxPlayers,
        turnTimerSeconds = 60,
        startingCards = 7,
        isOpen = true,
        accentColor = lobbyAccentPalette[index % lobbyAccentPalette.size]
    )
}
