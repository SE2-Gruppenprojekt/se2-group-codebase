package at.se2group.backend.service

import at.se2group.backend.dto.LobbyDeletedEvent
import at.se2group.backend.dto.LobbyStartedEvent
import at.se2group.backend.dto.LobbyUpdatedEvent
import at.se2group.backend.mapper.toResponse
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import shared.models.lobby.domain.Lobby

@Service
class LobbyBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun broadcastLobbyUpdated(lobby: Lobby) {
        logger.info(
            "Broadcasting lobby.updated to topic=/topic/lobbies/{} for lobbyId={} status={}",
            lobby.lobbyId,
            lobby.lobbyId,
            lobby.status
        )
        messagingTemplate.convertAndSend(
            "/topic/lobbies/${lobby.lobbyId}",
            LobbyUpdatedEvent(lobby = lobby.toResponse())
        )
    }

    fun broadcastLobbyDeleted(lobbyId: String) {
        logger.info(
            "Broadcasting lobby.deleted to topic=/topic/lobbies/{} for lobbyId={}",
            lobbyId,
            lobbyId
        )
        messagingTemplate.convertAndSend(
            "/topic/lobbies/$lobbyId",
            LobbyDeletedEvent(lobbyId = lobbyId)
        )
    }

    fun broadcastLobbyStarted(lobbyId: String, matchId: String) {
        logger.info(
            "Broadcasting lobby.started to topic=/topic/lobbies/{} for lobbyId={} matchId={}",
            lobbyId,
            lobbyId,
            matchId
        )
        messagingTemplate.convertAndSend(
            "/topic/lobbies/$lobbyId",
            LobbyStartedEvent(
                lobbyId = lobbyId,
                matchId = matchId
            )
        )
    }
}
