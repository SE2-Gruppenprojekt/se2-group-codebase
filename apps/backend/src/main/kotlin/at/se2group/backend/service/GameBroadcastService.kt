package at.se2group.backend.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class GameBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private companion object {
        const val GAME_TOPIC_PREFIX = "/topic/games"
    }

    private fun gameTopic(gameId: String) = "$GAME_TOPIC_PREFIX/$gameId"


}
