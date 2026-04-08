package at.se2group.backend.lobby.service

import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.LobbyBroadcastService
import at.se2group.backend.service.LobbyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LobbyServiceJoinTest {

    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @InjectMocks
    lateinit var lobbyService: LobbyService

    @Test
    fun `joinLobby allows a player to join an open lobby successfully`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                    joinedAt = Instant.now()
                )
            )
        )

        val request = JoinLobbyRequest(
            userId = "player-2",
            displayName = "Bob"
        )

        Mockito.`when`(lobbyRepository.findById("lobby-1"))
            .thenReturn(Optional.of(entity))
        Mockito.`when`(lobbyRepository.save(any(LobbyEntity::class.java)))
            .thenAnswer { it.arguments[0] as LobbyEntity }

        val result = lobbyService.joinLobby("lobby-1", request)

        assertEquals(2, result.players.size)
        assertEquals("player-2", result.players.last().userId)
        assertEquals("Bob", result.players.last().displayName)
        assertFalse(result.players.last().isReady)

        verify(lobbyRepository).findById("lobby-1")
        verify(lobbyRepository).save(any(LobbyEntity::class.java))
        verify(lobbyBroadcastService).broadcastLobbyUpdated(result)
    }

    @Test
    fun `joinLobby rejects joining when lobby is not open`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.IN_GAME,
            maxPlayers = 2,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                    joinedAt = Instant.now()
                ),
                LobbyPlayerEmbeddable(
                    userId = "player-2",
                    displayName = "Bob",
                    isReady = false,
                    joinedAt = Instant.now()
                )
            )
        )

        val request = JoinLobbyRequest(
            userId = "player-3",
            displayName = "Charlie"
        )

        Mockito.`when`(lobbyRepository.findById("lobby-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.joinLobby("lobby-1", request)
        }

        assertEquals("Lobby is not open", exception.message)

        verify(lobbyRepository).findById("lobby-1")
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `joinLobby rejects joining when lobby is full`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 2,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                    joinedAt = Instant.now()
                ),
                LobbyPlayerEmbeddable(
                    userId = "player-2",
                    displayName = "Bob",
                    isReady = false,
                    joinedAt = Instant.now()
                )
            )
        )

        val request = JoinLobbyRequest(
            userId = "player-3",
            displayName = "Charlie"
        )

        Mockito.`when`(lobbyRepository.findById("lobby-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.joinLobby("lobby-1", request)
        }

        assertEquals("Lobby is full", exception.message)

        verify(lobbyRepository).findById("lobby-1")
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `joinLobby rejects joining when player is already in lobby`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                    joinedAt = Instant.now()
                ),
                LobbyPlayerEmbeddable(
                    userId = "player-2",
                    displayName = "Bob",
                    isReady = false,
                    joinedAt = Instant.now()
                )
            )
        )

        val request = JoinLobbyRequest(
            userId = "player-2",
            displayName = "Bob"
        )

        Mockito.`when`(lobbyRepository.findById("lobby-1"))
            .thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.joinLobby("lobby-1", request)
        }

        assertEquals("Player already in lobby", exception.message)

        verify(lobbyRepository).findById("lobby-1")
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `joinLobby persists updated lobby through repository`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                    joinedAt = Instant.now()
                )
            )
        )

        val request = JoinLobbyRequest(
            userId = "player-2",
            displayName = "Bob"
        )

        Mockito.`when`(lobbyRepository.findById("lobby-1"))
            .thenReturn(Optional.of(entity))
        Mockito.`when`(lobbyRepository.save(any(LobbyEntity::class.java)))
            .thenAnswer { it.arguments[0] as LobbyEntity }

        lobbyService.joinLobby("lobby-1", request)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())

        val saved = captor.value
        assertEquals(2, saved.players.size)
        assertEquals("player-2", saved.players.last().userId)
        assertEquals("Bob", saved.players.last().displayName)
        assertFalse(saved.players.last().isReady)
    }
}
