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
import shared.models.game.domain.TurnDraft
import shared.models.game.event.GameDraftUpdatedEvent

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
}
