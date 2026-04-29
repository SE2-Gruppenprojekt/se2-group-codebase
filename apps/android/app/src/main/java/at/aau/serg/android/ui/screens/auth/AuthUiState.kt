package at.aau.serg.android.ui.screens.auth

import shared.validation.ValidationResult

sealed class AuthMode {
    data object CreateUser : AuthMode()
    data object ChangeUsername : AuthMode()
}

data class AuthUiState(
    val username: String = "",
    val validation: ValidationResult = ValidationResult(
        isValid = true,
        violations = emptyList()
    ),
    val uid: String = "",
    val mode: AuthMode = AuthMode.CreateUser
) {
    val canGoBack: Boolean
        get() = mode == AuthMode.ChangeUsername
}
