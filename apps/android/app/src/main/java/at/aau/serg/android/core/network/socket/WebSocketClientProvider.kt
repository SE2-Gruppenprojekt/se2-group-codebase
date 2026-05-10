package at.aau.serg.android.core.network.socket

interface WebSocketClientProvider {
    suspend fun connect(url: String): StompSessionWrapper
}
