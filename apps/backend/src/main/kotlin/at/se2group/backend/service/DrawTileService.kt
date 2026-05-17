package at.se2group.backend.service

import at.se2group.backend.mapper.toDomain as toDraftDomain
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
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
    private val turnDraftRepository: TurnDraftRepository,
    private val gameBroadcastService: GameBroadcastService,
    private val afterCommitExecutor: AfterCommitExecutor
) {

    private companion object {
        const val GAME_NOT_FOUND = "Game not found"
        const val GAME_NOT_ACTIVE = "Game is not active"
        const val PLAYER_NOT_IN_GAME = "Player is not in the game"
        const val NOT_ACTIVE_PLAYER = "User is not the active player"
        const val DRAFT_NOT_FOUND = "Draft not found"
        const val NOT_DRAFT_OWNER = "Draft belongs to a different user"
        const val DRAW_PILE_EMPTY = "Draw pile is empty"
    }

    @Transactional
    fun drawTile(gameId: String, playerId: String): ConfirmedGame {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }
            .toDomain()

        check(game.status == GameStatus.ACTIVE) { GAME_NOT_ACTIVE }

        check(game.players.any { it.userId == playerId }) { PLAYER_NOT_IN_GAME }

        check(game.currentPlayerUserId == playerId) { NOT_ACTIVE_PLAYER }

        val draftEntity = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException(DRAFT_NOT_FOUND)

        check(draftEntity.playerUserId == playerId) { NOT_DRAFT_OWNER }

        check(game.drawPile.isNotEmpty()) { DRAW_PILE_EMPTY }

        val drawnTile = game.drawPile.first()
        val draft = draftEntity.toDraftDomain()

        val updatedPlayers = game.players.map { player ->
            if (player.userId == playerId) {
                player.copy(rackTiles = player.rackTiles + drawnTile)
            } else {
                player
            }
        }

        val updatedGame = game.copy(
            players = updatedPlayers,
            drawPile = game.drawPile.drop(1)
        )

        val savedGame = gameRepository.save(updatedGame.toEntity()).toDomain()
        val updatedDraft = turnDraftRepository.save(
            draft.copy(
                rackTiles = draft.rackTiles + drawnTile,
                version = draftEntity.version + 1
            ).toEntity(draftEntity)
        ).toDraftDomain()

        afterCommitExecutor.execute {
            gameBroadcastService.broadcastGameUpdated(savedGame)
            gameBroadcastService.broadcastDraftUpdated(updatedDraft)
        }

        return savedGame
    }
}
