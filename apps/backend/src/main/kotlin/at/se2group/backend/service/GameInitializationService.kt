package at.se2group.backend.service

import at.se2group.backend.mapper.toEmbeddable

import at.se2group.backend.domain.GameStartResult
import shared.models.game.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.persistence.TurnDraftEntity
import shared.models.lobby.domain.Lobby

/**
 * Service responsible for creating the initial confirmedGame state from a lobby that has already been
 * started successfully.
 *
 * Its future implementation will typically:
 * - generate and shuffle the tile pool
 * - convert lobby players into confirmedGame players
 * - distribute initial hands
 * - create the draw pile
 * - determine the first active player
 * - create the initial confirmed [ConfirmedGame]
 * - create the initial [TurnDraft]
 * - return both values as a [GameStartResult]
 */
@Service
class GameInitializationService(
    private val tilePoolGenerationService: TilePoolGenerationService,
    private val tileShuffleService: TileShuffleService,
    private val turnDraftRepository: TurnDraftRepository
) {
    /**
     * Creates the initial confirmed confirmedGame state and first draft from a validated lobby.
     *
     * A future implementation will usually:
     * 1. generate and shuffle the tile pool
     * 2. map lobby players into confirmedGame players
     * 3. distribute starting hands
     * 4. derive the draw pile
     * 5. determine the first player
     * 6. create the initial [ConfirmedGame]
     * 7. create the initial [TurnDraft]
     * 8. return both inside [GameStartResult]
     *
     * This method should normally be transactional because confirmedGame creation and draft creation belong to
     * the same startup operation.
     *
     * The provided [lobby] is expected to already be validated by the caller.
     *
     * @param lobby the validated lobby from which the confirmedGame should be created
     * @return the created confirmedGame and first draft
     */
    @Transactional
    fun createGameFromLobby(lobby: Lobby): GameStartResult {
        val shuffledTiles = tileShuffleService.shuffleTiles(tilePoolGenerationService.createTilePool())
        val playersWithHands = tileShuffleService.distributedHands(
            lobby.toGamePlayers(),
            shuffledTiles
        )
        val firstPlayer = playersWithHands.firstByTurnOrder()
        val confirmedGame = ConfirmedGame(
            gameId = UUID.randomUUID().toString(),
            lobbyId = lobby.lobbyId,
            players = playersWithHands,
            boardSets = emptyList(),
            drawPile = tileShuffleService.createDrawPile(shuffledTiles, playersWithHands),
            currentPlayerUserId = firstPlayer.userId,
            status = GameStatus.ACTIVE
        )
        val draft = TurnDraft(
            gameId = confirmedGame.gameId,
            playerUserId = firstPlayer.userId,
            boardSets = emptyList(),
            rackTiles = firstPlayer.rackTiles,
            version = 0
        )
        turnDraftRepository.save(draft.toEntity())

        return GameStartResult(confirmedGame, draft)
    }

    /**
     * Maps the validated lobby player list into the initial backend game-player model.
     *
     * The lobby order is preserved and used as the initial `turnOrder`, so downstream
     * game setup can derive a deterministic first player without introducing extra
     * ordering logic. Newly created game players always start with an empty rack,
     * no completed initial meld, and a score of zero because those values are filled
     * or evolved later in the initialization and game flow.
     */
    private fun Lobby.toGamePlayers(): List<GamePlayer> = players.mapIndexed { index, player ->
        GamePlayer(
            userId = player.userId,
            displayName = player.displayName,
            turnOrder = index,
            rackTiles = emptyList(),
            hasCompletedInitialMeld = false,
            score = 0
        )
    }

    /**
     * Selects the first active player from the distributed game players.
     *
     * This keeps initial turn selection deterministic by using the lowest `turnOrder`
     * instead of randomness. The helper assumes that the player list has already been
     * created from lobby order and enriched with starting hands, so the returned player
     * can be used both for `currentPlayerUserId` in the confirmed game and for the
     * initial live draft owner.
     *
     * @throws IllegalArgumentException if the player list is unexpectedly empty
     */
    private fun List<GamePlayer>.firstByTurnOrder(): GamePlayer =
        minByOrNull { it.turnOrder }
            ?: throw IllegalArgumentException("players must not be empty")

    /**
     * Converts the freshly created domain draft into the persistence shape used for
     * backend-managed live draft editing.
     *
     * At initialization time the draft starts without any persisted board-set entities,
     * because the confirmed game begins with an empty board and the first player has not
     * edited anything yet. Rack tiles are converted into embeddables so the draft can be
     * stored immediately and later updated through the draft workflow without rebuilding
     * the initial state from scratch.
     */
    private fun TurnDraft.toEntity() = TurnDraftEntity(
        gameId = gameId,
        playerUserId = playerUserId,
        version = version,
        boardSets = mutableListOf(),
        rackTiles = rackTiles.map { it.toEmbeddable() }.toMutableList()
    )
}
