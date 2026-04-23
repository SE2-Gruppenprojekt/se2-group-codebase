package at.aau.serg.android.ui.screens.lobby.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyService
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyBrowseViewModel(
    private val api: LobbyAPI = LobbyAPI(
        RetrofitProvider.retrofit.create(LobbyService::class.java)
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyBrowseUiState())
    val uiState: StateFlow<LobbyBrowseUiState> = _uiState

    private val _effects = MutableSharedFlow<LobbyBrowseEffect>()
    val effects = _effects.asSharedFlow()

    init {
        loadLobbies()
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

    fun loadLobbies() = viewModelScope.launch {

        _uiState.update {
            it.copy(loadState = LoadState.Loading)
        }

        try {
            val lobbies = api.getLobbies().map { it.toUi() }

            _uiState.update {
                it.copy(
                    lobbies = lobbies,
                    loadState = LoadState.Success
                )
            }

        } catch (e: Throwable) {
            val appError = NetworkErrorMapper.map(e)

            _uiState.update {
                it.copy(loadState = LoadState.Error(appError))
            }
        }
    }
}
