package at.se2group.backend.game.service

import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Assertions.assertThrows


class GameServiceUpdateDraftTest {

    private val gameRepository: GameRepository = mock()
    private val turnDraftRepository: TurnDraftRepository = mock()

    private val gameService = GameService(
        gameRepository,
        turnDraftRepository
    )

    @Test
    fun `updateDraft returns updated draft`() {
        val request = UpdateDraftRequest(
            boardSets = emptyList(),
            rackTiles = emptyList()
        )

        val existingDraft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1"
        )
        whenever(gameRepository.findById("game-1"))
            .thenReturn(java.util.Optional.of(mock()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(existingDraft)

        whenever(turnDraftRepository.save(any()))
            .thenAnswer { it.arguments[0] as TurnDraft }

        val result = gameService.updateDraft(
            "game-1",
            "user-1",
            request
        )

        assertEquals("game-1", result.gameId)
        assertEquals("user-1", result.playerUserId)

        verify(turnDraftRepository).save(any())
    }

    @Test
    fun `updateDraft throws when game not found`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(java.util.Optional.empty())

        val request = UpdateDraftRequest(emptyList(), emptyList())

        assertThrows(NoSuchElementException::class.java) {
            gameService.updateDraft("game-1", "user-1", request)
        }
    }

    @Test
    fun `updateDraft throws when wrong player`() {
        val request = UpdateDraftRequest(emptyList(), emptyList())

        whenever(gameRepository.findById("game-1"))
            .thenReturn(java.util.Optional.of(mock()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(
                TurnDraft("game-1", "other-user")
            )

        assertThrows(IllegalStateException::class.java) {
            gameService.updateDraft("game-1", "user-1", request)
        }
    }

    @Test
    fun `updateDraft throws when draft not found`() {
        val request = UpdateDraftRequest(emptyList(), emptyList())

        whenever(gameRepository.findById("game-1"))
            .thenReturn(java.util.Optional.of(mock()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            gameService.updateDraft("game-1", "user-1", request)
        }
    }
}
