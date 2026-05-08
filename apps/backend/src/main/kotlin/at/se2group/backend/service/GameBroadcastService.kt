package at.se2group.backend.service

import at.se2group.backend.mapper.toResponse
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import shared.models.game.domain.TurnDraft
import shared.models.game.event.GameDraftUpdatedEvent

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
}
