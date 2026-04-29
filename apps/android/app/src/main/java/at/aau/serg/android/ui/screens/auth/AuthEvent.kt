package at.aau.serg.android.ui.screens.auth

sealed class AuthEvent {
    object OnBack : AuthEvent()
    object OnSubmit : AuthEvent()
    data class OnUsernameChanged(val value: String) : AuthEvent()
}
