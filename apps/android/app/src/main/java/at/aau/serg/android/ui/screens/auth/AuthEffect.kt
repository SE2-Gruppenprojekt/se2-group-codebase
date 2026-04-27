package at.aau.serg.android.ui.screens.auth

sealed class AuthEffect {
    object NavigateContinue : AuthEffect()
    object NavigateBack : AuthEffect()
}
