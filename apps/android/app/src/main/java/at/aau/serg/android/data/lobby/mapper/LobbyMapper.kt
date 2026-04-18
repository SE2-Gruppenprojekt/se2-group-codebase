package at.aau.serg.android.data.lobby.mapper

import androidx.compose.ui.graphics.Color
import at.aau.serg.android.ui.screens.browselobbies.LobbyBrowseItem
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import shared.models.lobby.response.LobbyListItemResponse
import shared.models.lobby.response.LobbyPlayerResponse
import shared.models.lobby.response.LobbyResponse

fun LobbyPlayerResponse.toDomain(): LobbyPlayer {
    return LobbyPlayer(
        userId = userId,
        displayName = displayName,
        isReady = isReady
    )
}

fun LobbyResponse.toDomain(): Lobby {
    return Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        players = players.map { it.toDomain() },
        status = LobbyStatus.valueOf(status),
        settings = LobbySettings(
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            allowGuests = allowGuests
        )
    )
}

private fun lobbyAccentColor(lobbyId: String): Color {
    val palette = listOf(
        Color(0xFF3B82F6),
        Color(0xFFA855F7),
        Color(0xFF22C55E),
        Color(0xFFF97316),
        Color(0xFFEC4899),
        Color(0xFF06B6D4),
        Color(0xFFEAB308)
    )
    return palette[(lobbyId.hashCode() and Int.MAX_VALUE) % palette.size]
}

fun LobbyListItemResponse.toBrowseItem(): LobbyBrowseItem {
    return LobbyBrowseItem(
        lobbyId = lobbyId,
        hostId = hostUserId,
        currentPlayers = currentPlayerCount,
        maxPlayers = maxPlayers,
        turnTimerSeconds = 60,
        startingCards = 7,
        isOpen = status == "OPEN" && currentPlayerCount < maxPlayers,
        accentColor = lobbyAccentColor(lobbyId)
    )
}
