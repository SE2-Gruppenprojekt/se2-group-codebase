package at.se2group.backend.mapper

import at.se2group.backend.domain.*
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.persistence.TurnDraftBoardSetEntity
import at.se2group.backend.persistence.TileEmbeddable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.TileColor

class TurnDraftEntityMapperTest {

    @Test
    fun `should map TurnDraft to TurnDraftEntity`() {

        val draft = TurnDraft(
            gameId = "game1",
            playerUserId = "user1",
            boardSets = listOf(
                BoardSet(
                    boardSetId = "1",
                    type = BoardSetType.UNRESOLVED,
                    tiles = listOf(
                        NumberedTile("tile-1", TileColor.RED, 5),
                        NumberedTile("tile-2", TileColor.BLUE, 6)
                    )
                )
            ),
            rackTiles = listOf(
                JokerTile("tile-3", TileColor.BLACK)
            ),
            version = 4
        )

        val entity = draft.toEntity(
            TurnDraftEntity(
                gameId = "game1",
                playerUserId = "user1"
            )
        )

        assertEquals(1, entity.boardSets.size)
        assertEquals(2, entity.boardSets[0].tiles.size)
        assertEquals(1, entity.rackTiles.size)

        assertTrue(entity.rackTiles[0].joker)
        assertEquals("tile-3", entity.rackTiles[0].tileId)
        assertEquals(4, entity.version)
    }

    @Test
    fun `should map TurnDraftEntity to TurnDraft`() {

        val entity = TurnDraftEntity(
            gameId = "game1",
            playerUserId = "user1",
            version = 6
        )

        val boardSet = TurnDraftBoardSetEntity(
            draft = entity,
            tiles = mutableListOf(
                TileEmbeddable("tile-4", TileColor.RED, 5, false),
                TileEmbeddable("tile-5", TileColor.BLUE, 6, false)
            )
        )

        entity.boardSets.add(boardSet)

        entity.rackTiles = mutableListOf(
            TileEmbeddable("tile-6", TileColor.BLACK, null, true)
        )

        val domain = entity.toDomain()

        assertEquals(1, domain.boardSets.size)
        assertEquals(2, domain.boardSets[0].tiles.size)
        assertEquals(1, domain.rackTiles.size)
        assertEquals(6, domain.version)
        assertEquals("tile-4", (domain.boardSets[0].tiles[0] as NumberedTile).tileId)
        assertEquals("tile-6", (domain.rackTiles[0] as JokerTile).tileId)
    }
}
