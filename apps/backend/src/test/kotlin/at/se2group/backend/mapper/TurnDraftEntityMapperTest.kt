package at.se2group.backend.mapper

import at.se2group.backend.domain.*
import at.se2group.backend.persistence.TurnDraftEntity
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

        assertEquals(2, entity.boardTiles.size)
        assertEquals(1, entity.rackTiles.size)

        assertTrue(entity.rackTiles[0].joker)
    }
}
