package at.se2group.backend.lobby.service

import shared.models.lobby.domain.LobbyStatus
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.GameBroadcastService
import at.se2group.backend.service.LobbyBroadcastService
import at.se2group.backend.service.GameInitializationService
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
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LobbyServiceJoinTest {

    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @Mock
    lateinit var gameInitializationService: GameInitializationService

    @Mock
    lateinit var gameRepository: GameRepository

    @Mock
    lateinit var gameBroadcastService: GameBroadcastService

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
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
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

        assertEquals("lobby-1", result.lobbyId)
        assertEquals("host-1", result.hostUserId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(4, result.settings.maxPlayers)
        assertFalse(result.settings.isPrivate)
        assertTrue(result.settings.allowGuests)

        assertEquals(2, result.players.size)

        val originalPlayer = result.players.first()
        assertEquals("host-1", originalPlayer.userId)
        assertEquals("Alice", originalPlayer.displayName)
        assertFalse(originalPlayer.isReady)

        val joinedPlayer = result.players.last()
        assertEquals("player-2", joinedPlayer.userId)
        assertEquals("Bob", joinedPlayer.displayName)
        assertFalse(joinedPlayer.isReady)

        val saveCaptor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(saveCaptor.capture())

        val saved = saveCaptor.value
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("host-1", saved.hostUserId)
        assertEquals(LobbyStatus.OPEN, saved.status)
        assertEquals(4, saved.maxPlayers)
        assertFalse(saved.isPrivate)
        assertTrue(saved.allowGuests)
        assertEquals(2, saved.players.size)
        assertEquals("host-1", saved.players.first().userId)
        assertEquals("Alice", saved.players.first().displayName)
        assertFalse(saved.players.first().isReady)
        assertEquals("player-2", saved.players.last().userId)
        assertEquals("Bob", saved.players.last().displayName)
        assertFalse(saved.players.last().isReady)

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
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                ),
                LobbyPlayerEmbeddable(
                    userId = "player-2",
                    displayName = "Bob",
                    isReady = false,
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
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                ),
                LobbyPlayerEmbeddable(
                    userId = "player-2",
                    displayName = "Bob",
                    isReady = false,
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
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                ),
                LobbyPlayerEmbeddable(
                    userId = "player-2",
                    displayName = "Bob",
                    isReady = false,
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
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
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
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("host-1", saved.hostUserId)
        assertEquals(LobbyStatus.OPEN, saved.status)
        assertEquals(4, saved.maxPlayers)
        assertFalse(saved.isPrivate)
        assertTrue(saved.allowGuests)

        assertEquals(2, saved.players.size)

        val originalPlayer = saved.players.first()
        assertEquals("host-1", originalPlayer.userId)
        assertEquals("Alice", originalPlayer.displayName)
        assertFalse(originalPlayer.isReady)

        val joinedPlayer = saved.players.last()
        assertEquals("player-2", joinedPlayer.userId)
        assertEquals("Bob", joinedPlayer.displayName)
        assertFalse(joinedPlayer.isReady)
    }
}
