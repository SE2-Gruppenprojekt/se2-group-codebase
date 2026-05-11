package at.se2group.backend.game.service

import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.service.DrawTileService
import at.se2group.backend.persistence.GameEntity
import at.se2group.backend.persistence.GamePlayerEntity
import at.se2group.backend.persistence.TileEmbeddable
import shared.models.game.domain.GameStatus
import shared.models.game.domain.TileColor
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.Mockito.verify
import java.util.Optional
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DrawTileServiceTest {

    @Mock
    lateinit var gameRepository: GameRepository

    lateinit var gameService: GameService
    lateinit var drawTileService: DrawTileService

    @BeforeEach
    fun setup() {
        gameService = GameService(gameRepository)
        drawTileService = DrawTileService(gameRepository, gameService)
    }



    fun gameEntity(
        gameId: String = "game-1",
        currentPlayerUserId: String = "user-1",
        status: GameStatus = GameStatus.ACTIVE,
        drawPile: MutableList<TileEmbeddable> = mutableListOf()
    ): GameEntity {
        val entity = GameEntity(
            gameId = gameId,
            lobbyId = "lobby-1",
            currentPlayerUserId = currentPlayerUserId,
            status = status,
            createdAt = Instant.parse("2026-04-27T18:00:00Z"),
            drawPile = drawPile
        )
        entity.players = mutableListOf(
            GamePlayerEntity(
                game = entity,
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                rackTiles = mutableListOf(),
                hasCompletedInitialMeld = false,
                score = 0,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z")
            ),
            GamePlayerEntity(
                game = entity,
                userId = "user-2",
                displayName = "Bob",
                turnOrder = 1,
                rackTiles = mutableListOf(),
                hasCompletedInitialMeld = false,
                score = 0,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z")
            )
        )
        return entity
    }

    fun tileEmbeddable(id: String) = TileEmbeddable(
        id,
        TileColor.RED,
        5,
        false
    )


    @Test
    fun `rejects draw when game does not exist`() {
        `when`(gameRepository.findById("missing-game"))
            .thenReturn(Optional.empty())


        val exception = assertThrows  <NoSuchElementException> {
            drawTileService.drawTile("missing-game", "user-1")
        }
        assertEquals("Game not found", exception.message)
    }

    @Test
    fun `rejects draw when game is not active`() {
        val entity = gameEntity(status = GameStatus.WAITING)
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            drawTileService.drawTile("game-1", "user-1")
        }

        assertEquals("Game is not active", exception.message)
    }

    @Test
    fun `rejects draw when player is not part of the game`() {
        val entity = gameEntity()
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            drawTileService.drawTile("game-1", "unknown-player")
        }

        assertEquals("Player is not in the game", exception.message)
    }

    @Test
    fun `rejects draw when it is not the player's turn`() {
        val entity = gameEntity(currentPlayerUserId = "user-1")
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            drawTileService.drawTile("game-1", "user-2")
        }
        assertEquals("User is not the active player", exception.message)
    }

    @Test
    fun `rejects draw when draw pile is empty`() {
        val entity = gameEntity(drawPile = mutableListOf())
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            drawTileService.drawTile("game-1", "user-1")
        }

        assertEquals("Draw pile is empty", exception.message)
    }

    @Test
    fun `draw tile added to active player's rack`() {
        val tile = tileEmbeddable("tile-1")
        val entity = gameEntity(drawPile = mutableListOf(tile))
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))
        `when`(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }


        val result = drawTileService.drawTile("game-1", "user-1")

        val activePlayer = result.players.first { it.userId == "user-1" }
        assertEquals(1, activePlayer.rackTiles.size)
        assertEquals("tile-1", activePlayer.rackTiles.first().tileId)

    }

    @Test
    fun `draw tile decreases by one after successful draw`() {
        val tile1 = tileEmbeddable("tile-1")
        val tile2 = tileEmbeddable("tile-2")
        val entity = gameEntity(drawPile = mutableListOf(tile1, tile2))
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))
        `when`(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }


        val result = drawTileService.drawTile("game-1", "user-1")

        assertEquals(1, result.drawPile.size)
    }

    @Test
    fun `turn goes to next player after successful draw`() {
        val tile = tileEmbeddable("tile-1")
        val entity = gameEntity(currentPlayerUserId = "user-1", drawPile = mutableListOf(tile))
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))
        `when`(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }


        val result = drawTileService.drawTile("game-1", "user-1")

        assertEquals("user-2", result.currentPlayerUserId)
    }

    @Test
    fun `turn wraps around to first player after successful last player's draw`() {
        val tile = tileEmbeddable("tile-1")
        val entity = gameEntity(currentPlayerUserId = "user-2", drawPile = mutableListOf(tile))
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))
        `when`(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }


        val result = drawTileService.drawTile("game-1", "user-2")

        assertEquals("user-1", result.currentPlayerUserId)
    }

    @Test
    fun `persists game after successful draw`() {
        val tile = tileEmbeddable("tile-1")
        val entity = gameEntity(drawPile = mutableListOf(tile))
        `when`(gameRepository.findById("game-1"))
            .thenReturn(Optional.of(entity))
        `when`(gameRepository.save(any()))
            .thenAnswer { it.arguments[0] }

        drawTileService.drawTile("game-1", "user-1")

        verify(gameRepository).save(any())
    }


}
