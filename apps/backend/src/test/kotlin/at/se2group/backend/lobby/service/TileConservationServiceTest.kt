package at.se2group.backend.lobby.service

import at.se2group.backend.domain.*
import at.se2group.backend.service.TileConservationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class TileConservationServiceTest {

    private val tileConservationService = TileConservationService()

    @Test
    fun `validate throws not implemented exception`() {
        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(
                    userId = "user-1",
                    displayName = "Alice",
                    turnOrder = 0
                )
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.now()
        )
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1"
        )
        val exception = assertThrows(UnsupportedOperationException::class.java) {
            tileConservationService.validate(game, "user-1", draft)
        }
        assertEquals("Tile conservation validation is not implemented yet", exception.message)
    }
}
