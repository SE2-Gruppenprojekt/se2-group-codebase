package at.se2group.backend.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class GameDomainModelTest {

    @Test
    fun `creates numbered tile with color and number`() {
        val tile = NumberedTile(
            color = TileColor.BLUE,
            number = 7
        )

        assertEquals(TileColor.BLUE, tile.color)
        assertEquals(7, tile.number)
    }

    @Test
    fun `creates joker tile with color`() {
        val tile = JokerTile(color = TileColor.RED)

        assertEquals(TileColor.RED, tile.color)
    }

    @Test
    fun `rejects numbered tile outside valid range`() {
        assertThrows(IllegalArgumentException::class.java) {
            NumberedTile(
                color = TileColor.RED,
                number = 14
            )
        }
    }

    @Test
    fun `rejects board set without tiles`() {
        assertThrows(IllegalArgumentException::class.java) {
            BoardSet(
                boardSetId = "set-1",
                tiles = emptyList()
            )
        }
    }

    @Test
    fun `rejects game player with negative score`() {
        assertThrows(IllegalArgumentException::class.java) {
            GamePlayer(
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                score = -1
            )
        }
    }

    @Test
    fun `creates confirmed game with player and current turn`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0
        )

        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(player),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE
        )

        assertEquals("game-1", game.gameId)
        assertEquals(GameStatus.ACTIVE, game.status)
        assertEquals("user-1", game.currentPlayerUserId)
    }

    @Test
    fun `rejects confirmed game when current player is not part of game`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0
        )

        assertThrows(IllegalArgumentException::class.java) {
            ConfirmedGame(
                gameId = "game-1",
                lobbyId = "lobby-1",
                players = listOf(player),
                currentPlayerUserId = "user-2"
            )
        }
    }

    @Test
    fun `rejects turn draft with updated time before creation time`() {
        val createdAt = Instant.parse("2026-04-20T10:00:00Z")
        val updatedAt = Instant.parse("2026-04-20T09:59:59Z")

        assertThrows(IllegalArgumentException::class.java) {
            TurnDraft(
                gameId = "game-1",
                playerUserId = "user-1",
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}
