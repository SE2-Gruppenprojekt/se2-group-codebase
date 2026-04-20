package at.aau.serg.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shared.validation.user.DisplayNameValidator
import java.util.UUID

class AuthViewModel(
    private val userStore: ProtoStore<User>
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            userStore.data.collect { user ->
                updateUsernameInternal(user.displayName, user.uid)
            }
        }
    }

    fun onUsernameChanged(value: String) {
        updateUsernameInternal(value, _uiState.value.uid)
    }

    private fun updateUsernameInternal(name: String, uid: String) {
        val trimmed = name.trim()
        val validation = DisplayNameValidator.validate(trimmed)

        _uiState.update {
            it.copy(
                username = trimmed,
                validation = validation,
                uid = uid
            )
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.validation.isValid) return

        viewModelScope.launch {
            val uid = state.uid.ifBlank { UUID.randomUUID().toString() }

            userStore.save(
                User.newBuilder()
                    .setUid(uid)
                    .setDisplayName(state.username)
                    .build()
            )

            onSuccess()
        }
    }
}
