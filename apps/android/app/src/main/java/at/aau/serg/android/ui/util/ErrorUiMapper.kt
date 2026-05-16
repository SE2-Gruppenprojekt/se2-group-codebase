package at.aau.serg.android.ui.util

import at.aau.serg.android.core.errors.AppError

object ErrorUiMapper {
    fun map(e: Throwable): AppError =
        when (e) {
            is IllegalStateException -> AppError.State(e.message ?: "Unexpected State Error")
            else -> AppError.Unknown(e.message ?: "Unexpected Error")
        }

    fun toMessage(error: AppError): String =
        when (error) {
            // REST
            AppError.Rest.Network -> "Network error"
            AppError.Rest.Server -> "Server error"
            AppError.Rest.BadRequest -> "Bad request"
            AppError.Rest.Forbidden -> "Access denied"
            AppError.Rest.NotFound -> "Resource not found"
            AppError.Rest.Conflict -> "Conflict"
            is AppError.Rest.Api -> error.message

            is AppError.Game.TurnTimedOut -> "Your turn has ended!"

            // Generic
            is AppError.State -> error.message
            is AppError.Unknown -> error.message
            is AppError.UnknownNetwork -> error.message

            // WebSocket
            is AppError.WebSocket.ConnectionFailed -> error.message
            AppError.WebSocket.Disconnected -> "WebSocket disconnected"
            AppError.WebSocket.ProtocolError -> "WebSocket protocol error"
            AppError.WebSocket.SubscriptionFailed -> "WebSocket subscription failed"
        }
}
