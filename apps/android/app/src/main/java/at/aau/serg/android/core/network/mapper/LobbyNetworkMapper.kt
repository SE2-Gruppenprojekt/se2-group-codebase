package at.aau.serg.android.core.network.mapper

import shared.models.lobby.domain.*
import shared.models.lobby.response.*

fun LobbyPlayerResponse.toDomain(): LobbyPlayer =
    LobbyPlayer(
        userId = userId,
        displayName = displayName,
        isReady = isReady
    )

fun LobbyResponse.toDomain(): Lobby =
    Lobby(
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
