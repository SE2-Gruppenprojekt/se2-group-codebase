package shared.models.game.validation

/**
 * Structured description of one concrete rule-validation failure in the
 * Rummikub game domain.
 *
 * This model is intended for validation paths that continue checking multiple
 * rules and return a collected result instead of failing fast with an
 * exception. That makes it suitable for submitted-draft validation where the
 * backend may want to report more than one problem at once.
 *
 * Each violation should remain small, stable, and transport-friendly:
 * - [code] identifies the violated rule in a machine-readable way
 * - [message] explains the failure in human-readable form
 * - [setIndex] can point to one affected board set if the violation is set-scoped
 * - [tileIds] can point to the specific tiles involved when tile-level
 *   highlighting or debugging is needed
 *
 * The class deliberately avoids carrying rich domain objects. Validation
 * results should reference the problematic area of the draft, not duplicate the
 * full game state inside the error payload.
 *
 * @property code stable machine-readable identifier for the violated rule
 * @property message human-readable explanation of the problem
 * @property setIndex optional board-set index for violations tied to one set
 * @property tileIds optional list of tile identifiers involved in the failure
 */
data class RuleViolation(
    val code: String,
    val message: String,
    val setIndex: Int? = null,
    val tileIds: List<String> = emptyList()
)
