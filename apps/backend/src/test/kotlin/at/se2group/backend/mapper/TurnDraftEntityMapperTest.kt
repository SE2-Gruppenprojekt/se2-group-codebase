package at.se2group.backend.mapper

import at.se2group.backend.domain.*
import at.se2group.backend.persistence.TurnDraftEntity
import at.se2group.backend.persistence.TurnDraftBoardSetEntity
import at.se2group.backend.persistence.TileEmbeddable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
                        NumberedTile(TileColor.RED, 5),
                        NumberedTile(TileColor.BLUE, 6)
                    )
                )
            ),
            rackTiles = listOf(
                JokerTile(TileColor.BLACK)
            )
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
    }

    @Test
    fun `should map TurnDraftEntity to TurnDraft`() {

        val entity = TurnDraftEntity(
            gameId = "game1",
            playerUserId = "user1"
        )

        val boardSet = TurnDraftBoardSetEntity(
            draft = entity,
            tiles = mutableListOf(
                TileEmbeddable(TileColor.RED, 5, false),
                TileEmbeddable(TileColor.BLUE, 6, false)
            )
        )

        entity.boardSets.add(boardSet)

        entity.rackTiles = mutableListOf(
            TileEmbeddable(TileColor.BLACK, null, true)
        )

        val domain = entity.toDomain()

        assertEquals(1, domain.boardSets.size)
        assertEquals(2, domain.boardSets[0].tiles.size)
        assertEquals(1, domain.rackTiles.size)
    }
}
