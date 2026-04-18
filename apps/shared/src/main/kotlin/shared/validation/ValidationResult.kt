package shared.validation

data class ValidationResult(
    val isValid: Boolean,
    val violations: List<ValidationError>
)
