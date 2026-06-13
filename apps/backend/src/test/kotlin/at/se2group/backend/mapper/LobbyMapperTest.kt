package at.se2group.backend.mapper

import at.se2group.backend.persistence.LobbyEntity
import at.se2group.backend.persistence.LobbyPlayerEmbeddable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer
import shared.models.lobby.domain.LobbySettings
import shared.models.lobby.domain.LobbyStatus
import java.time.Instant

class LobbyMapperTest {

    @Test
    fun `toDomain initializes currentGameId as null`() {
        val entity = LobbyEntity(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            status = LobbyStatus.OPEN,
            maxPlayers = 4,
            isPrivate = false,
            allowGuests = true,
            players = mutableListOf(
                LobbyPlayerEmbeddable(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = false,
                    joinedAt = Instant.now()
                )
            )
        )

        val result = entity.toDomain()

        assertNull(result.currentGameId)
    }

    @Test
    fun `toResponse propagates explicit currentGameId`() {
        val lobby = Lobby(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            players = listOf(LobbyPlayer("host-1", "Alice", true)),
            status = LobbyStatus.IN_GAME,
            settings = LobbySettings(4, false, true),
            currentGameId = "game-123"
        )

        val response = lobby.toResponse()

        assertEquals("game-123", response.currentGameId)
    }

    @Test
    fun `toResponse uses override currentGameId when provided`() {
        val lobby = Lobby(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            players = listOf(LobbyPlayer("host-1", "Alice", true)),
            status = LobbyStatus.IN_GAME,
            settings = LobbySettings(4, false, true),
            currentGameId = null
        )

        val response = lobby.toResponse(currentGameId = "game-456")

        assertEquals("game-456", response.currentGameId)
    }
}
