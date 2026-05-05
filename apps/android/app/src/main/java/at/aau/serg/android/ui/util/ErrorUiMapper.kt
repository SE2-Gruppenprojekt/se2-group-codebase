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
            AppError.WebSocket.ConnectionFailed -> "WebSocket connection failed"
            AppError.WebSocket.Disconnected -> "WebSocket disconnected"
            AppError.WebSocket.ProtocolError -> "WebSocket protocol error"
            is AppError.WebSocket.Serialization -> "WebSocket serialization error: ${error.message}"
            AppError.WebSocket.SubscriptionFailed -> "WebSocket subscription failed"
            is AppError.WebSocket.Unknown -> "WebSocket error: ${error.message}"
        }
}
