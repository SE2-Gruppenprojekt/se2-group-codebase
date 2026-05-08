package at.se2group.backend.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import shared.models.game.request.BoardSetRequest
import shared.models.game.request.DrawTileRequest
import shared.models.game.request.EndTurnRequest
import shared.models.game.request.TileRequest
import shared.models.game.request.UpdateDraftRequest

class GameRequestDtoTest {

    @Test
    fun `creates update draft request`() {
        val request = UpdateDraftRequest(
            boardSets = listOf(
                BoardSetRequest(
                    tiles = listOf(TileRequest("tile-1", "RED", 3, false))
                )
            ),
            rackTiles = listOf(TileRequest("tile-2", "BLACK", null, true))
        )

        assertEquals(1, request.boardSets.size)
        assertEquals(1, request.rackTiles.size)
        assertEquals("tile-1", request.boardSets.first().tiles.first().tileId)
        assertEquals("tile-2", request.rackTiles.first().tileId)
    }

    @Test
    fun `creates end turn request`() {
        val request = EndTurnRequest(playerId = "player-1")

        assertEquals("player-1", request.playerId)
    }

    @Test
    fun `creates draw tile request`() {
        val request = DrawTileRequest(playerId = "player-1")

        assertEquals("player-1", request.playerId)
    }
}
