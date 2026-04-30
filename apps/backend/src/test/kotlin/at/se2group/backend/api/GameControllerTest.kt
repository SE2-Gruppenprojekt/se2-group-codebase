package at.se2group.backend.api

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.GameStatus
import at.se2group.backend.domain.NumberedTile
import at.se2group.backend.domain.TileColor
import at.se2group.backend.service.GameService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import at.se2group.backend.domain.TurnDraft
import org.springframework.test.web.servlet.put
import org.springframework.http.MediaType
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.test.web.servlet.post


@WebMvcTest(GameController::class)
@Import(GlobalExceptionHandler::class)
class GameControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var gameService: GameService

    @Test
    fun `getGame returns game response`() {
        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(
                    userId = "user-1",
                    displayName = "Alice",
                    turnOrder = 0,
                    rackTiles = listOf(
                        NumberedTile(TileColor.BLUE, 3)
                    ),
                    score = 10,
                    joinedAt = Instant.parse("2026-04-27T17:55:00Z")
                )
            ),
            drawPile = listOf(
                NumberedTile(TileColor.RED, 7)
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:00:00Z")
        )

        `when`(gameService.getGame("game-1")).thenReturn(game)

        mockMvc.get("/api/games/game-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
                jsonPath("$.lobbyId") { value("lobby-1") }
                jsonPath("$.currentPlayerUserId") { value("user-1") }
                jsonPath("$.status") { value("ACTIVE") }
                jsonPath("$.players[0].userId") { value("user-1") }
                jsonPath("$.players[0].displayName") { value("Alice") }
                jsonPath("$.players[0].rackTiles[0].color") { value("BLUE") }
                jsonPath("$.players[0].rackTiles[0].number") { value(3) }
                jsonPath("$.players[0].rackTiles[0].joker") { value(false) }
                jsonPath("$.drawPile[0].color") { value("RED") }
            }
    }

    @Test
    fun `getGame returns 404 when game does not exist`() {
        `when`(gameService.getGame("missing-game"))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.get("/api/games/missing-game")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorCode") { value("NOT_FOUND") }
                jsonPath("$.errorMessage") { value("Game not found") }
            }
    }

    @Test
    fun `updateDraft returns ok`() {
        val requestJson = """
        {
            "boardSets": [
                { "tiles": [{ "color": "BLUE", "number": 3, "joker": false }] }
            ],
            "rackTiles": [
                { "color": "RED", "number": 5, "joker": false }
            ]
        }
    """.trimIndent()

        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "mock-user"
        )

        `when`(
            gameService.updateDraft(
                eq("game-1"),
                eq("mock-user"),
                any()
            )
        ).thenReturn(draft)

        mockMvc.put("/api/games/game-1/draft") {
            header("X-User-Id", "mock-user")
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
                jsonPath("$.playerUserId") { value("mock-user") }
            }
    }

    @Test
    fun `endTurn returns game`() {
        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(
                    userId = "user-2",
                    displayName = "Bob",
                    turnOrder = 0,
                    rackTiles = emptyList(),
                    score = 0,
                    joinedAt = Instant.now()
                )
            ),
            drawPile = emptyList(),
            currentPlayerUserId = "user-2",
            status = GameStatus.ACTIVE,
            createdAt = Instant.now()
        )

        `when`(gameService.endTurn("game-1", "user-1"))
            .thenReturn(game)

        mockMvc.post("/api/games/game-1/end-turn") {
            header("X-User-Id", "user-1")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
            }
    }

    @Test
    fun `resetDraft returns draft`() {
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        `when`(gameService.resetDraft("game-1", "user-1"))
            .thenReturn(draft)

        mockMvc.post("/api/games/game-1/reset-draft") {
            header("X-User-Id", "user-1")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
                jsonPath("$.playerUserId") { value("user-1") }
            }
    }
}
