package at.se2group.backend.service

import shared.models.game.domain.TurnDraft
import shared.models.game.request.UpdateDraftRequest
import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toDomain as toGameDomain
import at.se2group.backend.mapper.toDomain as toDraftDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for loading and updating persisted live turn draft state.
 *
 * This service owns backend access to the temporary in-progress draft of the
 * current player's turn. In contrast to confirmed game state, a turn draft
 * represents work in progress and may change repeatedly during a player's turn
 * before the move is finally validated and committed.
 *
 * Typical responsibilities include:
 * - locating the current draft for a given game
 * - validating that the acting user is allowed to modify that draft
 * - applying incoming draft updates from the API layer
 * - persisting the resulting updated draft state
 * - broadcasting successful live draft updates to subscribed game clients
 *
 * Confirmed, authoritative game state is handled separately through game-level
 * services and repositories. This service only deals with the temporary live
 * draft representation.
 *
 * @property gameRepository repository used to verify that the referenced game
 * exists before updating its draft.
 * @property turnDraftRepository repository used to load and persist turn draft
 * entities.
 */
@Service
@Transactional(readOnly = true)
class TurnDraftService(
    private val gameRepository: GameRepository,
    private val turnDraftRepository: TurnDraftRepository,
    private val tileConservationService: TileConservationService,
    private val gameBroadcastService: GameBroadcastService
) {

    /**
     * Internal constants used by [TurnDraftService].
     */
    private companion object {
        const val GAME_NOT_FOUND = "Game not found"

        const val DRAFT_NOT_FOUND = "Draft not found"

        const val NOT_CURRENT_PLAYER = "User is not the current active player"

        const val NOT_DRAFT_OWNER = "Draft belongs to a different user"
    }

    /**
     * Updates the persisted live turn draft for the given game and user.
     *
     * The update flow performs the following steps:
     * - verifies that the referenced game exists
     * - loads the currently persisted draft for that game
     * - checks that the acting user is the owner of the draft
     * - maps the incoming [UpdateDraftRequest] into the backend domain draft model
     * - applies that state onto the existing persistence entity
     * - saves the updated draft
     * - broadcasts the persisted draft update to subscribed game clients
     * - returns the updated draft as a domain object
     *
     * This method only updates temporary live draft state. It does not validate
     * or commit a final turn result into confirmed game state.
     *
     * @param gameId the unique identifier of the game whose draft should be
     * updated.
     * @param userId the unique identifier of the user attempting to update the
     * draft.
     * @param request the API request containing the new draft board and rack
     * state.
     * @return the persisted updated turn draft converted back into the domain
     * model.
     * @throws NoSuchElementException if the game does not exist or if no draft is
     * stored for the given game.
     * @throws IllegalStateException if the acting user is not the owner of the
     * current draft.
     */
    @Transactional
    fun updateDraft(
        gameId: String,
        userId: String,
        request: UpdateDraftRequest
    ): TurnDraft {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }
                .toGameDomain()

        val draftEntity = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException(DRAFT_NOT_FOUND)

        check(game.currentPlayerUserId == userId) { NOT_CURRENT_PLAYER }
        check(draftEntity.playerUserId == userId) { NOT_DRAFT_OWNER }

        val proposedDraft = request.toDraftDomain(gameId, userId)

        tileConservationService.validate(
            confirmedGame = game,
            activePlayerUserId = userId,
            candidateDraft = proposedDraft
        )
        val updatedDraft = turnDraftRepository.save(
            proposedDraft.copy(version = draftEntity.version + 1).toEntity(draftEntity)
        ).toDomain()

        gameBroadcastService.broadcastDraftUpdated(updatedDraft)

        return updatedDraft
    }
}
