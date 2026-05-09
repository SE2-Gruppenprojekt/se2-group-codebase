package at.se2group.backend.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import shared.models.lobby.domain.LobbyPlayer

class LobbyPlayerTest {

    @Test
    fun `should create player with defaults`() {
        val player = LobbyPlayer(
            userId = "u1",
            displayName = "Stefan"
        )

        assertEquals("u1", player.userId)
        assertEquals("Stefan", player.displayName)
        assertFalse(player.isReady)
    }

    @Test
    fun `should allow custom values`() {
        val player = LobbyPlayer(
            userId = "u2",
            displayName = "Test",
            isReady = true
        )

        assertTrue(player.isReady)
    }
}
