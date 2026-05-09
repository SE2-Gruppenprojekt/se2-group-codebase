package at.se2group.backend.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import shared.models.game.event.GameDraftUpdatedEvent
import shared.models.game.event.GameEndedEvent
import shared.models.game.event.GameUpdatedEvent
import shared.models.game.event.TurnChangedEvent
import shared.models.game.event.TurnTimedOutEvent
import shared.models.game.response.GameResponse
import shared.models.game.response.TurnDraftResponse
import java.time.Instant

class GameEventDtoTest {

    @Test
    fun `creates game draft updated event`() {
        val event = GameDraftUpdatedEvent(
            gameId = "game-1",
            playerId = "player-1",
            draft = TurnDraftResponse(
                gameId = "game-1",
                playerUserId = "player-1",
                draftBoard = emptyList(),
                draftHand = emptyList(),
                version = 2
            )
        )

        assertEquals(GameDraftUpdatedEvent.TYPE, event.type)
        assertEquals("game-1", event.gameId)
        assertEquals("player-1", event.playerId)
        assertEquals(2, event.draft.version)
    }

    @Test
    fun `creates game updated event`() {
        val event = GameUpdatedEvent(
            gameId = "game-1",
            game = GameResponse(
                gameId = "game-1",
                lobbyId = "lobby-1",
                players = emptyList(),
                board = emptyList(),
                drawPile = emptyList(),
                drawPileCount = 42,
                currentPlayerUserId = "player-1",
                currentTurnPlayerId = "player-1",
                turnDeadline = null,
                remainingTurnSeconds = null,
                status = "ACTIVE",
                createdAt = Instant.parse("2026-05-08T10:00:00Z"),
                startedAt = null,
                finishedAt = null
            )
        )

        assertEquals(GameUpdatedEvent.TYPE, event.type)
        assertEquals("game-1", event.gameId)
        assertEquals(42, event.game.drawPileCount)
    }

    @Test
    fun `creates turn changed event`() {
        val event = TurnChangedEvent(
            gameId = "game-1",
            currentTurnPlayerId = "player-2"
        )

        assertEquals(TurnChangedEvent.TYPE, event.type)
        assertEquals("player-2", event.currentTurnPlayerId)
    }

    @Test
    fun `creates turn timed out event`() {
        val event = TurnTimedOutEvent(
            gameId = "game-1",
            previousTurnPlayerId = "player-1"
        )

        assertEquals(TurnTimedOutEvent.TYPE, event.type)
        assertEquals("player-1", event.previousTurnPlayerId)
    }

    @Test
    fun `creates game ended event`() {
        val event = GameEndedEvent(
            gameId = "game-1",
            winnerUserId = "player-3"
        )

        assertEquals(GameEndedEvent.TYPE, event.type)
        assertEquals("player-3", event.winnerUserId)
    }
}
