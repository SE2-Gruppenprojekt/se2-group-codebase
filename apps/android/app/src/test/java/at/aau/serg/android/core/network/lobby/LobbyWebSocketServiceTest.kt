package at.aau.serg.android.core.network.lobby

import at.aau.serg.android.core.network.MoshiProvider
import at.aau.serg.android.core.network.WebConfig
import at.aau.serg.android.core.network.WebSocketManager
import com.squareup.moshi.Moshi
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LobbyWebSocketServiceTest {

    private lateinit var moshi: Moshi
    private lateinit var service: LobbyWebSocketService

    private lateinit var client: StompClient
    private lateinit var session: StompSession
    private lateinit var manager: WebSocketManager

    @Before
    fun setup() {

        moshi = MoshiProvider.moshi
        client = mockk()
        session = mockk()
        manager = spyk(WebSocketManager(client))
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

        val result = service.dispatch(json)

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
}
