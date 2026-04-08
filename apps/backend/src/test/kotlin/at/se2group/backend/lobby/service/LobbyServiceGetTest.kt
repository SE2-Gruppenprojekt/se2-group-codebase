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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LobbyServiceGetTest {

    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @InjectMocks
    lateinit var lobbyService: LobbyService

    @Test
    fun `getLobby returns existing lobby`() {
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

        Mockito.`when`(lobbyRepository.findById("lobby-1"))
            .thenReturn(Optional.of(entity))

        val result = lobbyService.getLobby("lobby-1")

        assertEquals("lobby-1", result.lobbyId)
        assertEquals("host-1", result.hostUserId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(4, result.settings.maxPlayers)
        assertFalse(result.settings.isPrivate)
        assertTrue(result.settings.allowGuests)
        assertEquals(1, result.players.size)
        assertEquals("host-1", result.players.first().userId)
        assertEquals("Alice", result.players.first().displayName)
        assertFalse(result.players.first().isReady)

        verify(lobbyRepository).findById("lobby-1")
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `getLobby throws NoSuchElementException when lobby does not exist`() {
        Mockito.`when`(lobbyRepository.findById("missing-lobby"))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NoSuchElementException> {
            lobbyService.getLobby("missing-lobby")
        }

        assertEquals("Lobby not found", exception.message)

        verify(lobbyRepository).findById("missing-lobby")
        verifyNoInteractions(lobbyBroadcastService)
    }
}
