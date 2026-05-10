package at.aau.serg.android.core.network.socket

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultClientProviderTest {
    @Test
    fun connect_validUrl_returnsRealStompSessionWrapper() = runTest {
        val mockStompSession = mockk<StompSession>()
        val mockStompClient = mockk<StompClient>()
        coEvery { mockStompClient.connect(any()) } returns mockStompSession
        val provider = DefaultClientProvider(mockStompClient)
        val result = provider.connect("ws://localhost")

        assertNotNull(result)
        assertTrue(result is RealStompSessionWrapper)

        coVerify { mockStompClient.connect("ws://localhost") }
    }
}
