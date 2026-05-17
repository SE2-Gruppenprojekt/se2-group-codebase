package at.se2group.backend.rules.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import shared.models.game.domain.BoardSet
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TileColor
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

class BoardValidationServiceTest {

    private val setValidationService = mock<SetValidationService>()
    private val service = BoardValidationService(setValidationService)

    @Test
    fun `returns valid when all board sets are valid`() {
        val boardSet = boardSet("set-1", 1)
        whenever(setValidationService.validate(boardSet)).thenReturn(valid())

        val result = service.validate(listOf(boardSet))

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `returns valid for multiple valid board sets`() {
        val firstSet = boardSet("set-1", 1)
        val secondSet = boardSet("set-2", 4)
        whenever(setValidationService.validate(firstSet)).thenReturn(valid())
        whenever(setValidationService.validate(secondSet)).thenReturn(valid())

        val result = service.validate(listOf(firstSet, secondSet))

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `returns valid for an empty board`() {
        val result = service.validate(emptyList())

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
        verifyNoInteractions(setValidationService)
    }

    @Test
    fun `returns invalid when one board set is invalid`() {
        val boardSet = boardSet("set-1", 1)
        val violation = violation("SET_INVALID", "tile-1")
        whenever(setValidationService.validate(boardSet)).thenReturn(invalid(violation))

        val result = service.validate(listOf(boardSet))

        assertFalse(result.isValid)
        assertEquals(listOf(violation), result.violations)
    }

    @Test
    fun `returns invalid when multiple board sets are invalid`() {
        val firstSet = boardSet("set-1", 1)
        val secondSet = boardSet("set-2", 4)
        val firstViolation = violation("FIRST_INVALID", "tile-1")
        val secondViolation = violation("SECOND_INVALID", "tile-4")
        whenever(setValidationService.validate(firstSet)).thenReturn(invalid(firstViolation))
        whenever(setValidationService.validate(secondSet)).thenReturn(invalid(secondViolation))

        val result = service.validate(listOf(firstSet, secondSet))

        assertFalse(result.isValid)
        assertEquals(listOf(firstViolation, secondViolation), result.violations)
    }

    @Test
    fun `aggregates all violations from invalid sets`() {
        val firstSet = boardSet("set-1", 1)
        val secondSet = boardSet("set-2", 4)
        val firstViolation = violation("FIRST_INVALID", "tile-1")
        val secondViolation = violation("SECOND_INVALID", "tile-4")
        val thirdViolation = violation("THIRD_INVALID", "tile-5")
        whenever(setValidationService.validate(firstSet)).thenReturn(invalid(firstViolation))
        whenever(setValidationService.validate(secondSet)).thenReturn(invalid(listOf(secondViolation, thirdViolation)))

        val result = service.validate(listOf(firstSet, secondSet))

        assertFalse(result.isValid)
        assertEquals(listOf(firstViolation, secondViolation, thirdViolation), result.violations)
    }

    @Test
    fun `verifies each board set is passed to SetValidationService`() {
        val firstSet = boardSet("set-1", 1)
        val secondSet = boardSet("set-2", 4)
        val thirdSet = boardSet("set-3", 7)
        whenever(setValidationService.validate(firstSet)).thenReturn(valid())
        whenever(setValidationService.validate(secondSet)).thenReturn(invalid(violation("SET_INVALID", "tile-4")))
        whenever(setValidationService.validate(thirdSet)).thenReturn(valid())

        service.validate(listOf(firstSet, secondSet, thirdSet))

        verify(setValidationService).validate(firstSet)
        verify(setValidationService).validate(secondSet)
        verify(setValidationService).validate(thirdSet)
    }

    private fun boardSet(boardSetId: String, firstNumber: Int): BoardSet =
        BoardSet(
            boardSetId = boardSetId,
            tiles = listOf(
                NumberedTile("tile-$firstNumber", TileColor.RED, firstNumber),
                NumberedTile("tile-${firstNumber + 1}", TileColor.RED, firstNumber + 1),
                NumberedTile("tile-${firstNumber + 2}", TileColor.RED, firstNumber + 2)
            )
        )

    private fun violation(code: String, tileId: String): RuleViolation =
        RuleViolation(
            code = code,
            message = "$code failed",
            setIndex = 0,
            tileIds = listOf(tileId)
        )
}
