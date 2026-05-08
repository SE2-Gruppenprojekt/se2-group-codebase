package at.se2group.backend.lobby.service

import shared.models.lobby.domain.LobbyStatus
import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyRepository
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
import kotlin.IllegalArgumentException

@ExtendWith(MockitoExtension::class)
class LobbyServiceCreateTest {

    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @Mock
    lateinit var gameInitializationService: GameInitializationService

    @Mock
    lateinit var gameRepository: GameRepository

    @InjectMocks
    lateinit var lobbyService: LobbyService

    @Test
    fun `createLobby creates a valid open lobby with host as first player`() {
        val request = CreateLobbyRequest(
            displayName = "Alice",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        Mockito.`when`(lobbyRepository.save(any(LobbyEntity::class.java)))
            .thenAnswer { it.arguments[0] as LobbyEntity }

        val result = lobbyService.createLobby("host alice", request)

        assertEquals("host alice", result.hostUserId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(4, result.settings.maxPlayers)
        assertFalse(result.settings.isPrivate)
        assertTrue(result.settings.allowGuests)
        assertEquals(1, result.players.size)
        assertEquals("host alice", result.players.first().userId)
        assertEquals("Alice", result.players.first().displayName)
        assertFalse(result.players.first().isReady)

        val saveCaptor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(saveCaptor.capture())

        val saved = saveCaptor.value
        assertEquals("host alice", saved.hostUserId)
        assertEquals(LobbyStatus.OPEN, saved.status)
        assertEquals(4, saved.maxPlayers)
        assertFalse(saved.isPrivate)
        assertTrue(saved.allowGuests)
        assertEquals(1, saved.players.size)
        assertEquals("host alice", saved.players.first().userId)
        assertEquals("Alice", saved.players.first().displayName)
        assertFalse(saved.players.first().isReady)

        verify(lobbyBroadcastService).broadcastLobbyUpdated(result)
    }

    @Test
    fun `createLobby rejects maxPlayers below minimum`() {
        val request = CreateLobbyRequest(
            displayName = "Alice",
            maxPlayers = 1,
            isPrivate = false,
            allowGuests = true
        )

        val exception = assertThrows<IllegalArgumentException> {
            lobbyService.createLobby("host alice", request)
        }

        assertEquals("maxPlayers must be between 2 and 8", exception.message)

        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `createLobby rejects maxPlayers above maximum`() {
        val request = CreateLobbyRequest(
            displayName = "Alice",
            maxPlayers = 99,
            isPrivate = false,
            allowGuests = true
        )

        val exception = assertThrows<IllegalArgumentException> {
            lobbyService.createLobby("host alice", request)
        }

        assertEquals("maxPlayers must be between 2 and 8", exception.message)

        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `createLobby persists created lobby`() {
        val request = CreateLobbyRequest(
            displayName = "Alice",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        Mockito.`when`(lobbyRepository.save(any(LobbyEntity::class.java)))
            .thenAnswer { it.arguments[0] as LobbyEntity }

        lobbyService.createLobby("host alice", request)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())

        val saved = captor.value
        assertEquals("host alice", saved.hostUserId)
        assertEquals(LobbyStatus.OPEN, saved.status)
        assertEquals(4, saved.maxPlayers)
        assertFalse(saved.isPrivate)
        assertTrue(saved.allowGuests)
        assertEquals(1, saved.players.size)
        assertEquals("host alice", saved.players.first().userId)
        assertEquals("Alice", saved.players.first().displayName)
        assertFalse(saved.players.first().isReady)
    }
}
