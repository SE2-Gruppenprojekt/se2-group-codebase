package at.se2group.backend.mapper

import shared.models.game.domain.*
import shared.models.game.request.BoardSetRequest
import shared.models.game.request.TileRequest
import shared.models.game.request.UpdateDraftRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TurnDraftMapperTest {

    @Test
    fun `should map UpdateDraftRequest to TurnDraft`() {

        val request = UpdateDraftRequest(
            boardSets = listOf(
                BoardSetRequest(
                    boardSetId = "set-1",
                    type = BoardSetType.RUN,
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
        assertEquals(0, result.version)

        assertEquals(1, result.boardSets.size)
        assertEquals("set-1", result.boardSets[0].boardSetId)
        assertEquals(BoardSetType.RUN, result.boardSets[0].type)
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
            isJoker = true
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
            isJoker = false
        )

        assertThrows(IllegalArgumentException::class.java) {
            request.toTileDomain()
        }
    }

    @Test
    fun `should map TurnDraft to turn draft response`() {
        val draft = TurnDraft(
            gameId = "game1",
            playerUserId = "user1",
            boardSets = listOf(
                BoardSet(
                    boardSetId = "set-1",
                    type = BoardSetType.UNRESOLVED,
                    tiles = listOf(NumberedTile("tile-1", TileColor.RED, 3))
                )
            ),
            rackTiles = listOf(JokerTile("tile-2", TileColor.BLUE)),
            version = 7
        )

        val response = draft.toResponse()

        assertEquals("game1", response.gameId)
        assertEquals("user1", response.playerUserId)
        assertEquals(1, response.draftBoard.size)
        assertEquals(1, response.draftHand.size)
        assertEquals(7, response.version)
    }
}
