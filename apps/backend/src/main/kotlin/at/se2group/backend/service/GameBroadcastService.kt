package at.se2group.backend.service

import at.se2group.backend.mapper.toResponse
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.TurnDraft
import shared.models.game.event.GameDraftUpdatedEvent
import shared.models.game.event.GameUpdatedEvent
import shared.models.game.event.TurnChangedEvent
import shared.models.game.event.TurnTimedOutEvent

@Service
class GameBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private companion object {
        const val GAME_TOPIC_PREFIX = "/topic/games"
    }

    private fun gameTopic(gameId: String) = "$GAME_TOPIC_PREFIX/$gameId"

    fun broadcastDraftUpdated(draft: TurnDraft) {
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
        messagingTemplate.convertAndSend(
            gameTopic(game.gameId),
            GameUpdatedEvent(
                gameId = game.gameId,
                game = game.toResponse()
            )
        )
    }

    fun broadcastTurnChanged(gameId: String, currentTurnPlayerId: String) {
        messagingTemplate.convertAndSend(
            gameTopic(gameId),
            TurnChangedEvent(
                gameId = gameId,
                currentTurnPlayerId = currentTurnPlayerId
            )
        )
    }

    fun broadcastTurnTimedOut(gameId: String, previousTurnPlayerId: String) {
        messagingTemplate.convertAndSend(
            gameTopic(gameId),
            TurnTimedOutEvent(
                gameId = gameId,
                previousTurnPlayerId = previousTurnPlayerId
            )
        )
    }
}
