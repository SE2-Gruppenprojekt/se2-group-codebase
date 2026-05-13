package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.BoardSet
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.validation.ValidationResult
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

@Service
class RunValidationService {

    fun validate(set: BoardSet): ValidationResult {
            if (set.tiles.size < 3) {
                return invalid(
                    code = "RUN_MIN_SIZE",
                    message = "Run must contain at least 3 tiles",
                    tileIds = set.tiles.map { it.tileId }
                )
            }

            if (set.tiles.any { it is JokerTile }) {
                return invalid(
                    code = "RUN_JOKER_NOT_SUPPORTED",
                    message = "Joker support is not implemented yet",
                    tileIds = set.tiles.filterIsInstance<JokerTile>().map { it.tileId }
                )
            }

            val numberedTiles = set.tiles.map { it as NumberedTile }

            val distinctColors = numberedTiles.map { it.color }.distinct()
            if (distinctColors.size != 1 ) {
                return invalid(
                    code = "RUN_COLOR_MISMATCH",
                    message = "All run tiles must have the same color",
                    tileIds = numberedTiles.map { it.tileId }
                )
            }

            val sortedNumbers = numberedTiles.sortedBy { it.number }.map { it.number }

            if (sortedNumbers.size != sortedNumbers.distinct().size) {
                return invalid(
                    code = "RUN_DUPLICATE_NUMBER",
                    message = "Run tiles must not have duplicate numbers",
                    tileIds = numberedTiles.map { it.tileId }
                )
            }

            for (i in 1 until sortedNumbers.size) {
            if (sortedNumbers[i] != sortedNumbers[i - 1] + 1) {
                    return invalid(
                    code = "RUN_NOT_CONSECUTIVE",
                    message = "Run tiles must create a consecutive ascending sequence",
                    tileIds = numberedTiles.map { it.tileId }
                )
            }
        }
            return valid()
    }


}
