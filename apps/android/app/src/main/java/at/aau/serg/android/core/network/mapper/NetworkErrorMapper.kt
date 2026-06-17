package at.aau.serg.android.core.network.mapper

import at.aau.serg.android.core.errors.ApiRuleViolation
import at.aau.serg.android.core.errors.AppError
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import java.io.IOException

data class ApiErrorResponse(
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val violations: List<ApiRuleViolation> = emptyList()
)

object NetworkErrorMapper {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(ApiErrorResponse::class.java)

    fun map(e: Throwable): AppError =
        when (e) {
            is IOException -> AppError.Rest.Network
            is HttpException -> mapHttpError(e)
            else -> AppError.UnknownNetwork(e.message ?: "Unexpected Network Error")
        }

    private fun mapHttpError(e: HttpException): AppError {
        val body = e.response()?.errorBody()?.string()
        val parsed = body?.let { runCatching { adapter.fromJson(it) }.getOrNull() }

        if (e.code() == 409 && parsed?.errorCode == "INVALID_TURN_SUBMISSION") {
            return AppError.Rest.RuleValidation(
                message = parsed.errorMessage ?: "Submitted draft is invalid",
                violations = parsed.violations
            )
        }

        val message = parsed?.errorMessage?.takeIf { it.isNotBlank() }
        if (message != null) return AppError.Rest.Api(message)

        return when (e.code()) {
            400 -> AppError.Rest.BadRequest
            401 -> AppError.Rest.Unauthorized
            403 -> AppError.Rest.Forbidden
            404 -> AppError.Rest.NotFound
            409 -> AppError.Rest.Conflict
            in 500..599 -> AppError.Rest.Server
            else -> AppError.Rest.Api("Unresolved error code encountered [http code: ${e.code()}]")
        }
    }
}
