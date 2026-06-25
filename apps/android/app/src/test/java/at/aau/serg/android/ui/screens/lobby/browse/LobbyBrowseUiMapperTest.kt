package at.aau.serg.android.ui.screens.lobby.browse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import shared.models.lobby.response.LobbyListItemResponse

class LobbyBrowseUiMapperTest {

    // --- Field Mapping ---

    @Test
    fun toUi_mapsLobbyId() {
        val response = fakeResponse(lobbyId = "123456")

        val item = response.toUiOrNull()

        assertEquals("123456", item?.lobbyId)
    }

    @Test
    fun toUi_mapsHostUserId() {
        val response = fakeResponse(hostUserId = "host-xyz")

        val item = response.toUiOrNull()

        assertEquals("host-xyz", item?.hostId)
    }

    @Test
    fun toUi_mapsCurrentPlayerCount() {
        val response = fakeResponse(currentPlayerCount = 3)

        val item = response.toUiOrNull()

        assertEquals(3, item?.currentPlayers)
    }

    @Test
    fun toUi_mapsMaxPlayers() {
        val response = fakeResponse(maxPlayers = 6)

        val item = response.toUiOrNull()

        assertEquals(6, item?.maxPlayers)
    }

    @Test
    fun toUi_setsTurnTimerSecondsTo60() {
        val item = fakeResponse().toUiOrNull()

        assertEquals(60, item?.turnTimerSeconds)
    }

    @Test
    fun toUi_setsStartingCardsTo7() {
        val item = fakeResponse().toUiOrNull()

        assertEquals(7, item?.startingCards)
    }

    // --- isOpen Logic ---

    @Test
    fun toUi_isOpen_whenStatusOpenAndNotFull() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 2, maxPlayers = 4)

        assertEquals(response.toUiOrNull()?.isOpen, true)
    }

    @Test
    fun toUi_isOpen_whenOneSlotRemaining() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 3, maxPlayers = 4)

        assertEquals(response.toUiOrNull()?.isOpen, true)
    }

    // --- Accent Color ---

    @Test
    fun toUi_accentColor_isNotNull() {
        val item = fakeResponse().toUiOrNull()

        assertNotNull(item?.accentColor)
    }

    @Test
    fun toUi_accentColor_isConsistentForSameIndex() {
        val response = fakeResponse(lobbyId = "stable-id")

        val color1 = response.toUiOrNull(3)?.accentColor
        val color2 = response.toUiOrNull(3)?.accentColor

        assertEquals(color1, color2)
    }

    @Test
    fun toUi_accentColor_alternatesBetweenTwoColors() {
        val color0 = fakeResponse().toUiOrNull(0)?.accentColor
        val color1 = fakeResponse().toUiOrNull(1)?.accentColor
        val color2 = fakeResponse().toUiOrNull(2)?.accentColor

        assertEquals(color0, color2)
        assertTrue(color0 != color1)
    }

    @Test
    fun toUi_null_whenStatusIsNotOpen() {
        val response = fakeResponse(status = "CLOSED", currentPlayerCount = 2, maxPlayers = 4)

        assertNull(response.toUiOrNull())
    }

    @Test
    fun toUi_null_whenLobbyId_invalid() {
        val response = fakeResponse(lobbyId = "test")

        assertNull(response.toUiOrNull())
    }

    @Test
    fun toUi_null_whenLobbyFull() {
        val response = fakeResponse(currentPlayerCount = 4, maxPlayers = 4)

        assertNull(response.toUiOrNull())
    }

    // --- Helpers ---

    private fun fakeResponse(
        lobbyId: String = "123456",
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
