package at.aau.serg.android.core.network.game

import at.aau.serg.android.core.network.MoshiProvider
import at.aau.serg.android.core.network.WebConfig
import at.aau.serg.android.core.network.WebSocketManager
import at.aau.serg.android.core.network.socket.FakeClientProvider
import at.aau.serg.android.core.network.socket.FakeStompSessionWrapper
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import shared.models.game.event.GameEvent

class GameWebSocketServiceTest {

    private lateinit var moshi: Moshi
    private lateinit var service: GameWebSocketService
    private val fakeSession: FakeStompSessionWrapper = FakeStompSessionWrapper()
    private lateinit var manager: WebSocketManager

    @Before
    fun setup() {
        moshi = MoshiProvider.moshi
        manager = spyk(WebSocketManager(FakeClientProvider(fakeSession)))
        service = GameWebSocketService(moshi, manager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun dispatch_returns_draftUpdated_event() {
        val json = """
            {
              "type": "game.draft.updated",
              "gameId": "g1",
              "playerId": "p1",
              "draft": {
                "gameId": "g1",
                "playerUserId": "p1",
                "draftBoard": [],
                "draftHand": [],
                "version": 0
              }
            }
        """.trimIndent()

        val result = service.dispatch(json)

        assertTrue(result is GameEvent.DraftUpdated)
        assertEquals("p1", (result as GameEvent.DraftUpdated).payload.playerId)
    }

    @Test
    fun dispatch_returns_gameEnded_event() {
        val json = """{"type":"game.ended","gameId":"g1","winnerUserId":"winner1"}"""

        val result = service.dispatch(json)

        assertTrue(result is GameEvent.Ended)
        assertEquals("winner1", (result as GameEvent.Ended).payload.winnerUserId)
    }

    @Test
    fun dispatch_returns_gameUpdated_event() {
        val json = """
            {
              "type": "game.updated",
              "gameId": "g1",
              "game": {
                "gameId": "g1",
                "lobbyId": "lobby1",
                "players": [],
                "board": [],
                "drawPile": [],
                "drawPileCount": 0,
                "currentPlayerUserId": "p1",
                "currentTurnPlayerId": "p1",
                "turnDeadline": null,
                "remainingTurnSeconds": 60,
                "status": "ACTIVE",
                "createdAt": "2026-01-01T00:00:00Z",
                "startedAt": null,
                "finishedAt": null
              }
            }
        """.trimIndent()

        val result = service.dispatch(json)

        assertTrue(result is GameEvent.Updated)
        assertEquals("g1", (result as GameEvent.Updated).payload.gameId)
    }

    @Test
    fun dispatch_returns_turnChanged_event() {
        val json = """{"type":"turn.changed","gameId":"g1","currentTurnPlayerId":"player2"}"""

        val result = service.dispatch(json)

        assertTrue(result is GameEvent.TurnChanged)
        assertEquals("player2", (result as GameEvent.TurnChanged).payload.currentTurnPlayerId)
    }

    @Test
    fun dispatch_returns_turnTimedOut_event() {
        val json = """{"type":"turn.timed_out","gameId":"g1","previousTurnPlayerId":"player1"}"""

        val result = service.dispatch(json)

        assertTrue(result is GameEvent.TurnTimedOut)
        assertEquals("player1", (result as GameEvent.TurnTimedOut).payload.previousTurnPlayerId)
    }

    @Test
    fun dispatch_returns_null_for_unknown_type() {
        val json = """{"type":"unknown.event"}"""

        val result = service.dispatch(json)

        assertNull(result)
    }

    @Test
    fun dispatch_returns_null_for_missing_type() {
        val json = """{"gameId":"g1"}"""

        val result = service.dispatch(json)

        assertNull(result)
    }

    @Test
    fun subscribe_emits_turnChanged_event() = runBlocking {
        val json = """{"type":"turn.changed","gameId":"g1","currentTurnPlayerId":"player2"}"""

        coEvery { manager.subscribe(WebConfig.Topics.match("g1")) } returns flowOf(json)

        val result = service.subscribe("g1").toList()

        assertEquals(1, result.size)
        assertTrue(result.first() is GameEvent.TurnChanged)
    }

    @Test
    fun subscribe_emits_turnTimedOut_event() = runBlocking {
        val json = """{"type":"turn.timed_out","gameId":"g1","previousTurnPlayerId":"player1"}"""

        coEvery { manager.subscribe(any()) } returns flowOf(json)

        val result = service.subscribe("g1").toList()

        assertTrue(result.first() is GameEvent.TurnTimedOut)
    }

    @Test
    fun subscribe_filters_unknown_events() = runBlocking {
        val json = """{"type":"unknown.event"}"""

        coEvery { manager.subscribe(any()) } returns flowOf(json)

        val result = service.subscribe("g1").toList()

        assertTrue(result.isEmpty())
    }

    @Test
    fun subscribe_uses_correct_topic() = runBlocking {
        coEvery { manager.subscribe(WebConfig.Topics.match("match42")) } returns flowOf()

        service.subscribe("match42").toList()

        io.mockk.coVerify { manager.subscribe(WebConfig.Topics.match("match42")) }
    }

    @Test
    fun default_constructor_isCovered() {
        val s = GameWebSocketService(moshi)
        assertNotNull(s)
    }

    @Test
    fun dispatch_returns_null_for_malformed_json() {
        val result = service.dispatch("not valid json {{{")
        assertNull(result)
    }

    @Test
    fun dispatch_returns_null_when_message_is_json_null() {
        val result = service.dispatch("null")
        assertNull(result)
    }

    @Test
    fun dispatch_returns_null_when_type_is_not_a_string() {
        val result = service.dispatch("""{"type": 123}""")
        assertNull(result)
    }
}
