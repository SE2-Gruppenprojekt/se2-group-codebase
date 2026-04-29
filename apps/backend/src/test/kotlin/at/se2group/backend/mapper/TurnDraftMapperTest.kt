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
                        TileRequest("RED", 3, false),
                        TileRequest("BLUE", 4, false)
                    )
                )
            ),
            rackTiles = listOf(
                TileRequest("BLACK", null, true)
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

        assertEquals(1, result.rackTiles.size)
        assertTrue(result.rackTiles[0] is JokerTile)
    }
}
