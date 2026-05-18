package at.se2group.backend.service

import shared.models.game.validation.RuleViolation

class InvalidTurnSubmissionException(
    val violations: List<RuleViolation>
): RuntimeException("Submitted draft is invalid")
