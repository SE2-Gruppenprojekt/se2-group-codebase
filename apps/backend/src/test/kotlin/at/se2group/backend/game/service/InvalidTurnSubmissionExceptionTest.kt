package at.se2group.backend.game.service

import at.se2group.backend.service.InvalidTurnSubmissionException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.ValidationResult

class InvalidTurnSubmissionExceptionTest {

    @Test
    fun `stores validation result and exposes default message`() {
        val validationResult = ValidationResult(
            violations = listOf(
                RuleViolation(
                    code = "INVALID_SET",
                    message = "Set is invalid",
                    boardSetId = "set-1",
                    tileIds = listOf("tile-1", "tile-2")
                )
            )
        )

        val exception = InvalidTurnSubmissionException(validationResult)

        assertEquals("Submitted draft is invalid", exception.message)
        assertEquals(validationResult, exception.validationResult)
    }
}
