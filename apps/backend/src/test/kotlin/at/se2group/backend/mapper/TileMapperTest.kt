package at.se2group.backend.mapper

import shared.models.game.domain.*
import shared.models.game.request.TileRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TileMapperTest {

    @Test
    fun `should map numbered tile correctly`() {
        val req = TileRequest(
            tileId = "tile-1",
            color = "RED",
            number = 5,
            isJoker = false
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
            isJoker = true
        )

        val result = req.toTileDomain()

        assertTrue(result is JokerTile)
        val tile = result as JokerTile

        assertEquals(TileColor.BLACK, tile.color)
        assertEquals("tile-2", tile.tileId)
    }
}
