package shared.models.game.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationResultsHelperTest {

    @Test
    fun `valid helper returns empty valid result`() {
        val result = valid()

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `invalid helper wraps one violation`() {
        val violation = RuleViolation(
            code = "RUN_COLOR_MISMATCH",
            message = "Run tiles must have the same color"
        )

        val result = invalid(violation)

        assertFalse(result.isValid)
        assertEquals(listOf(violation), result.violations)
    }

    @Test
    fun `invalid helper can build a violation inline`() {
        val result = invalid(
            code = "GROUP_MIN_SIZE",
            message = "Group must contain at least three tiles",
            boardSetId = "set-2",
            tileIds = listOf("tile-7")
        )

        assertFalse(result.isValid)
        assertEquals("GROUP_MIN_SIZE", result.violations.single().code)
        assertEquals("set-2", result.violations.single().boardSetId)
        assertEquals(listOf("tile-7"), result.violations.single().tileIds)
    }

    @Test
    fun `invalid helper with empty list falls back to valid result`() {
        val result = invalid(emptyList())

        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `merge vararg combines violations in stable order`() {
        val first = invalid("RULE_A", "First violation")
        val second = valid()
        val third = invalid("RULE_B", "Second violation")

        val result = merge(first, second, third)

        assertFalse(result.isValid)
        assertEquals(listOf("RULE_A", "RULE_B"), result.violations.map { it.code })
    }

    @Test
    fun `iterable merge combines violations in stable order`() {
        val results = listOf(
            invalid("RULE_A", "First violation"),
            invalid("RULE_B", "Second violation")
        )

        val result = results.merge()

        assertFalse(result.isValid)
        assertEquals(2, result.violations.size)
        assertEquals("RULE_A", result.violations[0].code)
        assertEquals("RULE_B", result.violations[1].code)
    }

    @Test
    fun `plus operator merges two validation results`() {
        val left = invalid("RULE_A", "First violation")
        val right = invalid("RULE_B", "Second violation")

        val result = left + right

        assertFalse(result.isValid)
        assertEquals(listOf("RULE_A", "RULE_B"), result.violations.map { it.code })
    }
}
