package at.aau.serg.android.core.network.mapper


import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import shared.models.game.domain.*
import shared.models.game.response.GamePlayerResponse
import shared.models.game.response.GameResponse
import shared.models.game.response.TileResponse
import shared.models.game.response.BoardSetResponse
import java.time.Instant

class GameNetworkMapperTest {
    private val fakeDate: String = "2025-01-01T10:00:00Z"

    @Test
    fun numberedTile_to_tileRequest() {
        val tile = NumberedTile(tileId = "1", color = TileColor.RED, number = 10)
        val request = tile.toRequest()

        assertEquals("1", request.tileId)
        assertEquals("RED", request.color)
        assertEquals(10, request.number)
        assertFalse(request.isJoker)
    }

    @Test
    fun jokerTile_to_tileRequest() {
        val tile = JokerTile(tileId = "j1", color = TileColor.BLACK)
        val request = tile.toRequest()

        assertEquals("j1", request.tileId)
        assertEquals("BLACK", request.color)
        assertNull(request.number)
        assertTrue(request.isJoker)
    }

    @Test
    fun tileResponse_to_jokerTile() {
        val response = TileResponse(tileId = "j1", color = "BLUE", number = null, isJoker = true)
        val domain = response.toDomain()

        assertTrue(domain is JokerTile)
        assertEquals(TileColor.BLUE, domain.color)
    }

    @Test
    fun tileResponse_to_numberedTile() {
        val response = TileResponse(tileId = "1", color = "BLACK", number = 5, isJoker = false)
        val domain = response.toDomain()

        assertTrue(domain is NumberedTile)
        assertEquals(5, (domain as NumberedTile).number)
        assertEquals(TileColor.BLACK, domain.color)
    }

    @Test
    fun boardset_to_boardSetRequest() {
        val boardSet = BoardSet(
            boardSetId = "b1",
            type = BoardSetType.RUN,
            tiles = listOf(NumberedTile("1", TileColor.RED, 1))
        )
        val request = boardSet.toRequest()

        assertEquals("b1", request.boardSetId)
        assertEquals(BoardSetType.RUN, request.type)
        assertEquals(1, request.tiles.size)
        assertEquals("1", request.tiles[0].tileId)
    }

    @Test
    fun boardSetResponse_to_BoardSet() {
        val boardSetResponse = BoardSetResponse(
            boardSetId = "b1",
            type = BoardSetType.GROUP.toString(),
            tiles = listOf(
                TileResponse(
                    tileId = "t1",
                    color = TileColor.BLACK.toString(),
                    number = 4,
                    isJoker = false,
                )
            )
        )
        val domain = boardSetResponse.toDomain()

        assertEquals("b1", domain.boardSetId)
        assertEquals(BoardSetType.GROUP, domain.type)
        assertEquals(1, domain.tiles.size)
        assertEquals("t1", domain.tiles[0].tileId)
    }

    @Test
    fun gamePlayerResponse_to_gamePlayer() {
        val now = Instant.now().toString()
        val response = GamePlayerResponse(
            userId = "u1",
            displayName = "Player 1",
            turnOrder = 1,
            rackTiles = listOf(TileResponse("t1", "RED", 5, false)),
            hasCompletedInitialMeld = true,
            score = 100,
            joinedAt = now
        )

        val domain = response.toDomain()

        assertEquals("u1", domain.userId)
        assertEquals("Player 1", domain.displayName)
        assertEquals(100, domain.score)
        assertEquals(Instant.parse(now), domain.joinedAt)
    }

    @Test
    fun gameResponse_to_gameResponseRequest() {
        val now = Instant.now().toString()
        val response = GameResponse(
            gameId = "g1",
            lobbyId = "l1",
            players = listOf(
                GamePlayerResponse(
                    userId = "u1",
                    displayName = "Bob",
                    turnOrder = 0,
                    rackTiles = emptyList(),
                    hasCompletedInitialMeld = false,
                    score = 0,
                    joinedAt = fakeDate
                )
            ),
            drawPile = emptyList(),
            currentPlayerUserId = "u1",
            status = GameStatus.ACTIVE.toString(),
            createdAt = now,
            startedAt = now,
            finishedAt = "null",
            board = listOf(
                BoardSetResponse(
                    boardSetId = "b1",
                    type = BoardSetType.UNRESOLVED.toString(),
                    tiles = emptyList()
                )
            ),
            drawPileCount = 0,
            currentTurnPlayerId = "u1",
            turnDeadline = fakeDate,
            remainingTurnSeconds = 0
        )

        val domain = response.toDomain()

        assertEquals("g1", domain.gameId)
        assertEquals(GameStatus.ACTIVE, domain.status)
        assertEquals(Instant.parse(now), domain.startedAt)
        assertNull(domain.finishedAt)
    }

    @Test
    fun gameResponse_to_gameResponseRequest_handlesNullTimestamps() {
        val response = GameResponse(
            gameId = "g1",
            lobbyId = "l1",
            players = listOf(
                GamePlayerResponse(
                    userId = "u1",
                    displayName = "Bob",
                    turnOrder = 0,
                    rackTiles = emptyList(),
                    hasCompletedInitialMeld = false,
                    score = 0,
                    joinedAt = fakeDate
                )
            ),
            drawPile = emptyList(),
            currentPlayerUserId = "u1",
            status = GameStatus.ACTIVE.toString(),
            createdAt = Instant.now().toString(),
            startedAt = "null",
            finishedAt = "null",
            board = emptyList(),
            drawPileCount = 0,
            currentTurnPlayerId = "u1",
            turnDeadline = "null",
            remainingTurnSeconds = 0
        )

        val domain = response.toDomain()
        assertNull(domain.startedAt)
        assertNull(domain.finishedAt)
    }
}
