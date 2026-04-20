package at.aau.serg.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userStore: ProtoStore<User>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            userStore.data.collect { user ->
                _uiState.value = HomeUiState(
                    username = user.displayName.ifBlank { "Guest" },
                    uid = user.uid,
                    loadState = LoadState.Success
                )
            }
        }
    }
}

