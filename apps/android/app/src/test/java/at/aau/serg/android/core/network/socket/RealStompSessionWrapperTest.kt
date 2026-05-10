package at.aau.serg.android.core.network.socket

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hildan.krossbow.stomp.StompSession
import kotlin.test.Test
import kotlin.test.assertEquals

class RealStompSessionWrapperTest {
    private val mockSession = mockk<StompSession>(relaxed = true)
    private val wrapper = RealStompSessionWrapper(mockSession)

    @Test
    fun subscribeText_callsSessionSubscribeText() = runTest {
        val destination = "/topic/test"
        val mockFrame = mockk<org.hildan.krossbow.stomp.frame.StompFrame.Message>()

        coEvery { mockFrame.bodyAsText } returns "message"
        coEvery { mockSession.subscribe(any()) } returns flowOf(mockFrame)

        val resultFlow = wrapper.subscribeText(destination)

        resultFlow.test {
            assertEquals("message", awaitItem())
            awaitComplete()
        }

        coVerify { mockSession.subscribe(any()) }
    }

    @Test
    fun disconnect_callsSessionDisconnect() = runTest {
        wrapper.disconnect()

        coVerify { mockSession.disconnect() }
    }
}
