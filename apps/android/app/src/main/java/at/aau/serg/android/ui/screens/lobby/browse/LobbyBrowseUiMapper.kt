package at.aau.serg.android.ui.screens.lobby.browse

import androidx.compose.ui.graphics.Color
import at.aau.serg.android.ui.theme.AccentBlue
import at.aau.serg.android.ui.theme.AccentPurple
import shared.models.lobby.response.LobbyListItemResponse

private val lobbyAccentPalette = listOf(AccentPurple, AccentBlue)

fun LobbyListItemResponse.toUi(index: Int): LobbyBrowseItem =
    LobbyBrowseItem(
        lobbyId = lobbyId,
        hostId = hostUserId,
        currentPlayers = currentPlayerCount,
        maxPlayers = maxPlayers,
        turnTimerSeconds = 60,
        startingCards = 7,
        isOpen = status == "OPEN" && currentPlayerCount < maxPlayers,
        accentColor = lobbyAccentPalette[index % lobbyAccentPalette.size]
    )
