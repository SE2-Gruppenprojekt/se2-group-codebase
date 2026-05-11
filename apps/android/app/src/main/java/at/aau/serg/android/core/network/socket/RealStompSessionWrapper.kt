package at.aau.serg.android.core.network.socket

import org.hildan.krossbow.stomp.StompSession
import kotlinx.coroutines.flow.Flow
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.WebSocketClient

class RealStompSessionWrapper(
    private val session: StompSession
) : StompSessionWrapper {

    override suspend fun subscribeText(destination: String): Flow<String> {
        return session.subscribeText(destination)
    }

    override suspend fun disconnect() {
        session.disconnect()
    }
}
