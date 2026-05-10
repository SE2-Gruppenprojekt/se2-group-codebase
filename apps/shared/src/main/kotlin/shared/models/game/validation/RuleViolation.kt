package shared.models.game.validation

data class RuleViolation(
    val code: String,
    val message: String,
    val setIndex: Int? = null,
    val tileIds: List<String> = emptyList()
)
