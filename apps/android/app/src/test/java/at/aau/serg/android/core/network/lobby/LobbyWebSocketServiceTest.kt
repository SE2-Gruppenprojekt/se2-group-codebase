package at.aau.serg.android.core.network.lobby

import android.util.Log
import at.aau.serg.android.core.network.lobby.LobbyDeletedPayload
import at.aau.serg.android.core.network.lobby.LobbyStartedPayload
import at.aau.serg.android.core.network.lobby.LobbyUpdatedPayload
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LobbyWebSocketServiceTest {

    private lateinit var client: StompClient
    private lateinit var session: StompSession
    private lateinit var service: LobbyWebSocketService

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any()) } returns 0

        client = mockk()
        session = mockk()

        service = LobbyWebSocketService(client)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }


    // --- CONNECT ---
    @Test
    fun connect_storesSessionAndCollectsMessages() = runBlocking {
        mockkStatic("org.hildan.krossbow.stomp.StompSessionKt")
        val message = """
        {
          "type": "lobby.deleted",
          "lobbyId": "123"
        }
        """.trimIndent()
        coEvery { client.connect(any()) } returns session
        coEvery { session.subscribeText(any()) } returns flowOf(message)
        var called = false
        val job = launch {
            service.connect(
                lobbyId = "123",
                onLobbyUpdated = {},
                onLobbyDeleted = { called = true },
                onLobbyStarted = {}
            )
        }
        delay(10)
        job.cancel()

        assertTrue(called)
    }

    @Test
    fun connect_logsErrorWhenFlowThrows() = runBlocking {
        mockkStatic("org.hildan.krossbow.stomp.StompSessionKt")
        val exception = RuntimeException("unexpected")
        coEvery { client.connect(any()) } returns session
        coEvery { session.subscribeText(any()) } returns flow {
            throw exception
        }
        every { Log.e("LobbyWebSocket", any<String>(), exception) } returns 0
        val job = launch {
            service.connect(
                lobbyId = "123",
                onLobbyUpdated = {},
                onLobbyDeleted = {},
                onLobbyStarted = {}
            )
        }
        delay(10)
        job.cancel()

        verify { Log.e("LobbyWebSocket", "Error receiving message", exception) }
    }


    // --- DISCONNECT ---
    @Test
    fun disconnect_callsSessionDisconnect() = runBlocking {
        coEvery { session.disconnect() } just Runs
        val sessionField = service.javaClass.getDeclaredField("session")
        sessionField.isAccessible = true
        sessionField.set(service, session)
        service.disconnect()

        coVerify { session.disconnect() }
    }

    @Test
    fun disconnect_logsErrorWhenExceptionThrown() = runBlocking {
        val exception = RuntimeException("disconnect failed")
        every { Log.e(any(), any(), exception) } returns 0
        coEvery { session.disconnect() } throws exception
        val sessionField = service.javaClass.getDeclaredField("session")
        sessionField.isAccessible = true
        sessionField.set(service, session)
        service.disconnect()

        coVerify { session.disconnect() }
        verify { Log.e(any(), any(), exception) }
    }

    // --- DISPATCHING ---
    @Test
    fun dispatch_callsOnLobbyUpdated() {
        var called = false
        lateinit var payload: LobbyUpdatedPayload

        val json = """
        {
          "type": "lobby.updated",
          "lobby": {
            "lobbyId": "123",
            "hostUserId": "HOST1",
            "status": "OPEN",
            "players": [
              { "userId": "p1", "displayName": "Alice", "isReady": true },
              { "userId": "p2", "displayName": "Bob", "isReady": false }
            ],
            "maxPlayers": 4,
            "isPrivate": false,
            "allowGuests": true
          }
        }
        """.trimIndent()

        service.testDispatch(
            json,
            onLobbyUpdated = {
                called = true
                payload = it
            }
        )

        assertTrue(called)
        assertEquals("lobby.updated", payload.type)
        assertEquals("123", payload.lobby.lobbyId)
        assertEquals("HOST1", payload.lobby.hostUserId)
        assertEquals(2, payload.lobby.players.size)
        assertEquals("Alice", payload.lobby.players[0].displayName)
        assertEquals(true, payload.lobby.players[0].isReady)
    }


    @Test
    fun dispatch_callsOnLobbyDeleted() {
        var called = false
        var payload: LobbyDeletedPayload? = null

        val json = """
        {
          "type": "lobby.deleted",
          "lobbyId": "123"
        }
        """.trimIndent()

        service.testDispatch(
            json,
            onLobbyDeleted = {
                called = true
                payload = it
            }
        )

        assertTrue(called)
        assertEquals("123", payload!!.lobbyId)
    }

    @Test
    fun dispatch_callsOnLobbyStarted() {
        var called = false
        var payload: LobbyStartedPayload? = null

        val json = """
        {
          "type": "lobby.started",
          "lobbyId": "123",
          "matchId": "MATCH42"
        }
        """.trimIndent()

        service.testDispatch(
            json,
            onLobbyStarted = {
                called = true
                payload = it
            }
        )

        assertTrue(called)
        assertEquals("123", payload!!.lobbyId)
    }

    @Test
    fun dispatch_unknownType_doesNotCrash() {
        val json = """{ "type": "something.else" }"""
        service.testDispatch(json)
    }
}
