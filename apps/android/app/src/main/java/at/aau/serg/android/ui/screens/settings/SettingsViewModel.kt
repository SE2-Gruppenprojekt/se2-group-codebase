package at.aau.serg.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.SharingStarted
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

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            userStore.wipe()
            onDone()
        }
    }
}
