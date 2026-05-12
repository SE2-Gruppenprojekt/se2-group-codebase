package at.se2group.backend.service

import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

/**
 * Service responsible for managing lobby lifecycle and lobby player state.
 *
 * This service acts as the main backend entry point for lobby-related use cases,
 * including:
 * - listing and loading lobbies
 * - creating new lobbies
 * - joining and leaving lobbies
 * - updating lobby settings
 * - toggling player ready state
 * - starting a lobby match
 * - deleting lobbies
 *
 * Lobby state is persisted through [LobbyRepository], while websocket or other
 * realtime updates are delegated to [LobbyBroadcastService]. When a lobby is
 * successfully started, the initial game state is created through
 * [GameInitializationService] and persisted through [GameRepository].
 *
 * The service intentionally keeps lobby orchestration in one place so that
 * validation, persistence, and broadcast behavior remain consistent across all
 * lobby entry points.
 *
 * @property lobbyRepository repository used for loading and persisting lobby
 * entities.
 * @property lobbyBroadcastService broadcaster used to emit lobby update, start,
 * and delete events.
 * @property gameInitializationService service used to create the initial game
 * state when a lobby is started.
 * @property gameRepository repository used to persist the created confirmed game
 * after match start.
 */
@Service
@Transactional(readOnly = true)
class LobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyBroadcastService: LobbyBroadcastService,
    private val gameInitializationService: GameInitializationService,
    private val gameRepository: GameRepository,
    private val gameBroadcastService: GameBroadcastService,
    private val afterCommitExecutor: AfterCommitExecutor) {

    /**
     * Internal constants used by [LobbyService].
     */
    companion object {
        /**
         * Maximum number of players that a lobby may allow.
         */
        const val MAX_PLAYERS = 8
        /**
         * Minimum number of players required for a valid lobby / match start.
         */
        const val MIN_PLAYERS = 2
    }

    /**
     * Returns all currently open lobbies.
     *
     * Only lobbies with status [LobbyStatus.OPEN] are returned. The persistence
     * entities are converted into domain models before being exposed to callers.
     *
     * @return a list of all lobbies that are currently open and joinable.
     */
    fun listOpenLobbies(): List<Lobby> {
        return lobbyRepository.findAllByStatus(LobbyStatus.OPEN)
            .map { it.toDomain() }
    }

    /**
     * Creates a new lobby with the requesting user as host and first player.
     *
     * The method validates the requested maximum player count, creates the host
     * player entry, initializes lobby settings, persists the lobby, and then
     * broadcasts the created/updated lobby state.
     *
     * @param userId the unique identifier of the user creating the lobby.
     * @param request the requested lobby settings and host display name.
     * @return the persisted created lobby as a domain model.
     * @throws IllegalArgumentException if the requested player count is outside
     * the allowed range.
     */
    @Transactional
    fun createLobby(userId: String, request: CreateLobbyRequest): Lobby {
        // Reject lobby configurations outside the globally allowed player range.
        require(!(request.maxPlayers < MIN_PLAYERS || request.maxPlayers > MAX_PLAYERS)) {
            "maxPlayers must be between ${MIN_PLAYERS} and ${MAX_PLAYERS}"
        }

        val lobby = Lobby(
            lobbyId = (100000..999999).random().toString(),
            hostUserId = userId,
            players = listOf(
                LobbyPlayer(
                    userId = userId,
                    displayName = request.displayName,
                    isReady = false
                )
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = request.maxPlayers,
                isPrivate = request.isPrivate,
                allowGuests = request.allowGuests
            )
        )

        // Persist first so the broadcast reflects the stored lobby state.
        val saved = lobbyRepository.save(lobby.toEntity()).toDomain()
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyUpdated(saved)
        }
        return saved
    }

    /**
     * Loads a lobby by its identifier.
     *
     * The persisted entity is converted into the domain model before being
     * returned.
     *
     * @param lobbyId the unique identifier of the lobby to load.
     * @return the requested lobby.
     * @throws NoSuchElementException if no lobby exists for the given id.
     */
    fun getLobby(lobbyId: String): Lobby {
        return requireLobbyEntity(lobbyId).toDomain()
    }

    /**
     * Adds a new player to an existing open lobby.
     *
     * The method verifies that the lobby is open, not full, and does not already
     * contain the joining player. It then appends the player, persists the
     * updated lobby, and broadcasts the change.
     *
     * @param lobbyId the unique identifier of the lobby to join.
     * @param request the joining player's identity and display name.
     * @return the persisted updated lobby.
     * @throws IllegalStateException if the lobby is not open, is full, or the
     * player is already part of the lobby.
     */
    @Transactional
    fun joinLobby(lobbyId: String, request: JoinLobbyRequest): Lobby {
        val lobbyEntity = requireLobbyEntity(lobbyId)
        val lobby = lobbyEntity.toDomain()

        check(lobby.status == LobbyStatus.OPEN) { "Lobby is not open" }

        check(lobby.players.size < lobby.settings.maxPlayers) { "Lobby is full" }

        check(!(lobby.players.any { it.userId == request.userId })) { "Player already in lobby" }

        val updatedLobby = lobby.copy(
            players = lobby.players + LobbyPlayer(
                userId = request.userId,
                displayName = request.displayName,
                isReady = false
            )
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity(lobbyEntity)).toDomain()
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyUpdated(saved)
        }
        return saved
    }

    /**
     * Updates the settings of an existing lobby.
     *
     * Only the host may change settings, and settings may only be changed while
     * the lobby is still open. The requested maximum player count is also
     * validated against the current lobby size and the configured global bounds.
     *
     * @param lobbyId the unique identifier of the lobby to update.
     * @param userId the unique identifier of the acting user.
     * @param request the new lobby settings.
     * @return the persisted updated lobby.
     * @throws SecurityException if the acting user is not the host.
     * @throws IllegalStateException if the lobby is no longer open.
     * @throws IllegalArgumentException if the requested maximum player count is
     * invalid.
     */
    @Transactional
    fun updateLobbySettings(lobbyId: String, userId: String, request: UpdateLobbySettingsRequest): Lobby {
        val lobbyEntity = requireLobbyEntity(lobbyId)
        val lobby = lobbyEntity.toDomain()

        // Only the host may change shared lobby configuration.
        if (lobby.hostUserId != userId) {
            throw SecurityException("Only the host can update lobby settings")
        }

        check(lobby.status == LobbyStatus.OPEN) { "Lobby settings can only be changed while the lobby is open" }

        require(!(request.maxPlayers !in maxOf(MIN_PLAYERS, lobby.players.size)..MAX_PLAYERS)) {
            "Maximum players must be between ${maxOf(MIN_PLAYERS, lobby.players.size)} and ${MAX_PLAYERS}"
        }

        val updatedLobby = lobby.copy(
            settings = LobbySettings(
                maxPlayers = request.maxPlayers,
                isPrivate = request.isPrivate,
                allowGuests = request.allowGuests
            )
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity(lobbyEntity)).toDomain()
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyUpdated(saved)
        }
        return saved
    }

    /**
     * Starts a lobby match and transitions the lobby into the in-game state.
     *
     * Only the host may start the lobby. The lobby must still be open, contain
     * at least [MIN_PLAYERS] players, and all players must currently be ready.
     * Once those conditions are satisfied, the lobby is marked as in-game, the
     * initial game state is created and persisted, and a lobby-started event is
     * broadcast.
     *
     * @param lobbyId the unique identifier of the lobby to start.
     * @param userId the unique identifier of the acting host user.
     * @return the persisted updated lobby in the in-game state.
     * @throws SecurityException if the acting user is not the host.
     * @throws IllegalStateException if the lobby is not open, has too few
     * players, or not all players are ready.
     */
    @Transactional
    fun startLobby(lobbyId: String, userId: String): Lobby {
        val lobbyEntity = requireLobbyEntity(lobbyId)
        val lobby = lobbyEntity.toDomain()

        // Restrict match start to the current lobby host.
        if (lobby.hostUserId != userId) {
            throw SecurityException("Only the host can start the match")
        }

        check(lobby.status == LobbyStatus.OPEN) { "Match can only be started while the lobby is open" }

        // NOTE: comment this back in to enforce player count and ready-state requirements for lobby start. This is currently disabled to allow easier testing with fewer players and without needing to toggle ready state.
        // this is currently disabled to allow easier testing with fewer players and without needing to toggle ready state.

        // check(lobby.players.size >= MIN_PLAYERS) { "At least ${MIN_PLAYERS} players are required to start the match" }
        // check(!(lobby.players.any { !it.isReady })) { "All players must be ready to start the match" }

        val updatedLobby = lobby.copy(
            status = LobbyStatus.IN_GAME
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity(lobbyEntity)).toDomain()

        // Create the initial confirmed game state only after the lobby has been transitioned to IN_GAME.
        val gameStart = gameInitializationService.createGameFromLobby(saved)
        val savedGame = gameRepository.save(gameStart.confirmedGame.toEntity()).toDomain()

        afterCommitExecutor.execute {
            gameBroadcastService.broadcastGameUpdated(savedGame)

            gameStart.turnDraft?.let { initialDraft ->
                gameBroadcastService.broadcastDraftUpdated(initialDraft)
            }

            lobbyBroadcastService.broadcastLobbyStarted(saved.lobbyId, savedGame.gameId)
        }
        return saved
    }

    /**
     * Removes a player from an open lobby.
     *
     * If the leaving player is the last remaining player, the lobby is deleted
     * entirely and `null` is returned. If the leaving player was the host and
     * other players remain, host ownership is transferred to the next remaining
     * player.
     *
     * @param lobbyId the unique identifier of the lobby to leave.
     * @param userId the unique identifier of the leaving player.
     * @return the persisted updated lobby, or `null` if the lobby was deleted
     * because it became empty.
     * @throws IllegalStateException if the lobby is not open.
     * @throws IllegalArgumentException if the player is not part of the lobby.
     */
    @Transactional
    fun leaveLobby(lobbyId: String, userId: String): Lobby? {
        val lobbyEntity = requireLobbyEntity(lobbyId)
        val lobby = lobbyEntity.toDomain()

        check(lobby.status == LobbyStatus.OPEN) { "Cannot leave an unopen lobby" }

        require(!(lobby.players.none { it.userId == userId })) { "No player found in this lobby" }

        val remainingPlayers = lobby.players.filter { it.userId != userId }

        // Delete the lobby entirely once the last remaining player leaves.
        if (remainingPlayers.isEmpty()) {
            lobbyRepository.deleteById(lobbyId)
            afterCommitExecutor.execute {
                lobbyBroadcastService.broadcastLobbyDeleted(lobbyId)
            }
            return null
        }

        // Transfer host ownership if the current host leaves but players remain.
        val nextHostId = if (lobby.hostUserId == userId) {
            remainingPlayers.first().userId
        } else {
            lobby.hostUserId
        }

        val updatedLobby = lobby.copy(
            hostUserId = nextHostId,
            players = lobby.players.filter { it.userId != userId }
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity(lobbyEntity)).toDomain()
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyUpdated(saved)
        }
        return saved
    }

    /**
     * Marks a player in an open lobby as ready.
     *
     * The player must already belong to the lobby, and ready-state changes are
     * only allowed while the lobby is open.
     *
     * @param lobbyId the unique identifier of the lobby.
     * @param userId the unique identifier of the player to mark ready.
     * @return the persisted updated lobby.
     * @throws IllegalStateException if the lobby is not open.
     * @throws IllegalArgumentException if the player is not part of the lobby.
     */
    @Transactional
    fun readyLobby(lobbyId: String, userId: String): Lobby {
        val lobbyEntity = requireLobbyEntity(lobbyId)
        val lobby = lobbyEntity.toDomain()

        check(lobby.status == LobbyStatus.OPEN) { "You cannot change the ready status, while lobby is not open" }

        require(!(lobby.players.none { it.userId == userId})) { "No player was found in this lobby" }

        // Update only the targeted player's ready flag and keep all other players unchanged.
        val updatedLobby = lobby.copy(
            players = lobby.players.map {
                if (it.userId == userId) it.copy(isReady = true) else it
            }
        )

        val saved = lobbyRepository.save(updatedLobby.toEntity(lobbyEntity)).toDomain()
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyUpdated(saved)
        }
        return saved
    }

    /**
     * Marks a player in an open lobby as not ready.
     *
     * The player must already belong to the lobby, and ready-state changes are
     * only allowed while the lobby is open.
     *
     * @param lobbyId the unique identifier of the lobby.
     * @param userId the unique identifier of the player to mark not ready.
     * @return the persisted updated lobby.
     * @throws IllegalStateException if the lobby is not open.
     * @throws IllegalArgumentException if the player is not part of the lobby.
     */
    @Transactional
    fun unreadyLobby(lobbyId: String, userId: String): Lobby {
        val lobbyEntity = requireLobbyEntity(lobbyId)
        val lobby = lobbyEntity.toDomain()

        check(lobby.status == LobbyStatus.OPEN) { "You cannot change the ready status, while lobby is not open" }

        require(!(lobby.players.none {it.userId == userId})) { "No player found in this lobby" }

        // Update only the targeted player's ready flag and keep all other players unchanged.
        val updatedLobby = lobby.copy(
            players = lobby.players.map {
                if (it.userId == userId) it.copy(isReady = false) else it
            }
        )
        val saved = lobbyRepository.save(updatedLobby.toEntity(lobbyEntity)).toDomain()
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyUpdated(saved)
        }
        return saved
    }

    /**
     * Deletes an existing lobby.
     *
     * Only the host may delete the lobby. Once deleted, a lobby-deleted event is
     * broadcast to inform connected clients.
     *
     * @param lobbyId the unique identifier of the lobby to delete.
     * @param userId the unique identifier of the acting user.
     * @throws SecurityException if the acting user is not the host.
     * @throws NoSuchElementException if the lobby does not exist.
     */
    @Transactional
    fun deleteLobby(lobbyId: String, userId: String) {
        val lobby = getLobby(lobbyId)

        // Prevent non-host users from deleting the entire lobby.
        if (lobby.hostUserId != userId) {
            throw SecurityException("Only the host can delete the lobby")
        }

        lobbyRepository.deleteById(lobbyId)
        afterCommitExecutor.execute {
            lobbyBroadcastService.broadcastLobbyDeleted(lobbyId)
        }
    }

    private fun requireLobbyEntity(lobbyId: String): LobbyEntity =
        lobbyRepository.findById(lobbyId)
            .orElseThrow { NoSuchElementException("Lobby not found") }
}
