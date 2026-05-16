package at.aau.serg.android.core.network.lobby

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
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import shared.models.lobby.event.LobbyEvent

class LobbyWebSocketServiceTest {
    private lateinit var moshi: Moshi
    private lateinit var service: LobbyWebSocketService
    private val fakeSession: FakeStompSessionWrapper = FakeStompSessionWrapper()
    private lateinit var manager: WebSocketManager

    @Before
    fun setup() {
        moshi = MoshiProvider.moshi
        manager = spyk(WebSocketManager(FakeClientProvider(fakeSession)))
        service = LobbyWebSocketService(moshi, manager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun subscribe_emits_lobby_updated_event() = runBlocking {
        val json = """
        {
          "type": "lobby.updated",
          "lobby": {
            "lobbyId": "123",
            "hostUserId": "HOST1",
            "status": "OPEN",
            "players": [],
            "maxPlayers": 4,
            "isPrivate": false,
            "allowGuests": true
          }
        }
        """.trimIndent()

        coEvery {
            manager.subscribe(WebConfig.Topics.lobby("123"))
        } returns flowOf(json)

        val result = service.subscribe("123").toList()

        assertEquals(1, result.size)
        assertTrue(result.first() is LobbyEvent.Updated)
    }

    @Test(expected = com.squareup.moshi.JsonDataException::class)
    fun dispatch_throws_exception_when_payload_is_malformed() {
        val json = """{"type":"lobby.updated", "lobby": {}}"""
        service.parseLobbyEvent(json)
    }

    @Test
    fun subscribe_emits_lobby_deleted_event() = runBlocking {
        val json = """{"type":"lobby.deleted","lobbyId":"123"}"""
        coEvery {
            manager.subscribe(any())
        } returns flowOf(json)
        val result = service.subscribe("123").toList()

        assertTrue(result.first() is LobbyEvent.Deleted)
    }

    @Test
    fun dispatch_emits_started_event() {
        val json = """{ "type": "lobby.started", "lobbyId": "123", "matchId": "MATCH42" }"""
        val result = service.parseLobbyEvent(json)

        assertTrue(result is LobbyEvent.Started)
    }

    @Test
    fun subscribe_filters_unknown_event() = runBlocking {
        val json = """{ "type": "unknown.event" }"""
        coEvery {
            manager.subscribe(any())
        } returns flowOf(json)
        val result = service.subscribe("123").toList()

        assertTrue(result.isEmpty())
    }

    @Test
    fun parseLobbyEvent_returns_null_when_type_is_unknown() {
        val json = """{"type": "unknown.event"}"""
        val result = service.parseLobbyEvent(json)
        assertNull(result)
    }

    @Test
    fun parseLobbyEvent_returns_null_when_json_is_null_literal() {
        val result = service.parseLobbyEvent("null")
        assertNull(result)
    }

    @Test
    fun parseLobbyEvent_returns_null_when_type_is_missing() {
        val json = """{"lobbyId":"123"}"""
        val result = service.parseLobbyEvent(json)
        assertNull(result)
    }

    @Test
    fun parseLobbyEvent_returns_null_when_type_is_not_a_string() {
        val json = """{"type": 123}"""
        val result = service.parseLobbyEvent(json)
        assertNull(result)
    }
}
