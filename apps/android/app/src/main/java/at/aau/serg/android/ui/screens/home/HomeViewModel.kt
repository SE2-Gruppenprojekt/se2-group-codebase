package at.aau.serg.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userStore: ProtoStore<User>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            userStore.data.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnCreateLobby -> {
                viewModelScope.launch {
                    _effects.emit(HomeEffect.NavigateToCreate)
                }
            }
            HomeEvent.OnBrowseLobby -> {
                viewModelScope.launch {
                    _effects.emit(HomeEffect.NavigateToBrowse)
                }
            }
            HomeEvent.OnSettings -> {
                viewModelScope.launch {
                    _effects.emit(HomeEffect.NavigateToSettings)
                }
            }
        }
    }
}

