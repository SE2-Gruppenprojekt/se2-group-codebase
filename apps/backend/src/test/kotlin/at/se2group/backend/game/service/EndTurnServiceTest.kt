package at.se2group.backend.game.service

import at.se2group.backend.persistence.GameEntity
import at.se2group.backend.persistence.GamePlayerEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TileEmbeddable
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.rules.service.BoardValidationService
import at.se2group.backend.rules.service.FirstMoveValidationService
import at.se2group.backend.rules.service.GroupValidationService
import at.se2group.backend.rules.service.RummikubRuleService
import at.se2group.backend.rules.service.RunValidationService
import at.se2group.backend.rules.service.SetValidationService
import at.se2group.backend.service.AfterCommitExecutor
import at.se2group.backend.service.EndTurnService
import at.se2group.backend.service.GameBroadcastService
import at.se2group.backend.service.GameMetricsService
import at.se2group.backend.service.GameService
import at.se2group.backend.service.InvalidTurnSubmissionException
import at.se2group.backend.service.TileConservationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import shared.models.game.domain.GameStatus
import shared.models.game.domain.JokerTile
import shared.models.game.domain.TileColor
import shared.models.game.request.BoardSetRequest
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.TileRequest
import shared.models.game.validation.invalid
import shared.models.game.validation.valid
import java.time.Instant
import java.util.Optional

class EndTurnServiceTest {

    private val gameRepository: GameRepository = mock()
    private val turnDraftRepository: TurnDraftRepository = mock()
    private val rummikubRuleService: RummikubRuleService = mock()
    private val gameBroadcastService: GameBroadcastService = mock()
    private val gameMetricsService = GameMetricsService()

    private val gameService = GameService(gameRepository)
    private val afterCommitExecutor = AfterCommitExecutor()

    private val endTurnService = EndTurnService(
        gameRepository = gameRepository,
        turnDraftRepository = turnDraftRepository,
        gameService = gameService,
        rummikubRuleService = rummikubRuleService,
        gameBroadcastService = gameBroadcastService,
        afterCommitExecutor = afterCommitExecutor,
        gameMetricsService = gameMetricsService
    )

    @Test
    fun `rejects when game does not exist`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            endTurnService.endTurn("game-1", "user-1", request())
        }

        verify(gameRepository, never()).save(any())
        verify(turnDraftRepository, never()).save(any())
        verify(gameBroadcastService, never()).broadcastGameUpdated(any())
    }

    @Test
    fun `rejects when draft does not exist`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(null)

        assertThrows<NoSuchElementException> {
            endTurnService.endTurn("game-1", "user-1", request())
        }

        verify(gameRepository, never()).save(any())
        verify(turnDraftRepository, never()).save(any())
    }

    @Test
    fun `rejects when user is not current player`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity(currentPlayerUserId = "user-1")))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity(playerUserId = "user-2"))

        assertThrows<IllegalStateException> {
            endTurnService.endTurn("game-1", "user-2", request())
        }

        verify(rummikubRuleService, never()).validateSubmittedDraft(any(), any(), any())
        verify(gameRepository, never()).save(any())
    }

    @Test
    fun `rejects when draft belongs to another user`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity(currentPlayerUserId = "user-1")))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity(playerUserId = "user-2"))

        assertThrows<IllegalStateException> {
            endTurnService.endTurn("game-1", "user-1", request())
        }

        verify(rummikubRuleService, never()).validateSubmittedDraft(any(), any(), any())
        verify(gameRepository, never()).save(any())
    }

    @Test
    fun `rejects when game is not active`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity(status = GameStatus.WAITING)))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())

        assertThrows<IllegalStateException> {
            endTurnService.endTurn("game-1", "user-1", request())
        }

        verify(rummikubRuleService, never()).validateSubmittedDraft(any(), any(), any())
        verify(gameRepository, never()).save(any())
    }

    @Test
    fun `rejects when submitted draft validation is invalid`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())

        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(invalid("INVALID_SET", "Set is invalid"))

        assertThrows<InvalidTurnSubmissionException> {
            endTurnService.endTurn("game-1", "user-1", request())
        }

        verify(gameRepository, never()).save(any())
        verify(turnDraftRepository, never()).save(any())
        verify(gameBroadcastService, never()).broadcastGameUpdated(any())
    }

    @Test
    fun `commits board and acting player rack then advances to next player`() {
        val game = gameEntity(
            user1Rack = mutableListOf(tile("old-rack-tile")),
            user2Rack = mutableListOf(tile("user-2-rack"))
        )

        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())

        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(valid())

        whenever(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        whenever(turnDraftRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val result = endTurnService.endTurn("game-1", "user-1", request())

        assertEquals("user-2", result.currentPlayerUserId)
        assertEquals(1, result.boardSets.size)
        assertEquals("set-1", result.boardSets.single().boardSetId)

        val user1 = result.players.first { it.userId == "user-1" }
        assertEquals(listOf("new-rack-tile"), user1.rackTiles.map { it.tileId })

        assertEquals(1, result.totalTurnsCompleted)
        assertEquals(1, user1.metrics.turnsCompleted)
        assertEquals(1, user1.metrics.tilesPlayed)
        assertEquals(1, user1.metrics.meldsCreated)
        assertEquals(5, user1.metrics.pointsPlayed)

        verify(gameRepository).save(any())
        verify(turnDraftRepository).save(any())
        verify(gameBroadcastService).broadcastGameUpdated(result)
        verify(gameBroadcastService).broadcastTurnChanged("game-1", "user-2")
        verify(gameBroadcastService).broadcastDraftUpdated(any())
    }

    @Test
    fun `auto draws tile on pass turn when player has not drawn yet`() {
        val game = gameEntity(
            user1Rack = mutableListOf(tile("old-rack-tile")),
            user2Rack = mutableListOf(tile("user-2-rack")),
            drawPile = mutableListOf(tile("draw-top"), tile("draw-next"))
        )

        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())

        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(valid())

        whenever(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        whenever(turnDraftRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val result = endTurnService.endTurn(
            gameId = "game-1",
            userId = "user-1",
            request = EndTurnRequest(
                boardSets = emptyList(),
                rackTiles = listOf(TileRequest("old-rack-tile", "RED", 5, false))
            )
        )

        val actingPlayer = result.players.first { it.userId == "user-1" }
        assertEquals(listOf("old-rack-tile", "draw-top"), actingPlayer.rackTiles.map { it.tileId })
        assertEquals(listOf("draw-next"), result.drawPile.map { it.tileId })
    }

    @Test
    fun `commits valid joker containing submitted draft`() {
        val realRuleService = RummikubRuleService(
            TileConservationService(),
            BoardValidationService(SetValidationService(GroupValidationService(), RunValidationService())),
            FirstMoveValidationService()
        )
        val realEndTurnService = EndTurnService(
            gameRepository = gameRepository,
            turnDraftRepository = turnDraftRepository,
            gameService = gameService,
            rummikubRuleService = realRuleService,
            gameBroadcastService = gameBroadcastService,
            afterCommitExecutor = afterCommitExecutor,
            gameMetricsService = gameMetricsService
        )

        whenever(
            gameRepository.findById("game-1")
        ).thenReturn(
            Optional.of(
                gameEntity(
                    user1Rack = mutableListOf(
                        tile("tile-1", TileColor.RED, 3, false),
                        tile("tile-2", TileColor.BLACK, null, true),
                        tile("tile-3", TileColor.RED, 5, false),
                        tile("new-rack-tile", TileColor.BLACK, 9, false)
                    ),
                    user2Rack = mutableListOf(tile("user-2-rack")),
                    hasCompletedInitialMeld = true
                )
            )
        )
        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())
        whenever(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }
        whenever(turnDraftRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val result = realEndTurnService.endTurn(
            gameId = "game-1",
            userId = "user-1",
            request = jokerRunRequest()
        )

        assertEquals("user-2", result.currentPlayerUserId)
        assertEquals("set-joker-run", result.boardSets.single().boardSetId)
        assertTrue(result.boardSets.single().tiles[1] is JokerTile)
        assertEquals(listOf("new-rack-tile"), result.players.first { it.userId == "user-1" }.rackTiles.map { it.tileId })

        verify(gameRepository).save(any())
        verify(turnDraftRepository).save(any())
        verify(gameBroadcastService).broadcastGameUpdated(result)
        verify(gameBroadcastService).broadcastTurnChanged("game-1", "user-2")
        verify(gameBroadcastService).broadcastDraftUpdated(any())
    }

    @Test
    fun `replaces old draft with next player draft`() {
        val draft = draftEntity(playerUserId = "user-1", version = 4)

        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity(user2Rack = mutableListOf(tile("user-2-rack")))))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(valid())

        whenever(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        whenever(turnDraftRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        endTurnService.endTurn("game-1", "user-1", request())

        assertEquals("user-2", draft.playerUserId)
        assertEquals(0, draft.version)
        assertEquals(listOf("user-2-rack"), draft.rackTiles.map { it.tileId })
    }

    @Test
    fun `finishes game when acting player rack becomes empty`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())

        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(valid())

        whenever(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val result = endTurnService.endTurn(
            gameId = "game-1",
            userId = "user-1",
            request = request(rackTiles = emptyList())
        )

        assertEquals(GameStatus.FINISHED, result.status)
        assertTrue(result.finishedAt != null)

        assertEquals("user-1", result.winnerUserId)

        val winner = result.players.first { it.userId == "user-1" }
        assertEquals(true, winner.metrics.winner)
        assertEquals(1, winner.metrics.finishPosition)
        assertEquals(0, winner.metrics.tilesRemainingAtEnd)
        assertEquals(0, winner.metrics.penaltyPointsAtEnd)

        verify(turnDraftRepository).deleteById("game-1")
        verify(turnDraftRepository, never()).save(any())
        verify(gameBroadcastService).broadcastGameUpdated(result)
        verify(gameBroadcastService).broadcastGameEnded("game-1", "user-1")
        verify(gameBroadcastService, never()).broadcastTurnChanged(any(), any())
        verify(gameBroadcastService, never()).broadcastDraftUpdated(any())
    }

    @Test
    fun `finishes game and skips next draft when acting player rack becomes empty`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(
                Optional.of(
                    gameEntity(
                        user1Rack = mutableListOf(tile("old-user-1-rack")),
                        user2Rack = mutableListOf(tile("user-2-rack"))
                    )
                )
            )

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draftEntity())

        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(valid())

        whenever(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        val result = endTurnService.endTurn(
            gameId = "game-1",
            userId = "user-1",
            request = request(rackTiles = emptyList())
        )

        assertEquals(GameStatus.FINISHED, result.status)
        assertTrue(result.finishedAt != null)
        assertEquals("user-1", result.currentPlayerUserId)
        assertEquals(emptyList<String>(), result.players.first { it.userId == "user-1" }.rackTiles.map { it.tileId })
        assertEquals(listOf("user-2-rack"), result.players.first { it.userId == "user-2" }.rackTiles.map { it.tileId })

        val loser = result.players.first { it.userId == "user-2" }
        assertEquals(false, loser.metrics.winner)
        assertEquals(2, loser.metrics.finishPosition)
        assertEquals(1, loser.metrics.tilesRemainingAtEnd)
        assertEquals(5, loser.metrics.penaltyPointsAtEnd)

        verify(gameRepository).save(any())
        verify(turnDraftRepository).deleteById("game-1")
        verify(turnDraftRepository, never()).save(any())

        verify(gameBroadcastService).broadcastGameUpdated(result)
        verify(gameBroadcastService).broadcastGameEnded("game-1", "user-1")
        verify(gameBroadcastService, never()).broadcastTurnChanged(any(), any())
        verify(gameBroadcastService, never()).broadcastDraftUpdated(any())
    }

    @Test
    fun `marks player as having completed initial meld after first turn`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity()))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(draftEntity())
        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any())).thenReturn(valid())
        whenever(gameRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(turnDraftRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = endTurnService.endTurn("game-1", "user-1", request())

        val actingPlayer = result.players.first { it.userId == "user-1" }
        assertTrue(actingPlayer.hasCompletedInitialMeld)
    }

    @Test
    fun `does not commit game state if initial meld validation failed`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity()))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(draftEntity())
        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any()))
            .thenReturn(invalid("INITIAL_MELD_TOO_LOW", "Initial meld must score at least 30 points"))

        assertThrows<InvalidTurnSubmissionException> {
            endTurnService.endTurn("game-1", "user-1", request())
        }

        verify(gameRepository, never()).save(any())
        verify(gameBroadcastService, never()).broadcastGameUpdated(any())
    }

    @Test
    fun `player with completed initial meld can submit a low-score turn`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity(hasCompletedInitialMeld = true)))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(draftEntity())
        whenever(rummikubRuleService.validateSubmittedDraft(any(), any(), any())).thenReturn(valid())
        whenever(gameRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(turnDraftRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = endTurnService.endTurn("game-1", "user-1", request())

        assertTrue(result.players.first { it.userId == "user-1" }.hasCompletedInitialMeld)
    }

    private fun request(
        rackTiles: List<TileRequest> = listOf(TileRequest("new-rack-tile", "BLACK", 9, false))
    ) = EndTurnRequest(
        boardSets = listOf(
            BoardSetRequest(
                boardSetId = "set-1",
                type = shared.models.game.domain.BoardSetType.RUN,
                tiles = listOf(
                    TileRequest("tile-1", "RED", 3, false),
                    TileRequest("tile-2", "RED", 4, false),
                    TileRequest("tile-3", "RED", 5, false)
                )
            )
        ),
        rackTiles = rackTiles
    )

    private fun jokerRunRequest() = EndTurnRequest(
        boardSets = listOf(
            BoardSetRequest(
                boardSetId = "set-joker-run",
                type = shared.models.game.domain.BoardSetType.RUN,
                tiles = listOf(
                    TileRequest("tile-1", "RED", 3, false),
                    TileRequest("tile-2", "BLACK", null, true),
                    TileRequest("tile-3", "RED", 5, false)
                )
            )
        ),
        rackTiles = listOf(TileRequest("new-rack-tile", "BLACK", 9, false))
    )

    private fun gameEntity(
        currentPlayerUserId: String = "user-1",
        status: GameStatus = GameStatus.ACTIVE,
        user1Rack: MutableList<TileEmbeddable> = mutableListOf(),
        user2Rack: MutableList<TileEmbeddable> = mutableListOf(),
        drawPile: MutableList<TileEmbeddable> = mutableListOf(),
        hasCompletedInitialMeld: Boolean = false
    ): GameEntity {
        val game = GameEntity(
            gameId = "game-1",
            lobbyId = "lobby-1",
            currentPlayerUserId = currentPlayerUserId,
            status = status,
            createdAt = Instant.parse("2026-04-27T18:00:00Z"),
            drawPile = drawPile
        )

        game.players = mutableListOf(
            GamePlayerEntity(
                game = game,
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                rackTiles = user1Rack,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z"),
                hasCompletedInitialMeld = hasCompletedInitialMeld
            ),
            GamePlayerEntity(
                game = game,
                userId = "user-2",
                displayName = "Bob",
                turnOrder = 1,
                rackTiles = user2Rack,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z")
            )
        )

        return game
    }

    private fun draftEntity(
        playerUserId: String = "user-1",
        version: Long = 0
    ) = TurnDraftEntity(
        gameId = "game-1",
        playerUserId = playerUserId,
        version = version
    )

    private fun tile(tileId: String) = tile(
        tileId = tileId,
        color = TileColor.RED,
        number = 5,
        joker = false
    )

    private fun tile(tileId: String, color: TileColor, number: Int?, joker: Boolean) = TileEmbeddable(
        tileId = tileId,
        color = color,
        number = number,
        joker = joker
    )
}
