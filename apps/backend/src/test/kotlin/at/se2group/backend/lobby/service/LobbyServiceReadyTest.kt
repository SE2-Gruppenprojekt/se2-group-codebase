package at.se2group.backend.lobby.service

import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.LobbyBroadcastService
import at.se2group.backend.service.LobbyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LobbyServiceReadyTest {

    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @InjectMocks
    lateinit var lobbyService: LobbyService

    @Test
    fun `readyLobby marks player as ready successfully`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Alice", false, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Bob", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))
        `when`(lobbyRepository.save(any(LobbyEntity::class.java)))
            .thenAnswer { it.arguments[0] as LobbyEntity }

        val result = lobbyService.readyLobby("lobby-1", "player-2")
        assertEquals("lobby-1", result.lobbyId)
        assertEquals("host-1", result.hostUserId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(4, result.settings.maxPlayers)
        assertFalse(result.settings.isPrivate)
        assertTrue(result.settings.allowGuests)
        assertEquals(2, result.players.size)

        val player = result.players.first { it.userId == "player-2" }
        assertTrue(player.isReady)

        val otherPlayer = result.players.first { it.userId == "host-1" }
        assertFalse(otherPlayer.isReady)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())

        val saved = captor.value
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("host-1", saved.hostUserId)
        assertEquals(LobbyStatus.OPEN, saved.status)
        assertEquals(4, saved.maxPlayers)
        assertFalse(saved.isPrivate)
        assertTrue(saved.allowGuests)
        assertEquals(2, saved.players.size)

        assertTrue(saved.players.first { it.userId == "player-2" }.isReady)
        assertFalse(saved.players.first { it.userId == "host-1" }.isReady)

        verify(lobbyBroadcastService).broadcastLobbyUpdated(result)
    }

    @Test
    fun `readyLobby rejects if lobby is not open`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.CLOSED,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("player-2", "Bob", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.readyLobby("lobby-1", "player-2")
        }

        assertEquals("You cannot change the ready status, while lobby is not open", exception.message)
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `readyLobby rejects if player is not in lobby`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Alice", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalArgumentException> {
            lobbyService.readyLobby("lobby-1", "missing-player")
        }

        assertEquals("No player was found in this lobby", exception.message)
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }
}
