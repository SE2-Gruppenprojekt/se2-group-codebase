package shared.validation.user

import shared.validation.ValidationError
import shared.validation.ValidationResult

object DisplayNameValidator {

    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 20
    private val ALLOWED_CHARS = Regex("^[A-Za-z0-9]+$")

    fun validate(name: String): ValidationResult {
        val trimmed = name.trim()

        val errors = mutableListOf<ValidationError>()

        if (trimmed.isEmpty()) {
            errors += ValidationError(
                code = "EMPTY",
                message = "Username cannot be empty"
            )
        }

        if (trimmed.length < MIN_LENGTH) {
            errors += ValidationError(
                code = "TOO_SHORT",
                message = "Minimum length is $MIN_LENGTH characters"
            )
        }

        if (trimmed.length > MAX_LENGTH) {
            errors += ValidationError(
                code = "TOO_LONG",
                message = "Maximum length is $MAX_LENGTH characters"
            )
        }

        if (!ALLOWED_CHARS.matches(trimmed)) {
            errors += ValidationError(
                code = "INVALID_CHARS",
                message = "Only letters and numbers are allowed"
            )
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            violations = errors
        )
    }
}
