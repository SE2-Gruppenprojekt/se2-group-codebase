package at.aau.serg.android.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class WebSocketManager(
    private val client : StompClient = StompClient(OkHttpWebSocketClient())
) {
    private var session: StompSession? = null
    private var subscriberCount = 0

    private val lock = Any()

    suspend fun connect() {
        synchronized(lock) {
            if (session != null) return
        }

        // connect OUTSIDE lock (important)
        val newSession = client.connect(WebConfig.SOCKET_URL)

        synchronized(lock) {
            session = newSession
        }
    }

    fun subscribe(topic: String): Flow<String> = flow {
        connect()
        val currentSession = synchronized(lock) {
            session ?: throw IllegalStateException("WebSocket not connected.")
        }

        synchronized(lock) {
            subscriberCount++
        }

        try {
            currentSession.subscribeText(topic).collect { emit(it) }
        } finally {
            val shouldDisconnect = synchronized(lock) {
                subscriberCount--
                subscriberCount <= 0
            }

            if (shouldDisconnect) {
                val sessionToClose = synchronized(lock) {
                    val s = session
                    session = null
                    s
                }

                if (sessionToClose != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        sessionToClose.disconnect()
                    }
                }
            }
        }
    }
}
