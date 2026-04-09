package at.aau.serg.android.data.lobby.mapper

import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
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
