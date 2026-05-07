package at.se2group.backend.service

import at.se2group.backend.dto.LobbyDeletedEvent
import at.se2group.backend.dto.LobbyStartedEvent
import at.se2group.backend.dto.LobbyUpdatedEvent
import at.se2group.backend.mapper.toResponse
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import shared.models.lobby.domain.Lobby

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

    fun broadcastLobbyDeleted(lobbyId: String) {
        messagingTemplate.convertAndSend(
            "/topic/lobbies/$lobbyId",
            LobbyDeletedEvent(lobbyId = lobbyId)
        )
    }

    fun broadcastLobbyStarted(lobbyId: String, matchId: String) {
        messagingTemplate.convertAndSend(
            "/topic/lobbies/$lobbyId",
            LobbyStartedEvent(
                lobbyId = lobbyId,
                matchId = matchId
            )
        )
    }
}
