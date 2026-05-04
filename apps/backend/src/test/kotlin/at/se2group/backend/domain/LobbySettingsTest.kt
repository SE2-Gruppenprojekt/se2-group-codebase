package at.se2group.backend.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LobbySettingsTest {

    @Test
    fun `should use default values`() {
        val settings = LobbySettings()

        assertEquals(4, settings.maxPlayers)
        assertFalse(settings.isPrivate)
        assertTrue(settings.allowGuests)
    }

    @Test
    fun `should create with custom values`() {
        val settings = LobbySettings(
            maxPlayers = 6,
            isPrivate = true,
            allowGuests = false
        )

        assertEquals(6, settings.maxPlayers)
        assertTrue(settings.isPrivate)
        assertFalse(settings.allowGuests)
    }

    @Test
    fun `should copy and update values`() {
        val original = LobbySettings()

        val updated = original.copy(
            maxPlayers = 8,
            isPrivate = true,
            allowGuests = false
        )

        assertEquals(8, updated.maxPlayers)
        assertTrue(updated.isPrivate)
        assertFalse(updated.allowGuests)
    }
}
