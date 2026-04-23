package at.aau.serg.android.ui.screens.lobby.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyBrowseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyBrowseUiState())
    val uiState: StateFlow<LobbyBrowseUiState> = _uiState

    private val _effects = MutableSharedFlow<LobbyBrowseEffect>()
    val effects = _effects.asSharedFlow()

    fun update(lobbies: List<LobbyBrowseItem>, isLoading: Boolean, errorMessage: String?) {
        _uiState.update {
            it.copy(lobbies = lobbies, isLoading = isLoading, errorMessage = errorMessage)
        }
    }

    fun onEvent(event: LobbyBrowseEvent) {
        when (event) {
            is LobbyBrowseEvent.OnLobbyIdChanged -> {
                _uiState.update { it.copy(lobbyIdInput = event.input) }
            }
            is LobbyBrowseEvent.OnJoinLobby -> {
                viewModelScope.launch {
                    _effects.emit(LobbyBrowseEffect.JoinLobby(event.lobbyId))
                }
            }
            LobbyBrowseEvent.OnCreateNewLobby -> {
                viewModelScope.launch {
                    _effects.emit(LobbyBrowseEffect.NavigateToCreate)
                }
            }
            LobbyBrowseEvent.OnSettings -> {
                viewModelScope.launch {
                    _effects.emit(LobbyBrowseEffect.NavigateToSettings)
                }
            }
            LobbyBrowseEvent.OnBack -> {
                viewModelScope.launch {
                    _effects.emit(LobbyBrowseEffect.NavigateBack)
                }
            }
        }
    }
}
