package at.se2group.backend.game.service

import at.se2group.backend.persistence.*
import at.se2group.backend.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*
import at.se2group.backend.service.GameService

class GameServiceTest {

    private val gameRepository: GameRepository = mock()
    private val turnDraftRepository: TurnDraftRepository = mock()

    private val gameService = GameService(
        gameRepository,
        turnDraftRepository
    )

    private fun mockGame(
        players: List<String>,
        currentPlayer: String
    ): GameEntity {
        val game = GameEntity(
            gameId = "g1",
            lobbyId = "l1",
            currentPlayerUserId = currentPlayer,
            status = GameStatus.ACTIVE,
            createdAt = java.time.Instant.now(),
            startedAt = java.time.Instant.now(),
            finishedAt = null,
            players = players.mapIndexed { i, id ->
                GamePlayerEntity(
                    game = null,
                    userId = id,
                    displayName = id,
                    turnOrder = i,
                    score = 0,
                    hasCompletedInitialMeld = false,
                    joinedAt = java.time.Instant.now(),
                    rackTiles = mutableListOf()
                )
            }.toMutableList(),
            boardSets = mutableListOf(),
            drawPile = mutableListOf()
        )
        return game
    }

    private fun mockDraft(): TurnDraftEntity {
        return TurnDraftEntity(
            gameId = "g1",
            playerUserId = "u1",
            rackTiles = mutableListOf(),
            boardSets = mutableListOf()
        )
    }


    @Test
    fun `should throw if game not found`() {
        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            gameService.endTurn("g1", "u1")
        }
    }

    @Test
    fun `should throw if draft not found`() {
        val game = mockGame(listOf("u1", "u2"), "u1")

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            gameService.endTurn("g1", "u1")
        }
    }

    @Test
    fun `should throw if not active player`() {
        val game = mockGame(listOf("u1", "u2"), "u2")
        val draft = mockDraft()

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(draft)

        assertThrows(IllegalStateException::class.java) {
            gameService.endTurn("g1", "u1")
        }
    }

    @Test
    fun `should switch to next player`() {
        val game = mockGame(listOf("u1", "u2"), "u1")
        val draft = mockDraft()

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(draft)
        whenever(gameRepository.save(any())).thenAnswer { it.arguments[0] }

        gameService.endTurn("g1", "u1")

        assertEquals("u2", game.currentPlayerUserId)
    }

    @Test
    fun `should reset draft for next player`() {
        val game = mockGame(listOf("u1", "u2"), "u1")
        val draft = mockDraft()

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(draft)
        whenever(gameRepository.save(any())).thenAnswer { it.arguments[0] }

        gameService.endTurn("g1", "u1")

        assertEquals("u2", draft.playerUserId)
        assertTrue(draft.boardSets.isEmpty())
    }

    @Test
    fun `should reset draft correctly`() {
        val game = mockGame(listOf("u1", "u2"), "u1")
        val draft = mockDraft()

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(draft)

        whenever(gameRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(turnDraftRepository.save(any())).thenAnswer { it.arguments[0] }

        gameService.resetDraft("g1", "u1")

        assertTrue(draft.boardSets.isEmpty())
    }

    @Test
    fun `should throw when resetting draft with wrong player`() {
        val game = mockGame(listOf("u1", "u2"), "u2")
        val draft = mockDraft()

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(draft)

        assertThrows(IllegalStateException::class.java) {
            gameService.resetDraft("g1", "u1")
        }
    }

    @Test
    fun `should throw when resetting draft but draft missing`() {
        val game = mockGame(listOf("u1", "u2"), "u1")

        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.of(game))
        whenever(turnDraftRepository.findByGameId("g1"))
            .thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            gameService.resetDraft("g1", "u1")
        }
    }

    @Test
    fun `should throw when resetting draft but game missing`() {
        whenever(gameRepository.findById("g1"))
            .thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            gameService.resetDraft("g1", "u1")
        }
    }
}
