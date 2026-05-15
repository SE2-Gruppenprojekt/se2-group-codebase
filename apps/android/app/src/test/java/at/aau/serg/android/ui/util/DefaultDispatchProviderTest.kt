package at.aau.serg.android.ui.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultDispatcherProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `main returns Dispatchers Main`() {
        // We verify that the provider's main is indeed the one we set
        assertEquals(Dispatchers.Main, DefaultDispatcherProvider.main)
    }

    @Test
    fun `io returns Dispatchers IO`() {
        assertEquals(Dispatchers.IO, DefaultDispatcherProvider.io)
    }
}
