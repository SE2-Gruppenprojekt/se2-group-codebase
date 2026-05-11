package at.se2group.backend.game.service

import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import shared.models.game.domain.*
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class NextPlayerServiceTest {

    @Mock
    lateinit var gameRepository: GameRepository
    private lateinit var gameService: GameService

    @BeforeEach
    fun setup() {
        gameService = GameService(gameRepository)
    }

    private fun player(id: String, order: Int) = GamePlayer(
        userId = id,
        displayName = id,
        turnOrder = order,
        joinedAt = Instant.now()
    )

    private fun game(vararg players: GamePlayer) = ConfirmedGame(
        gameId = "game-1",
        lobbyId = "lobby-1",
        players = players.toList(),
        currentPlayerUserId = players.first().userId,
        status = GameStatus.ACTIVE
    )

    @Test
    fun `advance to next player in turn order`() {
        val game = game(
            player("player-1", 0),
            player("player-2", 1),
            player("player-3", 2)
        )
        assertEquals("player-2", gameService.nextPlayerId(game, "player-1"))
        assertEquals("player-3", gameService.nextPlayerId(game, "player-2"))

    }

    @Test
    fun `wrap from last player to first player`() {
        val game = game(
            player("player-1", 0),
            player("player-2", 1),
            player("player-3", 2)
        )
        assertEquals("player-1", gameService.nextPlayerId(game, "player-3"))
    }

    @Test
    fun `alternate correctly between two players`() {
        val game = game(
            player("player-1", 0),
            player("player-2", 1)
        )
        assertEquals("player-2", gameService.nextPlayerId(game, "player-1"))
        assertEquals("player-1", gameService.nextPlayerId(game, "player-2"))
    }
}
