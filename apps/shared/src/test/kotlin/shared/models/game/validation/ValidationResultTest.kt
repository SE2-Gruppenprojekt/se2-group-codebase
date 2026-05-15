package shared.models.game.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationResultTest {

    @Test
    fun `validation result is valid when no violations exist`() {
        val result = ValidationResult()

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `validation result is invalid when violations exist`() {
        val violation = RuleViolation(
            code = "RUN_COLOR_MISMATCH",
            message = "Run tiles must have the same color"
        )

        val result = ValidationResult(violations = listOf(violation))

        assertFalse(result.isValid)
        assertEquals(listOf(violation), result.violations)
    }

    @Test
    fun `rule violation keeps optional defaults`() {
        val violation = RuleViolation(
            code = "GROUP_MIN_SIZE",
            message = "Group must contain at least three tiles"
        )

        assertEquals("GROUP_MIN_SIZE", violation.code)
        assertEquals("Group must contain at least three tiles", violation.message)
        assertNull(violation.boardSetId)
        assertTrue(violation.tileIds.isEmpty())
    }

    @Test
    fun `rule violation keeps set and tile references`() {
        val violation = RuleViolation(
            code = "TILE_DUPLICATED",
            message = "Submitted draft contains a duplicated tile",
            boardSetId = 1,
            tileIds = listOf("tile-17", "tile-22")
        )

        assertEquals(1, violation.boardSetId)
        assertEquals(listOf("tile-17", "tile-22"), violation.tileIds)
    }
}
