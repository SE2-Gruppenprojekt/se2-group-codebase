package at.se2group.backend.rules.service

import at.se2group.backend.service.TileConservationService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import shared.models.game.domain.*
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.ValidationResult
import shared.models.game.validation.invalid
import shared.models.game.validation.valid
import java.time.Instant



@ExtendWith(MockitoExtension::class)
class RummikubRuleServiceTest {

    @Mock
    lateinit var tileConservationService: TileConservationService

    @Mock
    lateinit var boardValidationService: BoardValidationService

    private val ruleService by lazy {
        RummikubRuleService(tileConservationService, boardValidationService)
    }
    private val realRuleService = RummikubRuleService(
        TileConservationService(),
        BoardValidationService(SetValidationService(GroupValidationService(), RunValidationService()))
    )

    private val player = GamePlayer(
        userId = "user-1",
        displayName = "Alice",
        turnOrder = 0,
        joinedAt = Instant.now()
    )

    private val confirmedGame = ConfirmedGame(
        gameId = "game-1",
        lobbyId = "lobby-1",
        players = listOf(player),
        currentPlayerUserId = "user-1",
        status = GameStatus.ACTIVE
    )

    private val submittedDraft = TurnDraft(
        gameId = "game-1",
        playerUserId = "user-1"
    )



    @Test
    fun `returns valid when both tile conservation and board validation pass`() {
        whenever(tileConservationService.validate(any(), any(), any())).thenReturn(valid())
        whenever(boardValidationService.validate(any())).thenReturn(valid())

        val result = ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `invokes tile conservation service`() {
        whenever(tileConservationService.validate(any(), any(), any())).thenReturn(valid())
        whenever(boardValidationService.validate(any())).thenReturn(valid())

        ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        verify(tileConservationService).validate(confirmedGame, "user-1", submittedDraft)
    }

    @Test
    fun `invokes board validation service`() {
        whenever(tileConservationService.validate(any(), any(), any())).thenReturn(valid())
        whenever(boardValidationService.validate(any())).thenReturn(valid())

        ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        verify(boardValidationService).validate(submittedDraft.boardSets)
    }

    @Test
    fun `returns invalid when tile conservation fails`() {
        whenever(tileConservationService.validate(any(), any(), any()))
            .thenReturn(invalid("TILE_CONSERVATION_VIOLATION", "tile mismatch"))
        whenever(boardValidationService.validate(any())).thenReturn(valid())

        val result = ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        assertFalse(result.isValid)
        assertEquals("TILE_CONSERVATION_VIOLATION", result.violations.single().code)
        assertEquals("tile mismatch", result.violations.single().message)
    }

    @Test
    fun `returns invalid when board validation fails`() {
        whenever(tileConservationService.validate(any(), any(), any())).thenReturn(valid())
        val boardViolation = RuleViolation(
            code = "RUN_MIN_SIZE",
            message = "Run must have at least 3 tiles",
        )
        whenever(boardValidationService.validate(any())).thenReturn(ValidationResult(violations = listOf(boardViolation)))


        val result = ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        assertFalse(result.isValid)
        assertEquals("RUN_MIN_SIZE", result.violations.single().code)

    }


    @Test
    fun `returns invalid when both tile conservation and board validation fail`() {
        whenever(tileConservationService.validate(any(), any(), any()))
            .thenReturn(invalid("TILE_CONSERVATION_VIOLATION", "tile mismatch"))
        val boardViolation = RuleViolation(
            code = "RUN_MIN_SIZE",
            message = "Run must have at least 3 tiles",
        )
        whenever(boardValidationService.validate(any()))
            .thenReturn(ValidationResult(violations = listOf(boardViolation)))


        val result = ruleService.validateSubmittedDraft(
            confirmedGame = confirmedGame,
            actingPlayerUserId = "user-1",
            submittedDraft = submittedDraft
        )

        assertFalse(result.isValid)
        assertEquals(2, result.violations.size)
        assertEquals("TILE_CONSERVATION_VIOLATION", result.violations[0].code)
        assertEquals("RUN_MIN_SIZE", result.violations[1].code)

    }

    @Test
    fun `accepts legal joker containing submitted draft`() {
        val jokerRun = listOf(
            NumberedTile("tile-1", TileColor.RED, 3),
            JokerTile("tile-2", TileColor.BLACK),
            NumberedTile("tile-3", TileColor.RED, 5)
        )
        val game = confirmedGame.copy(
            players = listOf(player.copy(rackTiles = jokerRun))
        )
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = listOf(BoardSet(boardSetId = "set-1", tiles = jokerRun)),
            rackTiles = emptyList()
        )

        val result = realRuleService.validateSubmittedDraft(
            confirmedGame = game,
            actingPlayerUserId = "user-1",
            submittedDraft = draft
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `returns joker validation violations for illegal joker draft`() {
        val allJokers = listOf(
            JokerTile("tile-1", TileColor.RED),
            JokerTile("tile-2", TileColor.BLACK),
            JokerTile("tile-3", TileColor.RED)
        )
        val game = confirmedGame.copy(
            players = listOf(player.copy(rackTiles = allJokers))
        )
        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = listOf(BoardSet(boardSetId = "set-1", tiles = allJokers)),
            rackTiles = emptyList()
        )

        val result = realRuleService.validateSubmittedDraft(
            confirmedGame = game,
            actingPlayerUserId = "user-1",
            submittedDraft = draft
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf("GROUP_ALL_JOKERS_NOT_ALLOWED", "RUN_ALL_JOKERS_NOT_ALLOWED"),
            result.violations.map { it.code }
        )
    }



}
