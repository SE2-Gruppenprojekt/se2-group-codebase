package at.se2group.backend.lobby.service

import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyPlayer
import at.se2group.backend.domain.LobbySettings
import at.se2group.backend.domain.LobbyStatus
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
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class LobbyBroadcastServiceTest {

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
                    isReady = true,
                    joinedAt = Instant.parse("2026-04-12T10:00:00Z")
                )
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            ),
            createdAt = Instant.parse("2026-04-12T09:00:00Z")
        )

        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)

        lobbyBroadcastService.broadcastLobbyUpdated(lobby)

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("/topic/lobbies/lobby-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value as LobbyUpdatedEvent
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
        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)

        lobbyBroadcastService.broadcastLobbyDeleted("lobby-1")

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("/topic/lobbies/lobby-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value as LobbyDeletedEvent
        assertEquals("lobby.deleted", event.type)
        assertEquals("lobby-1", event.lobbyId)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastLobbyStarted sends started event to lobby topic`() {
        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)

        lobbyBroadcastService.broadcastLobbyStarted("lobby-1", "match-1")

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("/topic/lobbies/lobby-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value as LobbyStartedEvent
        assertEquals("lobby.started", event.type)
        assertEquals("lobby-1", event.lobbyId)
        assertEquals("match-1", event.matchId)

        verifyNoMoreInteractions(messagingTemplate)
    }
}
