package at.se2group.backend.lobby.service

import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.Tile
import shared.models.game.domain.TileColor
import at.se2group.backend.service.TileShuffleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TileShuffleServiceTest {

    private val tileShuffleService = TileShuffleService()

    @Test
    fun `shuffleTiles returns same tiles with same size`() {
        val tiles = createNumberedTiles(1, 10)

        val result = tileShuffleService.shuffleTiles(tiles)

        assertEquals(tiles.size, result.size)
        assertEquals(tiles.toSet(), result.toSet())
    }

    @Test
    fun `distributedHands gives each player 14 tiles in order`() {
        val players = listOf(
            GamePlayer(userId = "user-1", displayName = "Alice", turnOrder = 0),
            GamePlayer(userId = "user-2", displayName = "Bob", turnOrder = 1)
        )
        val tiles = createNumberedTiles(1, 28)

        val result = tileShuffleService.distributedHands(players, tiles)

        assertEquals(2, result.size)
        assertIterableEquals(tiles.subList(0, 14), result[0].rackTiles)
        assertIterableEquals(tiles.subList(14, 28), result[1].rackTiles)
        assertEquals("user-1", result[0].userId)
        assertEquals("user-2", result[1].userId)
    }

    @Test
    fun `distributedHands rejects when there are not enough tiles for all players`() {
        val players = listOf(
            GamePlayer(userId = "user-1", displayName = "Alice", turnOrder = 0),
            GamePlayer(userId = "user-2", displayName = "Bob", turnOrder = 1)
        )
        val tiles = createNumberedTiles(1, 27)

        val exception = assertThrows<IllegalArgumentException> {
            tileShuffleService.distributedHands(players, tiles)
        }

        assertEquals("Not enough tiles to distribute 14 tiles to 2 players", exception.message)
    }

    @Test
    fun `createDrawPile drops tiles already distributed to players`() {
        val tiles = createNumberedTiles(1, 30)
        val players = listOf(
            GamePlayer(
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                rackTiles = tiles.subList(0, 14)
            ),
            GamePlayer(
                userId = "user-2",
                displayName = "Bob",
                turnOrder = 1,
                rackTiles = tiles.subList(14, 28)
            )
        )

        val result = tileShuffleService.createDrawPile(tiles, players)

        assertIterableEquals(tiles.drop(28), result)
        assertEquals(2, result.size)
    }

    private fun createNumberedTiles(
        startNumber: Int,
        count: Int
    ): List<Tile> {
        return (0 until count).map { offset ->
            NumberedTile(
                tileId = "tile-$offset",
                color = TileColor.entries[offset % TileColor.entries.size],
                number = ((startNumber - 1 + offset) % 13) + 1
            )
        }
    }
}
