package at.aau.serg.android.ui.screens.lobby

import at.aau.serg.android.data.lobby.mapper.toDomain
import at.aau.serg.android.network.RetrofitProvider
import at.aau.serg.android.network.lobby.LobbyAPI
import at.aau.serg.android.network.lobby.LobbyService
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

    private val _lobby = MutableStateFlow<Lobby?>(null)
    val lobby = _lobby.asStateFlow()

    private val _lobbies = MutableStateFlow<List<LobbyListItemResponse>>(emptyList())
    val lobbies = _lobbies.asStateFlow()

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
        userId: String = "test",
        displayName: String = "tester",
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
        userId: String = "test",
        displayName: String = "Player",
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

    fun leaveLobby(
        lobbyId: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        launchRequest(
            request = { api.leaveLobby("test", lobbyId) },
            onSuccess = { success ->
                if (success) onSuccess() else onError()
            },
            onError = { onError() }
        )
    }
}
