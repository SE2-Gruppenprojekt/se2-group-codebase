package at.se2group.backend.service

import at.se2group.backend.domain.GamePlayer
import org.springframework.stereotype.Service
import at.se2group.backend.domain.Tile


/**
 * Service responsible for shuffling tiles and deriving player hands / draw pile
 * during game initialization.
 *
 * This service does not create the tile pool itself. Instead, it operates on an
 * already constructed list of tiles and applies the next initialization steps:
 * - randomizing tile order
 * - distributing starting hands to players
 * - deriving the remaining draw pile after hand distribution
 *
 * The service is intentionally focused on tile-order and distribution concerns
 * so that game initialization logic can compose it together with tile pool
 * creation and confirmed game setup in a clean way.
 */
@Service
class TileShuffleService {
    /**
     * Internal constants used by [TileShuffleService].
     */
    companion object {
        /**
         * Number of tiles each player receives at the start of a game.
         */
        private const val HAND_SIZE = 14
    }

    /**
     * Returns a shuffled copy of the provided tile list.
     *
     * The original input list is not modified. The returned list represents the
     * randomized tile order that is later used for hand distribution and draw
     * pile creation.
     *
     * @param tiles the complete ordered tile pool before shuffling.
     * @return a shuffled copy of [tiles].
     */
    fun shuffleTiles(tiles: List<Tile>): List<Tile> {
        return tiles.shuffled()
    }

    /**
     * Distributes starting hands to all provided players.
     *
     * Tiles are assigned in order from the provided tile list. Each player
     * receives exactly [HAND_SIZE] consecutive tiles, and the returned player
     * list preserves the original player order.
     *
     * This method does not shuffle tiles itself, so callers are expected to pass
     * in a previously shuffled tile list if randomized hands are desired.
     *
     * @param players the players who should receive starting rack tiles.
     * @param tiles the tile list from which hands are distributed.
     * @return a new player list where each player contains their assigned rack
     * tiles.
     * @throws IllegalArgumentException if there are not enough tiles available to
     * distribute [HAND_SIZE] tiles to every player.
     */
    fun distributedHands(
        players: List<GamePlayer>,
        tiles: List<Tile>
    ): List<GamePlayer> {
        val requiredTiles = players.size * HAND_SIZE
        // Fail early if the provided tile pool cannot cover all starting hands.
        require(tiles.size >= requiredTiles) {
            "Not enough tiles to distribute $HAND_SIZE tiles to ${players.size} players"
        }

        return players.mapIndexed { index, player ->
            val handStart = index * HAND_SIZE
            player.copy(rackTiles = tiles.subList(handStart, handStart + HAND_SIZE))
        }
    }

    /**
     * Creates the remaining draw pile after player hands have been distributed.
     *
     * The method determines how many tiles are already assigned to players via
     * their current `rackTiles` and drops exactly that many tiles from the front
     * of the provided tile list.
     *
     * @param tiles the full tile list used for the current game setup.
     * @param players the players whose rack tiles have already been assigned.
     * @return the remaining tiles that are not currently part of any player's
     * rack and therefore form the draw pile.
     */
    fun createDrawPile(
        tiles: List<Tile>,
        players: List<GamePlayer>
    ): List<Tile> {
        return tiles.drop(players.sumOf { it.rackTiles.size })
    }
}
