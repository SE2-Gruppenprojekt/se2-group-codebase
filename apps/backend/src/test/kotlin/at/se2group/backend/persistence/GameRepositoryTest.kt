package at.se2group.backend.persistence

import at.se2group.backend.domain.BoardSetType
import at.se2group.backend.domain.GameStatus
import at.se2group.backend.domain.TileColor
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
                TileEmbeddable(color = TileColor.RED, number = 5, joker = false),
                TileEmbeddable(color = TileColor.BLUE, number = null, joker = true),
                TileEmbeddable(color = TileColor.BLACK, number = 11, joker = false)
            )
        )

        val player1 = GamePlayerEntity(
            game = game,
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0,
            rackTiles = mutableListOf(
                TileEmbeddable(color = TileColor.BLUE, number = 1, joker = false),
                TileEmbeddable(color = TileColor.ORANGE, number = null, joker = true)
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
                TileEmbeddable(color = TileColor.BLACK, number = 13, joker = false)
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
                TileEmbeddable(color = TileColor.RED, number = 7, joker = false),
                TileEmbeddable(color = TileColor.RED, number = 8, joker = false),
                TileEmbeddable(color = TileColor.RED, number = 9, joker = false)
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
                TileEmbeddable(color = TileColor.BLACK, number = 13, joker = false),
                TileEmbeddable(color = TileColor.ORANGE, number = null, joker = true),
                TileEmbeddable(color = TileColor.BLUE, number = 1, joker = false)
            )
        )

        val player1 = GamePlayerEntity(
            game = game,
            userId = "user-2",
            displayName = "Bob",
            turnOrder = 1,
            rackTiles = mutableListOf(
                TileEmbeddable(color = TileColor.RED, number = 4, joker = false),
                TileEmbeddable(color = TileColor.BLUE, number = 5, joker = false)
            ),
            joinedAt = Instant.parse("2026-04-27T17:56:00Z")
        )

        val player2 = GamePlayerEntity(
            game = game,
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0,
            rackTiles = mutableListOf(
                TileEmbeddable(color = TileColor.BLACK, number = 2, joker = false)
            ),
            joinedAt = Instant.parse("2026-04-27T17:55:00Z")
        )

        val boardSet1 = BoardSetEntity(
            game = game,
            boardSetId = "set-b",
            type = BoardSetType.SET,
            tiles = mutableListOf(
                TileEmbeddable(color = TileColor.RED, number = 10, joker = false)
            )
        )

        val boardSet2 = BoardSetEntity(
            game = game,
            boardSetId = "set-a",
            type = BoardSetType.RUN,
            tiles = mutableListOf(
                TileEmbeddable(color = TileColor.BLUE, number = 3, joker = false),
                TileEmbeddable(color = TileColor.BLUE, number = 4, joker = false)
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
}
