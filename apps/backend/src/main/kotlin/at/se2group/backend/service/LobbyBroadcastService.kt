package at.se2group.backend.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.dto.LobbyUpdatedEvent
import at.se2group.backend.mapper.toResponse
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class LobbyBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun broadcastLobbyUpdated(lobby: Lobby) {
        messagingTemplate.convertAndSend(
            "/topic/lobbies/${lobby.lobbyId}",
            LobbyUpdatedEvent(lobby = lobby.toResponse())
        )
    }
}
