package at.se2group.backend.rules.service

import at.se2group.backend.domain.TileRules
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
                boardSetId = set.boardSetId,
                tileIds = set.tiles.map { it.tileId }
            )
        }

        val jokers = set.tiles.filterIsInstance<JokerTile>()
        val numberedTiles = set.tiles.filterIsInstance<NumberedTile>()

        if (numberedTiles.isEmpty()) {
            return invalid(
                code = "RUN_ALL_JOKERS_NOT_ALLOWED",
                message = "All-joker runs are not allowed",
                boardSetId = set.boardSetId,
                tileIds = jokers.map { it.tileId }
            )
        }

        val distinctColors = numberedTiles.map { it.color }.distinct()
        if (distinctColors.size != 1) {
            return invalid(
                code = "RUN_COLOR_MISMATCH",
                message = "All run tiles must have the same color",
                boardSetId = set.boardSetId,
                tileIds = numberedTiles.map { it.tileId }
            )
        }

        val sortedNumbers = numberedTiles.map { it.number }.sorted()

        if (sortedNumbers.size != sortedNumbers.distinct().size) {
            return invalid(
                code = "RUN_DUPLICATE_NUMBER",
                message = "Run tiles must not have duplicate numbers",
                boardSetId = set.boardSetId,
                tileIds = numberedTiles.map { it.tileId }
            )
        }

        val requiredGapFillers = sortedNumbers
            .zipWithNext()
            .sumOf { (a, b) -> (b - a - 1).coerceAtLeast(0) }

        if (jokers.isEmpty() && requiredGapFillers > 0) {
            return invalid(
                code = "RUN_NOT_CONSECUTIVE",
                message = "Run tiles must create a consecutive ascending sequence",
                boardSetId = set.boardSetId,
                tileIds = numberedTiles.map { it.tileId }
            )
        }

        if (requiredGapFillers > jokers.size) {
            return invalid(
                code = "RUN_JOKER_GAP_TOO_LARGE",
                message = "Available jokers are not enough to fill the run gaps",
                boardSetId = set.boardSetId,
                tileIds = set.tiles.map { it.tileId }
            )
        }

        val spareJokers = jokers.size - requiredGapFillers
        val minNumber = sortedNumbers.first()
        val maxNumber = sortedNumbers.last()
        val extensionCapacity =
            (minNumber - TileRules.MIN_TILE_NUMBER) + (TileRules.MAX_TILE_NUMBER - maxNumber)

        if (spareJokers > extensionCapacity) {
            return invalid(
                code = "RUN_JOKER_OUT_OF_RANGE",
                message = "Resolving the joker would force numbers outside the legal tile range",
                boardSetId = set.boardSetId,
                tileIds = jokers.map { it.tileId }
            )
        }

        return valid()
    }
}
