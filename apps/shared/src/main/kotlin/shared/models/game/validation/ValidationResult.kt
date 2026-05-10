package shared.models.game.validation

data class ValidationResult(
    val violations: List<RuleViolation> = emptyList()
) {
    val isValid: Boolean
        get() = violations.isEmpty()
}
