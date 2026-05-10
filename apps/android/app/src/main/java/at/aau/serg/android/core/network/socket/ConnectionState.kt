package at.aau.serg.android.core.network.socket

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    object Disconnected : ConnectionState()
    data class Failed(val message: String) : ConnectionState()
}
