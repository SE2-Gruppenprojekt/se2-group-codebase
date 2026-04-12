package at.aau.serg.android.ui.screens.lobby

import at.aau.serg.android.data.lobby.mapper.toDomain
import at.aau.serg.android.network.RetrofitProvider
import at.aau.serg.android.network.lobby.LobbyAPI
import at.aau.serg.android.network.lobby.LobbyService
import at.aau.serg.android.session.AppSession
import at.aau.serg.android.util.DefaultDispatcherProvider
import at.aau.serg.android.util.DispatcherProvider
import at.aau.serg.android.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import shared.models.lobby.domain.Lobby
import shared.models.lobby.request.CreateLobbyRequest
import shared.models.lobby.request.JoinLobbyRequest
import shared.models.lobby.response.LobbyListItemResponse

class LobbyViewModel(
    private val api: LobbyAPI = LobbyAPI(
        RetrofitProvider.retrofit.create(LobbyService::class.java)
    ),
    dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : BaseViewModel(dispatchers) {

    private val _state = MutableStateFlow<LobbyUiState>(LobbyUiState.Loading)
    val state = _state.asStateFlow()

    private val _lobbiesState = MutableStateFlow<LobbyUiState>(LobbyUiState.Loading)
    val lobbiesState = _lobbiesState.asStateFlow()

    private val _lobby = MutableStateFlow<Lobby?>(null)
    val lobby = _lobby.asStateFlow()

    private val _lobbies = MutableStateFlow<List<LobbyListItemResponse>>(emptyList())
    val lobbies = _lobbies.asStateFlow()


    fun loadLobbies(onError: () -> Unit = {}) {
        _lobbiesState.value = LobbyUiState.Loading

        launchRequest(
            request = { api.getLobbies() },
            onSuccess = { loaded -> _lobbies.value = loaded },
            onError = { onError() }
            /*onSuccess = { loaded -> _lobbiesState.value = LobbyUiState.Success(loaded)
             },
            onError = {
                _lobbiesState.value = LobbyUiState.Error("Could not load lobby")
            }*/
        )
    }

    fun loadLobby(lobbyId: String) {
        _state.value = LobbyUiState.Loading

        launchRequest(
            request = { api.getLobby(lobbyId).toDomain() },
            onSuccess = { loaded -> _lobby.value = loaded },
            onError = {}
        )
            /*onSuccess = { lobby -> _state.value = LobbyUiState.Success(lobby) },
            onError = {
                _state.value = LobbyUiState.Error("Could not load lobby")
            }
        )*/
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

        _state.value = LobbyUiState.Loading

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
            onSuccess = { lobby -> _state.value = LobbyUiState.Success(lobby)
                onSuccess(lobby) },
            onError = { _state.value = LobbyUiState.Error("Could not create lobby") }
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
            /*onSuccess = { loaded ->
                _lobbiesState.value = LobbiesUiState.Success(loaded)
            },
            onError = { _state.value = LobbyUiState.Error("Could not join lobby")

            }
        )*/
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
