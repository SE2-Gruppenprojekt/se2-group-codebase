package at.se2group.backend.mapper

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbySettings
import at.se2group.backend.dto.LobbyListItemResponse
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.dto.LobbyResponse

fun LobbyEntity.toDomain(): Lobby =
    Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        currentPlayerCount = currentPlayerCount,
        status = status,
        settings = LobbySettings(
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            allowGuests = allowGuests
        ),
        createdAt = createdAt
    )

fun Lobby.toListItemResponse(): LobbyListItemResponse =
    LobbyListItemResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status.name,
        currentPlayerCount = currentPlayerCount,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate
    )

fun Lobby.toResponse(): LobbyResponse =
    LobbyResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status.name,
        currentPlayerCount = currentPlayerCount,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate,
        allowGuests = settings.allowGuests
    )
