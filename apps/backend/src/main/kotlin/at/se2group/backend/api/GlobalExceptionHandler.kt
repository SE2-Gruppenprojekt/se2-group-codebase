package at.se2group.backend.api

import at.se2group.backend.service.InvalidTurnSubmissionException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import shared.models.api.ApiErrorResponse

/**
 * Centralized REST exception mapping for the backend API.
 *
 * This advice is the single place where backend failures are translated into
 * stable HTTP status codes and the shared [ApiErrorResponse] payload. Its main
 * job is to keep controllers thin and to keep service code free to express
 * failures through normal exception types instead of duplicating transport-layer
 * response building in each endpoint.
 *
 * The handler covers two broad failure categories:
 *
 * 1. **Request-entry failures raised by Spring MVC**
 *    These happen before business logic is reached, for example when JSON is
 *    malformed, a required header is absent, a path value cannot be converted,
 *    or bean validation rejects a DTO.
 * 2. **Application failures raised intentionally by backend code**
 *    These come from mapper, service, and rule-validation layers after request
 *    binding has succeeded.
 *
 * The mapping policy is intentionally simple:
 *
 * - [MethodArgumentNotValidException] -> `400 BAD_REQUEST`
 * - [HttpMessageNotReadableException] -> `400 BAD_REQUEST`
 * - [MissingRequestHeaderException] -> `400 BAD_REQUEST`
 * - [MethodArgumentTypeMismatchException] -> `400 BAD_REQUEST`
 * - [IllegalArgumentException] -> `400 BAD_REQUEST`
 * - [NoSuchElementException] -> `404 NOT_FOUND`
 * - [IllegalStateException] -> `409 CONFLICT`
 * - [SecurityException] -> `403 FORBIDDEN`
 * - [InvalidTurnSubmissionException] -> `409 INVALID_TURN_SUBMISSION`
 * - all other [Exception] types -> `500 INTERNAL_SERVER_ERROR`
 *
 * Logging is also centralized here:
 *
 * - expected client-caused failures are logged at `WARN`
 * - unexpected backend failures are logged at `ERROR`
 *
 * The rule-validation path is handled explicitly through
 * [InvalidTurnSubmissionException], which preserves structured rule violations
 * for clients that need precise end-turn feedback instead of a generic
 * conflict message.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiErrorResponse> {
        // Bean-validation failures are reduced to one stable transport message
        // so clients do not depend on Spring's internal error rendering.
        logger.warn("Request validation failed: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    errorCode = "BAD_REQUEST",
                    errorMessage = "Request validation failed"
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ApiErrorResponse> {
        // JSON syntax errors, wrong scalar types, and invalid enum text all
        // arrive here before controller business logic is entered.
        logger.warn("Malformed request body: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    errorCode = "BAD_REQUEST",
                    errorMessage = "Malformed JSON request body"
                )
            )
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(
        ex: MissingRequestHeaderException
    ): ResponseEntity<ApiErrorResponse> {
        // Required transport metadata such as X-User-Id should fail as a normal
        // client error instead of bubbling up as framework-default HTML/JSON.
        logger.warn("Missing required header: {}", ex.headerName)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    errorCode = "BAD_REQUEST",
                    errorMessage = "Missing required header: ${ex.headerName}"
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException
    ): ResponseEntity<ApiErrorResponse> {
        // Keep conversion failures generic at the REST layer; the concrete bad
        // value is useful for logs but does not need to shape client logic.
        logger.warn("Request type mismatch for '{}': {}", ex.name, ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    errorCode = "BAD_REQUEST",
                    errorMessage = "Request parameter type mismatch"
                )
            )
    }

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
        // Missing game, lobby, or draft lookups should converge on one 404
        // contract regardless of the originating service.
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
        // IllegalStateException is used for lifecycle and turn conflicts such as
        // wrong current player, inactive game, or repeated draw attempts.
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
        // SecurityException is reserved for authorization boundaries such as
        // host-only lobby operations.
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
        // Keep rule-validation failures structured so clients can highlight
        // specific invalid sets and still show a top-level submission error.
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
