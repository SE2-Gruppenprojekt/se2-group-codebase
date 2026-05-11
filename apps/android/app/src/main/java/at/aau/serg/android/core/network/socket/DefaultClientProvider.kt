package at.aau.serg.android.core.network.socket

import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class DefaultClientProvider(
    private val client: StompClient = StompClient(OkHttpWebSocketClient())
) : WebSocketClientProvider {
    override suspend fun connect(url: String): StompSessionWrapper {
        val session = client.connect(url)
        return RealStompSessionWrapper(session)
    }
}
