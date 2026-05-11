package at.se2group.backend.persistence

import shared.models.game.domain.BoardSetType
import shared.models.game.domain.GameStatus
import shared.models.game.domain.TileColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Instant

@DataJpaTest
class GameRepositoryTest {

    @Autowired
    lateinit var gameRepository: GameRepository

    @Test
    fun `save and findById persists complete game graph`() {
        val game = GameEntity(
            gameId = "game-1",
            lobbyId = "lobby-1",
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:00:00Z"),
            startedAt = Instant.parse("2026-04-27T18:05:00Z"),
            finishedAt = null,
            drawPile = mutableListOf(
                embeddable("tile-1", TileColor.RED, 5, false),
                embeddable("tile-2", TileColor.BLUE, null, true),
                embeddable("tile-3", TileColor.BLACK, 11, false)
            )
        )

        val player1 = GamePlayerEntity(
            game = game,
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0,
            rackTiles = mutableListOf(
                embeddable("tile-4", TileColor.BLUE, 1, false),
                embeddable("tile-5", TileColor.ORANGE, null, true)
            ),
            hasCompletedInitialMeld = true,
            score = 25,
            joinedAt = Instant.parse("2026-04-27T17:55:00Z")
        )

        val player2 = GamePlayerEntity(
            game = game,
            userId = "user-2",
            displayName = "Bob",
            turnOrder = 1,
            rackTiles = mutableListOf(
                embeddable("tile-6", TileColor.BLACK, 13, false)
            ),
            hasCompletedInitialMeld = false,
            score = 10,
            joinedAt = Instant.parse("2026-04-27T17:56:00Z")
        )

        val boardSet = BoardSetEntity(
            game = game,
            boardSetId = "set-1",
            type = BoardSetType.RUN,
            tiles = mutableListOf(
                embeddable("tile-7", TileColor.RED, 7, false),
                embeddable("tile-8", TileColor.RED, 8, false),
                embeddable("tile-9", TileColor.RED, 9, false)
            )
        )

        game.players = mutableListOf(player1, player2)
        game.boardSets = mutableListOf(boardSet)

        gameRepository.save(game)

        val result = gameRepository.findById("game-1")

        assertTrue(result.isPresent)

        val saved = result.get()
        assertEquals("game-1", saved.gameId)
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("user-1", saved.currentPlayerUserId)
        assertEquals(GameStatus.ACTIVE, saved.status)
        assertEquals(2, saved.players.size)
        assertEquals(1, saved.boardSets.size)
        assertEquals(3, saved.drawPile.size)
        assertEquals("tile-1", saved.drawPile[0].tileId)

        assertEquals("user-1", saved.players[0].userId)
        assertEquals("Alice", saved.players[0].displayName)
        assertEquals(2, saved.players[0].rackTiles.size)
        assertEquals(true, saved.players[0].hasCompletedInitialMeld)
        assertEquals(25, saved.players[0].score)

        assertEquals("user-2", saved.players[1].userId)
        assertEquals("Bob", saved.players[1].displayName)
        assertEquals(1, saved.players[1].rackTiles.size)

        assertEquals("set-1", saved.boardSets[0].boardSetId)
        assertEquals(BoardSetType.RUN, saved.boardSets[0].type)
        assertEquals(3, saved.boardSets[0].tiles.size)
    }

    @Test
    fun `save and findById preserves collection order`() {
        val game = GameEntity(
            gameId = "game-2",
            lobbyId = "lobby-2",
            currentPlayerUserId = "user-2",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:10:00Z"),
            drawPile = mutableListOf(
                embeddable("tile-10", TileColor.BLACK, 13, false),
                embeddable("tile-11", TileColor.ORANGE, null, true),
                embeddable("tile-12", TileColor.BLUE, 1, false)
            )
        )

        val player1 = GamePlayerEntity(
            game = game,
            userId = "user-2",
            displayName = "Bob",
            turnOrder = 1,
            rackTiles = mutableListOf(
                embeddable("tile-13", TileColor.RED, 4, false),
                embeddable("tile-14", TileColor.BLUE, 5, false)
            ),
            joinedAt = Instant.parse("2026-04-27T17:56:00Z")
        )

        val player2 = GamePlayerEntity(
            game = game,
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0,
            rackTiles = mutableListOf(
                embeddable("tile-15", TileColor.BLACK, 2, false)
            ),
            joinedAt = Instant.parse("2026-04-27T17:55:00Z")
        )

        val boardSet1 = BoardSetEntity(
            game = game,
            boardSetId = "set-b",
            type = BoardSetType.GROUP,
            tiles = mutableListOf(
                embeddable("tile-16", TileColor.RED, 10, false)
            )
        )

        val boardSet2 = BoardSetEntity(
            game = game,
            boardSetId = "set-a",
            type = BoardSetType.RUN,
            tiles = mutableListOf(
                embeddable("tile-17", TileColor.BLUE, 3, false),
                embeddable("tile-18", TileColor.BLUE, 4, false)
            )
        )

        game.players = mutableListOf(player1, player2)
        game.boardSets = mutableListOf(boardSet1, boardSet2)

        gameRepository.save(game)

        val saved = gameRepository.findById("game-2").get()

        assertEquals(listOf("user-2", "user-1"), saved.players.map { it.userId })
        assertEquals(listOf("set-b", "set-a"), saved.boardSets.map { it.boardSetId })
        assertEquals(
            listOf(TileColor.BLACK, TileColor.ORANGE, TileColor.BLUE),
            saved.drawPile.map { it.color }
        )
        assertEquals(
            listOf(TileColor.RED, TileColor.BLUE),
            saved.players[0].rackTiles.map { it.color }
        )
        assertEquals(
            listOf(3, 4),
            saved.boardSets[1].tiles.map { it.number }
        )
    }

    @Test
    fun `findById returns empty for missing game`() {
        val result = gameRepository.findById("missing-game")

        assertFalse(result.isPresent)
    }

    private fun embeddable(tileId: String, color: TileColor, number: Int?, joker: Boolean) =
        TileEmbeddable(tileId = tileId, color = color, number = number, joker = joker)
}
