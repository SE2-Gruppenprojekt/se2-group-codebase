package at.se2group.backend.api

import at.se2group.backend.service.InvalidTurnSubmissionException
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.core.MethodParameter
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.slf4j.LoggerFactory
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.ValidationResult
import java.beans.PropertyDescriptor

/**
 * Direct unit tests for [GlobalExceptionHandler].
 *
 * These tests exercise the advice methods without going through Spring MVC so
 * that the response contract and logging policy can be verified in isolation.
 * MVC routing coverage for the same handlers lives in
 * [GlobalExceptionHandlerMvcTest].
 */
class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleIllegalArgument should return 400`() {
        val ex = IllegalArgumentException("bad request")

        val response = handler.handleIllegalArgument(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("BAD_REQUEST", response.body?.errorCode)
        assertEquals("bad request", response.body?.errorMessage)
    }

    @Test
    fun `handleNoSuchElement should return 404`() {
        val ex = NoSuchElementException("missing resource")

        val response = handler.handleNoSuchElement(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("NOT_FOUND", response.body?.errorCode)
        assertEquals("missing resource", response.body?.errorMessage)
    }

    @Test
    fun `handleIllegalState should return 409`() {
        val ex = IllegalStateException("test error")

        val response = handler.handleIllegalState(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("CONFLICT", response.body?.errorCode)
        assertEquals("test error", response.body?.errorMessage)
    }

    @Test
    fun `handleSecurity should return 403`() {
        val ex = SecurityException("forbidden")

        val response = handler.handleSecurity(ex)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("FORBIDDEN", response.body?.errorCode)
        assertEquals("forbidden", response.body?.errorMessage)
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
    fun `handleMethodArgumentNotValid should return 400`() {
        val bindingResult = BeanPropertyBindingResult(CreateLobbyRequestStub(), "request")
        bindingResult.addError(
            FieldError("request", "displayName", "displayName must not be blank")
        )
        val ex = MethodArgumentNotValidException(nullableMethodParameter(), bindingResult)

        val response = handler.handleMethodArgumentNotValid(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("BAD_REQUEST", response.body?.errorCode)
        assertEquals("Request validation failed", response.body?.errorMessage)
    }

    @Test
    fun `handleUnreadableBody should return 400`() {
        val ex = HttpMessageNotReadableException(
            "Unreadable JSON body",
            MockHttpInputMessage(ByteArray(0))
        )

        val response = handler.handleUnreadableBody(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("BAD_REQUEST", response.body?.errorCode)
        assertEquals("Malformed JSON request body", response.body?.errorMessage)
    }

    @Test
    fun `handleMissingHeader should return 400`() {
        val ex = MissingRequestHeaderException(
            "X-User-Id",
            nullableMethodParameter()
        )

        val response = handler.handleMissingHeader(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("BAD_REQUEST", response.body?.errorCode)
        assertEquals("Missing required header: X-User-Id", response.body?.errorMessage)
    }

    @Test
    fun `handleTypeMismatch should return 400`() {
        val ex = MethodArgumentTypeMismatchException(
            "abc",
            Int::class.java,
            "gameId",
            nullableMethodParameter(),
            NumberFormatException("For input string: \"abc\"")
        )

        val response = handler.handleTypeMismatch(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("BAD_REQUEST", response.body?.errorCode)
        assertEquals("Request parameter type mismatch", response.body?.errorMessage)
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

    @Test
    fun `handleMissingHeader logs warning`() {
        val appender = attachAppender()

        handler.handleMissingHeader(
            MissingRequestHeaderException("X-User-Id", nullableMethodParameter())
        )

        val event = appender.list.single()
        assertEquals(Level.WARN, event.level)
        assertTrue(event.formattedMessage.contains("Missing required header: X-User-Id"))
    }

    @Test
    fun `handleTypeMismatch logs warning`() {
        val appender = attachAppender()

        handler.handleTypeMismatch(
            MethodArgumentTypeMismatchException(
                "abc",
                Int::class.java,
                "gameId",
                nullableMethodParameter(),
                NumberFormatException("For input string: \"abc\"")
            )
        )

        val event = appender.list.single()
        assertEquals(Level.WARN, event.level)
        assertTrue(event.formattedMessage.contains("Request type mismatch for 'gameId'"))
    }

    private fun attachAppender(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java) as Logger
        return ListAppender<ILoggingEvent>().also {
            it.start()
            logger.addAppender(it)
        }
    }

    private fun nullableMethodParameter(): MethodParameter {
        // Build a writable method parameter that Spring's exception types accept
        // without requiring a real controller method from production code.
        val property = PropertyDescriptor("displayName", CreateLobbyRequestStub::class.java)
        val writeMethod = property.writeMethod
        assertNotNull(writeMethod)
        return MethodParameter(writeMethod!!, 0)
    }

    private class CreateLobbyRequestStub {
        @Suppress("unused")
        var displayName: String? = null
    }
}
