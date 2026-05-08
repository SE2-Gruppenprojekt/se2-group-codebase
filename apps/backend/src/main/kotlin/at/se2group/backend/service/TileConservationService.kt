package at.se2group.backend.service

import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.TurnDraft
import org.springframework.stereotype.Service
import shared.models.game.domain.Tile

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
     *
     * The allowed tile multiset is derived from the confirmed board tiles and the player's confirmed rack tiles.
     * The proposed tile multiset is derived from the draft board and rack tiles.
     * Both multisets must be equal.
     *
     * @param confirmedGame - current game state used to derive the allowed tile multiset
     * @param activePlayerUserId - the unique identifier of the player whose rack tiles are used to create the allowed multiset
     * @param candidateDraft - the proposed turn draft that should be validated
     * @throws IllegalArgumentException if the proposed turn draft violates the tile conservation rules
     */

    fun validate(
        confirmedGame: ConfirmedGame,
        activePlayerUserId: String,
        candidateDraft: TurnDraft
    ) {
        val allowed = allowedTiles(confirmedGame, activePlayerUserId)
        val candidate = proposedTiles(candidateDraft)

        if (allowed != candidate){
            val missingTiles = (allowed - candidate).entries.joinToString { "${it.key} (${it.value})" }
            val extraTiles = (candidate - allowed).entries.joinToString { "${it.key} (${it.value})" }
            throw IllegalArgumentException("The proposed turn draft violated - missing: [$missingTiles] and extra: [$extraTiles] tile conservation rules!")
        }
    }

    /**
     * Derives the allowed tile multiset for a given player.
     *
     * The allowed tile multiset is derived from the confirmed board tiles and the player's confirmed rack tiles.
     * These are the only tiles that the player is allowed to use in their turn.
     *
     * @param confirmedGame - current game state used to derive the allowed tile multiset
     * @param activePlayerUserId - the unique identifier of the player whose rack tiles are used to create the allowed multiset
     * @return - maps each allowed tile to its permitted count
     */

    private fun allowedTiles(
        confirmedGame: ConfirmedGame,
        activePlayerUserId: String
        ): Map<Tile, Int> {
        val boardTiles = confirmedGame.boardSets.flatMap { it.tiles }
        val rackTiles = confirmedGame.players.first { it.userId == activePlayerUserId }.rackTiles

        return (boardTiles + rackTiles).toMultiset()
    }

    /**
     * Converts a list of tiles into a multiset map where each tile is mapped to its count.
     *
     * @return - maps each tile to the number of times the tile appears in the list
     */

    private fun List<Tile>.toMultiset(): Map<Tile, Int> =
        groupingBy { it }.eachCount()

    /**
     * Derives the proposed tile multiset from the draft board and rack tiles.
     * This multiset is compared to the allowed tile multiset to validate the turn draft and detect tile conservation violations.
     *
     * @param proposedDraft - the proposed draft state submitted by the player
     * @return - maps each proposed tile to its permitted count
     *
     */

    private fun proposedTiles(proposedDraft: TurnDraft): Map<Tile, Int> {
        val boardTiles = proposedDraft.boardSets.flatMap { it.tiles }
        val rackTiles = proposedDraft.rackTiles
        return (boardTiles + rackTiles).toMultiset()
    }

    /**
     * Subtracts the count of each tile in [other] from the count of each tile in [this].
     * Tiles whose remaining count is greater than zero are returned in a new map and those below are discarded.
     * This is used to determine the tiles that are missing or extra tiles.
     *
     * @param other - multiset map to subtract from [this]
     * @return - new multiset map containing only tiles whose count is greater than the corresponding count in [other]
     */

    private operator fun Map<Tile, Int>.minus(other: Map<Tile, Int>): Map<Tile, Int> =
        entries
            .mapNotNull { (tile, count) ->
                val remaining = count - (other[tile] ?: 0)
                if (remaining > 0) tile to remaining else null
            }
            .toMap()
}
