package at.aau.serg.android.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.datastore.user.UserStore
import at.aau.serg.android.util.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import shared.validation.ValidationResult
import shared.validation.user.DisplayNameValidator
import java.util.UUID

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userStore = UserStore(application)

    val user = userStore.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000),
        initialValue = User.getDefaultInstance()
    )

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _validation = MutableStateFlow(
        DisplayNameValidator.validate("")
    )
    val validation: StateFlow<ValidationResult> = _validation

    init {
        viewModelScope.launch {
            user.collect { u ->
                val current = u.displayName
                _username.value = current
                _validation.value = DisplayNameValidator.validate(current)
            }
        }
    }

    fun onUsernameChanged(value: String) {
        _username.value = value
        _validation.value = DisplayNameValidator.validate(value)
    }

    fun canContinue(): Boolean = _validation.value.isValid

    fun submit(onSuccess: () -> Unit) {
        val name = _username.value.trim()

        if (!DisplayNameValidator.validate(name).isValid) return

        viewModelScope.launch {
            val uid = user.value.uid.ifBlank {
                UUID.randomUUID().toString()
            }

            userStore.save(
                user.value.toBuilder()
                    .setUid(uid)
                    .setDisplayName(name)
                    .build()
            )

            UserPrefs.saveUsername(getApplication(), name)

            onSuccess()
        }
    }
}
