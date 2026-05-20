package shared.models.api

import shared.models.game.validation.RuleViolation

data class ApiErrorResponse(
    val errorCode: String,
    val errorMessage: String,
    val violations: List<RuleViolation> = emptyList()
)
