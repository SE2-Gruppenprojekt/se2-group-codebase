package at.se2group.backend.api

import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.dto.UpdateLobbySettingsRequest
import at.se2group.backend.mapper.toResponse
import at.se2group.backend.security.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus

@SpringBootTest
@AutoConfigureMockMvc
class LobbyControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var lobbyService: at.se2group.backend.service.LobbyService

    @MockitoBean
    lateinit var lobbyAuthenticationService: at.se2group.backend.service.LobbyAuthenticationService

    @MockitoBean
    lateinit var jwtService: JwtService

    private fun lobby(): Lobby = Lobby(
        lobbyId = "123",
        hostUserId = "user1",
        players = listOf(LobbyPlayer("user1", "Stefan", false)),
        status = LobbyStatus.OPEN,
        settings = LobbySettings(4, false, true)
    )

    @Test
    fun `createLobby should return authenticated response for valid request`() {
        val request = CreateLobbyRequest(displayName = "Stefan", maxPlayers = 4)
        `when`(lobbyAuthenticationService.createAuthenticatedLobby(any()))
            .thenReturn(
                at.se2group.backend.dto.AuthenticatedLobbyResponse(
                    accessToken = "token-1",
                    lobby = lobby().toResponse()
                )
            )

        mockMvc.post("/api/lobbies") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("token-1") }
            jsonPath("$.lobby.lobbyId") { value("123") }
        }
    }

    @Test
    fun `createLobby should return 400 for invalid request`() {
        mockMvc.post("/api/lobbies") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"displayName":"","maxPlayers":4}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("BAD_REQUEST") }
            jsonPath("$.errorMessage") { value("Request validation failed") }
        }
    }

    @Test
    fun `joinLobby should return authenticated response for valid request`() {
        `when`(lobbyAuthenticationService.joinAuthenticatedLobby(any(), any()))
            .thenReturn(
                at.se2group.backend.dto.AuthenticatedLobbyResponse(
                    accessToken = "token-2",
                    lobby = lobby().copy(
                        players = listOf(
                            LobbyPlayer("user1", "Stefan", false),
                            LobbyPlayer("user2", "Max", false)
                        )
                    ).toResponse()
                )
            )

        mockMvc.post("/api/lobbies/123/join") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(JoinLobbyRequest(displayName = "Max"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("token-2") }
            jsonPath("$.lobby.players.length()") { value(2) }
        }
    }

    @Test
    fun `getLobby should return 200 for participant`() {
        `when`(lobbyService.getLobbyForUser("123", "user1")).thenReturn(lobby())

        mockMvc.get("/api/lobbies/123") {
            with(user("user1"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.lobbyId") { value("123") }
        }
    }

    @Test
    fun `getLobby should return 401 when auth is missing`() {
        mockMvc.get("/api/lobbies/123")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.errorCode") { value("UNAUTHORIZED") }
            }
    }

    @Test
    fun `deleteLobby should return 204`() {
        doNothing().`when`(lobbyService).deleteLobby(any(), any())

        mockMvc.delete("/api/lobbies/123") {
            with(user("user1"))
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `readyLobby should return 200`() {
        `when`(lobbyService.readyLobby(any(), any())).thenReturn(lobby())

        mockMvc.post("/api/lobbies/123/ready") {
            with(user("user1"))
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `unreadyLobby should return 200`() {
        `when`(lobbyService.unreadyLobby(any(), any())).thenReturn(lobby())

        mockMvc.post("/api/lobbies/123/unready") {
            with(user("user1"))
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `startLobby should return 200`() {
        `when`(lobbyService.startLobby(any(), any())).thenReturn(lobby().copy(status = LobbyStatus.IN_GAME))

        mockMvc.post("/api/lobbies/123/start") {
            with(user("user1"))
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `updateLobbySettings should return 200`() {
        `when`(lobbyService.updateLobbySettings(any(), any(), any()))
            .thenReturn(lobby())

        mockMvc.patch("/api/lobbies/123/settings") {
            with(user("user1"))
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateLobbySettingsRequest(maxPlayers = 4, isPrivate = false, allowGuests = true)
            )
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `leaveLobby should return 200 when lobby remains`() {
        `when`(lobbyService.leaveLobby(any(), any())).thenReturn(lobby())

        mockMvc.post("/api/lobbies/123/leave") {
            with(user("user1"))
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `leaveLobby should return 204 when lobby deleted`() {
        `when`(lobbyService.leaveLobby(any(), any())).thenReturn(null)

        mockMvc.post("/api/lobbies/123/leave") {
            with(user("user1"))
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `listLobbies should return 200`() {
        `when`(lobbyService.listOpenLobbies()).thenReturn(listOf(lobby()))

        mockMvc.get("/api/lobbies")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `protected lobby access should surface service errors`() {
        `when`(lobbyService.getLobbyForUser(any(), any())).thenThrow(SecurityException())

        mockMvc.get("/api/lobbies/123") {
            with(user("user1"))
        }.andExpect {
            status { isForbidden() }
        }
    }
}
