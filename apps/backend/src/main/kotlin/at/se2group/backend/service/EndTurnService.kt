package at.se2group.backend.service

import at.se2group.backend.mapper.toDomain
import at.se2group.backend.mapper.toEntity
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.TurnDraftRepository
import at.se2group.backend.rules.service.RummikubRuleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GameStatus
import shared.models.game.domain.TurnDraft
import shared.models.game.request.EndTurnRequest
import java.time.Instant

@Service
@Transactional(readOnly = true)
class EndTurnService(
    private val gameRepository: GameRepository,
    private val turnDraftRepository: TurnDraftRepository,
    private val gameService: GameService,
    private val rummikubRuleService: RummikubRuleService,
    private val gameBroadcastService: GameBroadcastService,
    private val afterCommitExecutor: AfterCommitExecutor,
    private val gameMetricsService: GameMetricsService
) {
    private companion object {
        const val GAME_NOT_FOUND = "Game not found"
        const val DRAFT_NOT_FOUND = "Draft not found"
        const val GAME_NOT_ACTIVE = "Game is not active"
        const val NOT_CURRENT_PLAYER = "User is not the current active player"
        const val NOT_DRAFT_OWNER = "Draft belongs to a different user"
    }

    @Transactional
    fun endTurn(
        gameId: String,
        userId: String,
        request: EndTurnRequest
    ): ConfirmedGame {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException(GAME_NOT_FOUND) }
            .toDomain()

        val draftEntity = turnDraftRepository.findByGameId(gameId)
            ?: throw NoSuchElementException(DRAFT_NOT_FOUND)

        check(game.status == GameStatus.ACTIVE) { GAME_NOT_ACTIVE }
        check(game.currentPlayerUserId == userId) { NOT_CURRENT_PLAYER }
        check(draftEntity.playerUserId == userId) { NOT_DRAFT_OWNER }

        val submittedDraft = request.toDomain(gameId, userId)

        val validation = rummikubRuleService.validateSubmittedDraft(
            confirmedGame = game,
            actingPlayerUserId = userId,
            submittedDraft = submittedDraft
        )

        if (!validation.isValid) {
            throw InvalidTurnSubmissionException(validation)
        }

        val committedGame = commitDraftToConfirmedGame(game, submittedDraft)

        val gameWithMetrics = gameMetricsService.applyCommittedTurnMetrics(
            confirmedBeforeTurn = game,
            committedGame = committedGame,
            actingPlayerUserId = userId
        )

        val resolvedGame = gameWithMetrics.finishIfWinnerExists(userId)

        val finalizedGame = if (resolvedGame.status == GameStatus.FINISHED) {
            gameMetricsService.finalizeEndGameMetrics(resolvedGame)
        } else {
            resolvedGame
        }

        if (finalizedGame.status == GameStatus.FINISHED) {
            val savedGame = gameRepository.save(finalizedGame.toEntity()).toDomain()

            turnDraftRepository.deleteById(gameId)

            afterCommitExecutor.execute {
                gameBroadcastService.broadcastGameUpdated(savedGame)
                gameBroadcastService.broadcastGameEnded(savedGame.gameId, userId)
            }

            return savedGame
        }

        val nextPlayerId = gameService.nextPlayerId(finalizedGame)
        val advancedGame = finalizedGame.copy(currentPlayerUserId = nextPlayerId)
        val savedGame = gameRepository.save(advancedGame.toEntity()).toDomain()

        val nextDraft = createNextDraft(savedGame)

        draftEntity.playerUserId = nextDraft.playerUserId
        turnDraftRepository.save(nextDraft.toEntity(draftEntity))

        afterCommitExecutor.execute {
            gameBroadcastService.broadcastGameUpdated(savedGame)
            gameBroadcastService.broadcastTurnChanged(savedGame.gameId, nextPlayerId)
            gameBroadcastService.broadcastDraftUpdated(nextDraft)
        }

        return savedGame
    }

    private fun commitDraftToConfirmedGame(
        confirmedGame: ConfirmedGame,
        draft: TurnDraft
    ): ConfirmedGame {
        val updatedPlayers = confirmedGame.players.map { player ->
            if (player.userId == draft.playerUserId) {
                player.copy(rackTiles = draft.rackTiles)
            } else {
                player
            }
        }

        return confirmedGame.copy(
            players = updatedPlayers,
            boardSets = draft.boardSets
        )
    }

    private fun ConfirmedGame.finishIfWinnerExists(
        actingPlayerUserId: String,
        finishedAt: Instant = Instant.now()
    ): ConfirmedGame {
        return if (hasEmptyRack(actingPlayerUserId)) {
            copy(
                status = GameStatus.FINISHED,
                finishedAt = finishedAt
            )
        } else {
            this
        }
    }

    private fun ConfirmedGame.hasEmptyRack(actingPlayerUserId: String): Boolean {
        return players
            .first { it.userId == actingPlayerUserId }
            .rackTiles
            .isEmpty()
    }

    private fun createNextDraft(game: ConfirmedGame): TurnDraft {
        val nextPlayer = game.players.first { it.userId == game.currentPlayerUserId }

        return TurnDraft(
            gameId = game.gameId,
            playerUserId = nextPlayer.userId,
            boardSets = game.boardSets,
            rackTiles = nextPlayer.rackTiles,
            version = 0
        )
    }
}
