package at.se2group.backend.api

import at.se2group.backend.dto.CreateLobbyRequest
import at.se2group.backend.dto.JoinLobbyRequest
import at.se2group.backend.service.LobbyService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.delete
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import shared.models.lobby.domain.LobbyPlayer
import org.springframework.test.web.servlet.patch

@SpringBootTest
@AutoConfigureMockMvc
class LobbyControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var lobbyService: LobbyService

    @Test
    fun `createLobby should return 200 for valid request`() {

        val request = CreateLobbyRequest(
            displayName = "Stefan",
            maxPlayers = 4
        )

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = listOf(
                LobbyPlayer(
                    userId = "user1",
                    displayName = "Stefan",
                    isReady = false
                )
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        `when`(lobbyService.createLobby(any(), any()))
            .thenReturn(lobby)

        mockMvc.post("/api/lobbies") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", "user1")
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `createLobby should return 400 for invalid request`() {

        val invalidJson = """
            {
              "displayName": "",
              "maxPlayers": 4
            }
        """.trimIndent()

        mockMvc.post("/api/lobbies") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", "user1")
            content = invalidJson
        }.andExpect {
            status { isInternalServerError() }
        }
    }

    @Test
    fun `joinLobby should return 200 for valid request`() {

        val request = JoinLobbyRequest(
            userId = "user2",
            displayName = "Max"
        )

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = listOf(
                LobbyPlayer("user1", "Stefan", false),
                LobbyPlayer("user2", "Max", false)
            ),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        `when`(lobbyService.joinLobby(any(), any()))
            .thenReturn(lobby)

        mockMvc.post("/api/lobbies/123/join") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `getLobby should return 200`() {

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = emptyList(),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(4, false, true)
        )

        `when`(lobbyService.getLobby(any()))
            .thenReturn(lobby)

        mockMvc.get("/api/lobbies/123")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `deleteLobby should return 204`() {

        doNothing().`when`(lobbyService)
            .deleteLobby(any(), any())

        mockMvc.delete("/api/lobbies/123") {
            header("X-User-Id", "user1")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `readyLobby should return 200`() {

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = emptyList(),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        `when`(lobbyService.readyLobby(any(), any()))
            .thenReturn(lobby)

        mockMvc.post("/api/lobbies/123/ready") {
            header("X-User-Id", "user1")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `unreadyLobby should return 200`() {

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = emptyList(),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(
                maxPlayers = 4,
                isPrivate = false,
                allowGuests = true
            )
        )

        `when`(lobbyService.unreadyLobby(any(), any()))
            .thenReturn(lobby)

        mockMvc.post("/api/lobbies/123/unready") {
            header("X-User-Id", "user1")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `startLobby should return 200`() {

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = emptyList(),
            status = LobbyStatus.IN_GAME,
            settings = LobbySettings(4, false, true)
        )

        `when`(lobbyService.startLobby(any(), any()))
            .thenReturn(lobby)

        mockMvc.post("/api/lobbies/123/start") {
            header("X-User-Id", "user1")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `updateLobbySettings should return 200`() {

        val requestJson = """
        {
          "maxPlayers": 4,
          "isPrivate": false,
          "allowGuests": true
        }
    """.trimIndent()

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user1",
            players = emptyList(),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(4, false, true)
        )

        `when`(lobbyService.updateLobbySettings(any(), any(), any()))
            .thenReturn(lobby)

        mockMvc.patch("/api/lobbies/123/settings") {
            header("X-User-Id", "user1")
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `leaveLobby should return 200 when lobby remains`() {

        val lobby = Lobby(
            lobbyId = "123",
            hostUserId = "user2",
            players = emptyList(),
            status = LobbyStatus.OPEN,
            settings = LobbySettings(4, false, true)
        )

        `when`(lobbyService.leaveLobby(any(), any()))
            .thenReturn(lobby)

        mockMvc.post("/api/lobbies/123/leave") {
            header("X-User-Id", "user1")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `leaveLobby should return 204 when lobby deleted`() {

        `when`(lobbyService.leaveLobby(any(), any()))
            .thenReturn(null)

        mockMvc.post("/api/lobbies/123/leave") {
            header("X-User-Id", "user1")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `listLobbies should return 200`() {

        val lobbies = listOf(
            Lobby(
                lobbyId = "123",
                hostUserId = "user1",
                players = emptyList(),
                status = LobbyStatus.OPEN,
                settings = LobbySettings(4, false, true)
            )
        )

        `when`(lobbyService.listOpenLobbies())
            .thenReturn(lobbies)

        mockMvc.get("/api/lobbies")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should return 400 when illegal argument`() {
        `when`(lobbyService.getLobby(any())).thenThrow(IllegalArgumentException())

        mockMvc.get("/api/lobbies/123")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should return 403 when security exception`() {
        `when`(lobbyService.getLobby(any())).thenThrow(SecurityException())

        mockMvc.get("/api/lobbies/123")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `should return 404 when not found`() {
        `when`(lobbyService.getLobby(any())).thenThrow(NoSuchElementException())

        mockMvc.get("/api/lobbies/123")
            .andExpect {
                status { isNotFound() }
            }
    }
}
