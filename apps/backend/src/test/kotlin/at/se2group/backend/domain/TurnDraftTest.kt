package at.se2group.backend.domain
import shared.models.game.domain.TileColor
import shared.models.game.domain.TurnDraftStatus

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class TurnDraftTest {

    @Test
    fun `should return drawn tile`() {
        val tile = NumberedTile("tile-1", TileColor.RED, 5)

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
    fun `should return version`() {
        val draft = TurnDraft(
            gameId = "g1",
            playerUserId = "p1",
            boardSets = mutableListOf(),
            rackTiles = mutableListOf(),
            version = 4,
            drawnTile = null,
            status = TurnDraftStatus.values()[0],
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        assertEquals(4, draft.version)
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

    @Test
    fun `should reject negative version`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TurnDraft(
                gameId = "g1",
                playerUserId = "p1",
                version = -1
            )
        }

        assertEquals("version must not be negative", exception.message)
    }
}
