package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.BoardSet
import shared.models.game.validation.ValidationResult

@Service
class RunValidationService {

    fun validate(set: BoardSet): ValidationResult {
        throw UnsupportedOperationException("Run validation is not implemented yet.")
    }

}
