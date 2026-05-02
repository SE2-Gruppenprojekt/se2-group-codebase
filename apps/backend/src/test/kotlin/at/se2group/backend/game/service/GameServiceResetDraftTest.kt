package at.se2group.backend.game.service

import at.se2group.backend.persistence.*
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.Mockito.*
import java.time.Instant
import java.util.Optional
import at.se2group.backend.dto.UpdateDraftRequest

class GameServiceResetDraftTest {

    private val gameRepository = mock(GameRepository::class.java)
    private val turnDraftRepository = mock(TurnDraftRepository::class.java)

    private val gameService = GameService(gameRepository, turnDraftRepository)

    @Test
    fun `resetDraft resets to game state`() {

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
                }
            )

            boardSets = mutableListOf()
        }

        val draft = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        `when`(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        `when`(turnDraftRepository.save(any()))
            .thenReturn(draft)

        val result = gameService.resetDraft("game-1", "user-1")

        assertEquals("game-1", result.gameId)
        assertEquals("user-1", result.playerUserId)
    }

    @Test
    fun `updateDraft throws when not active player`() {

        val game = GameEntity().apply {
            gameId = "game-1"
            lobbyId = "lobby-1"
        }

        val draft = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        `when`(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        assertThrows(IllegalStateException::class.java) {
            gameService.updateDraft(
                "game-1",
                "user-2",
                mock(UpdateDraftRequest::class.java)
            )
        }
    }

    @Test
    fun `resetDraft throws when not active player`() {

        val game = GameEntity().apply {
            gameId = "game-1"
            lobbyId = "lobby-1"
            currentPlayerUserId = "user-1"
        }

        val draft = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        `when`(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        assertThrows(IllegalStateException::class.java) {
            gameService.resetDraft("game-1", "user-2")
        }
    }
}
