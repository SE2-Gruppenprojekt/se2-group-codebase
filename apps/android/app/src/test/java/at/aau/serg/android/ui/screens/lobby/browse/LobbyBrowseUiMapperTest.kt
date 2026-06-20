package at.aau.serg.android.ui.screens.lobby.browse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import shared.models.lobby.response.LobbyListItemResponse

class LobbyBrowseUiMapperTest {

    // --- Field Mapping ---

    @Test
    fun toUi_mapsLobbyId() {
        val response = fakeResponse(lobbyId = "lobby-abc")

        val item = response.toUi(0)

        assertEquals("lobby-abc", item.lobbyId)
    }

    @Test
    fun toUi_mapsHostUserId() {
        val response = fakeResponse(hostUserId = "host-xyz")

        val item = response.toUi(0)

        assertEquals("host-xyz", item.hostId)
    }

    @Test
    fun toUi_mapsCurrentPlayerCount() {
        val response = fakeResponse(currentPlayerCount = 3)

        val item = response.toUi(0)

        assertEquals(3, item.currentPlayers)
    }

    @Test
    fun toUi_mapsMaxPlayers() {
        val response = fakeResponse(maxPlayers = 6)

        val item = response.toUi(0)

        assertEquals(6, item.maxPlayers)
    }

    @Test
    fun toUi_setsTurnTimerSecondsTo60() {
        val item = fakeResponse().toUi(0)

        assertEquals(60, item.turnTimerSeconds)
    }

    @Test
    fun toUi_setsStartingCardsTo7() {
        val item = fakeResponse().toUi(0)

        assertEquals(7, item.startingCards)
    }

    // --- isOpen Logic ---

    @Test
    fun toUi_isOpen_whenStatusOpenAndNotFull() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 2, maxPlayers = 4)

        assertTrue(response.toUi(0).isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenStatusIsNotOpen() {
        val response = fakeResponse(status = "CLOSED", currentPlayerCount = 2, maxPlayers = 4)

        assertFalse(response.toUi(0).isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenStatusIsStarted() {
        val response = fakeResponse(status = "STARTED", currentPlayerCount = 2, maxPlayers = 4)

        assertFalse(response.toUi(0).isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenLobbyIsFull() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 4, maxPlayers = 4)

        assertFalse(response.toUi(0).isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenFullAndStatusNotOpen() {
        val response = fakeResponse(status = "CLOSED", currentPlayerCount = 4, maxPlayers = 4)

        assertFalse(response.toUi(0).isOpen)
    }

    @Test
    fun toUi_isOpen_whenOneSlotRemaining() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 3, maxPlayers = 4)

        assertTrue(response.toUi(0).isOpen)
    }

    // --- Accent Color ---

    @Test
    fun toUi_accentColor_isNotNull() {
        val item = fakeResponse(lobbyId = "some-id").toUi(0)

        assertNotNull(item.accentColor)
    }

    @Test
    fun toUi_accentColor_isConsistentForSameIndex() {
        val response = fakeResponse(lobbyId = "stable-id")

        val color1 = response.toUi(3).accentColor
        val color2 = response.toUi(3).accentColor

        assertEquals(color1, color2)
    }

    @Test
    fun toUi_accentColor_alternatesBetweenTwoColors() {
        val color0 = fakeResponse().toUi(0).accentColor
        val color1 = fakeResponse().toUi(1).accentColor
        val color2 = fakeResponse().toUi(2).accentColor

        assertEquals(color0, color2)
        assertTrue(color0 != color1)
    }

    // --- Helpers ---

    private fun fakeResponse(
        lobbyId: String = "lobby-1",
        hostUserId: String = "host-1",
        status: String = "OPEN",
        currentPlayerCount: Int = 1,
        maxPlayers: Int = 4,
        isPrivate: Boolean = false
    ) = LobbyListItemResponse(
        lobbyId = lobbyId,
        hostUserId = hostUserId,
        status = status,
        currentPlayerCount = currentPlayerCount,
        maxPlayers = maxPlayers,
        isPrivate = isPrivate
    )
}
