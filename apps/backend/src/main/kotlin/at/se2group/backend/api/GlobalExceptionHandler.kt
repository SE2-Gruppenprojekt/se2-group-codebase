package at.se2group.backend.api

import at.se2group.backend.service.InvalidTurnSubmissionException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import shared.models.api.ApiErrorResponse

/**
 * Centralized HTTP exception mapping for backend REST endpoints.
 *
 * This advice translates common backend exceptions into stable API responses so
 * that controllers can remain thin and service code can signal failures through
 * exceptions instead of hand-building response entities. The handler is also a
 * logging boundary: expected client-caused failures are logged at `WARN`, while
 * unexpected backend failures are logged at `ERROR`.
 *
 * Current mapping policy:
 *
 * - [IllegalArgumentException] -> `400 BAD_REQUEST`
 * - [NoSuchElementException] -> `404 NOT_FOUND`
 * - [IllegalStateException] -> `409 CONFLICT`
 * - [SecurityException] -> `403 FORBIDDEN`
 * - [InvalidTurnSubmissionException] -> `409 INVALID_TURN_SUBMISSION`
 * - all other [Exception] types -> `500 INTERNAL_SERVER_ERROR`
 *
 * The rule-validation path is handled explicitly through
 * [InvalidTurnSubmissionException], which preserves structured
 * `RuleViolation` data for clients that need detailed move rejection reasons.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiErrorResponse> {
        // Unexpected exceptions indicate a backend failure rather than a normal client mistake.
        logger.error("Unhandled backend exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponse(
                    errorCode = "INTERNAL_SERVER_ERROR",
                    errorMessage = "An unexpected error occurred"
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        // Invalid input is expected to happen at the API boundary and should stay visible but non-fatal.
        logger.warn("Bad request: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    errorCode = "BAD_REQUEST",
                    errorMessage = ex.message ?: "Invalid request"
                )
            )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ApiErrorResponse> {
        logger.warn("Resource not found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiErrorResponse(
                    errorCode = "NOT_FOUND",
                    errorMessage = ex.message ?: "Resource not found"
                )
            )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiErrorResponse> {
        logger.warn("Conflict while processing request: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiErrorResponse(
                    errorCode = "CONFLICT",
                    errorMessage = ex.message ?: "Invalid state"
                )
            )
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurity(ex: SecurityException): ResponseEntity<ApiErrorResponse> {
        logger.warn("Forbidden request: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ApiErrorResponse(
                    errorCode = "FORBIDDEN",
                    errorMessage = ex.message ?: "Access denied"
                )
            )
    }

    @ExceptionHandler(InvalidTurnSubmissionException::class)
    fun handleInvalidTurnSubmission(
        ex: InvalidTurnSubmissionException
    ): ResponseEntity<ApiErrorResponse> {
        // Keep rule-validation failures structured so clients can show precise move feedback.
        logger.warn(
            "Invalid turn submission with {} rule violation(s): {}",
            ex.validationResult.violations.size,
            ex.message
        )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiErrorResponse(
                    errorCode = "INVALID_TURN_SUBMISSION",
                    errorMessage = ex.message ?: "Submitted draft is invalid",
                    violations = ex.validationResult.violations
                )
            )
    }
}
