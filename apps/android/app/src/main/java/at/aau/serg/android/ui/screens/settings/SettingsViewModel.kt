package at.aau.serg.android.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userStore: UserStore
) : ViewModel() {

    val user = userStore.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = User.getDefaultInstance()
    )

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            userStore.wipe()
            onDone()
        }
    }
}
