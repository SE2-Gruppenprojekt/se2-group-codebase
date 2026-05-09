package at.se2group.backend.service

/**
 * Service responsible for read access to persisted confirmed game state.
 *
 * This service forms the backend entry point for loading the authoritative game
 * snapshot that represents the last committed and valid state of a running or
 * finished match.
 *
 * Typical use cases include:
 * - initial game screen loading
 * - reconnect recovery after a client disconnect
 * - refreshing the confirmed match state after other game actions
 *
 * The service deliberately focuses on confirmed game state only. Temporary
 * in-progress turn draft state is handled separately by dedicated draft-related
 * services.
 *
 * Persistence is delegated to [GameRepository], while conversion from the
 * persistence/entity model into the backend domain model is delegated to the
 * `toDomain()` mapper.
 *
 * @property gameRepository repository used to load persisted confirmed game
 * state by its identifier.
 */
import shared.models.game.domain.ConfirmedGame
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GameService(
    private val gameRepository: GameRepository
) {

    /**
     * Internal constants used by [GameService].
     */
    private companion object {
        /**
         * Error message used when no persisted confirmed game can be found for a
         * requested game id.
         */
        const val GAME_NOT_FOUND = "Game not found"
    }

    /**
     * Loads the confirmed authoritative game state for the given game id.
     *
     * The returned [ConfirmedGame] represents the committed backend game state,
     * not a temporary in-progress draft. This means the result is intended to be
     * used whenever a client needs the current reliable game snapshot from the
     * server.
     *
     * Lookup is performed through [gameRepository]. If a matching persisted game
     * entity is found, it is converted into the domain model through `toDomain()`
     * before being returned.
     *
     * @param gameId the unique identifier of the confirmed game to load.
     * @return the confirmed game state associated with [gameId].
     * @throws NoSuchElementException if no confirmed game exists for the given
     * id.
     */
    fun getGame(gameId: String): ConfirmedGame {
        return gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }
            .toDomain()
    }
}
