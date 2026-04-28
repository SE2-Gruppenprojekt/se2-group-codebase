package at.se2group.backend.lobby.service

import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.LobbyBroadcastService
import at.se2group.backend.service.GameInitializationService
import at.se2group.backend.service.LobbyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LobbyServiceDeleteTest {

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
    fun `deleteLobby allows host to delete lobby successfully`() {
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

        lobbyService.deleteLobby("lobby-1", "host-1")

        verify(lobbyRepository).findById("lobby-1")
        verify(lobbyRepository).deleteById("lobby-1")
        verify(lobbyBroadcastService).broadcastLobbyDeleted("lobby-1")
        verifyNoMoreInteractions(lobbyRepository, lobbyBroadcastService)
    }

    @Test
    fun `deleteLobby rejects if non host tries to delete lobby`() {
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

        val exception = assertThrows<SecurityException> {
            lobbyService.deleteLobby("lobby-1", "player-2")
        }

        assertEquals("Only the host can delete the lobby", exception.message)
        verify(lobbyRepository).findById("lobby-1")
        verify(lobbyRepository, never()).deleteById("lobby-1")
        verifyNoInteractions(lobbyBroadcastService)
    }
}
