package shared.models.game.validation

/**
 * Common result wrapper for game-rule validation in the shared Rummikub model.
 *
 * A validation run is represented as a collection of [violations]. An empty
 * collection means the validated input satisfied all rules checked by the
 * validator. A non-empty collection means validation failed and each contained
 * [RuleViolation] describes one specific problem.
 *
 * The class derives [isValid] from the current violation list instead of
 * storing a second mutable truth source. This prevents contradictory states
 * such as "valid result with violations" or "invalid result with no
 * violations" and keeps downstream validator composition straightforward.
 *
 * Typical uses:
 * - set validation
 * - board validation
 * - first-move validation
 * - top-level submitted-draft validation orchestration
 *
 * This model is intentionally different from fail-fast use-case errors such as
 * missing game state, unauthorized player access, or broken persistence state.
 * Those conditions should still be represented through exceptions, while
 * [ValidationResult] is reserved for collectable rule failures.
 *
 * @property violations collected rule violations produced by one validation run
 */
data class ValidationResult(
    val violations: List<RuleViolation> = emptyList()
) {
    /**
     * Whether the validation finished without any collected rule violations.
     */
    val isValid: Boolean
        get() = violations.isEmpty()
}
