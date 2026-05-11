package at.se2group.backend.mapper

import at.se2group.backend.dto.LobbyListItemResponse
import at.se2group.backend.dto.LobbyPlayerResponse
import at.se2group.backend.dto.LobbyResponse
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import java.time.Instant

fun LobbyEntity.toDomain(): Lobby =
    Lobby(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        players = players.map {
            LobbyPlayer(
                userId = it.userId,
                displayName = it.displayName,
                isReady = it.isReady
            )
        },
        status = status,
        settings = LobbySettings(
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            allowGuests = allowGuests
        )
    )

fun Lobby.toEntity(existing: LobbyEntity? = null): LobbyEntity =
    LobbyEntity(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status,
        maxPlayers = settings.maxPlayers,
        isPrivate = settings.isPrivate,
        allowGuests = settings.allowGuests,
        createdAt = existing?.createdAt ?: Instant.now(),
        players = players.map {
            val existingPlayer = existing?.players?.firstOrNull { persisted -> persisted.userId == it.userId }
            LobbyPlayerEmbeddable(
                userId = it.userId,
                displayName = it.displayName,
                isReady = it.isReady,
                joinedAt = existingPlayer?.joinedAt ?: Instant.now()
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
                displayName = it.displayName,
                isReady = it.isReady
            )
        }
    )
