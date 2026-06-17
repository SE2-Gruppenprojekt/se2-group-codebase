package at.se2group.backend.lobby.service

import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.security.JwtService
import at.se2group.backend.service.LobbyAuthenticationService
import at.se2group.backend.service.LobbyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

@ExtendWith(MockitoExtension::class)
class LobbyAuthenticationServiceTest {

    @Mock
    lateinit var lobbyService: LobbyService

    @Mock
    lateinit var jwtService: JwtService

    private fun lobby(userId: String, displayName: String) = Lobby(
        lobbyId = "123456",
        hostUserId = userId,
        players = listOf(LobbyPlayer(userId, displayName, false)),
        status = LobbyStatus.OPEN,
        settings = LobbySettings(maxPlayers = 4, isPrivate = false, allowGuests = true)
    )

    @Test
    fun `createAuthenticatedLobby issues token for generated user and returns lobby response`() {
        val request = CreateLobbyRequest(displayName = "Alice", maxPlayers = 4)

        `when`(lobbyService.createLobby(org.mockito.kotlin.any(), org.mockito.kotlin.eq(request)))
            .thenAnswer { invocation ->
                val generatedUserId = invocation.arguments[0] as String
                lobby(generatedUserId, "Alice")
            }
        `when`(jwtService.issueAccessToken(org.mockito.kotlin.any()))
            .thenAnswer { invocation -> "token:${invocation.arguments[0] as String}" }

        val service = LobbyAuthenticationService(lobbyService, jwtService)
        val response = service.createAuthenticatedLobby(request)
        val generatedUserId = response.lobby.hostUserId

        assertTrue(generatedUserId.isNotBlank())
        assertEquals("token:$generatedUserId", response.accessToken)
        assertEquals(generatedUserId, response.lobby.hostUserId)
        assertEquals(generatedUserId, response.lobby.players.first().userId)
        assertEquals("Alice", response.lobby.players.first().displayName)
        assertFalse(response.lobby.players.first().isReady)
    }

    @Test
    fun `joinAuthenticatedLobby issues token for generated user and returns joined lobby response`() {
        val request = JoinLobbyRequest(displayName = "Bob")

        `when`(lobbyService.joinLobby(org.mockito.kotlin.eq("123456"), org.mockito.kotlin.any(), org.mockito.kotlin.eq(request)))
            .thenAnswer { invocation ->
                val generatedUserId = invocation.arguments[1] as String
                Lobby(
                    lobbyId = "123456",
                    hostUserId = "host-1",
                    players = listOf(
                        LobbyPlayer("host-1", "Alice", false),
                        LobbyPlayer(generatedUserId, "Bob", false)
                    ),
                    status = LobbyStatus.OPEN,
                    settings = LobbySettings(maxPlayers = 4, isPrivate = false, allowGuests = true)
                )
            }
        `when`(jwtService.issueAccessToken(org.mockito.kotlin.any()))
            .thenAnswer { invocation -> "token:${invocation.arguments[0] as String}" }

        val service = LobbyAuthenticationService(lobbyService, jwtService)
        val response = service.joinAuthenticatedLobby("123456", request)
        val generatedUserId = response.lobby.players.last().userId

        assertTrue(generatedUserId.isNotBlank())
        assertTrue(response.lobby.players.any { it.userId == generatedUserId })
        assertEquals("token:$generatedUserId", response.accessToken)
        assertEquals("host-1", response.lobby.hostUserId)
        assertEquals("Bob", response.lobby.players.last().displayName)
    }
}
