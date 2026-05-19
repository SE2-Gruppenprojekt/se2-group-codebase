package at.se2group.backend.service

import shared.models.game.validation.ValidationResult

class InvalidTurnSubmissionException(
    val validationResult: ValidationResult
) : RuntimeException("Submitted draft is invalid")
