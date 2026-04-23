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

        val item = response.toUi()

        assertEquals("lobby-abc", item.lobbyId)
    }

    @Test
    fun toUi_mapsHostUserId() {
        val response = fakeResponse(hostUserId = "host-xyz")

        val item = response.toUi()

        assertEquals("host-xyz", item.hostId)
    }

    @Test
    fun toUi_mapsCurrentPlayerCount() {
        val response = fakeResponse(currentPlayerCount = 3)

        val item = response.toUi()

        assertEquals(3, item.currentPlayers)
    }

    @Test
    fun toUi_mapsMaxPlayers() {
        val response = fakeResponse(maxPlayers = 6)

        val item = response.toUi()

        assertEquals(6, item.maxPlayers)
    }

    @Test
    fun toUi_setsTurnTimerSecondsTo60() {
        val item = fakeResponse().toUi()

        assertEquals(60, item.turnTimerSeconds)
    }

    @Test
    fun toUi_setsStartingCardsTo7() {
        val item = fakeResponse().toUi()

        assertEquals(7, item.startingCards)
    }

    // --- isOpen Logic ---

    @Test
    fun toUi_isOpen_whenStatusOpenAndNotFull() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 2, maxPlayers = 4)

        assertTrue(response.toUi().isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenStatusIsNotOpen() {
        val response = fakeResponse(status = "CLOSED", currentPlayerCount = 2, maxPlayers = 4)

        assertFalse(response.toUi().isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenStatusIsStarted() {
        val response = fakeResponse(status = "STARTED", currentPlayerCount = 2, maxPlayers = 4)

        assertFalse(response.toUi().isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenLobbyIsFull() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 4, maxPlayers = 4)

        assertFalse(response.toUi().isOpen)
    }

    @Test
    fun toUi_isNotOpen_whenFullAndStatusNotOpen() {
        val response = fakeResponse(status = "CLOSED", currentPlayerCount = 4, maxPlayers = 4)

        assertFalse(response.toUi().isOpen)
    }

    @Test
    fun toUi_isOpen_whenOneSlotRemaining() {
        val response = fakeResponse(status = "OPEN", currentPlayerCount = 3, maxPlayers = 4)

        assertTrue(response.toUi().isOpen)
    }

    // --- Accent Color ---

    @Test
    fun toUi_accentColor_isNotNull() {
        val item = fakeResponse(lobbyId = "some-id").toUi()

        assertNotNull(item.accentColor)
    }

    @Test
    fun toUi_accentColor_isConsistentForSameLobbyId() {
        val response = fakeResponse(lobbyId = "stable-id")

        val color1 = response.toUi().accentColor
        val color2 = response.toUi().accentColor

        assertEquals(color1, color2)
    }

    @Test
    fun toUi_accentColor_fromPaletteRange() {
        // The palette has 7 colors — verify the color is always one of them
        val paletteColors = listOf(
            0xFF3B82F6, 0xFFA855F7, 0xFF22C55E, 0xFFF97316,
            0xFFEC4899, 0xFF06B6D4, 0xFFEAB308
        ).map { androidx.compose.ui.graphics.Color(it) }

        repeat(20) { i ->
            val color = fakeResponse(lobbyId = "lobby-$i").toUi().accentColor
            assertTrue("Color $color not in palette", paletteColors.contains(color))
        }
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
