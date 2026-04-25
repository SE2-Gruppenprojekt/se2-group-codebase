package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.Tile
import at.se2group.backend.domain.TurnDraft
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for creating the initial game state from a lobby that has already been
 * started successfully.
 *
 * Its future implementation will typically:
 * - generate and shuffle the tile pool
 * - convert lobby players into game players
 * - distribute initial hands
 * - create the draw pile
 * - determine the first active player
 * - create the initial confirmed [ConfirmedGame]
 * - create the initial [TurnDraft]
 * - return both values as a [GameStartResult]
 *
 * This class is currently only a skeleton. The methods are intentionally unimplemented so the
 * startup flow can be added step by step later.
 */
@Service
class GameInitializationService {

    /**
     * Creates the initial confirmed game state and first draft from a validated lobby.
     *
     * A future implementation will usually:
     * 1. generate and shuffle the tile pool
     * 2. map lobby players into game players
     * 3. distribute starting hands
     * 4. derive the draw pile
     * 5. determine the first player
     * 6. create the initial [ConfirmedGame]
     * 7. create the initial [TurnDraft]
     * 8. return both inside [GameStartResult]
     *
     * This method should normally be transactional because game creation and draft creation belong to
     * the same startup operation.
     *
     * The provided [lobby] is expected to already be validated by the caller.
     *
     * @param lobby the validated lobby from which the game should be created
     * @return the created game and first draft
     * @throws UnsupportedOperationException because the startup flow is not implemented yet
     */
    @Transactional
    fun createGameFromLobby(lobby: Lobby): GameStartResult {
        throw UnsupportedOperationException("Game initialization is not implemented yet")
    }

}

/**
 * Small result wrapper returned after successful game initialization.
 *
 * Starting a match usually produces two important backend objects:
 * - the confirmed initial [ConfirmedGame]
 * - the initial [TurnDraft] for the first active player
 *
 * Returning both values in one data class keeps the service contract clear and makes it obvious
 * that both results belong to the same startup operation.
 *
 * @property game the newly created confirmed game state
 * @property initialDraft the newly created first draft
 */
data class GameStartResult(
    val game: ConfirmedGame,
    val initialDraft: TurnDraft
)
