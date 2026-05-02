package at.se2group.backend.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

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
        assertNotNull(player.joinedAt)
    }

    @Test
    fun `should allow custom values`() {
        val time = Instant.now()

        val player = LobbyPlayer(
            userId = "u2",
            displayName = "Test",
            isReady = true,
            joinedAt = time
        )

        assertTrue(player.isReady)
        assertEquals(time, player.joinedAt)
    }
}
