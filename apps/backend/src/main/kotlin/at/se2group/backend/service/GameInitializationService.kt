package at.se2group.backend.service

import at.se2group.backend.domain.*
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
class GameInitializationService(
    private val tilePoolGenerationService: TilePoolGenerationService,
    private val tileShuffleService: TileShuffleService
) {
    companion object {
        private const val INITIAL_HAND_SIZE = 14
    }



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

        val orderedPool = tilePoolGenerationService.createTilePool()
        val tiles = tileShuffleService.shuffleTiles(orderedPool)

        val basePlayers = lobby.players.mapIndexed { index, it ->
            GamePlayer(
                userId = it.userId,
                displayName = it.displayName,
                turnOrder = index,
                rackTiles = emptyList(),
                hasCompletedInitialMeld = false,
                score = 0
            )
        }

        val playersWithHands = tileShuffleService.distributedHands(basePlayers, tiles)
        val drawPile = tileShuffleService.createDrawPile(tiles, playersWithHands)

        val firstPlayerId = playersWithHands.random().userId

        val game = Game(
            gameId = UUID.randomUUID().toString(),
            lobbyId = lobby.lobbyId,
            players = playersWithHands,
            boardSets = emptyList(),
            drawPile = drawPile,
            currentPlayerUserId = firstPlayerId,
            status = GameStatus.ACTIVE
        )

        val starter = game.players.first { it.userId == firstPlayerId }
        val draft = TurnDraft(
            gameId = game.gameId,
            playerUserId = starter.userId,
            boardSets = emptyList(),
            rackTiles = starter.rackTiles
        )
        return GameStartResult(game, draft )
    }
}
