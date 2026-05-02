package at.se2group.backend.game.service

import at.se2group.backend.persistence.*
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.*
import java.time.Instant
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertThrows

class GameServiceEndTurnTest {

    private val gameRepository = mock(GameRepository::class.java)
    private val turnDraftRepository = mock(TurnDraftRepository::class.java)

    private val gameService = GameService(gameRepository, turnDraftRepository)

    @Test
    fun `endTurn sets next player`() {

        val game = GameEntity().apply {
            gameId = "game-1"
            lobbyId = "lobby-1"
            currentPlayerUserId = "user-1"

            players = mutableListOf(
                GamePlayerEntity().apply {
                    userId = "user-1"
                    displayName = "Player1"
                    turnOrder = 0
                    joinedAt = Instant.now()
                },
                GamePlayerEntity().apply {
                    userId = "user-2"
                    displayName = "Player2"
                    turnOrder = 1
                    joinedAt = Instant.now()
                }
            )
        }

        val draft = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        `when`(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        `when`(gameRepository.save(any()))
            .thenReturn(game)

        val result = gameService.endTurn("game-1", "user-1")

        assertEquals("user-2", result.currentPlayerUserId)
    }

    @Test
    fun `should throw when user is not active player`() {

        val game = GameEntity().apply {
            gameId = "game1"
            currentPlayerUserId = "user1"
            players = mutableListOf(
                GamePlayerEntity().apply { userId = "user1" },
                GamePlayerEntity().apply { userId = "user2" }
            )
        }

        val draft = TurnDraftEntity(
            gameId = "game1",
            playerUserId = "user1"
        )

        `when`(gameRepository.findById("game1"))
            .thenReturn(Optional.of(game))

        `when`(turnDraftRepository.findByGameId("game1"))
            .thenReturn(draft)

        assertThrows(IllegalStateException::class.java) {
            gameService.endTurn("game1", "user2")
        }
    }
}
