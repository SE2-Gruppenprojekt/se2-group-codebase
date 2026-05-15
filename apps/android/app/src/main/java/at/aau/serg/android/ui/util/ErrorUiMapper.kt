package at.aau.serg.android.ui.util

import at.aau.serg.android.core.errors.AppError

object ErrorUiMapper {
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

            // Generic
            AppError.Unknown -> "Unexpected error"

            // WebSocket
            is AppError.WebSocket.ConnectionFailed -> error.message
            AppError.WebSocket.Disconnected -> "WebSocket disconnected"
            AppError.WebSocket.ProtocolError -> "WebSocket protocol error"
            AppError.WebSocket.SubscriptionFailed -> "WebSocket subscription failed"
        }
}
