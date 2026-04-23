package at.aau.serg.android.core.errors

sealed class AppError {
    data object Network : AppError()
    data object Server : AppError()
    data object BadRequest : AppError()
    data object Forbidden : AppError()
    data object NotFound : AppError()
    data object Conflict : AppError()
    data class Api(val message: String) : AppError()
    data object Unknown : AppError()
}
