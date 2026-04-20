package at.aau.serg.android.ui.util

import at.aau.serg.android.core.errors.AppError

object ErrorUiMapper {
    fun toMessage(error: AppError): String =
        when (error) {
            AppError.Network -> "Network error"
            AppError.Server -> "Server error"
            AppError.BadRequest -> "Bad request"
            AppError.Forbidden -> "Access denied"
            AppError.NotFound -> "Resource not found"
            AppError.Conflict -> "Conflict"
            is AppError.Api -> error.message
            AppError.Unknown -> "Unexpected error"
        }
}
