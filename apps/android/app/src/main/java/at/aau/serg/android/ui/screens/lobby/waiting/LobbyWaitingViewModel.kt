package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.user.UserStore
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.ServiceLocator
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.util.ErrorUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyStatus
import shared.models.lobby.event.LobbyEvent

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyWaitingViewModel(
    private val userStore: UserStore,
    private val api: LobbyAPI = RetrofitProvider.retrofit.create(LobbyAPI::class.java),
    private val socket : LobbyWebSocketService = ServiceLocator.lobbyWebSocketService
) : ViewModel() {
    private var socketJob: Job? = null
    private val _uiState = MutableStateFlow(LobbyWaitingUiState())
    val uiState: StateFlow<LobbyWaitingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LobbyWaitingEffect>(Channel.BUFFERED)
    val effects: Flow<LobbyWaitingEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            userStore.data.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    @VisibleForTesting
    internal fun startSocket(lobbyId: String) {
        socketJob?.cancel()

        socketJob = viewModelScope.launch {
            socket.subscribe(lobbyId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            loadState = LoadState.Error(NetworkErrorMapper.map(exception))
                        )
                    }
                }
                .collect { event ->
                    handleLobbyEvent(event)
                }
        }
    }

    internal fun handleLobbyEvent(event: LobbyEvent) {
        when (event) {
            is LobbyEvent.Deleted -> {
                _effect.trySend(LobbyWaitingEffect.NavigateBack)
            }
            is LobbyEvent.Started -> {
                viewModelScope.launch {
                    try {
                        _effect.trySend(LobbyWaitingEffect.NavigateToMatch(event.payload.matchId))
                        userStore.updateGameId(event.payload.matchId)
                    } catch (e : Exception) {
                        val appError = ErrorUiMapper.map(e)

                        _uiState.update {
                            it.copy(loadState = LoadState.Error(appError))
                        }
                    }
                }
            }
            is LobbyEvent.Updated -> {
                applyLobbyState(event.payload.lobby.toDomain())
            }
        }
    }

    private fun applyLobbyState(lobby: Lobby) {
        if (lobby.status == LobbyStatus.CLOSED) {
            _uiState.update {
                it.copy(
                    lobby = lobby,
                    loadState = LoadState.Success
                )
            }
            _effect.trySend(LobbyWaitingEffect.NavigateBack)
            return
        }

        val currentGameId = lobby.currentGameId
        val shouldNavigateToMatch = lobby.status == LobbyStatus.IN_GAME &&
            !currentGameId.isNullOrBlank() &&
            _uiState.value.lobby?.currentGameId != currentGameId

        _uiState.update {
            it.copy(
                lobby = lobby,
                loadState = LoadState.Success
            )
        }

        if (shouldNavigateToMatch) {
            viewModelScope.launch {
                _effect.trySend(LobbyWaitingEffect.NavigateToMatch(currentGameId))
                userStore.updateGameId(currentGameId)
            }
        }
    }

    fun onEvent(event: LobbyWaitingEvent) {
        val state = _uiState.value
        try {
            when (event) {
                is LobbyWaitingEvent.OnLoadLobby -> {
                    startSocket(event.lobbyId)
                    loadLobby(event.lobbyId)
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
                    val user = state.user ?: throw IllegalStateException("User must not be null when attempting to start a match.")
                    val lobby = state.lobby ?: throw IllegalStateException("Lobby must not be null when attempting to start a match.")
                    check(lobby.status == LobbyStatus.OPEN) {
                        "Match can only be started while the lobby is open"
                    }

                    startMatch(lobby.lobbyId)
                }

                LobbyWaitingEvent.OnSettings -> {
                    _effect.trySend(LobbyWaitingEffect.NavigateToSettings)
                }

                LobbyWaitingEvent.OnBack -> {
                    _effect.trySend(LobbyWaitingEffect.NavigateBack)
                }

                is LobbyWaitingEvent.ToggleReadyState -> {
                    val user = uiState.value.user
                        ?: throw IllegalStateException("User must not be null when attempting ready change.")
                    if (user.uid != event.userId) return

                    val lobby = uiState.value.lobby
                        ?: throw IllegalStateException("Lobby must not be null when attempting ready change.")
                    check(lobby.status == LobbyStatus.OPEN) {
                        "You cannot change the ready status, while lobby is not open"
                    }
                    val currentPlayer = lobby.players.find { it.userId == user.uid } ?: throw IllegalStateException("Player must be in lobby to attempt ready change.")

                    requestReadyChange(lobby.lobbyId, !currentPlayer.isReady)
                }
            }
        } catch (e : Exception) {
            val appError = ErrorUiMapper.map(e)

            _uiState.update {
                it.copy(loadState = LoadState.Error(appError))
            }
        }
    }

    private fun requestReadyChange(lobbyId: String, newReadyState: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }

            try {
                if (newReadyState) {
                    applyLobbyState(api.ready(lobbyId = lobbyId).toDomain())
                } else {
                    applyLobbyState(api.unready(lobbyId = lobbyId).toDomain())
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
                if (_uiState.value.lobby == null) {
                    applyLobbyState(lobby)
                }
            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }

    private fun startMatch(lobbyId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadState = LoadState.Loading)
            }

            try {
                applyLobbyState(api.startMatch(lobbyId).toDomain())
            } catch (e: Throwable) {
                val appError = NetworkErrorMapper.map(e)

                _uiState.update {
                    it.copy(loadState = LoadState.Error(appError))
                }
            }
        }
    }

    @VisibleForTesting
    internal fun setUiStateForTest(state: LobbyWaitingUiState) {
        _uiState.value = state
    }
}
