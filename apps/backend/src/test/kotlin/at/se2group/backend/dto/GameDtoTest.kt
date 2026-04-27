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
            color = "BLUE",
            number = 7,
            joker = false
        )

        assertEquals("BLUE", dto.color)
        assertEquals(7, dto.number)
        assertEquals(false, dto.joker)
    }

    @Test
    fun `creates joker tile response`() {
        val dto = TileResponse(
            color = "RED",
            number = null,
            joker = true
        )

        assertEquals("RED", dto.color)
        assertNull(dto.number)
        assertTrue(dto.joker)
    }

    @Test
    fun `creates board set response`() {
        val dto = BoardSetResponse(
            boardSetId = "set-1",
            type = "RUN",
            tiles = listOf(
                TileResponse("BLUE", 7, false),
                TileResponse("BLUE", 8, false),
                TileResponse("BLUE", 9, false)
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
                TileResponse("BLACK", 5, false),
                TileResponse("ORANGE", null, true)
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
                    rackTiles = listOf(TileResponse("BLUE", 3, false)),
                    hasCompletedInitialMeld = false,
                    score = 10,
                    joinedAt = Instant.parse("2026-04-27T17:55:00Z")
                )
            ),
            boardSets = listOf(
                BoardSetResponse(
                    boardSetId = "set-1",
                    type = "SET",
                    tiles = listOf(
                        TileResponse("RED", 10, false),
                        TileResponse("BLUE", 10, false),
                        TileResponse("BLACK", 10, false)
                    )
                )
            ),
            drawPile = listOf(
                TileResponse("ORANGE", 5, false),
                TileResponse("RED", null, true)
            ),
            currentPlayerUserId = "user-1",
            status = "ACTIVE",
            createdAt = createdAt,
            startedAt = startedAt,
            finishedAt = null
        )

        assertEquals("game-1", dto.gameId)
        assertEquals("lobby-1", dto.lobbyId)
        assertEquals(1, dto.players.size)
        assertEquals(1, dto.boardSets.size)
        assertEquals(2, dto.drawPile.size)
        assertEquals("user-1", dto.currentPlayerUserId)
        assertEquals("ACTIVE", dto.status)
        assertEquals(createdAt, dto.createdAt)
        assertEquals(startedAt, dto.startedAt)
        assertNull(dto.finishedAt)
    }
}
