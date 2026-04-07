package at.aau.serg.android.errors

import java.io.IOException
import retrofit2.HttpException

object ErrorCatalog {
    const val NETWORK = "Network error"
    const val SERVER = "Server error"
    const val UNKNOWN = "Unexpected error"
}

object ErrorMapper {
    fun map(e: Throwable): String =
        when (e) {
            is IOException -> ErrorCatalog.NETWORK
            is HttpException -> ErrorCatalog.SERVER
            else -> ErrorCatalog.UNKNOWN
        }
}
