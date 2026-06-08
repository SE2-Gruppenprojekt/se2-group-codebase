package at.se2group.backend.api

import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GameStatus
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import at.se2group.backend.service.GameService
import at.se2group.backend.service.TurnDraftService
import at.se2group.backend.service.DrawTileService
import at.se2group.backend.service.EndTurnService
import at.se2group.backend.service.InvalidTurnSubmissionException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import shared.models.game.domain.TurnDraft
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.post
import org.springframework.http.MediaType
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import shared.models.game.domain.GamePlayerMetrics
import shared.models.game.validation.ValidationResult

/**
 * Web-layer tests for [GameController].
 *
 * This class exercises the controller through Spring MVC with mocked game
 * services so that transport behavior can be verified independently from game
 * rules or persistence. For the error-handling PR, the important responsibility
 * here is proving that game endpoints surface the shared REST contract when:
 *
 * - request-entry failures happen before service code is reached
 * - service-layer exceptions are propagated into the global advice
 * - structured invalid-turn conflicts stay distinct from generic conflicts
 *
 * The tests in this class therefore serve as the game-specific confirmation
 * that the global error-handling policy is actually visible at the public API.
 */
@WebMvcTest(GameController::class)
@Import(GlobalExceptionHandler::class)
class GameControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var gameService: GameService

    @MockitoBean
    lateinit var turnDraftService: TurnDraftService

    @MockitoBean
    lateinit var drawTileService: DrawTileService

    @MockitoBean
    lateinit var endTurnService: EndTurnService

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
                        NumberedTile("tile-1", TileColor.BLUE, 3)
                    ),
                    score = 10,
                    joinedAt = Instant.parse("2026-04-27T17:55:00Z"),
                    metrics = GamePlayerMetrics(
                        turnsCompleted = 2,
                        tilesPlayed = 5,
                        meldsCreated = 1,
                        pointsPlayed = 18,
                        winner = false
                    )
                )
            ),
            drawPile = listOf(
                NumberedTile("tile-2", TileColor.RED, 7)
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:00:00Z"),
            totalTurnsCompleted = 4,
            winnerUserId = null
        )

        `when`(gameService.getGame("game-1")).thenReturn(game)

        mockMvc.get("/api/games/game-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
                jsonPath("$.lobbyId") { value("lobby-1") }
                jsonPath("$.currentPlayerUserId") { value("user-1") }
                jsonPath("$.currentTurnPlayerId") { value("user-1") }
                jsonPath("$.status") { value("ACTIVE") }
                jsonPath("$.drawPileCount") { value(1) }
                jsonPath("$.turnDeadline") { isEmpty() }
                jsonPath("$.remainingTurnSeconds") { isEmpty() }
                jsonPath("$.players[0].userId") { value("user-1") }
                jsonPath("$.players[0].displayName") { value("Alice") }
                jsonPath("$.players[0].rackTiles[0].tileId") { value("tile-1") }
                jsonPath("$.players[0].rackTiles[0].color") { value("BLUE") }
                jsonPath("$.players[0].rackTiles[0].number") { value(3) }
                jsonPath("$.players[0].rackTiles[0].isJoker") { value(false) }
                jsonPath("$.board") { isArray() }
                jsonPath("$.drawPile[0].tileId") { value("tile-2") }
                jsonPath("$.drawPile[0].color") { value("RED") }
                jsonPath("$.totalTurnsCompleted") { value(4) }
                jsonPath("$.winnerUserId") { isEmpty() }
                jsonPath("$.players[0].metrics.turnsCompleted") { value(2) }
                jsonPath("$.players[0].metrics.tilesPlayed") { value(5) }
                jsonPath("$.players[0].metrics.meldsCreated") { value(1) }
                jsonPath("$.players[0].metrics.pointsPlayed") { value(18) }
                jsonPath("$.players[0].metrics.winner") { value(false) }
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
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `updateDraft returns ok`() {
        val requestJson = """
        {
            "boardSets": [
                {
                    "boardSetId": "set-1",
                    "type": "UNRESOLVED",
                    "tiles": [{ "tileId": "tile-3", "color": "BLUE", "number": 3, "isJoker": false }]
                }
            ],
            "rackTiles": [
                { "tileId": "tile-4", "color": "RED", "number": 5, "isJoker": false }
            ]
        }
    """.trimIndent()

        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "mock-user",
            version = 3
        )

        `when`(
            turnDraftService.updateDraft(
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
                jsonPath("$.draftBoard") { isArray() }
                jsonPath("$.draftHand") { isArray() }
                jsonPath("$.version") { value(3) }
            }
    }



    private fun confirmedGame() = ConfirmedGame(
        gameId = "game-1",
        lobbyId = "lobby-1",
        players = listOf(
            GamePlayer(
                userId = "user-1",
                displayName = "Alice",
                turnOrder = 0,
                joinedAt = Instant.parse("2026-04-27T17:55:00Z")
            )
        ),
        currentPlayerUserId = "user-1",
        status = GameStatus.ACTIVE,
        createdAt = Instant.parse("2026-04-27T18:00:00Z")
    )

    @Test
    fun `drawTile returns 200 with game response`() {
        val game = confirmedGame()
        `when`(drawTileService.drawTile("game-1", "user-1"))
            .thenReturn(game)

        mockMvc.post("/api/games/game-1/draw") {
            header("X-User-Id", "user-1")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
                jsonPath("$.currentPlayerUserId") { value("user-1") }
                jsonPath("$.status") { value("ACTIVE") }
                jsonPath("$.totalTurnsCompleted") { value(0) }
                jsonPath("$.winnerUserId") { isEmpty() }
                jsonPath("$.players[0].metrics.turnsCompleted") { value(0) }
            }
    }

    @Test
    fun `drawTile returns 404 when game does not exist`() {
        `when`(drawTileService.drawTile(any(), any()))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.post("/api/games/missing-game/draw") {
            header("X-User-Id", "user-1")
        }
            .andExpect{
                status { isNotFound() }
                jsonPath("$.errorCode") { value("NOT_FOUND") }
                jsonPath("$.errorMessage") { value("Game not found") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `drawTile returns 409 when game rule is violated`() {
        `when`(drawTileService.drawTile(any(), any()))
            .thenThrow(IllegalStateException("Game is not active"))

        mockMvc.post("/api/games/game-1/draw") {
            header("X-User-Id", "user-1")
        }

            .andExpect{
                status { isConflict() }
                jsonPath("$.errorCode") { value("CONFLICT") }
                jsonPath("$.errorMessage") { value("Game is not active") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `drawTile returns 400 when required user header is missing`() {
        mockMvc.post("/api/games/game-1/draw")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.errorMessage") { value("Missing required header: X-User-Id") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `endTurn returns 200 with game response`() {
        val requestJson = """
            {
                "boardSets": [
                    {
                        "boardSetId": "set-1",
                        "type": "RUN",
                        "tiles": [
                            { "tileId": "tile-1", "color": "RED", "number": 3, "isJoker": false },
                            { "tileId": "tile-2", "color": "RED", "number": 4, "isJoker": false },
                            { "tileId": "tile-3", "color": "RED", "number": 5, "isJoker": false }
                        ]
                    }
                ],
                "rackTiles": [
                    { "tileId": "tile-4", "color": "BLACK", "number": 9, "isJoker": false }
                ]
            }
        """.trimIndent()

        val game = confirmedGame()

        `when`(
            endTurnService.endTurn(
                eq("game-1"),
                eq("user-1"),
                any()
            )
        ).thenReturn(game)

        mockMvc.post("/api/games/game-1/end-turn") {
            header("X-User-Id", "user-1")
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.gameId") { value("game-1") }
                jsonPath("$.currentPlayerUserId") { value("user-1") }
                jsonPath("$.status") { value("ACTIVE") }
            }
    }

    @Test
    fun `endTurn returns 404 when game or draft does not exist`() {
        `when`(endTurnService.endTurn(any(), any(), any()))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.post("/api/games/missing-game/end-turn") {
            header("X-User-Id", "user-1")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "boardSets": [],
                    "rackTiles": []
                }
            """.trimIndent()
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorCode") { value("NOT_FOUND") }
                jsonPath("$.errorMessage") { value("Game not found") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `endTurn returns 409 when game rule is violated`() {
        `when`(endTurnService.endTurn(any(), any(), any()))
            .thenThrow(IllegalStateException("Game is not active"))

        mockMvc.post("/api/games/game-1/end-turn") {
            header("X-User-Id", "user-1")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "boardSets": [],
                    "rackTiles": []
                }
            """.trimIndent()
        }
            .andExpect {
                status { isConflict() }
                jsonPath("$.errorCode") { value("CONFLICT") }
                jsonPath("$.errorMessage") { value("Game is not active") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `endTurn returns 409 when submitted draft is invalid`() {
        `when`(endTurnService.endTurn(any(), any(), any()))
            .thenThrow(InvalidTurnSubmissionException(ValidationResult()))

        mockMvc.post("/api/games/game-1/end-turn") {
            header("X-User-Id", "user-1")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "boardSets": [],
                    "rackTiles": []
                }
            """.trimIndent()
        }
            .andExpect {
                status { isConflict() }
                jsonPath("$.errorCode") { value("INVALID_TURN_SUBMISSION") }
                jsonPath("$.errorMessage") { value("Submitted draft is invalid") }
            }
    }

    @Test
    fun `updateDraft returns 400 for malformed json body`() {
        mockMvc.put("/api/games/game-1/draft") {
            header("X-User-Id", "mock-user")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "boardSets": [
                        {
                            "boardSetId": "set-1",
                            "type": "NOT_A_REAL_TYPE",
                            "tiles": []
                        }
                    ],
                    "rackTiles": []
                }
            """.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.errorMessage") { value("Malformed JSON request body") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

    @Test
    fun `getGame returns 500 for unexpected backend exception`() {
        `when`(gameService.getGame("game-1"))
            .thenThrow(RuntimeException("boom"))

        mockMvc.get("/api/games/game-1")
            .andExpect {
                status { isInternalServerError() }
                jsonPath("$.errorCode") { value("INTERNAL_SERVER_ERROR") }
                jsonPath("$.errorMessage") { value("An unexpected error occurred") }
                jsonPath("$.violations.length()") { value(0) }
            }
    }

}
