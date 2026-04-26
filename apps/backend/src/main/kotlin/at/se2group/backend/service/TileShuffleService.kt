package at.se2group.backend.service

import at.se2group.backend.domain.GamePlayer
import org.springframework.stereotype.Service
import at.se2group.backend.domain.Tile

@Service
class TileShuffleService {
    companion object {
        private const val HAND_SIZE = 14
    }

    fun shuffleTiles(tiles: List<Tile>): List<Tile> {
        return tiles.shuffled()
    }

    fun distributedHands(
        players: List<GamePlayer>,
        tiles: List<Tile>
    ): List<GamePlayer> {
        val requiredTiles = players.size * HAND_SIZE
        require(tiles.size >= requiredTiles) {
            "Not enough tiles to distribute $HAND_SIZE tiles to ${players.size} players"
        }

        var index = 0

        return players.map { player ->
            val hand = tiles.subList(index, index + HAND_SIZE)
            index += HAND_SIZE

            player.copy(rackTiles = hand)

        }
    }

    fun createDrawPile(
        tiles: List<Tile>,
        players: List<GamePlayer>
    ): List<Tile> {
        val used = players.sumOf { it.rackTiles.size }
        return tiles.drop(used)
    }
}
