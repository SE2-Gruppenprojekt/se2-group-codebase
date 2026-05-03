package at.se2group.backend.game.service

import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.persistence.*
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.Optional

class GameServiceUpdateDraftTest {

    private val gameRepository: GameRepository = mock()
    private val turnDraftRepository: TurnDraftRepository = mock()

    private val gameService = GameService(gameRepository, turnDraftRepository)

    @Test
    fun `updateDraft throws when game not found`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.empty())

        val request = UpdateDraftRequest(emptyList(), emptyList())

        assertThrows(NoSuchElementException::class.java) {
            gameService.updateDraft("game-1", "user-1", request)
        }
    }

    @Test
    fun `updateDraft returns draft when valid`() {

        val game = GameEntity().apply {
            gameId = "game-1"
        }

        val draft = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        whenever(turnDraftRepository.save(any()))
            .thenReturn(draft)

        val result = gameService.updateDraft(
            "game-1",
            "user-1",
            mock()
        )

        assertEquals("game-1", result.gameId)
        assertEquals("user-1", result.playerUserId)
    }

    @Test
    fun `updateDraft throws when draft not found`() {

        val game = GameEntity().apply {
            gameId = "game-1"
        }

        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(game))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            gameService.updateDraft(
                "game-1",
                "user-1",
                mock()
            )
        }
    }
}
