package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.BoardSet
import shared.models.game.validation.ValidationResult
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

@Service
class SetValidationService(
    private val groupValidationService: GroupValidationService,
    private val runValidationService: RunValidationService
) {

    fun validate(set: BoardSet): ValidationResult {
        val groupResult = groupValidationService.validate(set)
        val runResult = runValidationService.validate(set)

        val isValidSet = groupResult.isValid || runResult.isValid

        return if (isValidSet) {
            valid()
        } else {
            invalid(groupResult.violations + runResult.violations)
        }
    }
}
