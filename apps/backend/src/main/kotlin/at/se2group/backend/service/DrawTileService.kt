package at.se2group.backend.service

import org.springframework.stereotype.Service

/**
* Service responsible for handling draw tile action during a player's turn.
* When a player draws a tile, the tile is removed from the draw pile and added
* to the player's hand.*
* This service handles the actions by:
* verifying that the player is allowed to draw a tile
* removing the tile from the draw pile
* adding the tile to the player's rack
* advancing the turn to the next player in the game
* persisting the updated confirmed game state
 */


@Service
class DrawTileService {

    fun drawTile(gameId: String, playerId: String) {
        throw UnsupportedOperationException("Draw tile is not implemented yet.")
    }
}
