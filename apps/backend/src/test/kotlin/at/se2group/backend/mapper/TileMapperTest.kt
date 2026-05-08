package at.se2group.backend.mapper

import at.se2group.backend.domain.*
import at.se2group.backend.dto.TileRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import shared.models.game.domain.TileColor

class TileMapperTest {

    @Test
    fun `should map numbered tile correctly`() {
        val req = TileRequest(
            tileId = "tile-1",
            color = "RED",
            number = 5,
            joker = false
        )

        val result = req.toTileDomain()

        assertTrue(result is NumberedTile)
        val tile = result as NumberedTile

        assertEquals(TileColor.RED, tile.color)
        assertEquals("tile-1", tile.tileId)
        assertEquals(5, tile.number)
    }

    @Test
    fun `should map joker tile correctly`() {
        val req = TileRequest(
            tileId = "tile-2",
            color = "BLACK",
            number = null,
            joker = true
        )

        val result = req.toTileDomain()

        assertTrue(result is JokerTile)
        val tile = result as JokerTile

        assertEquals(TileColor.BLACK, tile.color)
        assertEquals("tile-2", tile.tileId)
    }
}
