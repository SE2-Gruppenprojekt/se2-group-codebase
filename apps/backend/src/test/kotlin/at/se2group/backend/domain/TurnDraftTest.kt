package at.se2group.backend.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class TurnDraftTest {

    @Test
    fun `should return drawn tile`() {
        val tile = NumberedTile(TileColor.RED, 5)

        val draft = TurnDraft(
            gameId = "g1",
            playerUserId = "p1",
            boardSets = mutableListOf(),
            rackTiles = mutableListOf(),
            drawnTile = tile,
            status = TurnDraftStatus.values()[0],
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        assertEquals(tile, draft.drawnTile)
    }

    @Test
    fun `should return null if no drawn tile`() {
        val draft = TurnDraft(
            gameId = "g1",
            playerUserId = "p1",
            boardSets = mutableListOf(),
            rackTiles = mutableListOf(),
            drawnTile = null,
            status = TurnDraftStatus.values()[0],
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        assertNull(draft.drawnTile)
    }

    @Test
    fun `should return status`() {
        val draft = TurnDraft(
            gameId = "g1",
            playerUserId = "p1",
            boardSets = mutableListOf(),
            rackTiles = mutableListOf(),
            drawnTile = null,
            status = TurnDraftStatus.values()[0],
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        assertEquals(TurnDraftStatus.values()[0], draft.status)
    }

    @Test
    fun `should return timestamps`() {
        val created = Instant.now()
        val updated = Instant.now()

        val draft = TurnDraft(
            gameId = "g1",
            playerUserId = "p1",
            boardSets = mutableListOf(),
            rackTiles = mutableListOf(),
            drawnTile = null,
            status = TurnDraftStatus.values()[0],
            createdAt = created,
            updatedAt = updated
        )

        assertEquals(created, draft.createdAt)
        assertEquals(updated, draft.updatedAt)
    }
}
