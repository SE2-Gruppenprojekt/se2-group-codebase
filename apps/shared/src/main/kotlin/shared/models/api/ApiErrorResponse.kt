package shared.models.api

data class ApiErrorResponse(
    val errorCode: String,
    val errorMessage: String
)
