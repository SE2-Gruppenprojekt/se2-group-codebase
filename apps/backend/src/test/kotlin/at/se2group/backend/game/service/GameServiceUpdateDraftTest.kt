package at.se2group.backend.game.service

import at.se2group.backend.domain.GameStatus
import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.persistence.*
import at.se2group.backend.service.TurnDraftService
import at.se2group.backend.service.TileConservationService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.util.Optional

class TurnDraftServiceTest {

    private val gameRepository: GameRepository = mock()
    private val turnDraftRepository: TurnDraftRepository = mock()
    private val tileConservationService: TileConservationService = mock()

    private val turnDraftService = TurnDraftService(gameRepository, turnDraftRepository, tileConservationService)

    private fun gameEntity(currentPlayer: String = "user-1"): GameEntity {
        val game = GameEntity(
            gameId = "game-1",
            lobbyId = "lobby-1",
            currentPlayerUserId = currentPlayer,
            status = GameStatus.ACTIVE,
            createdAt = Instant.now()
        )
        game.players = mutableListOf(
            GamePlayerEntity(game = game, userId = currentPlayer, displayName = "Anna", turnOrder = 0)
        )
        return game
    }

    @Test
    fun `updateDraft throws when game not found`() {
        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.empty())

        val request = UpdateDraftRequest(emptyList(), emptyList())

        assertThrows(NoSuchElementException::class.java) {
            turnDraftService.updateDraft("game-1", "user-1", request)
        }
    }

    @Test
    fun `updateDraft returns draft when valid`() {


        val draft = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(draft)

        whenever(turnDraftRepository.save(any()))
            .thenReturn(draft)

        val result = turnDraftService.updateDraft(
            "game-1",
            "user-1",
            mock()
        )

        assertEquals("game-1", result.gameId)
        assertEquals("user-1", result.playerUserId)
    }

    @Test
    fun `updateDraft throws when draft not found`() {


        whenever(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(gameEntity()))

        whenever(turnDraftRepository.findByGameId("game-1"))
            .thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            turnDraftService.updateDraft(
                "game-1",
                "user-1",
                mock()
            )
        }
    }

    @Test
    fun `updateDraft throws when player is not active`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity(currentPlayer = "user-1")))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(TurnDraftEntity(gameId = "game-1", playerUserId = "user-2"))

        assertThrows(IllegalStateException::class.java) {
            turnDraftService.updateDraft("game-1", "user-2", mock())
        }
    }

    @Test
    fun `updateDraft throws when draft belongs to different user`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity(currentPlayer = "user-2")))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(TurnDraftEntity(gameId = "game-1", playerUserId = "user-1"))

        assertThrows(IllegalStateException::class.java) {
            turnDraftService.updateDraft("game-1", "user-2", mock())
        }
    }

    @Test
    fun `updateDraft throws when user is draft owner but not active player`() {
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity(currentPlayer = "user-2")))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(TurnDraftEntity(gameId = "game-1", playerUserId = "user-1"))

        assertThrows(IllegalStateException::class.java) {
            turnDraftService.updateDraft("game-1", "user-1", mock())
        }
    }


    @Test
    fun `updateDraft does not persist when tile conservation fails`(){
        whenever(gameRepository.findById("game-1")).thenReturn(Optional.of(gameEntity()))
        whenever(turnDraftRepository.findByGameId("game-1")).thenReturn(TurnDraftEntity(gameId = "game-1", playerUserId = "user-1"))
        whenever(tileConservationService.validate(any(), any(), any())).thenThrow(IllegalArgumentException("Tile conservation failed!"))

        runCatching {
            turnDraftService.updateDraft("game-1", "user-1", UpdateDraftRequest(emptyList(), emptyList()))
        }

        verify(turnDraftRepository, never()).save(any())
    }
}
