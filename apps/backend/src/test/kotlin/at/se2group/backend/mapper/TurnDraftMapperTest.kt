package at.se2group.backend.mapper

import at.se2group.backend.domain.*
import at.se2group.backend.dto.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TurnDraftMapperTest {

    @Test
    fun `should map UpdateDraftRequest to TurnDraft`() {

        val request = UpdateDraftRequest(
            boardSets = listOf(
                BoardSetRequest(
                    tiles = listOf(
                        TileRequest("tile-1", "RED", 3, false),
                        TileRequest("tile-2", "BLUE", 4, false)
                    )
                )
            ),
            rackTiles = listOf(
                TileRequest("tile-3", "BLACK", null, true)
            )
        )

        val result = request.toDomain(
            gameId = "game1",
            userId = "user1"
        )

        assertEquals("game1", result.gameId)
        assertEquals("user1", result.playerUserId)

        assertEquals(1, result.boardSets.size)
        assertEquals(2, result.boardSets[0].tiles.size)
        assertEquals("tile-1", (result.boardSets[0].tiles[0] as NumberedTile).tileId)

        assertEquals(1, result.rackTiles.size)
        assertTrue(result.rackTiles[0] is JokerTile)
        assertEquals("tile-3", (result.rackTiles[0] as JokerTile).tileId)
    }

    @Test
    fun `toTileDomain maps joker`() {
        val request = TileRequest(
            tileId = "tile-4",
            color = "RED",
            number = null,
            joker = true
        )

        val result = request.toTileDomain()

        assertTrue(result is JokerTile)
        assertEquals("tile-4", (result as JokerTile).tileId)
    }

    @Test
    fun `toTileDomain throws if number missing`() {
        val request = TileRequest(
            tileId = "tile-5",
            color = "RED",
            number = null,
            joker = false
        )

        assertThrows(IllegalArgumentException::class.java) {
            request.toTileDomain()
        }
    }
}
