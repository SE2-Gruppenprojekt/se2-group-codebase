package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.BoardSet
import shared.models.game.validation.RuleViolation
import shared.models.game.validation.ValidationResult
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

@Service
class BoardValidationService(
    private val setValidationService: SetValidationService
) {

    fun validate(boardSets: List<BoardSet>): ValidationResult {
        val violations = mutableListOf<RuleViolation>()

        boardSets.forEach { set ->
            val result = setValidationService.validate(set)
            violations += result.violations
        }

        return if (violations.isEmpty()) {
            valid()
        } else {
            invalid(violations)
        }
    }
}
