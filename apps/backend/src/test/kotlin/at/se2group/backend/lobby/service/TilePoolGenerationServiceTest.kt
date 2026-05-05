package at.se2group.backend.service

import at.se2group.backend.domain.JokerTile
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.TileColor
import at.se2group.backend.domain.TileRules
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TilePoolGenerationServiceTest {

    private val service = TilePoolGenerationService()

    @Test
    fun `createTilePool returns correct total number of tiles`() {
        val tiles = service.createTilePool()

        val expectedNumberedTiles =
            TileColor.entries.size *
                (TileRules.MAX_TILE_NUMBER - TileRules.MIN_TILE_NUMBER + 1) *
                TileRules.NUMBERED_TILE_COPY_COUNT

        val expectedTotal = expectedNumberedTiles + TileRules.jokerColors.size

        assertEquals(expectedTotal, tiles.size)
    }

    @Test
    fun `createTilePool contains exactly two copies of every numbered tile`() {
        val tiles = service.createTilePool()
        val numberedTiles = tiles.filterIsInstance<NumberedTile>()

        for (color in TileColor.entries) {
            for (number in TileRules.MIN_TILE_NUMBER..TileRules.MAX_TILE_NUMBER) {
                val count = numberedTiles.count { it.color == color && it.number == number }
                assertEquals(
                    TileRules.NUMBERED_TILE_COPY_COUNT,
                    count,
                    "Expected $color $number to appear ${TileRules.NUMBERED_TILE_COPY_COUNT} times"
                )
            }
        }
    }

    @Test
    fun `createTilePool contains exactly one joker for each configured joker color`() {
        val tiles = service.createTilePool()
        val jokerTiles = tiles.filterIsInstance<JokerTile>()

        assertEquals(TileRules.jokerColors.size, jokerTiles.size)

        for (color in TileRules.jokerColors) {
            val count = jokerTiles.count { it.color == color }
            assertEquals(1, count, "Expected exactly one joker with color $color")
        }
    }

    @Test
    fun `createTilePool contains only configured joker colors for jokers`() {
        val tiles = service.createTilePool()
        val jokerTiles = tiles.filterIsInstance<JokerTile>()

        assertTrue(jokerTiles.all { it.color in TileRules.jokerColors })
    }

    @Test
    fun `createTilePool assigns unique tile ids`() {
        val tiles = service.createTilePool()

        assertEquals(tiles.size, tiles.map { it.tileId }.toSet().size)
        assertTrue(tiles.all { it.tileId.isNotBlank() })
    }
}
