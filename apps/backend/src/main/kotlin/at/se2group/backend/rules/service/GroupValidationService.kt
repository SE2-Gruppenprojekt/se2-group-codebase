package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.BoardSet
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.validation.ValidationResult
import shared.models.game.validation.invalid
import shared.models.game.validation.valid

@Service
class GroupValidationService {

    fun validate(set: BoardSet): ValidationResult {
        if (set.tiles.size < 3) {
            return invalid(
                code = "GROUP_MIN_SIZE",
                message = "Group must contain at least 3 tiles",
                setIndex = 0,
                tileIds = set.tiles.map { it.tileId }
            )
        }

        if (set.tiles.size > 4) {
            return invalid(
                code = "GROUP_MAX_SIZE",
                message = "Group must contain at most 4 tiles",
                setIndex = 0,
                tileIds = set.tiles.map { it.tileId }
            )
        }

        if (set.tiles.any { it is JokerTile }) {
            return invalid(
                code = "GROUP_JOKER_NOT_SUPPORTED",
                message = "Joker support is not implemented yet",
                setIndex = 0,
                tileIds = set.tiles.filterIsInstance<JokerTile>().map { it.tileId }
            )
        }

        // JokerTile is the only non-numbered tile type right now and is rejected above.
        val numberedTiles = set.tiles.map { it as NumberedTile }

        val distinctNumbers = numberedTiles.map { it.number }.distinct()
        if (distinctNumbers.size != 1) {
            return invalid(
                code = "GROUP_NUMBER_MISMATCH",
                message = "All group tiles must have the same number",
                setIndex = 0,
                tileIds = numberedTiles.map { it.tileId }
            )
        }

        val colors = numberedTiles.map { it.color }
        if (colors.size != colors.distinct().size) {
            return invalid(
                code = "GROUP_DUPLICATE_COLOR",
                message = "Group tiles must have unique colors",
                setIndex = 0,
                tileIds = numberedTiles.map { it.tileId }
            )
        }

        return valid()
    }
}
