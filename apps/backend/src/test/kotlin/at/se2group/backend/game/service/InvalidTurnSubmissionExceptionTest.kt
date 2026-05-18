package at.se2group.backend.game.service

import at.se2group.backend.service.InvalidTurnSubmissionException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import shared.models.game.validation.RuleViolation

class InvalidTurnSubmissionExceptionTest {

    @Test
    fun `stores rule violations and exposes default message`() {
        val violations = listOf(
            RuleViolation(
                code = "INVALID_SET",
                message = "Set is invalid",
                boardSetId = "set-1",
                tileIds = listOf("tile-1", "tile-2")
            )
        )

        val exception = InvalidTurnSubmissionException(violations)

        assertEquals("Submitted draft is invalid", exception.message)
        assertEquals(violations, exception.violations)
    }
}
