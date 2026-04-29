package at.se2group.backend.game.service

import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import at.se2group.backend.persistence.TurnDraftRepository

class GameServiceUpdateDraftTest {

    private val gameRepository: GameRepository = mock()

    private val turnDraftRepository: TurnDraftRepository = mock()

    private val gameService = GameService(
        gameRepository,
        turnDraftRepository
    )

    @Test
    fun `updateDraft throws when game not found`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(java.util.Optional.empty())

        val request = UpdateDraftRequest(emptyList(), emptyList())

        assertThrows(NoSuchElementException::class.java) {
            gameService.updateDraft("game-1", "user-1", request)
        }
    }
}
