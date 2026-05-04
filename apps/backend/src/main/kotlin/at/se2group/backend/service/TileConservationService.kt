package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.TurnDraft
import org.springframework.stereotype.Service
import at.se2group.backend.domain.Tile

/**
 * Service responsible for validating that a candidate turn draft conserves the
 * exact multiset of tiles that the active player is allowed to use.
 *
 * Tile conservation core rule: whatever tiles a player
 * rearranges during their turn - the total number of tiles they hold must never change.
 *
 * No tile may be invented, duplicated, or lost between the confirmed state and the candidate draft.
 *
 * The allowed tiles for a given player are derived from:
 * - the confirmed board tiles
 * - the active player's confirmed rack tiles
 *
 */

@Service
class TileConservationService {

    /**
     * Validates that [candidateDraft] conserves the exact number of tiles that the
     * active player is allowed to use in [confirmedGame].
     */

    fun validate(
        confirmedGame: ConfirmedGame,
        activePlayerUserId: String,
        candidateDraft: TurnDraft
    ) {
        val allowed = allowedTiles(confirmedGame, activePlayerUserId)
        val candidate = proposedTiles(candidateDraft)
        throw UnsupportedOperationException("Tile conservation validation is not implemented yet")
    }

    private fun allowedTiles(
        confirmedGame: ConfirmedGame,
        activePlayerUserId: String
        ): Map<Tile, Int> {
        val boardTiles = confirmedGame.boardSets.flatMap { it.tiles }
        val rackTiles = confirmedGame.players.first { it.userId == activePlayerUserId }.rackTiles

        return (boardTiles + rackTiles).toMultiset()
    }

    private fun List<Tile>.toMultiset(): Map<Tile, Int> =
        groupingBy { it }.eachCount()

    private fun proposedTiles(proposedDraft: TurnDraft): Map<Tile, Int> {
        val boardTiles = proposedDraft.boardSets.flatMap { it.tiles }
        val rackTiles = proposedDraft.rackTiles
        return (boardTiles + rackTiles).toMultiset()
    }
}
