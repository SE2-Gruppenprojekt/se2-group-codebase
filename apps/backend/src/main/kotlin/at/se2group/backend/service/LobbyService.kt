package at.se2group.backend.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyPlayer
import at.se2group.backend.domain.LobbySettings
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.LobbyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional(readOnly = true)
class LobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyBroadcastService: LobbyBroadcastService,
    private val gameInitializationService: GameInitializationService) {

    companion object {
        const val MAX_PLAYERS = 8
        const val MIN_PLAYERS = 2
    }

    fun listOpenLobbies(): List<Lobby> {
        return lobbyRepository.findAllByStatus(LobbyStatus.OPEN)
            .map { it.toDomain() }
    }

    @Transactional
    fun createLobby(userId: String, request: CreateLobbyRequest): Lobby {
        if (request.maxPlayers < MIN_PLAYERS || request.maxPlayers > MAX_PLAYERS) {
            throw IllegalArgumentException("maxPlayers must be between ${MIN_PLAYERS} and ${MAX_PLAYERS}")
        }

        val lobby = Lobby(
            lobbyId = (100000..999999).random().toString(),
            hostUserId = userId,
            players = listOf(
                LobbyPlayer(
                    userId = userId,
                    displayName = request.displayName,
                    isReady = false,
                    joinedAt = Instant.now()
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

        val saved = lobbyRepository.save(lobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun getLobby(lobbyId: String): Lobby {
        return lobbyRepository.findById(lobbyId)
            .orElseThrow {
                NoSuchElementException("Lobby not found")
            }
            .toDomain()
    }

    @Transactional
    fun joinLobby(lobbyId: String, request: JoinLobbyRequest): Lobby {
        val lobby = getLobby(lobbyId)

        if (lobby.status != LobbyStatus.OPEN) {
            throw IllegalStateException("Lobby is not open")
        }

        if (lobby.players.size >= lobby.settings.maxPlayers) {
            throw IllegalStateException("Lobby is full")
        }

        if (lobby.players.any { it.userId == request.userId }) {
            throw IllegalStateException("Player already in lobby")
        }

        val updatedLobby = lobby.copy(
            players = lobby.players + LobbyPlayer(
                userId = request.userId,
                displayName = request.displayName,
                isReady = false,
                joinedAt = Instant.now()
            )
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    @Transactional
    fun updateLobbySettings(lobbyId: String, userId: String, request: UpdateLobbySettingsRequest): Lobby {
        val lobby = getLobby(lobbyId)

        if (lobby.hostUserId != userId) {
            throw SecurityException("Only the host can update lobby settings")
        }

        if (lobby.status != LobbyStatus.OPEN) {
            throw IllegalStateException("Lobby settings can only be changed while the lobby is open")
        }

        if (request.maxPlayers !in maxOf(MIN_PLAYERS, lobby.players.size)..MAX_PLAYERS) {
            throw IllegalArgumentException("Maximum players must be between ${maxOf(MIN_PLAYERS, lobby.players.size)} and ${MAX_PLAYERS}")
        }

        val updatedLobby = lobby.copy(
            settings = LobbySettings(
                maxPlayers = request.maxPlayers,
                isPrivate = request.isPrivate,
                allowGuests = request.allowGuests
            )
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    @Transactional
    fun startLobby(lobbyId: String, userId: String): Lobby {
        val lobby = getLobby(lobbyId)

        if (lobby.hostUserId != userId) {
            throw SecurityException("Only the host can start the match")
        }

        if (lobby.status != LobbyStatus.OPEN) {
            throw IllegalStateException("Match can only be started while the lobby is open")
        }

        if (lobby.players.size < MIN_PLAYERS) {
            throw IllegalStateException("At least ${MIN_PLAYERS} players are required to start the match")
        }

        if (lobby.players.any { !it.isReady }) {
            throw IllegalStateException("All players must be ready to start the match")
        }

        val updatedLobby = lobby.copy(
            status = LobbyStatus.IN_GAME
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity()).toDomain()

        lobbyBroadcastService.broadcastLobbyStarted(saved.lobbyId, saved.lobbyId)
        return saved
    }

    @Transactional
    fun leaveLobby(lobbyId: String, userId: String): Lobby? {
        val lobby = getLobby(lobbyId)

        if(lobby.status != LobbyStatus.OPEN) {
            throw IllegalStateException("Cannot leave an unopen lobby")
        }

        if (lobby.players.none { it.userId == userId }) {
            throw IllegalArgumentException("No player found in this lobby")
        }

        val remainingPlayers = lobby.players.filter { it.userId != userId }

        if (remainingPlayers.isEmpty()) {
            lobbyRepository.deleteById(lobbyId)
            lobbyBroadcastService.broadcastLobbyDeleted(lobbyId)
            return null
        }

        val nextHostId = if (lobby.hostUserId == userId) {
            remainingPlayers.first().userId
        } else {
            lobby.hostUserId
        }

        val updatedLobby = lobby.copy(
            hostUserId = nextHostId,
            players = lobby.players.filter { it.userId != userId }
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    @Transactional
    fun readyLobby(lobbyId: String, userId: String): Lobby {
        val lobby = getLobby(lobbyId)

        if(lobby.status != LobbyStatus.OPEN) {
            throw IllegalStateException("You cannot change the ready status, while lobby is not open")
        }

        if(lobby.players.none { it.userId == userId}) {
            throw IllegalArgumentException("No player was found in this lobby")
        }

        val updatedLobby = lobby.copy(
            players = lobby.players.map {
                if (it.userId == userId) it.copy(isReady = true) else it
            }
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    @Transactional
    fun unreadyLobby(lobbyId: String, userId: String): Lobby {
        val lobby = getLobby(lobbyId)

        if(lobby.status != LobbyStatus.OPEN) {
            throw IllegalStateException("You cannot change the ready status, while lobby is not open")
        }

        if(lobby.players.none {it.userId == userId}) {
            throw IllegalArgumentException("No player found in this lobby")
        }

        val updatedLobby = lobby.copy(
            players = lobby.players.map {
                if (it.userId == userId) it.copy(isReady = false) else it
            }
        )
        val saved = lobbyRepository.save(updatedLobby.toEntity()).toDomain()
        lobbyBroadcastService.broadcastLobbyUpdated(saved)
        return saved
    }

    fun deleteLobby(lobbyId: String, userId: String) {
        val lobby = getLobby(lobbyId)

        if (lobby.hostUserId != userId) {
            throw SecurityException("Only the host can delete the lobby")
        }

        lobbyRepository.deleteById(lobbyId)
        lobbyBroadcastService.broadcastLobbyDeleted(lobbyId)
    }
}
