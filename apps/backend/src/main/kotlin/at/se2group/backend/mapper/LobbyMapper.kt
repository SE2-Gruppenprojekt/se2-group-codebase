package at.se2group.backend.mapper

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyPlayer
import at.se2group.backend.domain.LobbySettings
import at.se2group.backend.dto.LobbyListItemResponse
import at.se2group.backend.dto.LobbyPlayerResponse
import at.se2group.backend.dto.LobbyResponse
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable

fun LobbyEntity.toDomain(): Lobby =
    Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        players = players.map {
            LobbyPlayer(
                userId = it.userId,
                displayName = it.displayName
            )
        },
        status = status,
        settings = LobbySettings(
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            allowGuests = allowGuests
        ),
        createdAt = createdAt
    )

fun Lobby.toEntity(): LobbyEntity =
    LobbyEntity(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate,
        allowGuests = settings.allowGuests,
        createdAt = createdAt,
        players = players.map {
            LobbyPlayerEmbeddable(
                userId = it.userId,
                displayName = it.displayName
            )
        }.toMutableList()
    )

fun Lobby.toListItemResponse(): LobbyListItemResponse =
    LobbyListItemResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status.name,
        currentPlayerCount = players.size,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate
    )

fun Lobby.toResponse(): LobbyResponse =
    LobbyResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status.name,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate,
        allowGuests = settings.allowGuests,
        players = players.map {
            LobbyPlayerResponse(
                userId = it.userId,
                displayName = it.displayName
            )
        }
    )
