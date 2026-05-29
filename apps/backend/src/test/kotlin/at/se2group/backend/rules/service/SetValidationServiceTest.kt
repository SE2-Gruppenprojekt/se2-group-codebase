package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import shared.models.game.domain.BoardSet
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import shared.models.game.domain.TileColor
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

class SetValidationServiceTest {

    private val groupValidationService = mock<GroupValidationService>()
    private val runValidationService = mock<RunValidationService>()
    private val service = SetValidationService(groupValidationService, runValidationService)
    private val realService = SetValidationService(GroupValidationService(), RunValidationService())

    private val boardSet = BoardSet(
        boardSetId = "set-1",
        tiles = listOf(
            NumberedTile("tile-1", TileColor.RED, 1),
            NumberedTile("tile-2", TileColor.RED, 2),
            NumberedTile("tile-3", TileColor.RED, 3)
        )
    )

    @Test
    fun `returns valid without violations when group succeeds and run fails with violations`() {
        whenever(groupValidationService.validate(boardSet)).thenReturn(valid())
        whenever(runValidationService.validate(boardSet)).thenReturn(invalid(runViolation()))

        val result = service.validate(boardSet)

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
        verify(groupValidationService).validate(boardSet)
        verify(runValidationService).validate(boardSet)
    }

    @Test
    fun `returns valid without violations when run succeeds and group fails with violations`() {
        whenever(groupValidationService.validate(boardSet)).thenReturn(invalid(groupViolation()))
        whenever(runValidationService.validate(boardSet)).thenReturn(valid())

        val result = service.validate(boardSet)

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
        verify(groupValidationService).validate(boardSet)
        verify(runValidationService).validate(boardSet)
    }

    @Test
    fun `returns valid when both validators are valid`() {
        whenever(groupValidationService.validate(boardSet)).thenReturn(valid())
        whenever(runValidationService.validate(boardSet)).thenReturn(valid())

        val result = service.validate(boardSet)

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
        verify(groupValidationService).validate(boardSet)
        verify(runValidationService).validate(boardSet)
    }

    @Test
    fun `returns invalid with combined violations when both validators are invalid`() {
        val groupViolation = groupViolation()
        val runViolation = runViolation()
        whenever(groupValidationService.validate(boardSet)).thenReturn(invalid(groupViolation))
        whenever(runValidationService.validate(boardSet)).thenReturn(invalid(runViolation))

        val result = service.validate(boardSet)

        assertFalse(result.isValid)
        assertEquals(listOf(groupViolation, runViolation), result.violations)
        verify(groupValidationService).validate(boardSet)
        verify(runValidationService).validate(boardSet)
    }

    @Test
    fun `accepts joker set through valid group path`() {
        val result = realService.validate(
            BoardSet(
                boardSetId = "set-1",
                tiles = listOf(
                    tile("tile-1", TileColor.RED, 7),
                    tile("tile-2", TileColor.BLUE, 7),
                    joker("tile-3", TileColor.BLACK)
                )
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `accepts joker set through valid run path`() {
        val result = realService.validate(
            BoardSet(
                boardSetId = "set-1",
                tiles = listOf(
                    tile("tile-1", TileColor.RED, 3),
                    joker("tile-2", TileColor.BLACK),
                    tile("tile-3", TileColor.RED, 5)
                )
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `returns combined joker violations when group and run paths fail`() {
        val result = realService.validate(
            BoardSet(
                boardSetId = "set-1",
                tiles = listOf(
                    joker("tile-1", TileColor.RED),
                    joker("tile-2", TileColor.BLACK),
                    joker("tile-3", TileColor.RED)
                )
            )
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf("GROUP_ALL_JOKERS_NOT_ALLOWED", "RUN_ALL_JOKERS_NOT_ALLOWED"),
            result.violations.map { it.code }
        )
    }

    private fun groupViolation(): RuleViolation =
        RuleViolation(
            code = "GROUP_INVALID",
            message = "Group validation failed",
            boardSetId = "set-1",
            tileIds = listOf("tile-1")
        )

    private fun runViolation(): RuleViolation =
        RuleViolation(
            code = "RUN_INVALID",
            message = "Run validation failed",
            boardSetId = "set-1",
            tileIds = listOf("tile-2")
        )

    private fun tile(id: String, color: TileColor, number: Int): Tile =
        NumberedTile(id, color, number)

    private fun joker(id: String, color: TileColor): Tile =
        JokerTile(id, color)
}
