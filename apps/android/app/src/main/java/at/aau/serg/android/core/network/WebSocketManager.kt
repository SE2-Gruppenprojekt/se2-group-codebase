package at.aau.serg.android.core.network

import at.aau.serg.android.core.errors.AppError
import at.aau.serg.android.core.network.socket.ConnectionState
import at.aau.serg.android.core.network.socket.DefaultClientProvider
import at.aau.serg.android.core.network.socket.StompSessionWrapper
import at.aau.serg.android.core.network.socket.WebSocketClientProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketManager(
    private val clientProvider: WebSocketClientProvider = DefaultClientProvider(),
    private val socketUrl: String = WebConfig.SOCKET_URL,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
    private val autoReconnect: Boolean = true,
    private val idleTimeoutMs: Long = WebConfig.Socket.IDLE_TIMEOUT,
) {
    private val connectionMutex = Mutex()
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _session = MutableStateFlow<StompSessionWrapper?>(null)
    private val session: StateFlow<StompSessionWrapper?> = _session.asStateFlow()

    private val _errors = MutableSharedFlow<AppError>()
    val errors: SharedFlow<AppError> = _errors

    private var manualDisconnect = false
    internal var maxReconnectAttemptsForTest: Int? = null
    val activeSubscriptions = MutableStateFlow(0)

    private var supervisorJob: Job? = null
    private var idleWatcherJob: Job? = null

    init {
        activateBackgroundProcesses()
    }

    fun subscribe(topic: String): Flow<String> = flow {
        activeSubscriptions.value++

        try {
            while (true) {
                val currentState = connectionState.value
                if (currentState is ConnectionState.Failed || currentState is ConnectionState.Reconnecting) {
                    delay(2000)
                    continue
                }

                try {
                    val currentSession = ensureConnected()
                    emitAll(currentSession.subscribeText(topic))

                    break
                } catch (e: Exception) {
                    handleSessionFailure(e, AppError.WebSocket.SubscriptionFailed)

                    delay(2000)
                }
            }
        } finally {
            activeSubscriptions.value = (activeSubscriptions.value - 1).coerceAtLeast(0)
        }
    }

    internal fun getIdleTimerFlow(): Flow<Unit> = combine(
        activeSubscriptions,
        _session
    ) { count, currentSession ->
        Pair(count, currentSession)
    }.flatMapLatest { (count, currentSession) ->
        flow {
            if (count <= 0 && currentSession != null) {
                delay(idleTimeoutMs)
                emit(Unit)
            }
        }
    }

    internal suspend fun ensureConnected(): StompSessionWrapper {
        return connectionMutex.withLock {
            session.value?.let { return it }

            manualDisconnect = false
            _connectionState.value = ConnectionState.Connecting

            val newSession = clientProvider.connect(socketUrl)
            _session.value = newSession
            _connectionState.value = ConnectionState.Connected

            activateBackgroundProcesses()

            newSession
        }
    }

    private fun activateBackgroundProcesses() {
        if (supervisorJob?.isActive != true && autoReconnect) {
            supervisorJob = scope.launch {
                _connectionState.collect { state ->
                    val isUnexpectedDisconnection = state == ConnectionState.Disconnected || state is ConnectionState.Failed

                    if (isUnexpectedDisconnection && !manualDisconnect) {
                        reconnectWithBackoff()
                    }
                }
            }
        }

        if (idleWatcherJob?.isActive != true) {
            idleWatcherJob = scope.launch {
                getIdleTimerFlow().collect {
                    disconnect(manual = true)
                }
            }
        }
    }

    suspend fun disconnect(manual: Boolean = false) {
        val sessionToClose = resetSessionState(
            newState = ConnectionState.Disconnected,
            isManual = manual
        )
        safelyDisconnectSession(sessionToClose)
    }


    private suspend fun resetSessionState(
        newState: ConnectionState,
        isManual: Boolean = false
    ): StompSessionWrapper? = connectionMutex.withLock {
        manualDisconnect = isManual
        if (isManual) {
            supervisorJob?.cancel()
            supervisorJob = null
            idleWatcherJob?.cancel()
            idleWatcherJob = null
        }

        val oldSession = _session.value
        _session.value = null
        _connectionState.value = newState

        oldSession
    }

    private suspend fun safelyDisconnectSession(session: StompSessionWrapper?) {
        withContext(NonCancellable) {
            try {
                session?.disconnect()
            } catch (e: Exception) {
                // TODO: possibly create log.txt for error report sending.
            }
        }
    }

    private suspend fun handleSessionFailure(error: Throwable, appError: AppError) {
        if (error is CancellationException) return
        _errors.emit(appError)

        val sessionToClose = resetSessionState(
            newState = ConnectionState.Failed(error.message ?: "")
        )

        safelyDisconnectSession(sessionToClose)
    }

    private suspend fun reconnectWithBackoff() {
        var attempt = 0
        while (_session.value == null && attempt < WebConfig.Socket.MAX_ATTEMPTS) {
            if (maxReconnectAttemptsForTest?.let { attempt >= it } == true) return

            attempt++
            _connectionState.value = ConnectionState.Reconnecting(attempt)

            try {
                ensureConnected()
                return
            } catch (e: Exception) {
                handleSessionFailure(e, AppError.WebSocket.ConnectionFailed("Reconnect failed (attempt: $attempt)."))
                val delayMs = minOf(
                    WebConfig.Socket.MAX_ATTEMPT_DELAY,
                    WebConfig.Socket.ATTEMPT_DELAY * attempt
                )
                delay(delayMs)
            }
        }
    }

    // Test Helpers
    internal fun simulateSessionLossForTest() {
        _session.value = null
        _connectionState.value = ConnectionState.Disconnected
    }

    internal fun setActiveSubscriptionsForTest(subscribers: Int) {
        activeSubscriptions.value = subscribers
    }

    internal fun simulateFailureForTest() {
        _session.value = null
        _connectionState.value = ConnectionState.Failed("Simulated error")
    }
}
