package at.se2group.backend.rules.service

import org.springframework.stereotype.Service
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.TurnDraft
import shared.models.game.validation.ValidationResult

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
    fun validate(
        confirmedGame: ConfirmedGame,
        actingPlayer: GamePlayer,
        submittedDraft: TurnDraft
    ): ValidationResult {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
