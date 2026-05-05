package at.se2group.backend.mapper

import at.se2group.backend.domain.BoardSet
import at.se2group.backend.domain.BoardSetType
import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.GameStatus
import at.se2group.backend.domain.JokerTile
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.TileColor
import at.se2group.backend.persistence.BoardSetEntity
import at.se2group.backend.persistence.GameEntity
import at.se2group.backend.persistence.GamePlayerEntity
import at.se2group.backend.persistence.TileEmbeddable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class GameMapperTest {
    @Test
    fun `toEntity maps complete confirmed game`() {
        val createdAt = Instant.parse("2026-04-27T18:00:00Z")
        val startedAt = Instant.parse("2026-04-27T18:05:00Z")

        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(
                    userId = "user-1",
                    displayName = "Alice",
                    turnOrder = 0,
                    rackTiles = listOf(
                        numbered("tile-1", TileColor.BLUE, 3),
                        joker("tile-2", TileColor.RED)
                    ),
                    hasCompletedInitialMeld = true,
                    score = 25,
                    joinedAt = Instant.parse("2026-04-27T17:55:00Z")
                ),
                GamePlayer(
                    userId = "user-2",
                    displayName = "Bob",
                    turnOrder = 1,
                    rackTiles = listOf(
                        numbered("tile-3", TileColor.BLACK, 11)
                    ),
                    hasCompletedInitialMeld = false,
                    score = 10,
                    joinedAt = Instant.parse("2026-04-27T17:56:00Z")
                )
            ),
            boardSets = listOf(
                BoardSet(
                    boardSetId = "set-1",
                    type = BoardSetType.RUN,
                    tiles = listOf(
                        numbered("tile-4", TileColor.BLUE, 7),
                        numbered("tile-5", TileColor.BLUE, 8),
                        numbered("tile-6", TileColor.BLUE, 9)
                    )
                )
            ),
            drawPile = listOf(
                numbered("tile-7", TileColor.ORANGE, 5),
                joker("tile-8", TileColor.BLACK)
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = createdAt,
            startedAt = startedAt,
            finishedAt = null
        )

        val entity = game.toEntity()

        assertEquals("game-1", entity.gameId)
        assertEquals("lobby-1", entity.lobbyId)
        assertEquals("user-1", entity.currentPlayerUserId)
        assertEquals(GameStatus.ACTIVE, entity.status)
        assertEquals(createdAt, entity.createdAt)
        assertEquals(startedAt, entity.startedAt)

        assertEquals(2, entity.players.size)
        assertEquals("user-1", entity.players[0].userId)
        assertEquals("Alice", entity.players[0].displayName)
        assertEquals(0, entity.players[0].turnOrder)
        assertEquals(2, entity.players[0].rackTiles.size)
        assertTrue(entity.players.all { it.game === entity })

        assertEquals(1, entity.boardSets.size)
        assertEquals("set-1", entity.boardSets[0].boardSetId)
        assertEquals(BoardSetType.RUN, entity.boardSets[0].type)
        assertEquals(3, entity.boardSets[0].tiles.size)
        assertTrue(entity.boardSets.all { it.game === entity })

        assertEquals(2, entity.drawPile.size)
        assertEquals("tile-7", entity.drawPile[0].tileId)
        assertEquals(TileColor.ORANGE, entity.drawPile[0].color)
        assertEquals(5, entity.drawPile[0].number)
        assertEquals(false, entity.drawPile[0].joker)
        assertEquals("tile-8", entity.drawPile[1].tileId)
        assertEquals(true, entity.drawPile[1].joker)
    }

    @Test
    fun `toDomain maps complete game entity`() {
        val entity = GameEntity(
            gameId = "game-1",
            lobbyId = "lobby-1",
            currentPlayerUserId = "user-2",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:00:00Z"),
            startedAt = Instant.parse("2026-04-27T18:05:00Z"),
            finishedAt = null,
            drawPile = mutableListOf(
                embeddable("tile-9", TileColor.RED, 6, false),
                embeddable("tile-10", TileColor.BLUE, null, true)
            )
        )

        entity.players = mutableListOf(
            GamePlayerEntity(
                game = entity,
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                rackTiles = mutableListOf(
                    embeddable("tile-11", TileColor.BLACK, 4, false)
                ),
                hasCompletedInitialMeld = true,
                score = 20,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z")
            ),
            GamePlayerEntity(
                game = entity,
                userId = "user-2",
                displayName = "Bob",
                turnOrder = 1,
                rackTiles = mutableListOf(
                    embeddable("tile-12", TileColor.ORANGE, null, true)
                ),
                hasCompletedInitialMeld = false,
                score = 15,
                joinedAt = Instant.parse("2026-04-27T17:56:00Z")
            )
        )

        entity.boardSets = mutableListOf(
            BoardSetEntity(
                game = entity,
                boardSetId = "set-1",
                type = BoardSetType.GROUP,
                tiles = mutableListOf(
                    embeddable("tile-13", TileColor.RED, 10, false),
                    embeddable("tile-14", TileColor.BLUE, 10, false),
                    embeddable("tile-15", TileColor.BLACK, 10, false)
                )
            )
        )

        val game = entity.toDomain()

        assertEquals("game-1", game.gameId)
        assertEquals("lobby-1", game.lobbyId)
        assertEquals("user-2", game.currentPlayerUserId)
        assertEquals(GameStatus.ACTIVE, game.status)

        assertEquals(2, game.players.size)
        assertEquals("Alice", game.players[0].displayName)
        assertEquals(0, game.players[0].turnOrder)
        assertEquals(1, game.players[0].rackTiles.size)

        assertEquals(1, game.boardSets.size)
        assertEquals("set-1", game.boardSets[0].boardSetId)
        assertEquals(BoardSetType.GROUP, game.boardSets[0].type)
        assertEquals(3, game.boardSets[0].tiles.size)

        assertEquals(2, game.drawPile.size)
        assertEquals(numbered("tile-9", TileColor.RED, 6), game.drawPile[0])
        assertEquals(joker("tile-10", TileColor.BLUE), game.drawPile[1])
    }

    @Test
    fun `toEntity preserves list order`() {
        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(userId = "user-2", displayName = "Bob", turnOrder = 1),
                GamePlayer(userId = "user-1", displayName = "Alice", turnOrder = 0)
            ),
            boardSets = listOf(
                BoardSet(
                    boardSetId = "set-b",
                    tiles = listOf(numbered("tile-16", TileColor.RED, 1))
                ),
                BoardSet(
                    boardSetId = "set-a",
                    tiles = listOf(numbered("tile-17", TileColor.BLUE, 2))
                )
            ),
            drawPile = listOf(
                numbered("tile-18", TileColor.BLACK, 13),
                joker("tile-19", TileColor.ORANGE),
                numbered("tile-20", TileColor.BLUE, 1)
            ),
            currentPlayerUserId = "user-2"
        )

        val entity = game.toEntity()

        assertEquals(listOf("user-2", "user-1"), entity.players.map { it.userId })
        assertEquals(listOf("set-b", "set-a"), entity.boardSets.map { it.boardSetId })
        assertEquals(
            listOf(TileColor.BLACK, TileColor.ORANGE, TileColor.BLUE),
            entity.drawPile.map { it.color }
        )
    }

    @Test
    fun `tile embeddable toDomain throws when numbered tile has no number`() {
        val tile = TileEmbeddable(
            tileId = "tile-21",
            color = TileColor.RED,
            number = null,
            joker = false
        )

        val exception = assertThrows<IllegalStateException> {
            tile.toDomain()
        }

        assertEquals("Numbered tile must have a number", exception.message)
    }

    @Test
    fun `toResponse maps complete confirmed game`() {
        val createdAt = Instant.parse("2026-04-27T18:00:00Z")
        val startedAt = Instant.parse("2026-04-27T18:05:00Z")

        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(
                    userId = "user-1",
                    displayName = "Alice",
                    turnOrder = 0,
                    rackTiles = listOf(
                        numbered("tile-22", TileColor.BLUE, 3),
                        joker("tile-23", TileColor.RED)
                    ),
                    hasCompletedInitialMeld = true,
                    score = 25,
                    joinedAt = Instant.parse("2026-04-27T17:55:00Z")
                )
            ),
            boardSets = listOf(
                BoardSet(
                    boardSetId = "set-1",
                    type = BoardSetType.RUN,
                    tiles = listOf(
                        numbered("tile-24", TileColor.BLUE, 7),
                        numbered("tile-25", TileColor.BLUE, 8),
                        joker("tile-26", TileColor.BLACK)
                    )
                )
            ),
            drawPile = listOf(
                numbered("tile-27", TileColor.ORANGE, 5),
                joker("tile-28", TileColor.BLUE)
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = createdAt,
            startedAt = startedAt,
            finishedAt = null
        )

        val response = game.toResponse()

        assertEquals("game-1", response.gameId)
        assertEquals("lobby-1", response.lobbyId)
        assertEquals("user-1", response.currentPlayerUserId)
        assertEquals("ACTIVE", response.status)
        assertEquals(createdAt, response.createdAt)
        assertEquals(startedAt, response.startedAt)
        assertEquals(1, response.players.size)
        assertEquals(1, response.boardSets.size)
        assertEquals(2, response.drawPile.size)

        assertEquals("user-1", response.players[0].userId)
        assertEquals("Alice", response.players[0].displayName)
        assertEquals(0, response.players[0].turnOrder)
        assertEquals(2, response.players[0].rackTiles.size)
        assertEquals("tile-22", response.players[0].rackTiles[0].tileId)
        assertEquals("BLUE", response.players[0].rackTiles[0].color)
        assertEquals(3, response.players[0].rackTiles[0].number)
        assertEquals(false, response.players[0].rackTiles[0].isJoker)
        assertEquals("tile-23", response.players[0].rackTiles[1].tileId)
        assertEquals("RED", response.players[0].rackTiles[1].color)
        assertEquals(null, response.players[0].rackTiles[1].number)
        assertEquals(true, response.players[0].rackTiles[1].isJoker)

        assertEquals("set-1", response.boardSets[0].boardSetId)
        assertEquals("RUN", response.boardSets[0].type)
        assertEquals(3, response.boardSets[0].tiles.size)
        assertEquals("tile-26", response.boardSets[0].tiles[2].tileId)
        assertEquals("BLACK", response.boardSets[0].tiles[2].color)
        assertEquals(true, response.boardSets[0].tiles[2].isJoker)

        assertEquals("tile-27", response.drawPile[0].tileId)
        assertEquals("ORANGE", response.drawPile[0].color)
        assertEquals(5, response.drawPile[0].number)
        assertEquals(false, response.drawPile[0].isJoker)
        assertEquals("tile-28", response.drawPile[1].tileId)
        assertEquals("BLUE", response.drawPile[1].color)
        assertEquals(null, response.drawPile[1].number)
        assertEquals(true, response.drawPile[1].isJoker)
    }

    @Test
    fun `tile toResponse maps numbered and joker tiles`() {
        val numbered = numbered("tile-29", TileColor.BLACK, 11)
        val joker = joker("tile-30", TileColor.ORANGE)

        val numberedResponse = numbered.toResponse()
        val jokerResponse = joker.toResponse()

        assertEquals("tile-29", numberedResponse.tileId)
        assertEquals("BLACK", numberedResponse.color)
        assertEquals(11, numberedResponse.number)
        assertEquals(false, numberedResponse.isJoker)

        assertEquals("tile-30", jokerResponse.tileId)
        assertEquals("ORANGE", jokerResponse.color)
        assertEquals(null, jokerResponse.number)
        assertEquals(true, jokerResponse.isJoker)
    }

    private fun numbered(tileId: String, color: TileColor, number: Int) =
        NumberedTile(tileId = tileId, color = color, number = number)

    private fun joker(tileId: String, color: TileColor) =
        JokerTile(tileId = tileId, color = color)

    private fun embeddable(tileId: String, color: TileColor, number: Int?, joker: Boolean) =
        TileEmbeddable(tileId = tileId, color = color, number = number, joker = joker)
}
