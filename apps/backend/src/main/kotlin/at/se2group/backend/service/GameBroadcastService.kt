package at.se2group.backend.service

import at.se2group.backend.mapper.toResponse
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.TurnDraft
import shared.models.game.event.GameDraftUpdatedEvent
import shared.models.game.event.GameEndedEvent
import shared.models.game.event.GameUpdatedEvent
import shared.models.game.event.TurnChangedEvent
import shared.models.game.event.TurnTimedOutEvent

@Service
class GameBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val GAME_TOPIC_PREFIX = "/topic/games"
    }

    private fun gameTopic(gameId: String) = "$GAME_TOPIC_PREFIX/$gameId"

    fun broadcastDraftUpdated(draft: TurnDraft) {
        logger.info(
            "Broadcasting game.draft.updated to topic={} for gameId={} playerUserId={}",
            gameTopic(draft.gameId),
            draft.gameId,
            draft.playerUserId
        )
        messagingTemplate.convertAndSend(
            gameTopic(draft.gameId),
            GameDraftUpdatedEvent(
                gameId = draft.gameId,
                playerId = draft.playerUserId,
                draft = draft.toResponse()
            )
        )
    }

    fun broadcastGameUpdated(game: ConfirmedGame) {
        logger.info(
            "Broadcasting game.updated to topic={} for gameId={} currentPlayerUserId={} status={}",
            gameTopic(game.gameId),
            game.gameId,
            game.currentPlayerUserId,
            game.status
        )
        messagingTemplate.convertAndSend(
            gameTopic(game.gameId),
            GameUpdatedEvent(
                gameId = game.gameId,
                game = game.toResponse()
            )
        )
    }

    fun broadcastTurnChanged(gameId: String, currentTurnPlayerId: String) {
        logger.info(
            "Broadcasting turn.changed to topic={} for gameId={} currentTurnPlayerId={}",
            gameTopic(gameId),
            gameId,
            currentTurnPlayerId
        )
        messagingTemplate.convertAndSend(
            gameTopic(gameId),
            TurnChangedEvent(
                gameId = gameId,
                currentTurnPlayerId = currentTurnPlayerId
            )
        )
    }

    fun broadcastTurnTimedOut(gameId: String, previousTurnPlayerId: String) {
        logger.info(
            "Broadcasting turn.timed_out to topic={} for gameId={} previousTurnPlayerId={}",
            gameTopic(gameId),
            gameId,
            previousTurnPlayerId
        )
        messagingTemplate.convertAndSend(
            gameTopic(gameId),
            TurnTimedOutEvent(
                gameId = gameId,
                previousTurnPlayerId = previousTurnPlayerId
            )
        )
    }

    fun broadcastGameEnded(gameId: String, winnerUserId: String) {
        logger.info(
            "Broadcasting game.ended to topic={} for gameId={} winnerUserId={}",
            gameTopic(gameId),
            gameId,
            winnerUserId
        )
        messagingTemplate.convertAndSend(
            gameTopic(gameId),
            GameEndedEvent(
                gameId = gameId,
                winnerUserId = winnerUserId
            )
        )
    }
}
