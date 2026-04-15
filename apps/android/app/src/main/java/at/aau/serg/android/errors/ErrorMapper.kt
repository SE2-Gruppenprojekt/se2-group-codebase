package at.aau.serg.android.errors

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import retrofit2.HttpException

object ErrorCatalog {
    const val NETWORK = "Network error"
    const val SERVER = "Server error"
    const val UNKNOWN = "Unexpected error"
}

data class ApiErrorResponse(
    val errorCode: String,
    val errorMessage: String
)

object ErrorMapper {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val apiErrorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    fun map(e: Throwable): String =
        when (e) {
            is IOException -> ErrorCatalog.NETWORK
            is HttpException -> mapHttpError(e)
            else -> ErrorCatalog.UNKNOWN
        }

    private fun mapHttpError(e: HttpException): String {
        val response = e.response()
        val body = response?.errorBody()?.string()
        val apiMessage = body
            ?.let { apiErrorAdapter.fromJson(it)?.errorMessage }
            ?.takeIf { it.isNotBlank() }

        return apiMessage ?: when (e.code()) {
            400 -> "Bad request"
            403 -> "Access denied"
            404 -> "Resource not found"
            409 -> "Conflict"
            in 500..599 -> ErrorCatalog.SERVER
            else -> ErrorCatalog.UNKNOWN
        }
    }
}
