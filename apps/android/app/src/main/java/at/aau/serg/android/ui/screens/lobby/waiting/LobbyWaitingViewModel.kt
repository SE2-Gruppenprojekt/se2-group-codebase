package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyService
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseEffect
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseEvent
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import shared.models.lobby.domain.Lobby

class LobbyWaitingViewModel(
    private val userStore: ProtoStore<User>,
    private val api: LobbyAPI = LobbyAPI(
        RetrofitProvider.retrofit.create(LobbyService::class.java)
    ),

) : ViewModel() {

    val _uiState = MutableStateFlow(LobbyWaitingUiState())
    val uiState: StateFlow<LobbyWaitingUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LobbyWaitingEffect>()
    val effects: SharedFlow<LobbyWaitingEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            userStore.data.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun onEvent(event: LobbyWaitingEvent) {

        when (event) {

            is LobbyWaitingEvent.OnLoadLobby -> {
                loadLobby(event.lobbyId)
            }

            LobbyWaitingEvent.OnTurnTimerIncrease -> {
                _uiState.update {
                    it.copy(turnTimer = it.turnTimer + 10)
                }
            }

            LobbyWaitingEvent.OnTurnTimerDecrease -> {
                _uiState.update {
                    if (it.turnTimer > 10) {
                        it.copy(turnTimer = it.turnTimer - 10)
                    } else it
                }
            }

            LobbyWaitingEvent.OnStartingCardsIncrease -> {
                _uiState.update {
                    it.copy(startingCards = it.startingCards + 1)
                }
            }

            LobbyWaitingEvent.OnStartingCardsDecrease -> {
                _uiState.update {
                    if (it.startingCards > 1) {
                        it.copy(startingCards = it.startingCards - 1)
                    } else it
                }
            }

            is LobbyWaitingEvent.OnStackToggle -> {
                _uiState.update {
                    it.copy(stackEnabled = event.enabled)
                }
            }

            LobbyWaitingEvent.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateBack)
                }
            }

            LobbyWaitingEvent.OnSettingsClicked -> {
                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateToSettings)
                }
            }

            LobbyWaitingEvent.onMatchStart -> {
               startMatch()
            }

            LobbyWaitingEvent.OnSettings -> {
                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateToSettings)
                }
            }
            LobbyWaitingEvent.OnBack -> {
                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateBack)
                }
            }

        }
    }

    private fun loadLobby(lobbyId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }

            try {
                val lobby: Lobby = api.getLobby(lobbyId).toDomain()

                _uiState.update {
                    it.copy(
                        loadState = LoadState.Success,
                        lobby = lobby,

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

    private fun startMatch() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }

            val user = userStore.data.first()
            val state = _uiState.value


            try {
                val result = api.startMatch(user.uid, state.lobby?.lobbyId ?: " ")
                _uiState.update {
                    it.copy(
                        loadState = LoadState.Success

                        )
                }

                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateToMatch)
                }

            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }
}


