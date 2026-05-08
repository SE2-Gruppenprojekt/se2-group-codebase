package at.se2group.backend.game.service

import at.se2group.backend.service.GameBroadcastService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.SimpMessagingTemplate
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.GameStatus
import shared.models.game.domain.TurnDraft
import shared.models.game.event.GameDraftUpdatedEvent
import shared.models.game.event.GameEndedEvent
import shared.models.game.event.GameUpdatedEvent
import shared.models.game.event.TurnChangedEvent
import shared.models.game.event.TurnTimedOutEvent
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class GameBroadcastServiceTest {

    companion object {
        private const val TOPIC_GAMES_PATH = "/topic/games"
    }

    @Mock
    lateinit var messagingTemplate: SimpMessagingTemplate

    @Test
    fun `broadcastDraftUpdated sends draft updated event to game topic`() {
        val service = GameBroadcastService(messagingTemplate)

        val draft = TurnDraft(
            gameId = "game-1",
            playerUserId = "user-1",
            boardSets = emptyList(),
            rackTiles = emptyList(),
            version = 3
        )

        val payloadCaptor = ArgumentCaptor.forClass(GameDraftUpdatedEvent::class.java)

        service.broadcastDraftUpdated(draft)

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_GAMES_PATH/game-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value

        assertEquals("game.draft.updated", event.type)
        assertEquals("game-1", event.gameId)
        assertEquals("user-1", event.playerId)
        assertEquals("game-1", event.draft.gameId)
        assertEquals("user-1", event.draft.playerUserId)
        assertEquals(3, event.draft.version)
        assertEquals(0, event.draft.draftBoard.size)
        assertEquals(0, event.draft.draftHand.size)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastGameUpdated sends game updated event to game topic`() {
        val service = GameBroadcastService(messagingTemplate)

        val createdAt = Instant.parse("2026-05-08T12:00:00Z")
        val player = GamePlayer(
            userId = "user-1",
            displayName = "Alice",
            turnOrder = 0,
            rackTiles = emptyList(),
            hasCompletedInitialMeld = false,
            score = 0,
            joinedAt = createdAt
        )
        val game = ConfirmedGame(
            gameId = "game-1",
            lobbyId = "lobby-1",
            players = listOf(player),
            boardSets = emptyList(),
            drawPile = emptyList(),
            currentPlayerUserId = "user-1",
            status = GameStatus.ACTIVE,
            createdAt = createdAt,
            startedAt = null,
            finishedAt = null
        )

        val payloadCaptor = ArgumentCaptor.forClass(GameUpdatedEvent::class.java)

        service.broadcastGameUpdated(game)

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_GAMES_PATH/game-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value

        assertEquals("game.updated", event.type)
        assertEquals("game-1", event.gameId)
        assertEquals("game-1", event.game.gameId)
        assertEquals("lobby-1", event.game.lobbyId)
        assertEquals("user-1", event.game.currentPlayerUserId)
        assertEquals("user-1", event.game.currentTurnPlayerId)
        assertEquals("ACTIVE", event.game.status)
        assertEquals(1, event.game.players.size)
        assertEquals("user-1", event.game.players.first().userId)
        assertEquals("Alice", event.game.players.first().displayName)
        assertEquals(0, event.game.board.size)
        assertEquals(0, event.game.drawPile.size)
        assertEquals(0, event.game.drawPileCount)
        assertEquals(createdAt, event.game.createdAt)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastTurnChanged sends turn changed event to game topic`() {
        val service = GameBroadcastService(messagingTemplate)

        val payloadCaptor = ArgumentCaptor.forClass(TurnChangedEvent::class.java)

        service.broadcastTurnChanged(
            gameId = "game-1",
            currentTurnPlayerId = "user-2"
        )

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_GAMES_PATH/game-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value

        assertEquals("turn.changed", event.type)
        assertEquals("game-1", event.gameId)
        assertEquals("user-2", event.currentTurnPlayerId)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastTurnTimedOut sends turn timed out event to game topic`() {
        val service = GameBroadcastService(messagingTemplate)

        val payloadCaptor = ArgumentCaptor.forClass(TurnTimedOutEvent::class.java)

        service.broadcastTurnTimedOut(
            gameId = "game-1",
            previousTurnPlayerId = "user-1"
        )

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_GAMES_PATH/game-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value

        assertEquals("turn.timed_out", event.type)
        assertEquals("game-1", event.gameId)
        assertEquals("user-1", event.previousTurnPlayerId)

        verifyNoMoreInteractions(messagingTemplate)
    }

    @Test
    fun `broadcastGameEnded sends game ended event to game topic`() {
        val service = GameBroadcastService(messagingTemplate)

        val payloadCaptor = ArgumentCaptor.forClass(GameEndedEvent::class.java)

        service.broadcastGameEnded(
            gameId = "game-1",
            winnerUserId = "user-1"
        )

        verify(messagingTemplate).convertAndSend(
            org.mockito.Mockito.eq("$TOPIC_GAMES_PATH/game-1"),
            payloadCaptor.capture()
        )

        val event = payloadCaptor.value

        assertEquals("game.ended", event.type)
        assertEquals("game-1", event.gameId)
        assertEquals("user-1", event.winnerUserId)

        verifyNoMoreInteractions(messagingTemplate)
    }
}
