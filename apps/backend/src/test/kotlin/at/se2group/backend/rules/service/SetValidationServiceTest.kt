package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import shared.models.game.domain.BoardSet
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

class SetValidationServiceTest {

    private val groupValidationService = mock<GroupValidationService>()
    private val runValidationService = mock<RunValidationService>()
    private val service = SetValidationService(groupValidationService, runValidationService)

    private val boardSet = BoardSet(
        boardSetId = "set-1",
        tiles = listOf(
            NumberedTile("tile-1", TileColor.RED, 1),
            NumberedTile("tile-2", TileColor.RED, 2),
            NumberedTile("tile-3", TileColor.RED, 3)
        )
    )

    @Test
    fun `returns valid when only group validator is valid`() {
        whenever(groupValidationService.validate(boardSet)).thenReturn(valid())
        whenever(runValidationService.validate(boardSet)).thenReturn(invalid(runViolation()))

        val result = service.validate(boardSet)

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
        verify(groupValidationService).validate(boardSet)
        verify(runValidationService).validate(boardSet)
    }

    @Test
    fun `returns valid when only run validator is valid`() {
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

    private fun groupViolation(): RuleViolation =
        RuleViolation(
            code = "GROUP_INVALID",
            message = "Group validation failed",
            setIndex = 0,
            tileIds = listOf("tile-1")
        )

    private fun runViolation(): RuleViolation =
        RuleViolation(
            code = "RUN_INVALID",
            message = "Run validation failed",
            setIndex = 0,
            tileIds = listOf("tile-2")
        )
}
