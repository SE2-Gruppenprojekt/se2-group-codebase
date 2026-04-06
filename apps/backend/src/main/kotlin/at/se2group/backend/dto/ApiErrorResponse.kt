package at.se2group.backend.dto

data class ApiErrorResponse(
    val errorCode: String,
    val errorMessage: String
)
