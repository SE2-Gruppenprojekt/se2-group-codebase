package at.se2group.backend.service

import at.se2group.backend.domain.ConfirmedGame
import at.se2group.backend.domain.GamePlayer
import at.se2group.backend.domain.GameStatus
import at.se2group.backend.domain.Lobby
import at.se2group.backend.domain.LobbyPlayer
import at.se2group.backend.domain.LobbySettings
import at.se2group.backend.domain.LobbyStatus
import at.se2group.backend.domain.TurnDraft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class GameInitializationServiceTest {

    private val gameInitializationService = GameInitializationService()

    @Test
    fun `createGameFromLobby throws not implemented exception`() {
        val lobby = Lobby(
            lobbyId = "lobby-1",
            hostUserId = "host-1",
            players = listOf(
                LobbyPlayer(
                    userId = "host-1",
                    displayName = "Alice",
                    isReady = true,
                    joinedAt = Instant.parse("2026-04-25T10:00:00Z")
                )
            ),
            status = LobbyStatus.IN_GAME,
            settings = LobbySettings(),
            createdAt = Instant.parse("2026-04-25T09:00:00Z")
        )

        val exception = assertThrows(UnsupportedOperationException::class.java) {
            gameInitializationService.createGameFromLobby(lobby)
        }

        assertEquals("Game initialization is not implemented yet", exception.message)
    }

    @Test
    fun `game start result stores game and draft`() {
        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(
                GamePlayer(
                    userId = "user-1",
                    displayName = "Alice",
                    turnOrder = 0
                )
            ),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE
        )

        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1"
        )

        val result = GameStartResult(
            game = game,
            initialDraft = draft
        )

        assertSame(game, result.game)
        assertSame(draft, result.initialDraft)
    }
}
