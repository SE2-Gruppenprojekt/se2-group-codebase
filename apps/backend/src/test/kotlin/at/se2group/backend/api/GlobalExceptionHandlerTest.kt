package at.se2group.backend.api

import at.se2group.backend.service.InvalidTurnSubmissionException
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.slf4j.LoggerFactory
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.ValidationResult

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

        val ex = InvalidTurnSubmissionException(validationResult)

        val response = handler.handleInvalidTurnSubmission(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("INVALID_TURN_SUBMISSION", response.body?.errorCode)
        assertEquals("Submitted draft is invalid", response.body?.errorMessage)
        assertEquals(validationResult.violations, response.body?.violations)
    }

    @Test
    fun `handleIllegalState logs warning`() {
        val appender = attachAppender()

        handler.handleIllegalState(IllegalStateException("state conflict"))

        val event = appender.list.single()
        assertEquals(Level.WARN, event.level)
        assertTrue(event.formattedMessage.contains("Conflict while processing request: state conflict"))
    }

    @Test
    fun `handleGeneric logs error with exception`() {
        val appender = attachAppender()
        val exception = RuntimeException("boom")

        handler.handleGeneric(exception)

        val event = appender.list.single()
        assertEquals(Level.ERROR, event.level)
        assertEquals("boom", event.throwableProxy?.message)
        assertTrue(event.formattedMessage.contains("Unhandled backend exception"))
    }

    private fun attachAppender(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java) as Logger
        return ListAppender<ILoggingEvent>().also {
            it.start()
            logger.addAppender(it)
        }
    }
}
