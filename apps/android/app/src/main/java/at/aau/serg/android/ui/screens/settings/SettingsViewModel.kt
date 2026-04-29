package at.aau.serg.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.theme.ThemeState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userStore: ProtoStore<User>
) : ViewModel() {

    val user = userStore.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = User.getDefaultInstance()
    )

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects = _effects.asSharedFlow()


    fun onEvent(event: SettingsEvent) {
        when (event) {

            SettingsEvent.OnChangeUsername -> {
                viewModelScope.launch {
                    _effects.emit(SettingsEffect.NavigateChangeUsername)
                }
            }

            SettingsEvent.OnBack -> {
                viewModelScope.launch {
                    _effects.emit(SettingsEffect.NavigateBack)
                }
            }

            SettingsEvent.OnLogout -> {
                viewModelScope.launch {
                    userStore.wipe()
                    _effects.emit(SettingsEffect.Logout)
                }
            }

            is SettingsEvent.SetDarkMode ->
                ThemeState.isDarkMode.value = event.value
        }
    }
}
