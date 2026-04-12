package at.aau.serg.android.ui.screens.lobby

import android.util.Log
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.data.lobby.mapper.toDomain
import at.aau.serg.android.network.RetrofitProvider
import at.aau.serg.android.network.lobby.LobbyAPI
import at.aau.serg.android.network.lobby.LobbyService
import at.aau.serg.android.network.lobby.LobbyWebSocketService
import at.aau.serg.android.session.AppSession
import at.aau.serg.android.util.DefaultDispatcherProvider
import at.aau.serg.android.util.DispatcherProvider
import at.aau.serg.android.viewmodel.BaseViewModel
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
        super.onCleared()
    }

    fun loadLobbies(onError: () -> Unit = {}) {
        launchRequest(
            request = { api.getLobbies() },
            onSuccess = { loaded -> _lobbies.value = loaded },
            onError = { onError() }
        )
    }

    fun loadLobby(lobbyId: String) {
        launchRequest(
            request = { api.getLobby(lobbyId).toDomain() },
            onSuccess = { loaded -> _lobby.value = loaded },
            onError = {}
        )
    }

    fun createLobby(
        userId: String = AppSession.userId,
        displayName: String = AppSession.displayName,
        maxPlayers: Int = 4,
        isPrivate: Boolean = false,
        allowGuests: Boolean = true,
        onSuccess: (Lobby) -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = {
                api.createLobby(
                    userId,
                    CreateLobbyRequest(
                        displayName = displayName,
                        maxPlayers = maxPlayers,
                        isPrivate = isPrivate,
                        allowGuests = allowGuests
                    )
                ).toDomain()
            },
            onSuccess = { lobby -> onSuccess(lobby) },
            onError = { onError() }
        )
    }

    fun joinLobby(
        lobbyId: String,
        userId: String = AppSession.userId,
        displayName: String = AppSession.displayName,
        onSuccess: (Lobby) -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = {
                api.joinLobby(
                    lobbyId,
                    JoinLobbyRequest(
                        userId = userId,
                        displayName = displayName
                    )
                ).toDomain()
            },
            onSuccess = { lobby -> onSuccess(lobby) },
            onError = { onError() }
        )
    }

    fun joinLobbyOrOpen(
        lobbyId: String,
        userId: String = AppSession.userId,
        displayName: String = AppSession.displayName,
        onSuccess: (Lobby) -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = {
                val existingLobby = api.getLobby(lobbyId).toDomain()
                if (existingLobby.players.any { it.userId == userId }) {
                    existingLobby
                } else {
                    api.joinLobby(
                        lobbyId,
                        JoinLobbyRequest(
                            userId = userId,
                            displayName = displayName
                        )
                    ).toDomain()
                }
            },
            onSuccess = { lobby -> onSuccess(lobby) },
            onError = { onError() }
        )
    }

    fun leaveLobby(
        lobbyId: String,
        userId: String = AppSession.userId,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = { api.leaveLobby(userId, lobbyId) },
            onSuccess = { success ->
                if (success) onSuccess() else onError()
            },
            onError = { onError() }
        )
    }
}
