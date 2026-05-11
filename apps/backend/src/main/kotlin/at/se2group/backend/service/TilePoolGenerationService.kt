package at.se2group.backend.service

import at.se2group.backend.domain.TileRules
import shared.models.game.domain.*
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service responsible for creating the full initial Rummikub tile pool.
 *
 * The generated tile pool contains all numbered tiles and all joker tiles that
 * are available at the start of a new game according to [TileRules].
 *
 * This service acts as the backend source of truth for tile pool composition so
 * that game initialization logic does not have to duplicate tile creation rules
 * in multiple places.
 *
 * The created list is ordered by generation sequence:
 * - all numbered tile copies are generated first
 * - joker tiles are appended afterwards
 *
 * The exact tile counts, color set, and numeric bounds are determined through
 * [TileRules] and [TileColor].
 */
@Service
class TilePoolGenerationService {

    /**
     * Creates the full initial tile pool for a new game.
     *
     * The returned list contains:
     * - every numbered tile for every supported [TileColor]
     * - repeated according to [TileRules.NUMBERED_TILE_COPY_COUNT]
     * - every joker tile defined by [TileRules.jokerColors]
     *
     * This method only constructs the ordered tile pool. It does not shuffle the
     * tiles or distribute them to players. Those responsibilities belong to
     * later game initialization steps.
     *
     * @return a complete list of all tiles that should exist in a newly created
     * game before shuffling and hand distribution.
     */
    fun createTilePool(): List<Tile> {
        val tiles = mutableListOf<Tile>()

        repeat(TileRules.NUMBERED_TILE_COPY_COUNT) {
            for (color in TileColor.entries) {
                for (number in TileRules.MIN_TILE_NUMBER..TileRules.MAX_TILE_NUMBER) {
                    tiles += NumberedTile(
                        tileId = UUID.randomUUID().toString(),
                        color = color,
                        number = number
                    )
                }
            }
        }
        tiles += TileRules.jokerColors.map { color ->
            JokerTile(
                tileId = UUID.randomUUID().toString(),
                color = color
            )
        }
        return tiles
    }
}
