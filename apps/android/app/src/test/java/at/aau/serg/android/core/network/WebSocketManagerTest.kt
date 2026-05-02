package at.aau.serg.android.core.network

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebSocketManagerTest {

    private lateinit var client: StompClient
    private lateinit var session: StompSession
    private lateinit var manager: WebSocketManager

    @Before
    fun setup() {
        client = mockk()
        session = mockk()
        manager = WebSocketManager(client)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun connect_sets_session_once() = runTest {
        coEvery { client.connect(WebConfig.SOCKET_URL) } returns session

        manager.connect()

        coVerify(exactly = 1) { client.connect(WebConfig.SOCKET_URL) }
    }

    @Test
    fun connect_is_idempotent() = runTest {
        coEvery { client.connect(any()) } returns session

        manager.connect()
        manager.connect()

        coVerify(exactly = 1) { client.connect(WebConfig.SOCKET_URL) }
    }

    @Test
    fun subscribe_emits_values() = runTest {
        coEvery { client.connect(any()) } returns session

        coEvery {
            session.subscribe(any())
        } returns flowOf(
            mockk<StompFrame.Message> {
                every { bodyAsText } returns "a"
            },
            mockk<StompFrame.Message> {
                every { bodyAsText } returns "b"
            },
            mockk<StompFrame.Message> {
                every { bodyAsText } returns "c"
            }
        )

        coEvery { session.disconnect() } just Runs

        manager.connect()

        val result = manager.subscribe("topic").toList()

        assertEquals(listOf("a", "b", "c"), result)
    }


    @Test
    fun subscribe_calls_subscribeText() = runTest {
        coEvery { client.connect(any()) } returns session

        coEvery {
            session.subscribe(any())
        } returns flowOf(
            mockk<StompFrame.Message> {
                every { bodyAsText } returns "msg"
            }
        )

        coEvery { session.disconnect() } just Runs

        manager.connect()
        manager.subscribe("topic").toList()

        coVerify { session.subscribe(any()) }
        coVerify { session.disconnect() }
    }

    @Test
    fun subscribe_triggers_disconnect_on_completion() = runTest {
        coEvery { client.connect(any()) } returns session

        val message = mockk<StompFrame.Message> {
            every { body } returns FrameBody.Text("msg")
            every { bodyAsText } returns "msg"
        }

        coEvery {
            session.subscribe(any())
        } returns flowOf(message)

        coEvery { session.disconnect() } just Runs

        manager.connect()

        val result = manager.subscribe("topic").toList()

        assertEquals(listOf("msg"), result)

        coVerify { session.subscribe(any()) }
        coVerify { session.disconnect() }
    }

}
