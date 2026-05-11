package at.se2group.backend.service

import at.se2group.backend.mapper.toDomain
import at.se2group.backend.persistence.GameRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GameStatus


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
class DrawTileService(
    private val gameRepository: GameRepository,
    private val gameService: GameService
) {

    private companion object {
        const val GAME_NOT_FOUND = "Game not found"
        const val GAME_NOT_ACTIVE = "Game is not active"
        const val PLAYER_NOT_IN_GAME = "Player is not in the game"
        const val NOT_ACTIVE_PLAYER = "User is not the active player"
        const val DRAW_PILE_EMPTY = "Draw pile is empty"
    }

    @Transactional
    fun drawTile(gameId: String, playerId: String): ConfirmedGame {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }
            .toDomain()

        check(game.status == GameStatus.ACTIVE) { GAME_NOT_ACTIVE }

        check(game.players.any { it.userId == playerId }) { PLAYER_NOT_IN_GAME }

        return game
    }
}
