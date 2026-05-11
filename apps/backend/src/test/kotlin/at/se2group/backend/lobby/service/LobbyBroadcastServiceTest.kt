package at.se2group.backend.lobby.service

import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import at.se2group.backend.dto.LobbyDeletedEvent
import at.se2group.backend.dto.LobbyStartedEvent
import at.se2group.backend.dto.LobbyUpdatedEvent
import at.se2group.backend.service.LobbyBroadcastService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.SimpMessagingTemplate

@ExtendWith(MockitoExtension::class)
class LobbyBroadcastServiceTest {

    companion object {
        private const val TOPIC_LOBBIES_PATH = "/topic/lobbies"
    }

    @Mock
    lateinit var messagingTemplate: SimpMessagingTemplate

    @InjectMocks
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @Test
    fun `broadcastLobbyUpdated sends updated event to lobby topic`() {
        val lobby = Lobby(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            players = listOf(
                LobbyPlayer(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = true
                )
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        val payloadCaptor = ArgumentCaptor.forClass(LobbyUpdatedEvent::class.java)

        lobbyBroadcastService.broadcastLobbyUpdated(lobby)

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_LOBBIES_PATH/lobby-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value
        assertEquals("lobby.updated", event.type)
        assertEquals("lobby-1", event.lobby.lobbyId)
        assertEquals("host-1", event.lobby.hostUserId)
        assertEquals("OPEN", event.lobby.status)
        assertEquals(4, event.lobby.maxPlayers)
        assertEquals(false, event.lobby.isPrivate)
        assertEquals(true, event.lobby.allowGuests)
        assertEquals(1, event.lobby.players.size)
        assertEquals("Alice", event.lobby.players.first().displayName)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastLobbyDeleted sends deleted event to lobby topic`() {
        val payloadCaptor = ArgumentCaptor.forClass(LobbyDeletedEvent::class.java)

        lobbyBroadcastService.broadcastLobbyDeleted("lobby-1")

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_LOBBIES_PATH/lobby-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value
        assertEquals("lobby.deleted", event.type)
        assertEquals("lobby-1", event.lobbyId)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastLobbyStarted sends started event to lobby topic`() {
        val payloadCaptor = ArgumentCaptor.forClass(LobbyStartedEvent::class.java)

        lobbyBroadcastService.broadcastLobbyStarted("lobby-1", "match-1")

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_LOBBIES_PATH/lobby-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value
        assertEquals("lobby.started", event.type)
        assertEquals("lobby-1", event.lobbyId)
        assertEquals("match-1", event.matchId)

        verifyNoMoreInteractions(messagingTemplate)
    }
}
