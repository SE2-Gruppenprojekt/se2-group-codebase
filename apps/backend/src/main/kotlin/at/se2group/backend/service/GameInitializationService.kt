package at.se2group.backend.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.Game
import at.se2group.backend.domain.JokerTile
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.Tile
import at.se2group.backend.domain.TileColor
import at.se2group.backend.domain.TileRules
import at.se2group.backend.domain.TurnDraft
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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
 * - create the initial confirmed [Game]
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
     * 6. create the initial [Game]
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
        throw UnsupportedOperationException("Not implemented yet")
    }

    /**
     * Generates the full tile pool for one new match and returns it in shuffled order.
     *
     * A future implementation will typically:
     * - create all normal colored numbered tiles
     * - create the joker tiles
     * - shuffle the final list before returning it
     *
     * This logic is kept in its own method so tile creation stays separate from the larger game
     * initialization flow.
     *
     * @return the shuffled tile pool for one new match
     */
    fun createShuffledTilePool(): List<Tile> {
        val tiles = mutableListOf<Tile>()

        repeat(TileRules.NUMBERED_TILE_COPY_COUNT) {
            for (color in TileColor.entries) {
                for (number in TileRules.MIN_TILE_NUMBER..TileRules.MAX_TILE_NUMBER) {
                    tiles += NumberedTile(
                        tileId = UUID.randomUUID().toString(),
                        color = color,
                        number = number
                    )
                }
            }
        }

        tiles += TileRules.jokerColors.map { color ->
            JokerTile(
                tileId = UUID.randomUUID().toString(),
                color = color
            )
        }

        return tiles.shuffled()
    }

    /**
     * Determines which player should receive the first turn of the match.
     *
     * The exact rule can later be implemented in different ways, for example random selection,
     * a simple MVP rule, or a more game-accurate opening rule.
     *
     * Keeping this logic separate makes the startup flow easier to read and test.
     *
     * @param players the players that will participate in the new game
     * @return the user ID of the player who should start
     * @throws UnsupportedOperationException because the selection logic is not implemented yet
     */
    fun determineFirstPlayerId(players: List<Any>): String {
        throw UnsupportedOperationException("Not implemented yet")
    }
}

/**
 * Small result wrapper returned after successful game initialization.
 *
 * Starting a match usually produces two important backend objects:
 * - the confirmed initial [Game]
 * - the initial [TurnDraft] for the first active player
 *
 * Returning both values in one data class keeps the service contract clear and makes it obvious
 * that both results belong to the same startup operation.
 *
 * @property game the newly created confirmed game state
 * @property initialDraft the newly created first draft
 */
data class GameStartResult(
    val game: Game,
    val initialDraft: TurnDraft
)
