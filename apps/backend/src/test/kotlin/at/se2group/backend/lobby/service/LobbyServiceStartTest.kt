package at.se2group.backend.lobby.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.GameStartResult
import at.se2group.backend.domain.GameStatus
import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.domain.TurnDraft
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import at.se2group.backend.persistence.LobbyRepository
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.ArgumentCaptor
import java.util.Optional
import java.time.Instant


@ExtendWith(MockitoExtension::class)
class LobbyServiceStartTest {
    @Mock
    lateinit var lobbyRepository: LobbyRepository

    @Mock
    lateinit var lobbyBroadcastService: LobbyBroadcastService

    @Mock
    lateinit var gameInitializationService: GameInitializationService

    @InjectMocks
    lateinit var lobbyService: LobbyService

    private fun <T> any(): T {
        org.mockito.Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

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
        `when`(lobbyRepository.save(any()))
            .thenAnswer { it.arguments[0] as LobbyEntity }
        `when`(gameInitializationService.createGameFromLobby(any()))
            .thenReturn(
                GameStartResult(
                    confirmedGame = ConfirmedGame(
                        gameId = "game-123",
                        lobbyId = "lobby-1",
                        players = listOf(
                            GamePlayer(
                                userId = "host-1",
                                displayName = "Anna",
                                turnOrder = 0
                            )
                        ),
                        currentPlayerUserId = "host-1",
                        status = GameStatus.ACTIVE
                    ),
                    turnDraft = TurnDraft(
                        gameId = "game-123",
                        playerUserId = "host-1"
                    )
                )
            )


        val result = lobbyService.startLobby("lobby-1", "host-1")

        assertEquals("lobby-1", result.lobbyId)
        assertEquals("host-1", result.hostUserId)
        assertEquals(LobbyStatus.IN_GAME, result.status)
        assertEquals(3, result.players.size)

        val captor = ArgumentCaptor.forClass(LobbyEntity::class.java)
        verify(lobbyRepository).save(captor.capture())
        val saved = captor.value
        assertEquals("lobby-1", saved.lobbyId)
        assertEquals("host-1", saved.hostUserId)
        assertEquals(LobbyStatus.IN_GAME, saved.status)
        assertEquals(3, saved.players.size)


        verify(gameInitializationService).createGameFromLobby(result)
        verify(lobbyBroadcastService).broadcastLobbyStarted(result.lobbyId, "game-123")

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
        verify(lobbyRepository, never()).save(any())
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
                LobbyPlayerEmbeddable("host-1", "Anna", true, Instant.now()),
                LobbyPlayerEmbeddable("player-2", "Marco", true, Instant.now())
            )
        )
        `when`(lobbyRepository.findById("lobby-1")).thenReturn(Optional.of(entity))

        val exception = assertThrows<IllegalStateException> {
            lobbyService.startLobby("lobby-1", "host-1")
        }


        assertEquals("Match can only be started while the lobby is open", exception.message)
        verify(lobbyRepository, never()).save(any())
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
        verify(lobbyRepository, never()).save(any())
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
        verify(lobbyRepository, never()).save(any())
        verifyNoInteractions(lobbyBroadcastService)
    }

}
