package at.aau.serg.android.core.network

import app.cash.turbine.test
import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.errors.AppError
import at.aau.serg.android.core.network.socket.ConnectionState
import at.aau.serg.android.core.network.socket.FakeClientProvider
import at.aau.serg.android.core.network.socket.FakeStompSessionWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeSession: FakeStompSessionWrapper = FakeStompSessionWrapper()
    private val idleTimeout = 5000L

    @Test
    fun coverDefaultParameters() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            idleTimeoutMs = 100,
            scope = backgroundScope
        )

        manager.disconnect(manual = true)
        manager.disconnect()
    }

    @Test
    fun ensureConnected_successful_updatesStateToConnected() = runTest {
        val manager = WebSocketManager(FakeClientProvider(fakeSession), autoReconnect = false)

        manager.connectionState.test {
            assertEquals(ConnectionState.Idle, awaitItem())

            manager.ensureConnected()

            assertEquals(ConnectionState.Connecting, awaitItem())
            assertEquals(ConnectionState.Connected, awaitItem())
        }
    }

    @Test
    fun subscribe_validTopic_emitsReceivedMessages() = runTest {
        val manager = WebSocketManager(
            clientProvider = FakeClientProvider(fakeSession),
            scope = backgroundScope,
            autoReconnect = false
        )

        manager.ensureConnected()

        manager.subscribe("/topic/test").test {
            fakeSession.emit("/topic/test", "hello")

            assertEquals("hello", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun subscribe_onFailure_emitsSubscriptionError() = runTest {
        val manager = WebSocketManager(FakeClientProvider(fakeSession), autoReconnect = false)

        manager.ensureConnected()

        manager.errors.test {
            fakeSession.failSubscription("/topic/test", RuntimeException("boom"))

            val job = launch {
                manager.subscribe("/topic/test").collect()
            }

            assertEquals(AppError.WebSocket.SubscriptionFailed, awaitItem())

            job.cancel()
        }
    }

    @Test
    fun disconnect_manual_updatesStateToDisconnected() = runTest {
        val manager = WebSocketManager(FakeClientProvider(fakeSession))

        manager.ensureConnected()
        manager.disconnect(manual = true)

        assertTrue(fakeSession.isDisconnected)
        assertEquals(ConnectionState.Disconnected, manager.connectionState.value)
    }

    @Test
    fun onSessionLoss_autoReconnectEnabled_triggersReconnection() = runTest {
        val provider = FakeClientProvider(fakeSession)

        val manager = WebSocketManager(
            clientProvider = provider,
            scope = backgroundScope,
            autoReconnect = true
        )
        manager.maxReconnectAttemptsForTest = 1

        manager.connectionState.test {
            assertEquals(ConnectionState.Idle, awaitItem())
            manager.ensureConnected()
            assertEquals(ConnectionState.Connecting, awaitItem())
            assertEquals(ConnectionState.Connected, awaitItem())

            val initialConnectCount = provider.connectCount

            manager.simulateSessionLossForTest()
            assertEquals(ConnectionState.Disconnected, awaitItem())

            val nextState = awaitItem()
            assertTrue(nextState is ConnectionState.Reconnecting)
            assertEquals(1, nextState.attempt)

            assertTrue(provider.connectCount > initialConnectCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onFailed_autoReconnectEnabled_triggersReconnection() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(provider, scope = backgroundScope, autoReconnect = true)

        manager.connectionState.test {
            awaitItem()
            manager.ensureConnected()
            awaitItem()
            awaitItem()

            manager.simulateFailureForTest()
            assertTrue(awaitItem() is ConnectionState.Failed)

            assertTrue(awaitItem() is ConnectionState.Reconnecting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun subscribe_onFlowException_emitsAppError() = runTest {
        val manager = WebSocketManager(
            clientProvider = FakeClientProvider(fakeSession),
            scope = backgroundScope,
            autoReconnect = false
        )

        manager.ensureConnected()

        manager.errors.test {
            fakeSession.failSubscription("/topic/test", RuntimeException("boom"))

            val job = launch {
                manager.subscribe("/topic/test").collect()
            }

            assertEquals(AppError.WebSocket.SubscriptionFailed, awaitItem())

            job.cancel()
        }
    }

    @Test
    fun reconnect_onRetryFailure_staysInDisconnectedOrConnectingState() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            scope = backgroundScope,
            autoReconnect = true
        )
        manager.maxReconnectAttemptsForTest = 1

        manager.connectionState.test {
            assertEquals(ConnectionState.Idle, awaitItem())
            manager.ensureConnected()
            assertEquals(ConnectionState.Connecting, awaitItem())
            assertEquals(ConnectionState.Connected, awaitItem())

            provider.failNextConnect = true
            manager.simulateSessionLossForTest()

            val stateAfterLoss = awaitItem()
            if (stateAfterLoss is ConnectionState.Disconnected) {
                val next = awaitItem()
                assertTrue(next is ConnectionState.Reconnecting || next is ConnectionState.Connecting)
            } else {
                assertTrue(stateAfterLoss is ConnectionState.Reconnecting || stateAfterLoss is ConnectionState.Connecting)
            }


            assertTrue(provider.connectCount > 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reconnect_emitsNewErrorForEachFailedAttempt() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            scope = backgroundScope,
            autoReconnect = true
        )

        manager.ensureConnected()

        manager.errors.test {
            provider.failNextConnect = true
            manager.simulateSessionLossForTest()

            val error1 = awaitItem()
            assertTrue(error1 is AppError.WebSocket.ConnectionFailed)

            provider.failNextConnect = true
            advanceTimeBy(WebConfig.Socket.MAX_ATTEMPT_DELAY)

            val error2 = awaitItem()
            assertTrue(error2 is AppError.WebSocket.ConnectionFailed)

            assertTrue(error1.message != error2.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getIdleTimerFlow_onTimeout_emitsUnit() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            idleTimeoutMs = idleTimeout,
            autoReconnect = false
        )

        manager.ensureConnected()

        manager.getIdleTimerFlow().test {
            advanceTimeBy(idleTimeout - 1)
            expectNoEvents()

            advanceTimeBy(1)
            awaitItem()
        }
    }

    @Test
    fun getIdleTimerFlow_onNewSubscription_resetsTimer() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            idleTimeoutMs = idleTimeout,
            autoReconnect = false
        )

        manager.ensureConnected()

        manager.getIdleTimerFlow().test {
            manager.activeSubscriptions.value = 0

            advanceTimeBy(idleTimeout - 100)
            expectNoEvents()

            manager.activeSubscriptions.value = 1
            advanceTimeBy(idleTimeout + 500)

            expectNoEvents()

            manager.activeSubscriptions.value = 0

            advanceTimeBy(idleTimeout)
            awaitItem()
        }
    }

    @Test
    fun onIdleTimeout_withNoActiveSubscriptions_disconnectsSession() = runTest {
        val manager = WebSocketManager(
            clientProvider = FakeClientProvider(fakeSession),
            idleTimeoutMs = 5000L,
            scope = backgroundScope,
            autoReconnect = false
        )

        manager.connectionState.test {
            assertEquals(ConnectionState.Idle, awaitItem())

            manager.ensureConnected()

            assertEquals(ConnectionState.Connecting, awaitItem())
            assertEquals(ConnectionState.Connected, awaitItem())

            manager.setActiveSubscriptionsForTest(1)
            runCurrent()
            manager.setActiveSubscriptionsForTest(0)
            runCurrent()

            advanceTimeBy(5000)

            runCurrent()

            assertEquals(ConnectionState.Disconnected, awaitItem())
        }
    }

    @Test
    fun disconnect_manual_preventsFutureAutoReconnection() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            scope = backgroundScope,
            autoReconnect = true
        )

        manager.ensureConnected()

        manager.disconnect(manual = true)

        val connectCountAfterDisconnect = provider.connectCount

        manager.simulateSessionLossForTest()

        runCurrent()

        assertEquals(connectCountAfterDisconnect, provider.connectCount)
        assertEquals(ConnectionState.Disconnected, manager.connectionState.value)
    }

    @Test
    fun afterIdleTimeout_newSubscription_successfullyReconnects() = runTest {
        val provider = FakeClientProvider(fakeSession)
        val manager = WebSocketManager(
            clientProvider = provider,
            idleTimeoutMs = 100,
            scope = backgroundScope
        )

        val firstSession = manager.ensureConnected()

        manager.setActiveSubscriptionsForTest(0)
        advanceTimeBy(200)
        runCurrent()

        assertEquals(ConnectionState.Disconnected, manager.connectionState.value)
        val secondSession = manager.ensureConnected()
        val job = launch {
            manager.subscribe("/topic/new").collect()
        }
        runCurrent()

        assertEquals(ConnectionState.Connected, manager.connectionState.value)
        assertEquals(firstSession, secondSession)
        job.cancel()
    }

    @Test
    fun subscribe_onCancellation_doesNotDestroySession() = runTest {
        val manager = WebSocketManager(
            clientProvider = FakeClientProvider(fakeSession),
            scope = backgroundScope
        )

        manager.ensureConnected()

        manager.subscribe("/topic/test").test {
            fakeSession.emit("/topic/test", "ping")
            assertEquals("ping", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(ConnectionState.Connected, manager.connectionState.value)
    }

    @Test
    fun safelyDisconnectSession_handlesExceptions() = runTest {
        val breakingSession = object : FakeStompSessionWrapper() {
            override suspend fun disconnect() {
                throw RuntimeException("Disconnect failed")
            }
        }
        val manager = WebSocketManager(FakeClientProvider(breakingSession))
        manager.ensureConnected()
        manager.disconnect()
    }
}
