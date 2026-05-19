package at.se2group.backend.api

import at.se2group.backend.service.InvalidTurnSubmissionException
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import shared.models.game.validation.RuleViolation

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleIllegalState should return 409`() {
        val ex = IllegalStateException("test error")

        val response = handler.handleIllegalState(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("CONFLICT", response.body?.errorCode)
        assertEquals("test error", response.body?.errorMessage)
    }

    @Test
    fun `handleInvalidTurnSubmission should return 409`() {
        val ex = InvalidTurnSubmissionException(
            violations = listOf(
                RuleViolation(
                    code = "INVALID_SET",
                    message = "Set is invalid",
                    boardSetId = "set-1",
                    tileIds = listOf("tile-1", "tile-2")
                )
            )
        )

        val response = handler.handleInvalidTurnSubmission(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("INVALID_TURN_SUBMISSION", response.body?.errorCode)
        assertEquals("Submitted draft is invalid", response.body?.errorMessage)
        assertEquals(ex.violations, response.body?.violations)
    }
}
