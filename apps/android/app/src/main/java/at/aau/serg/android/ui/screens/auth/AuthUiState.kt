package at.aau.serg.android.ui.screens.auth

import shared.validation.ValidationResult

data class AuthUiState(
    val username: String = "",
    val validation: ValidationResult = ValidationResult(
        isValid = true,
        violations = emptyList()
    ),
    val uid: String = ""
)
