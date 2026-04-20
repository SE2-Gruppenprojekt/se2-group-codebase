package at.aau.serg.android.ui.screens.lobby.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.viewmodel.BaseViewModel
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyService
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import at.aau.serg.android.ui.lobby.LobbiesUiState
import at.aau.serg.android.ui.lobby.LobbyUiStateLoading
import at.aau.serg.android.util.DefaultDispatcherProvider
import at.aau.serg.android.util.DispatcherProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shared.models.lobby.domain.Lobby
import shared.models.lobby.request.CreateLobbyRequest
import shared.models.lobby.request.JoinLobbyRequest
import shared.models.lobby.response.LobbyListItemResponse

class LobbyViewModel(
    private val api: LobbyAPI = LobbyAPI(
        RetrofitProvider.retrofit.create(LobbyService::class.java)
    ),
    private val webSocketService: LobbyWebSocketService = LobbyWebSocketService(),
    dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : BaseViewModel(dispatchers) {

    private val _state = MutableStateFlow<LobbyUiStateLoading>(LobbyUiStateLoading.Loading)
    val state = _state.asStateFlow()

    private val _lobbiesState = MutableStateFlow<LobbiesUiState>(LobbiesUiState.Loading)
    val lobbiesState = _lobbiesState.asStateFlow()

    private val _lobby = MutableStateFlow<Lobby?>(null)
    val lobby = _lobby.asStateFlow()

    private val _lobbies = MutableStateFlow<List<LobbyListItemResponse>>(emptyList())
    val lobbies = _lobbies.asStateFlow()

    // WebSocket-State
    private val _isDeleted = MutableStateFlow(false)
    val isDeleted = _isDeleted.asStateFlow()

    private val _matchId = MutableStateFlow<String?>(null)
    val matchId = _matchId.asStateFlow()

    private var webSocketJob: Job? = null

    // connect WebSocket
    fun connectWebSocket(lobbyId: String) {
        webSocketJob?.cancel()
        webSocketJob = viewModelScope.launch(dispatchers.io) {
            try {
                webSocketService.connect(
                    lobbyId = lobbyId,
                    onLobbyUpdated = { payload ->
                        _lobby.value = payload.lobby.toDomain()
                    },
                    onLobbyDeleted = {
                        _isDeleted.value = true
                    },
                    onLobbyStarted = { payload ->
                        _matchId.value = payload.matchId
                    }
                )
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "WebSocket-Error", e)
            }
        }
    }

    // clean up when screen is exited
    override fun onCleared() {
        webSocketJob?.cancel()
        viewModelScope.launch {
            webSocketService.disconnect()
        }
        super.onCleared()
    }

    fun loadLobbies(onError: () -> Unit = {}) {
        _lobbiesState.value = LobbiesUiState.Loading
        launchRequest(
            request = { api.getLobbies() },
            onSuccess = { loaded ->
                _lobbies.value = loaded
                _lobbiesState.value = LobbiesUiState.Success(loaded)
            },
            onError = {
                _lobbiesState.value = LobbiesUiState.Error("Could not load lobbies")
                onError()
            }
        )
    }

    fun loadLobby(lobbyId: String) {
        _state.value = LobbyUiStateLoading.Loading
        launchRequest(
            request = { api.getLobby(lobbyId).toDomain() },
            onSuccess = { lobby ->
                _lobby.value = lobby
                _state.value = LobbyUiStateLoading.Success(lobby)
            },
            onError = {
                _state.value = LobbyUiStateLoading.Error("Could not load lobby")
            }
        )
    }

    fun createLobby(
        userId: String,
        displayName: String,
        maxPlayers: Int = 4,
        isPrivate: Boolean = false,
        allowGuests: Boolean = true,
        onSuccess: (Lobby) -> Unit = {},
        onError: () -> Unit = {}
    ) {
        _state.value = LobbyUiStateLoading.Loading
        launchRequest(
            request = {
                api.createLobby(
                    userId,
                    CreateLobbyRequest(displayName, maxPlayers, isPrivate, allowGuests)
                ).toDomain()
            },
            onSuccess = { lobby ->
                _state.value = LobbyUiStateLoading.Success(lobby)
                onSuccess(lobby)
            },
            onError = {
                _state.value = LobbyUiStateLoading.Error("Could not create lobby")
                onError()
            }
        )
    }

    fun joinLobby(
        lobbyId: String,
        userId: String,
        displayName: String,
        onSuccess: (Lobby) -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = { api.joinLobby(lobbyId, JoinLobbyRequest(userId, displayName)).toDomain() },
            onSuccess = { lobby -> onSuccess(lobby) },
            onError = { onError() }
        )
    }

    fun joinLobbyOrOpen(
        lobbyId: String,
        userId: String,
        displayName: String,
        onSuccess: (Lobby) -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = {
                val existing = api.getLobby(lobbyId).toDomain()
                if (existing.players.any { it.userId == userId }) existing
                else api.joinLobby(lobbyId, JoinLobbyRequest(userId, displayName)).toDomain()
            },
            onSuccess = { lobby ->
                _lobby.value = lobby
                onSuccess(lobby) },
            onError = { onError() }
        )
    }

    fun leaveLobby(
        lobbyId: String,
        userId: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = { api.leaveLobby(userId, lobbyId) },
            onSuccess = { if (it) onSuccess() else onError() },
            onError = { onError() }
        )
    }

    fun startMatch(
        lobbyId: String,
        userId: String,
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = { api.startMatch(userId, lobbyId) },
            onSuccess = { },
            onError = { onError() }
        )
    }

}
