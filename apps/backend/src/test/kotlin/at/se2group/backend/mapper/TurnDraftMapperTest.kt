package at.se2group.backend.mapper

import at.se2group.backend.persistence.TileEmbeddable
import at.se2group.backend.persistence.TurnDraftBoardSetEntity
import at.se2group.backend.persistence.TurnDraftEntity
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
    fun `toBoardSetDomain maps id type and tiles`() {
        val request = BoardSetRequest(
            boardSetId = "set-42",
            type = BoardSetType.GROUP,
            tiles = listOf(
                TileRequest("tile-1", "BLUE", 8, false),
                TileRequest("tile-2", "BLACK", null, true)
            )
        )

        val boardSet = request.toBoardSetDomain()

        assertEquals("set-42", boardSet.boardSetId)
        assertEquals(BoardSetType.GROUP, boardSet.type)
        assertEquals(numbered("tile-1", TileColor.BLUE, 8), boardSet.tiles[0])
        assertEquals(joker("tile-2", TileColor.BLACK), boardSet.tiles[1])
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
    fun `toTileDomain maps numbered tile`() {
        val request = TileRequest(
            tileId = "tile-6",
            color = "BLACK",
            number = 12,
            isJoker = false
        )

        val result = request.toTileDomain()

        assertEquals(numbered("tile-6", TileColor.BLACK, 12), result)
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
    fun `toEntity maps and replaces mutable state`() {
        val existing = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1",
            version = 1
        )
        existing.boardSets.add(
            TurnDraftBoardSetEntity(
                draft = existing,
                boardSetId = "old-set",
                type = BoardSetType.UNRESOLVED,
                tiles = mutableListOf(
                    TileEmbeddable("old-tile", TileColor.RED, 1, false)
                )
            )
        )
        existing.rackTiles = mutableListOf(
            TileEmbeddable("old-rack-tile", TileColor.BLUE, 2, false)
        )

        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = listOf(
                BoardSet(
                    boardSetId = "new-set",
                    type = BoardSetType.GROUP,
                    tiles = listOf(
                        numbered("tile-1", TileColor.BLACK, 11),
                        joker("tile-2", TileColor.ORANGE)
                    )
                )
            ),
            rackTiles = listOf(joker("tile-3", TileColor.RED)),
            version = 5
        )

        val mapped = draft.toEntity(existing)

        assertSame(existing, mapped)
        assertEquals(5, mapped.version)
        assertEquals(1, mapped.boardSets.size)
        assertEquals("new-set", mapped.boardSets.single().boardSetId)
        assertEquals(BoardSetType.GROUP, mapped.boardSets.single().type)
        assertEquals(listOf("tile-1", "tile-2"), mapped.boardSets.single().tiles.map { it.tileId })
        assertEquals(listOf("tile-3"), mapped.rackTiles.map { it.tileId })
    }

    @Test
    fun `entity toDomain maps board sets rack tiles and version`() {
        val entity = TurnDraftEntity(
            gameId = "game-1",
            playerUserId = "user-1",
            version = 11
        )
        entity.boardSets.add(
            TurnDraftBoardSetEntity(
                draft = entity,
                boardSetId = "set-1",
                type = BoardSetType.GROUP,
                tiles = mutableListOf(
                    TileEmbeddable("tile-1", TileColor.RED, 10, false),
                    TileEmbeddable("tile-2", TileColor.BLUE, null, true)
                )
            )
        )
        entity.rackTiles = mutableListOf(
            TileEmbeddable("tile-3", TileColor.BLACK, 7, false)
        )

        val draft = entity.toDomain()

        assertEquals("game-1", draft.gameId)
        assertEquals("user-1", draft.playerUserId)
        assertEquals(11, draft.version)
        assertEquals("set-1", draft.boardSets.single().boardSetId)
        assertEquals(BoardSetType.GROUP, draft.boardSets.single().type)
        assertEquals(numbered("tile-1", TileColor.RED, 10), draft.boardSets.single().tiles[0])
        assertEquals(joker("tile-2", TileColor.BLUE), draft.boardSets.single().tiles[1])
        assertEquals(numbered("tile-3", TileColor.BLACK, 7), draft.rackTiles.single())
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
        assertEquals("set-1", response.draftBoard.single().boardSetId)
        assertEquals("UNRESOLVED", response.draftBoard.single().type)
        assertEquals("tile-1", response.draftBoard.single().tiles.single().tileId)
        assertEquals("tile-2", response.draftHand.single().tileId)
        assertTrue(response.draftHand.single().isJoker)
    }

    private fun numbered(tileId: String, color: TileColor, number: Int) =
        NumberedTile(tileId = tileId, color = color, number = number)

    private fun joker(tileId: String, color: TileColor) =
        JokerTile(tileId = tileId, color = color)
}
