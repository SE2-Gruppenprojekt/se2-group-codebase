package at.se2group.backend.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.LobbyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.persistence.LobbyEntity
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

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
        val lobbyEntity = LobbyEntity(
            lobbyId = UUID.randomUUID().toString(),
            hostUserId = userId,
            currentPlayerCount = 1,
            status = LobbyStatus.OPEN,
            maxPlayers = request.maxPlayers,
            isPrivate = request.isPrivate,
            allowGuests = request.allowGuests,
            createdAt = Instant.now()
        )

        return lobbyRepository.save(lobbyEntity).toDomain()
    }
    fun getLobby(lobbyId: String): Lobby {
        return lobbyRepository.findById(lobbyId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found") }
            .toDomain()
    }
}
