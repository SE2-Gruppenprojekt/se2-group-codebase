package at.se2group.backend.mapper

import at.se2group.backend.persistence.TileEmbeddable
import at.se2group.backend.persistence.TurnDraftBoardSetEntity
import at.se2group.backend.persistence.TurnDraftEntity
import shared.models.game.domain.*
import shared.models.game.request.BoardSetRequest
import shared.models.game.request.TileRequest
class TurnDraftMapperTest {

    @Test
    fun `toDomain maps request board sets and rack tiles`() {
        val request = UpdateDraftRequest(
            boardSets = listOf(
                BoardSetRequest(
                    boardSetId = "set-request",
                    type = BoardSetType.GROUP,
                    tiles = listOf(
                        TileRequest("tile-1", "ORANGE", 10, false),
                        TileRequest("tile-2", "RED", null, true)
                    )
                )
            ),
            rackTiles = listOf(
                TileRequest("tile-3", "BLACK", 7, false)
            )
        )

        val draft = request.toDomain(gameId = "game-1", userId = "user-1")

        assertEquals("game-1", draft.gameId)
        assertEquals("user-1", draft.playerUserId)
        assertEquals("set-request", draft.boardSets.single().boardSetId)
        assertEquals(BoardSetType.GROUP, draft.boardSets.single().type)
        assertEquals(numbered("tile-1", TileColor.ORANGE, 10), draft.boardSets.single().tiles[0])
        assertEquals(joker("tile-2", TileColor.RED), draft.boardSets.single().tiles[1])
        assertEquals(numbered("tile-3", TileColor.BLACK, 7), draft.rackTiles.single())
    }

    @Test
    fun `toBoardSetDomain maps id type and tiles`() {
        val request = BoardSetRequest(
            boardSetId = "set-42",
            type = BoardSetType.RUN,
            tiles = listOf(
                TileRequest("tile-1", "BLUE", 8, false),
                TileRequest("tile-2", "BLACK", null, true)
            )
        )

        val boardSet = request.toBoardSetDomain()

        assertEquals("set-42", boardSet.boardSetId)
        assertEquals(BoardSetType.RUN, boardSet.type)
        assertEquals(2, boardSet.tiles.size)
        assertEquals(numbered("tile-1", TileColor.BLUE, 8), boardSet.tiles[0])
        assertEquals(joker("tile-2", TileColor.BLACK), boardSet.tiles[1])
    }

    @Test
    fun `should map UpdateDraftRequest to TurnDraft`() {

        val request = UpdateDraftRequest(
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
        assertEquals(1, response.draftBoard.size)
        assertEquals(1, response.draftHand.size)
        assertEquals(7, response.version)
        assertEquals("set-1", response.draftBoard.single().boardSetId)
        assertEquals("UNRESOLVED", response.draftBoard.single().type)
        assertEquals("tile-1", response.draftBoard.single().tiles.single().tileId)
        assertEquals("tile-2", response.draftHand.single().tileId)
        assertEquals(true, response.draftHand.single().isJoker)
    }

    @Test
    fun `toEntity maps board sets rack tiles and version`() {
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = listOf(
                BoardSet(
                    boardSetId = "set-1",
                    type = BoardSetType.RUN,
                    tiles = listOf(
                        numbered("tile-1", TileColor.RED, 3),
                        joker("tile-2", TileColor.BLUE)
                    )
                )
            ),
            rackTiles = listOf(
                numbered("tile-3", TileColor.BLACK, 7)
            ),
            version = 9
        )

        val entity = draft.toEntity(
            TurnDraftEntity(
                gameId = "game-1",
                playerUserId = "user-1"
            )
        )

        assertEquals(9, entity.version)
        assertEquals("set-1", entity.boardSets.single().boardSetId)
        assertEquals(BoardSetType.RUN, entity.boardSets.single().type)
        assertEquals("tile-1", entity.boardSets.single().tiles[0].tileId)
        assertEquals(false, entity.boardSets.single().tiles[0].joker)
        assertEquals("tile-2", entity.boardSets.single().tiles[1].tileId)
        assertEquals(true, entity.boardSets.single().tiles[1].joker)
        assertEquals("tile-3", entity.rackTiles.single().tileId)
        assertEquals(false, entity.rackTiles.single().joker)
    }

    @Test
    fun `toEntity clears existing board sets and replaces mutable state`() {
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
            rackTiles = listOf(
                joker("tile-3", TileColor.RED)
            ),
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
    fun `toResponse maps draft board and draft hand collections`() {
        val draft = TurnDraft(
            gameId = "game-2",
            playerUserId = "user-2",
            boardSets = listOf(
                BoardSet(
                    boardSetId = "set-a",
                    type = BoardSetType.RUN,
                    tiles = listOf(
                        numbered("tile-1", TileColor.BLUE, 4),
                        joker("tile-2", TileColor.RED)
                    )
                )
            ),
            rackTiles = listOf(
                numbered("tile-3", TileColor.BLACK, 9),
                joker("tile-4", TileColor.ORANGE)
            ),
            version = 3
        )

        val response = draft.toResponse()

        assertEquals("game-2", response.gameId)
        assertEquals("user-2", response.playerUserId)
        assertEquals(3, response.version)
        assertEquals(1, response.draftBoard.size)
        assertEquals("set-a", response.draftBoard.single().boardSetId)
        assertEquals("RUN", response.draftBoard.single().type)
        assertEquals(listOf("tile-1", "tile-2"), response.draftBoard.single().tiles.map { it.tileId })
        assertEquals(listOf("tile-3", "tile-4"), response.draftHand.map { it.tileId })
        assertEquals(listOf(false, true), response.draftHand.map { it.isJoker })
    }

    private fun numbered(tileId: String, color: TileColor, number: Int) =
        NumberedTile(tileId = tileId, color = color, number = number)

    private fun joker(tileId: String, color: TileColor) =
        JokerTile(tileId = tileId, color = color)
}
