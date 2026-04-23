package at.aau.serg.android.core.network.mapper

import junit.framework.TestCase.assertEquals
import org.junit.Test
import shared.models.lobby.domain.LobbyStatus
import shared.models.lobby.response.LobbyPlayerResponse
import shared.models.lobby.response.LobbyResponse

class LobbyNetworkMapperTest {
    @Test
    fun map_returnsLobbyPlayer_forLobbyPlayerResponse() {
        val response = LobbyPlayerResponse(
            userId = "u1",
            displayName = "Alice",
            isReady = true
        )

        val result = response.toDomain()

        assertEquals("u1", result.userId)
        assertEquals("Alice", result.displayName)
        assertEquals(true, result.isReady)
    }

    @Test
    fun map_returnsLobby_forLobbyResponse() {
        val response = LobbyResponse(
            lobbyId = "L123",
            hostUserId = "host1",
            players = listOf(
                LobbyPlayerResponse("u1", "Alice", true),
                LobbyPlayerResponse("u2", "Bob", false)
            ),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true
        )

        val result = response.toDomain()

        assertEquals("L123", result.lobbyId)
        assertEquals("host1", result.hostUserId)
        assertEquals(2, result.players.size)
        assertEquals(LobbyStatus.OPEN, result.status)
    }

    @Test
    fun map_mapsPlayersCorrectly_forLobbyResponse() {
        val response = LobbyResponse(
            lobbyId = "L1",
            hostUserId = "host",
            players = listOf(
                LobbyPlayerResponse("u1", "Alice", true)
            ),
            status = "OPEN",
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = false
        )

        val result = response.toDomain()
        val player = result.players.first()

        assertEquals("u1", player.userId)
        assertEquals("Alice", player.displayName)
        assertEquals(true, player.isReady)
    }

    @Test
    fun map_returnsLobbySettings_forLobbyResponse() {
        val response = LobbyResponse(
            lobbyId = "L1",
            hostUserId = "host",
            players = emptyList(),
            status = "OPEN",
            maxPlayers = 6,
            isPrivate = true,
            allowGuests = false
        )

        val result = response.toDomain()
        val settings = result.settings

        assertEquals(6, settings.maxPlayers)
        assertEquals(true, settings.isPrivate)
        assertEquals(false, settings.allowGuests)
    }
}
