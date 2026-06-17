package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.TurnDraft
import shared.models.game.validation.ValidationResult
import shared.models.game.validation.valid
import shared.models.game.validation.invalid

/**
 * Service responsible for validating the initial meld rule during the first
 * submitted turn of a player
 *
 * The initial meld rule requires that a player with who has not yet completed their initial meld
 * must place tiles from their rack (worth at least 30 points) in a single submitted turn before the turn
 * can be committed.
 *
 * Once the player has officially completed the initial meld, this validator skips
 * all the checks on all subsequent turns for that player.
 */

@Service
class FirstMoveValidationService {

    private companion object {
        const val INITIAL_MELD_MINIMUM_SCORE = 30
    }
    fun validate(
        confirmedGame: ConfirmedGame,
        actingPlayer: GamePlayer,
        submittedDraft: TurnDraft
    ): ValidationResult {
        if (actingPlayer.hasCompletedInitialMeld) {
            return valid()
        }

        val score = calculateNewlyCommittedScore(confirmedGame, actingPlayer, submittedDraft)

        return if (score >= INITIAL_MELD_MINIMUM_SCORE) {
            valid()
        } else {
            invalid(
                code = "INITIAL_MELD_TOO_LOW",
                message = "Initial meld must score at least 30 points"
            )
        }
    }

    private fun calculateNewlyCommittedScore(
        confirmedGame: ConfirmedGame,
        actingPlayer: GamePlayer,
        submittedDraft: TurnDraft
    ): Int {
        val confirmedBoardTileIds = confirmedGame.boardSets
            .flatMap { it.tiles }
            .map { it.tileId }
            .toSet()

        val confirmedRackTileIds = actingPlayer.rackTiles
            .map { it.tileId }
            .toSet()

        val newlyCommittedTileIds = submittedDraft.boardSets
            .flatMap { it.tiles }
            .filter { it.tileId !in confirmedBoardTileIds }
            .filter { it.tileId in confirmedRackTileIds }

        return newlyCommittedTileIds.sumOf { tile ->
            when(tile) {
                is NumberedTile -> tile.number
                else -> 0

            }
        }
    }
}
