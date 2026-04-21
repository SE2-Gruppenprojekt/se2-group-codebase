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
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NumberedTile(
                color = TileColor.RED,
                number = 14
            )
        }

        assertEquals("tile number must be between 1 and 13", exception.message)
    }

    @Test
    fun `rejects numbered tile below valid range`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NumberedTile(
                color = TileColor.BLUE,
                number = 0
            )
        }

        assertEquals("tile number must be between 1 and 13", exception.message)
    }

    @Test
    fun `rejects board set without tiles`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            BoardSet(
                boardSetId = "set-1",
                tiles = emptyList()
            )
        }

        assertEquals("board sets must contain at least one tile", exception.message)
    }

    @Test
    fun `rejects game player with negative score`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            GamePlayer(
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                score = -1
            )
        }

        assertEquals("score must not be negative", exception.message)
    }

    @Test
    fun `creates confirmed game with player and current turn`() {
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0
        )

        val game = Game(
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

        val exception = assertThrows(IllegalArgumentException::class.java) {
            Game(
                gameId = "game-1",
                lobbyId = "lobby-1",
                players = listOf(player),
                currentPlayerUserId = "user-2"
            )
        }

        assertEquals(
            "currentPlayerUserId must belong to one of the game players",
            exception.message
        )
    }

    @Test
    fun `rejects turn draft with updated time before creation time`() {
        val createdAt = Instant.parse("2026-04-20T10:00:00Z")
        val updatedAt = Instant.parse("2026-04-20T09:59:59Z")

        val exception = assertThrows(IllegalArgumentException::class.java) {
            TurnDraft(
                gameId = "game-1",
                playerUserId = "user-1",
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }

        assertEquals("updatedAt must not be before createdAt", exception.message)
    }
}
