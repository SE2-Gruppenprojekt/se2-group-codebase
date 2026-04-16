package at.aau.serg.android.viewmodel

import android.content.Context
import at.aau.serg.android.util.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UsernameViewModel : BaseViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError

    fun onUsernameChanged(value: String) {
        _username.value = value
        validateUsername()
    }

    private fun validateUsername(): Boolean {
        val name = _username.value

        return when {
            name.isBlank() -> {
                _usernameError.value = "Username darf nicht leer sein"
                false
            }
            name.length < 3 -> {
                _usernameError.value = "Mindestens 3 Zeichen"
                false
            }
            !name.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                _usernameError.value = "Nur Buchstaben, Zahlen und _ erlaubt"
                false
            }
            else -> {
                _usernameError.value = null
                true
            }
        }
    }

    fun submit(
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (!validateUsername()) return

        launchRequest(
            request = {
                UserPrefs.saveUsername(context, _username.value)
            },
            onSuccess = {
                onSuccess()
            },
            onError = {
            }
        )
    }

    fun logout(context: Context) {
        launchRequest(
            request = {
                UserPrefs.clear(context)
            },
            onSuccess = {
                _username.value = ""
                _usernameError.value = null
            },
            onError = {

            }
        )
    }
}
