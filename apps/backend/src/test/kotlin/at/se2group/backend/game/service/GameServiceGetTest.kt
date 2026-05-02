package at.se2group.backend.game.service

import at.se2group.backend.domain.GameStatus
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional
import org.junit.jupiter.api.BeforeEach
import at.se2group.backend.persistence.TurnDraftRepository

@ExtendWith(MockitoExtension::class)
class GameServiceGetTest {

    @Mock
    lateinit var gameRepository: GameRepository

    lateinit var gameService: GameService

    @Mock
    lateinit var turnDraftRepository: TurnDraftRepository

    @BeforeEach
    fun setup() {
        gameService = GameService(gameRepository, turnDraftRepository)
    }

    @Test
    fun `getGame returns existing game`() {
        val entity = GameEntity(
            gameId = "game-1",
            lobbyId = "lobby-1",
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:00:00Z")
        )

        entity.players = mutableListOf(
            GamePlayerEntity(
                game = entity,
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                rackTiles = mutableListOf(),
                hasCompletedInitialMeld = false,
                score = 0,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z")
            )
        )

        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))

        val result = gameService.getGame("game-1")

        assertEquals("game-1", result.gameId)
        assertEquals("lobby-1", result.lobbyId)
        assertEquals("user-1", result.currentPlayerUserId)
        assertEquals(GameStatus.ACTIVE, result.status)
        assertEquals(1, result.players.size)
        assertEquals("Alice", result.players[0].displayName)

        verify(gameRepository).findById("game-1")
    }

    @Test
    fun `getGame throws NoSuchElementException when game does not exist`() {
        `when`(gameRepository.findById("missing-game"))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NoSuchElementException> {
            gameService.getGame("missing-game")
        }

        assertEquals("Game not found", exception.message)
        verify(gameRepository).findById("missing-game")
    }
}
