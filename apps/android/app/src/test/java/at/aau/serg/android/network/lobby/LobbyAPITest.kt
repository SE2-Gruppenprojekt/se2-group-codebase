package at.aau.serg.android.network.lobby

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.Response
import shared.models.lobby.request.CreateLobbyRequest
import shared.models.lobby.request.JoinLobbyRequest
import shared.models.lobby.response.LobbyListItemResponse
import shared.models.lobby.response.LobbyResponse

class LobbyAPITest {
    private val service = mockk<LobbyService>()
    private val api = LobbyAPI(service)

    @Test
    fun getLobbies_returnsListFromService() = runBlocking {
        val expected = listOf(
            LobbyListItemResponse(
                lobbyId = "1",
                hostUserId = "hostA",
                status = "OPEN",
                currentPlayerCount = 1,
                maxPlayers = 4,
                isPrivate = false
            ),
            LobbyListItemResponse(
                lobbyId = "2",
                hostUserId = "hostB",
                status = "IN_PROGRESS",
                currentPlayerCount = 3,
                maxPlayers = 6,
                isPrivate = true
            )
        )
        coEvery { service.getLobbies() } returns expected
        val result = api.getLobbies()

        assertEquals(expected, result)
    }

    @Test
    fun getLobby_returnsLobbyFromService() = runBlocking {
        val expected = LobbyResponse(
            lobbyId = "1",
            hostUserId = "hostA",
            status = "OPEN",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery { service.getLobby("1") } returns expected
        val result = api.getLobby("1")

        assertEquals(expected, result)
    }

    @Test
    fun createLobby_returnsResponseFromService() = runBlocking {
        val request = CreateLobbyRequest(
            displayName = "My Lobby",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        val expected = LobbyResponse(
            lobbyId = "1",
            hostUserId = "hostA",
            status = "OPEN",
            players = emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )
        coEvery { service.createLobby("user123", request) } returns expected
        val result = api.createLobby("user123", request)

        assertEquals(expected, result)
    }

    @Test
    fun joinLobby_returnsResponseFromService() = runBlocking {
        val request = JoinLobbyRequest("user123", "User")
        val expected = LobbyResponse(
            lobbyId = "1",
            hostUserId ="user123",
            status="OPEN",
            emptyList(),
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true)
        coEvery { service.joinLobby("1", request) } returns expected
        val result = api.joinLobby("1", request)

        assertEquals(expected, result)
    }

    @Test
    fun leaveLobby_returnsTrueWhenSuccessful() = runBlocking {
        coEvery { service.leaveLobby("user123", "1") } returns Response.success(Unit)
        val result = api.leaveLobby("user123", "1")

        assertTrue(result)
    }

    @Test
    fun leaveLobby_returnsFalseWhenNotSuccessful() = runBlocking {
        coEvery { service.leaveLobby("user123", "1") } returns Response.error(400, okhttp3.ResponseBody.create(null, ""))
        val result = api.leaveLobby("user123", "1")

        assertFalse(result)
    }
}
