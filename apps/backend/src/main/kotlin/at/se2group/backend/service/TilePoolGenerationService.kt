package at.se2group.backend.service

import at.se2group.backend.domain.*
import org.springframework.stereotype.Service

@Service
class TilePoolGenerationService {

    fun createTilePool(): List<Tile> {
        val tiles = mutableListOf<Tile>()

        repeat(TileRules.NUMBERED_TILE_COPY_COUNT) {
            for (color in TileColor.entries) {
                for (number in TileRules.MIN_TILE_NUMBER..TileRules.MAX_TILE_NUMBER) {
                    tiles += NumberedTile(
                        color = color,
                        number = number
                    )
                }
            }
        }
        tiles += TileRules.jokerColors.map { color ->
            JokerTile(
                color = color
            )
        }
        return tiles
    }
}
