package at.se2group.backend.lobby.service

import at.se2group.backend.persistence.LobbyEntity
import shared.models.lobby.domain.LobbyStatus
import at.se2group.backend.persistence.GameRepository
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
import at.se2group.backend.service.AfterCommitExecutor
import at.se2group.backend.service.GameBroadcastService
import at.se2group.backend.service.LobbyBroadcastService
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
import org.mockito.ArgumentCaptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.Optional
import java.time.Instant



@ExtendWith(MockitoExtension::class)
class LobbyServiceLeaveTest {

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

    @Mock
    lateinit var afterCommitExecutor: AfterCommitExecutor

    @InjectMocks
    lateinit var lobbyService: LobbyService

    private fun <T> any(): T {
        org.mockito.Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    private fun runDeferredAction(invocation: org.mockito.invocation.InvocationOnMock) {
        @Suppress("UNCHECKED_CAST")
        (invocation.arguments[0] as () -> Unit).invoke()
    }

    @Test
    fun `leaveLobby allows player to leave successfully`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))
        `when`(lobbyRepository.save(any()))
            .thenAnswer {it.arguments[0] as LobbyEntity}

        `when`(afterCommitExecutor.execute(org.mockito.kotlin.any()))
            .thenAnswer {
                runDeferredAction(it)
            }

        val result = lobbyService.leaveLobby("lobby-1", "player-2")

        assertEquals("lobby-1", result!!.lobbyId)
        assertEquals("host-1", result.hostUserId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(1, result.players.size)
        assertEquals("host-1", result.players[0].userId)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())
        val saved = captor.value
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("host-1", saved.hostUserId)
        assertEquals(1, saved.players.size)
        assertEquals("host-1", saved.players[0].userId)

        verify(afterCommitExecutor).execute(org.mockito.kotlin.any())
        verify(lobbyBroadcastService).broadcastLobbyUpdated(any())
    }

    @Test
    fun `leaveLobby rejects when lobby is not open`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.IN_GAME,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.leaveLobby("lobby-1", "player-2")
        }

        assertEquals("Cannot leave an unopen lobby", exception.message)
        verify(lobbyRepository, never()).save(any())
        verifyNoInteractions(lobbyBroadcastService)
    }


    @Test
    fun `leaveLobby rejects when player is not in lobby`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now())

            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalArgumentException> {
            lobbyService.leaveLobby("lobby-1", "missed-player")
        }

        assertEquals("No player found in this lobby", exception.message)
        verify(lobbyRepository, never()).save(any())
        verifyNoInteractions(lobbyBroadcastService)
    }


    @Test
    fun `leaveLobby assigns new host when previous host and its players exit the game`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))
        `when`(lobbyRepository.save(any()))
            .thenAnswer {it.arguments[0] as LobbyEntity }

        `when`(afterCommitExecutor.execute(org.mockito.kotlin.any()))
            .thenAnswer {
                runDeferredAction(it)
            }

        val result = lobbyService.leaveLobby("lobby-1", "host-1")

        assertEquals("lobby-1", result!!.lobbyId)
        assertEquals(LobbyStatus.OPEN, result.status)
        assertEquals(1, result.players.size)
        assertEquals("player-2", result.players[0].userId)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())

        val saved = captor.value
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("player-2", saved.hostUserId)
        assertEquals(1, saved.players.size)
        assertEquals("player-2", saved.players[0].userId)

        verify(afterCommitExecutor).execute(org.mockito.kotlin.any())
        verify(lobbyBroadcastService).broadcastLobbyUpdated(any())
    }




    @Test
    fun `leaveLobby deletes lobby when host leaves`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            players = mutableListOf(
                LobbyPlayerEmbeddable("host-1", "Anna", false, Instant.now())
            )
        )

        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))
        `when`(afterCommitExecutor.execute(org.mockito.kotlin.any()))
            .thenAnswer {
                runDeferredAction(it)
            }

        val result = lobbyService.leaveLobby("lobby-1", "host-1")

        assertEquals(null, result)

        verify(lobbyRepository).deleteById("lobby-1")
        verify(afterCommitExecutor).execute(org.mockito.kotlin.any())
        verify(lobbyBroadcastService).broadcastLobbyDeleted("lobby-1")
    }
}
