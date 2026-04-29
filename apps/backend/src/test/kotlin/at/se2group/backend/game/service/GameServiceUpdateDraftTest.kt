package at.se2group.backend.game.service

import at.se2group.backend.dto.UpdateDraftRequest
import at.se2group.backend.persistence.GameEntity
import at.se2group.backend.persistence.GamePlayerEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class GameServiceUpdateDraftTest {

    @Mock
    lateinit var gameRepository: GameRepository

    @InjectMocks
    lateinit var gameService: GameService

    @Test
    fun `updateDraft rejects if not active player`() {
        val entity = GameEntity(
            gameId = "g1",
            lobbyId = "l1",
            currentPlayerUserId = "host-1",
            players = mutableListOf(
                GamePlayerEntity(
                    id = 1,
                    game = null,
                    userId = "host-1",
                    displayName = "Host",
                    turnOrder = 0,
                    hasCompletedInitialMeld = false,
                    score = 0,
                    joinedAt = Instant.now()
                )
            )
        )

        `when`(gameRepository.findById("g1"))
            .thenReturn(Optional.of(entity))

        assertThrows<SecurityException> {
            gameService.updateDraft(
                "g1",
                "other",
                UpdateDraftRequest(emptyList(), emptyList())
            )
        }
    }

    @Test
    fun `updateDraft returns draft for active player`() {
        val entity = GameEntity(
            gameId = "g1",
            lobbyId = "l1",
            currentPlayerUserId = "host-1",
            players = mutableListOf(
                GamePlayerEntity(
                    id = 1,
                    game = null,
                    userId = "host-1",
                    displayName = "Host",
                    turnOrder = 0,
                    hasCompletedInitialMeld = false,
                    score = 0,
                    joinedAt = Instant.now()
                )
            )
        )

        `when`(gameRepository.findById("g1"))
            .thenReturn(Optional.of(entity))

        val result = gameService.updateDraft(
            "g1",
            "host-1",
            UpdateDraftRequest(emptyList(), emptyList())
        )

        assertEquals("g1", result.gameId)
        assertEquals("host-1", result.playerUserId)
    }
}
