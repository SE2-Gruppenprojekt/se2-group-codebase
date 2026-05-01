package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.ServiceLocator
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyEvent
import at.aau.serg.android.core.network.lobby.LobbyService
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shared.models.lobby.domain.Lobby

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyWaitingViewModel(
    private val userStore: ProtoStore<User>,
    private val api: LobbyAPI = LobbyAPI(
        RetrofitProvider.retrofit.create(LobbyService::class.java)
    ),
    private val socket : LobbyWebSocketService = ServiceLocator.lobbyWebSocketService
) : ViewModel() {
    private var socketJob: Job? = null
    private val _uiState = MutableStateFlow(LobbyWaitingUiState())
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

    private fun startSocket(lobbyId: String) {
        socketJob?.cancel()

        socketJob = viewModelScope.launch {
            socket.subscribe(lobbyId)
                .collect { handleLobbyEvent(it) }
        }
    }

    fun handleLobbyEvent(event: LobbyEvent) {
        when (event) {
            is LobbyEvent.Deleted -> {
                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateBack)
                }
            }
            is LobbyEvent.Started -> {
                viewModelScope.launch {
                    _effect.emit(LobbyWaitingEffect.NavigateToMatch(event.payload.matchId))
                }
            }
            is LobbyEvent.Updated -> {
                val lobby = event.payload.lobby.toDomain()

                _uiState.update {
                    it.copy(lobby = lobby)
                }
            }
        }
    }

    fun onEvent(event: LobbyWaitingEvent) {
        when (event) {
            is LobbyWaitingEvent.OnLoadLobby -> {
                loadLobby(event.lobbyId)
                startSocket(event.lobbyId)
            }

            LobbyWaitingEvent.OnTurnTimerIncrease -> {
                _uiState.update {
                    it.copy(turnTimer = it.turnTimer + 10)
                }
            }

            LobbyWaitingEvent.OnTurnTimerDecrease -> {
                _uiState.update { state ->
                    val newValue = (state.turnTimer - 10).coerceAtLeast(10)
                    state.copy(turnTimer = newValue)
                }
            }

            LobbyWaitingEvent.OnStartingCardsIncrease -> {
                _uiState.update {
                    it.copy(startingCards = it.startingCards + 1)
                }
            }

            LobbyWaitingEvent.OnStartingCardsDecrease -> {
                _uiState.update { state ->
                    val newValue = (state.startingCards - 1).coerceAtLeast(1)
                    state.copy(startingCards = newValue)
                }
            }

            is LobbyWaitingEvent.OnStackToggle -> {
                _uiState.update {
                    it.copy(stackEnabled = event.enabled)
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

            is LobbyWaitingEvent.ToggleReadyState -> {
                val userId = uiState.value.user?.uid ?: return
                if (userId != event.userId) return

                val lobby = uiState.value.lobby ?: return
                val currentPlayer = lobby.players.find { it.userId == userId } ?: return

                requestReadyChange(lobby.lobbyId, !currentPlayer.isReady)
            }
        }
    }

    private fun requestReadyChange(lobbyId: String, newReadyState: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }

            val user = userStore.data.first()

            try {
                if (newReadyState) {
                    api.ready(
                        userId = user.uid,
                        lobbyId = lobbyId
                    )
                } else {
                    api.unready(
                        userId = user.uid,
                        lobbyId = lobbyId
                    )
                }

                _uiState.update {
                    it.copy(loadState = LoadState.Success)
                }

            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
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
                api.startMatch(user.uid, state.lobby?.lobbyId ?: " ")
                _uiState.update {
                    it.copy(loadState = LoadState.Success)
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


