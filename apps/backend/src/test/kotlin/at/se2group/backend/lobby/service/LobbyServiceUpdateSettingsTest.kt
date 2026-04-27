package at.se2group.backend.lobby.service

import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.LobbyBroadcastService
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.service.GameInitializationService
import at.se2group.backend.service.LobbyService
import org.junit.jupiter.api.extension.ExtendWith
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
import org.mockito.ArgumentCaptor
import java.util.Optional
import java.time.Instant


@ExtendWith(MockitoExtension::class)
class LobbyServiceUpdateSettingsTest {

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

    private fun <T> any(): T {
        org.mockito.Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }


    @Test
    fun ` updateLobbySettings allows host to update successfully`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate  = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now())
            )

        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))
        `when`(lobbyRepository.save(any()))
        .thenAnswer { it.arguments[0] as LobbyEntity }

        val request = UpdateLobbySettingsRequest(maxPlayers = 3, isPrivate = true, allowGuests = false)
        val result = lobbyService.updateLobbySettings("lobby-1", "host-1", request)

        assertEquals("lobby-1", result.lobbyId)
        assertEquals("host-1", result.hostUserId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(true, result.settings.isPrivate)
        assertEquals(false, result.settings.allowGuests)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())
        val saved = captor.value
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("host-1", saved.hostUserId)
        assertEquals(3, saved.maxPlayers)
        assertEquals(true, saved.isPrivate)
        assertEquals(false, saved.allowGuests)

        assertEquals(3, result.settings.maxPlayers)
        verify(lobbyBroadcastService).broadcastLobbyUpdated(result)
    }

    @Test
    fun ` updateLobbySettings rejects when non host tries to update`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate  = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", false, Instant.now())
            )

        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val request = UpdateLobbySettingsRequest(maxPlayers = 3, isPrivate = true, allowGuests = false)
        val exception = assertThrows<SecurityException> {
            lobbyService.updateLobbySettings("lobby-1", "player-2",request)
        }

        assertEquals("Only the host can update lobby settings", exception.message)
        verify(lobbyRepository, never()).save(any())
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun ` updateLobbySettings rejects when lobby is not open`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.IN_GAME,
            maxPlayers = 4,
            isPrivate  = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now()),

            )

        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val request = UpdateLobbySettingsRequest(maxPlayers = 3, isPrivate = true, allowGuests = false)
        val exception = assertThrows<IllegalStateException> {
            lobbyService.updateLobbySettings("lobby-1", "host-1",request)
        }

        assertEquals("Lobby settings can only be changed while the lobby is open", exception.message)
        verify(lobbyRepository, never()).save(any())
        verifyNoInteractions(lobbyBroadcastService)
    }

    @Test
    fun `updateLobbySettings rejects when number of players exceeds maxPlayers`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate  = false,
            allowGuests = true,
            createdAt = Instant.now(),
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", false, Instant.now()),
                LobbyPlayerEmbeddable("player-3", "Alex", false, Instant.now())

            )

        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val request = UpdateLobbySettingsRequest(maxPlayers = 2, isPrivate = true, allowGuests = false)
        val exception = assertThrows<IllegalArgumentException> {
            lobbyService.updateLobbySettings("lobby-1", "host-1",request)
        }

        assertEquals("Maximum players must be between 3 and 8", exception.message)
        verify(lobbyRepository, never()).save(any())
        verifyNoInteractions(lobbyBroadcastService)
    }
}
