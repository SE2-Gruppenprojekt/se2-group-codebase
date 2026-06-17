package at.se2group.backend.api

import at.se2group.backend.security.JwtAuthenticationFilter
import at.se2group.backend.security.InvalidBearerTokenAuthenticationException
import at.se2group.backend.security.JwtService
import at.se2group.backend.security.RestAccessDeniedHandler
import at.se2group.backend.security.RestAuthenticationEntryPoint
import at.se2group.backend.security.SecurityConfig
import at.se2group.backend.service.DrawTileService
import at.se2group.backend.service.EndTurnService
import at.se2group.backend.service.GameService
import at.se2group.backend.service.InvalidTurnSubmissionException
import at.se2group.backend.service.TurnDraftService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GamePlayerMetrics
import shared.models.game.domain.GameStatus
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.domain.TurnDraft
import shared.models.game.validation.ValidationResult
import java.time.Instant

@WebMvcTest(GameController::class)
@Import(
    GlobalExceptionHandler::class,
    SecurityConfig::class,
    RestAuthenticationEntryPoint::class,
    RestAccessDeniedHandler::class,
    JwtAuthenticationFilter::class
)
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

    @MockitoBean
    lateinit var jwtService: JwtService

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
                    rackTiles = listOf(NumberedTile("tile-1", TileColor.BLUE, 3)),
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
            drawPile = listOf(NumberedTile("tile-2", TileColor.RED, 7)),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = Instant.parse("2026-04-27T18:00:00Z"),
            totalTurnsCompleted = 4,
            winnerUserId = null
        )

        `when`(gameService.getGameForUser("game-1", "user-1")).thenReturn(game)

        mockMvc.get("/api/games/game-1") {
            with(user("user-1"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.gameId") { value("game-1") }
            jsonPath("$.lobbyId") { value("lobby-1") }
            jsonPath("$.currentPlayerUserId") { value("user-1") }
            jsonPath("$.currentTurnPlayerId") { value("user-1") }
            jsonPath("$.status") { value("ACTIVE") }
            jsonPath("$.drawPileCount") { value(1) }
            jsonPath("$.players[0].metrics.turnsCompleted") { value(2) }
            jsonPath("$.players[0].metrics.tilesPlayed") { value(5) }
            jsonPath("$.players[0].metrics.meldsCreated") { value(1) }
            jsonPath("$.players[0].metrics.pointsPlayed") { value(18) }
            jsonPath("$.players[0].metrics.winner") { value(false) }
            jsonPath("$.totalTurnsCompleted") { value(4) }
            jsonPath("$.winnerUserId") { isEmpty() }
        }
    }

    @Test
    fun `getGame returns 404 when game does not exist`() {
        `when`(gameService.getGameForUser("missing-game", "user-1"))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.get("/api/games/missing-game") {
            with(user("user-1"))
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("NOT_FOUND") }
            jsonPath("$.errorMessage") { value("Game not found") }
        }
    }

    @Test
    fun `updateDraft returns ok`() {
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "mock-user",
            version = 3
        )

        `when`(turnDraftService.updateDraft(eq("game-1"), eq("mock-user"), any())).thenReturn(draft)

        mockMvc.put("/api/games/game-1/draft") {
            with(user("mock-user"))
            contentType = MediaType.APPLICATION_JSON
            content = """
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
        }.andExpect {
            status { isOk() }
            jsonPath("$.playerUserId") { value("mock-user") }
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
        `when`(drawTileService.drawTile("game-1", "user-1")).thenReturn(confirmedGame())

        mockMvc.post("/api/games/game-1/draw") {
            with(user("user-1"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.gameId") { value("game-1") }
            jsonPath("$.currentPlayerUserId") { value("user-1") }
        }
    }

    @Test
    fun `drawTile returns 404 when game does not exist`() {
        `when`(drawTileService.drawTile(any(), any()))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.post("/api/games/missing-game/draw") {
            with(user("user-1"))
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("NOT_FOUND") }
            jsonPath("$.errorMessage") { value("Game not found") }
        }
    }

    @Test
    fun `drawTile returns 409 when game rule is violated`() {
        `when`(drawTileService.drawTile(any(), any()))
            .thenThrow(IllegalStateException("Game is not active"))

        mockMvc.post("/api/games/game-1/draw") {
            with(user("user-1"))
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("CONFLICT") }
            jsonPath("$.errorMessage") { value("Game is not active") }
        }
    }

    @Test
    fun `drawTile returns 401 when bearer authentication is missing`() {
        mockMvc.post("/api/games/game-1/draw")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.errorCode") { value("UNAUTHORIZED") }
                jsonPath("$.errorMessage") { value("Missing or invalid bearer token") }
            }
    }

    @Test
    fun `drawTile returns 401 when bearer token is invalid`() {
        `when`(jwtService.extractUserId("invalid-token"))
            .thenThrow(InvalidBearerTokenAuthenticationException())

        mockMvc.post("/api/games/game-1/draw") {
            header("Authorization", "Bearer invalid-token")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value("UNAUTHORIZED") }
            jsonPath("$.errorMessage") { value("Missing or invalid bearer token") }
        }
    }

    @Test
    fun `endTurn returns 200 with game response`() {
        `when`(endTurnService.endTurn(eq("game-1"), eq("user-1"), any())).thenReturn(confirmedGame())

        mockMvc.post("/api/games/game-1/end-turn") {
            with(user("user-1"))
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "boardSets": [],
                  "rackTiles": []
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.gameId") { value("game-1") }
        }
    }

    @Test
    fun `endTurn returns 404 when game or draft does not exist`() {
        `when`(endTurnService.endTurn(any(), any(), any()))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.post("/api/games/missing-game/end-turn") {
            with(user("user-1"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"boardSets":[],"rackTiles":[]}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("NOT_FOUND") }
        }
    }

    @Test
    fun `endTurn returns 409 when game rule is violated`() {
        `when`(endTurnService.endTurn(any(), any(), any()))
            .thenThrow(IllegalStateException("Game is not active"))

        mockMvc.post("/api/games/game-1/end-turn") {
            with(user("user-1"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"boardSets":[],"rackTiles":[]}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("CONFLICT") }
        }
    }

    @Test
    fun `endTurn returns 409 when submitted draft is invalid`() {
        `when`(endTurnService.endTurn(any(), any(), any()))
            .thenThrow(InvalidTurnSubmissionException(ValidationResult()))

        mockMvc.post("/api/games/game-1/end-turn") {
            with(user("user-1"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"boardSets":[],"rackTiles":[]}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("INVALID_TURN_SUBMISSION") }
        }
    }

    @Test
    fun `updateDraft returns 400 for malformed json body`() {
        mockMvc.put("/api/games/game-1/draft") {
            with(user("mock-user"))
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
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("BAD_REQUEST") }
            jsonPath("$.errorMessage") { value("Malformed JSON request body") }
        }
    }

    @Test
    fun `getGame returns 500 for unexpected backend exception`() {
        `when`(gameService.getGameForUser("game-1", "user-1"))
            .thenThrow(RuntimeException("boom"))

        mockMvc.get("/api/games/game-1") {
            with(user("user-1"))
        }.andExpect {
            status { isInternalServerError() }
            jsonPath("$.errorCode") { value("INTERNAL_SERVER_ERROR") }
        }
    }

    @Test
    fun `resetDraft returns 200 with TurnDraftResponse`() {
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = emptyList(),
            rackTiles = listOf(NumberedTile("rack-1", TileColor.RED, 5)),
            version = 4
        )
        `when`(turnDraftService.resetDraft("game-1", "user-1")).thenReturn(draft)

        mockMvc.post("/api/games/game-1/reset-draft") {
            with(user("user-1"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.playerUserId") { value("user-1") }
            jsonPath("$.version") { value("4") }
        }
    }

    @Test
    fun `resetDraft returns 404 when game not found`() {
        `when`(turnDraftService.resetDraft("missing", "user-1"))
            .thenThrow(NoSuchElementException("Game not found"))

        mockMvc.post("/api/games/missing/reset-draft") {
            with(user("user-1"))
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorMessage") { value("Game not found") }
        }
    }

    @Test
    fun `resetDraft returns 404 when draft not found`() {
        `when`(turnDraftService.resetDraft("game-1", "user-1"))
            .thenThrow(NoSuchElementException("Draft not found"))

        mockMvc.post("/api/games/game-1/reset-draft") {
            with(user("user-1"))
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorMessage") { value("Draft not found") }
        }
    }

    @Test
    fun `resetDraft returns 409 when user is not current player`() {
        `when`(turnDraftService.resetDraft("game-1", "user-2"))
            .thenThrow(IllegalStateException("User is not the current active player"))

        mockMvc.post("/api/games/game-1/reset-draft") {
            with(user("user-2"))
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorMessage") { value("User is not the current active player") }
        }
    }

    @Test
    fun `resetDraft returns 409 when draft belongs to different user`() {
        `when`(turnDraftService.resetDraft("game-1", "user-2"))
            .thenThrow(IllegalStateException("Draft belongs to a different user"))

        mockMvc.post("/api/games/game-1/reset-draft") {
            with(user("user-2"))
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorMessage") { value("Draft belongs to a different user") }
        }
    }

    @Test
    fun `resetDraft returns 401 when bearer authentication is missing`() {
        mockMvc.post("/api/games/game-1/reset-draft")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.errorCode") { value("UNAUTHORIZED") }
                jsonPath("$.errorMessage") { value("Missing or invalid bearer token") }
            }
    }
}
