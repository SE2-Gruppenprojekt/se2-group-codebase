package shared.models.game.validation

/**
 * Creates a successful rule-validation result with no collected violations.
 *
 * This helper keeps validator implementations concise when a validation path
 * completes successfully and does not need to report any rule failures.
 *
 * @return a valid [ValidationResult] without violations
 */
fun valid(): ValidationResult = ValidationResult()

/**
 * Creates an invalid validation result from one prebuilt [RuleViolation].
 *
 * This helper is useful when a validator has already constructed the exact
 * violation object it wants to return and only needs to wrap it into the common
 * [ValidationResult] container.
 *
 * @param violation the single violation that should make the result invalid
 * @return an invalid [ValidationResult] containing exactly [violation]
 */
fun invalid(violation: RuleViolation): ValidationResult =
    ValidationResult(violations = listOf(violation))

/**
 * Creates an invalid validation result from a list of [violations].
 *
 * If the provided list is empty, the helper falls back to [valid] so callers do
 * not accidentally construct a contradictory "invalid but empty" result.
 *
 * @param violations collected violations that should be returned together
 * @return a valid result when [violations] is empty, otherwise an invalid
 * result containing all provided violations
 */
fun invalid(violations: List<RuleViolation>): ValidationResult =
    if (violations.isEmpty()) {
        valid()
    } else {
        ValidationResult(violations = violations)
    }

/**
 * Creates an invalid validation result from one violation described inline.
 *
 * This helper is intended for validators that want to return a single
 * rule-specific failure without manually constructing a [RuleViolation] first.
 *
 * @param code stable machine-readable rule identifier
 * @param message human-readable explanation of the failure
 * @param setIndex optional affected board-set index
 * @param tileIds optional affected tile identifiers
 * @return an invalid [ValidationResult] containing the constructed violation
 */
fun invalid(
    code: String,
    message: String,
    setIndex: Int? = null,
    tileIds: List<String> = emptyList()
): ValidationResult =
    invalid(
        RuleViolation(
            code = code,
            message = message,
            boardSetId = setIndex,
            tileIds = tileIds
        )
    )

/**
 * Merges multiple validation results into one combined result.
 *
 * The combined result is valid only if every input result is valid. Violation
 * order is preserved in the same order as the provided [results], which makes
 * aggregated outputs deterministic for testing, logging, and frontend display.
 *
 * @param results individual validation results that should be combined
 * @return one [ValidationResult] containing all collected violations
 */
fun merge(vararg results: ValidationResult): ValidationResult =
    results.asIterable().merge()

/**
 * Merges an arbitrary iterable of validation results into one combined result.
 *
 * This overload is useful when a validator builds results dynamically in a list
 * during iteration and wants to aggregate them at the end without converting
 * back to varargs.
 *
 * @return one [ValidationResult] whose violations are the concatenation of all
 * violations in this iterable
 */
fun Iterable<ValidationResult>.merge(): ValidationResult {
    // Flatten all collected rule failures while preserving validator order.
    val mergedViolations = flatMap { it.violations }
    return invalid(mergedViolations)
}

/**
 * Combines this validation result with [other] and returns the aggregated
 * result.
 *
 * This operator-style helper keeps small validator compositions readable when
 * only two result values need to be merged.
 *
 * @param other the second validation result to combine with this one
 * @return one aggregated [ValidationResult]
 */
operator fun ValidationResult.plus(other: ValidationResult): ValidationResult =
    merge(this, other)
