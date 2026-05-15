package at.aau.serg.android.core.errors

sealed class AppError {

    sealed class Game : AppError() {
        data object TurnTimedOut : Game()
    }

    // REST / HTTP
    sealed class Rest : AppError() {
        data object Network : Rest()
        data object Server : Rest()
        data object BadRequest : Rest()
        data object Forbidden : Rest()
        data object NotFound : Rest()
        data object Conflict : Rest()
        data class Api(val message: String) : Rest()
    }

    // WebSocket / STOMP
    sealed class WebSocket : AppError() {
        data class ConnectionFailed(val message: String) : WebSocket()
        data object Disconnected : WebSocket()
        data object SubscriptionFailed : WebSocket()
        data object ProtocolError : WebSocket()
        data class Unknown(val message: String) : WebSocket()
    }

    data object Unknown : AppError()

    companion object {
        fun allStaticErrors(): List<AppError> = listOf(
            Rest.Network,
            Rest.Server,
            Rest.BadRequest,
            Rest.Forbidden,
            Rest.NotFound,
            Rest.Conflict,
            Unknown,
            WebSocket.Disconnected,
            WebSocket.SubscriptionFailed,
            WebSocket.ProtocolError
        )
    }
}

