package at.aau.serg.android.core.network.socket

class FakeClientProvider(
    private val session: FakeStompSessionWrapper
) : WebSocketClientProvider {

    var connectCount = 0
        private set
    var alwaysFail = false

    var failNextConnect: Boolean = false

    override suspend fun connect(url: String): StompSessionWrapper {
        connectCount++
        if (failNextConnect || alwaysFail) {
            failNextConnect = false
            throw Exception("Connection Failed")
        }
        return session
    }
}
