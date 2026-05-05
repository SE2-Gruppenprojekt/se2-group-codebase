package at.se2group.backend.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.Instant

class GameDtoTest {

    @Test
    fun `creates tile response`() {
        val dto = TileResponse(
            tileId = "tile-1",
            color = "BLUE",
            number = 7,
            isJoker = false
        )

        assertEquals("tile-1", dto.tileId)
        assertEquals("BLUE", dto.color)
        assertEquals(7, dto.number)
        assertEquals(false, dto.isJoker)
    }

    @Test
    fun `creates joker tile response`() {
        val dto = TileResponse(
            tileId = "tile-2",
            color = "RED",
            number = null,
            isJoker = true
        )

        assertEquals("tile-2", dto.tileId)
        assertEquals("RED", dto.color)
        assertNull(dto.number)
        assertTrue(dto.isJoker)
    }

    @Test
    fun `creates board set response`() {
        val dto = BoardSetResponse(
            boardSetId = "set-1",
            type = "RUN",
            tiles = listOf(
                TileResponse("tile-3", "BLUE", 7, false),
                TileResponse("tile-4", "BLUE", 8, false),
                TileResponse("tile-5", "BLUE", 9, false)
            )
        )

        assertEquals("set-1", dto.boardSetId)
        assertEquals("RUN", dto.type)
        assertEquals(3, dto.tiles.size)
        assertEquals(7, dto.tiles[0].number)
    }

    @Test
    fun `creates game player response`() {
        val joinedAt = Instant.parse("2026-04-27T18:00:00Z")

        val dto = GamePlayerResponse(
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0,
            rackTiles = listOf(
                TileResponse("tile-6", "BLACK", 5, false),
                TileResponse("tile-7", "ORANGE", null, true)
            ),
            hasCompletedInitialMeld = true,
            score = 25,
            joinedAt = joinedAt
        )

        assertEquals("user-1", dto.userId)
        assertEquals("Alice", dto.displayName)
        assertEquals(0, dto.turnOrder)
        assertEquals(2, dto.rackTiles.size)
        assertEquals(true, dto.hasCompletedInitialMeld)
        assertEquals(25, dto.score)
        assertEquals(joinedAt, dto.joinedAt)
    }

    @Test
    fun `creates game response`() {
        val createdAt = Instant.parse("2026-04-27T18:00:00Z")
        val startedAt = Instant.parse("2026-04-27T18:05:00Z")

        val dto = GameResponse(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayerResponse(
                    userId = "user-1",
                    displayName = "Alice",
                    turnOrder = 0,
                    rackTiles = listOf(TileResponse("tile-8", "BLUE", 3, false)),
                    hasCompletedInitialMeld = false,
                    score = 10,
                    joinedAt = Instant.parse("2026-04-27T17:55:00Z")
                )
            ),
            board = listOf(
                BoardSetResponse(
                    boardSetId = "set-1",
                    type = "GROUP",
                    tiles = listOf(
                        TileResponse("tile-9", "RED", 10, false),
                        TileResponse("tile-10", "BLUE", 10, false),
                        TileResponse("tile-11", "BLACK", 10, false)
                    )
                )
            ),
            drawPile = listOf(
                TileResponse("tile-12", "ORANGE", 5, false),
                TileResponse("tile-13", "RED", null, true)
            ),
            drawPileCount = 2,
            currentPlayerUserId = "user-1",
            currentTurnPlayerId = "user-1",
            turnDeadline = null,
            remainingTurnSeconds = null,
            status = "ACTIVE",
            createdAt = createdAt,
            startedAt = startedAt,
            finishedAt = null
        )

        assertEquals("game-1", dto.gameId)
        assertEquals("lobby-1", dto.lobbyId)
        assertEquals(1, dto.players.size)
        assertEquals(1, dto.board.size)
        assertEquals(2, dto.drawPile.size)
        assertEquals(2, dto.drawPileCount)
        assertEquals("user-1", dto.currentPlayerUserId)
        assertEquals("user-1", dto.currentTurnPlayerId)
        assertEquals("ACTIVE", dto.status)
        assertEquals(createdAt, dto.createdAt)
        assertEquals(startedAt, dto.startedAt)
        assertNull(dto.finishedAt)
        assertNull(dto.turnDeadline)
        assertNull(dto.remainingTurnSeconds)
    }

    @Test
    fun `creates turn draft response`() {
        val dto = TurnDraftResponse(
            gameId = "game-1",
            playerUserId = "user-1",
            draftBoard = listOf(
                BoardSetResponse(
                    boardSetId = "set-1",
                    type = "UNRESOLVED",
                    tiles = listOf(TileResponse("tile-1", "BLUE", 3, false))
                )
            ),
            draftHand = listOf(
                TileResponse("tile-2", "RED", null, true)
            ),
            version = 5
        )

        assertEquals("game-1", dto.gameId)
        assertEquals("user-1", dto.playerUserId)
        assertEquals(1, dto.draftBoard.size)
        assertEquals(1, dto.draftHand.size)
        assertEquals(5, dto.version)
    }
}
