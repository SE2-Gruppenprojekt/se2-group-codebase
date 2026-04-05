package at.se2group.backend.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyPlayer
import at.se2group.backend.domain.LobbySettings
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.LobbyRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class LobbyService(
    private val lobbyRepository: LobbyRepository
) {

    fun listOpenLobbies(): List<Lobby> {
        return lobbyRepository.findAllByStatus(LobbyStatus.OPEN)
            .map { it.toDomain() }
    }

    @Transactional
    fun createLobby(userId: String, request: CreateLobbyRequest): Lobby {
        val lobby = Lobby(
            lobbyId = UUID.randomUUID().toString(),
            hostUserId = userId,
            players = listOf(
                LobbyPlayer(
                    userId = userId,
                    displayName = request.displayName
                )
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = request.maxPlayers,
                isPrivate = request.isPrivate,
                allowGuests = request.allowGuests
            ),
            createdAt = Instant.now()
        )

        return lobbyRepository.save(lobby.toEntity()).toDomain()
    }

    fun getLobby(lobbyId: String): Lobby {
        return lobbyRepository.findById(lobbyId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found")
            }
            .toDomain()
    }

    @Transactional
    fun joinLobby(lobbyId: String, request: JoinLobbyRequest): Lobby {
        val lobby = getLobby(lobbyId)

        if (lobby.status != LobbyStatus.OPEN) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby is not open")
        }

        if (lobby.players.size >= lobby.settings.maxPlayers) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby is full")
        }

        if (lobby.players.any { it.userId == request.userId }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player already in lobby")
        }

        val updatedLobby = lobby.copy(
            players = lobby.players + LobbyPlayer(
                userId = request.userId,
                displayName = request.displayName
            )
        )

        return lobbyRepository.save(updatedLobby.toEntity()).toDomain()
    }
}
