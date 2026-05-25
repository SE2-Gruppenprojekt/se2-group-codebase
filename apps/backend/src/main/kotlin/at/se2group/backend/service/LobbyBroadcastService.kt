package at.se2group.backend.service

import at.se2group.backend.dto.LobbyDeletedEvent
import at.se2group.backend.dto.LobbyStartedEvent
import at.se2group.backend.dto.LobbyUpdatedEvent
import at.se2group.backend.mapper.toResponse
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import shared.models.lobby.domain.Lobby

/**
 * Emits backend lobby websocket events to lobby-specific topics.
 *
 * The service keeps websocket topic construction and event payload creation out
 * of the main lobby service layer. That keeps the lobby application logic
 * focused on state transitions while this class owns the realtime transport
 * contract and its logging.
 *
 * Emitted event families:
 *
 * - lobby state updates
 * - lobby deletion notifications
 * - lobby start notifications with the created match id
 *
 * Each emission is logged with the event type and its routing identifiers so
 * that lobby lifecycle problems can be traced without inspecting the full
 * payload bodies.
 */
@Service
class LobbyBroadcastService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun broadcastLobbyUpdated(lobby: Lobby) {
        // Log before emission so lobby lifecycle transitions remain visible even if delivery is investigated later.
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
