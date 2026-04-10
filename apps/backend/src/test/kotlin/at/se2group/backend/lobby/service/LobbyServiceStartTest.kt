package at.se2group.backend.lobby.service

import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.LobbyBroadcastService
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import at.se2group.backend.service.LobbyService
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.Optional
import java.time.Instant


@ExtendWith(MockitoExtension::class)
class LobbyServiceStartTest {
    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @InjectMocks
    lateinit var lobbyService: LobbyService

    @Test
    fun `startLobby allows host to start successfully`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", true, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", true, Instant.now()),
                LobbyPlayerEmbeddable("player-3", "Alex", true, Instant.now())
            )
        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))
        `when`(lobbyRepository.save(any(LobbyEntity::class.java)))
            .thenAnswer { it.arguments[0] as LobbyEntity }


        val result = lobbyService.startLobby("lobby-1", "host-1")

        assertEquals(LobbyStatus.IN_GAME, result.status)
        verify(lobbyRepository).save(any(LobbyEntity::class.java))
        verify(lobbyBroadcastService).broadcastLobbyStarted(result.lobbyId, result.lobbyId)

    }

    @Test
    fun `startLobby rejects when non host tries to start`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", true, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", true, Instant.now())
            )
        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))


        val exception = assertThrows<SecurityException> {
            lobbyService.startLobby("lobby-1","player-2")
        }

        assertEquals("Only the host can start the match", exception.message)
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)

    }

    @Test
    fun `startLobby rejects when lobby is not open`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.IN_GAME,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", true, Instant.now())
            )
        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.startLobby("lobby-1", "host-1")
        }


        assertEquals("Match can only be started while the lobby is open", exception.message)
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `startLobby rejects when MIN_PLAYERS is not valid`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", true, Instant.now())
            )
        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))



        val exception = assertThrows<IllegalStateException> {
            lobbyService.startLobby("lobby-1", "host-1")
        }

        assertEquals("At least 2 players are required to start the match", exception.message)
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `startLobby rejects when not all players are ready`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", true, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", false, Instant.now()),
                LobbyPlayerEmbeddable("player-3", "Alex", true, Instant.now())
            )
        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))


        val exception = assertThrows<IllegalStateException> {
            lobbyService.startLobby("lobby-1", "host-1")
        }

        assertEquals("All players must be ready to start the match", exception.message)
        verify(lobbyRepository, never()).save(any(LobbyEntity::class.java))
        verifyNoInteractions(lobbyBroadcastService)
    }

}
