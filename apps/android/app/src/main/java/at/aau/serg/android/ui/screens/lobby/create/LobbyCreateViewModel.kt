package at.aau.serg.android.ui.screens.lobby.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.lobby.LobbyService
import at.aau.serg.android.core.network.lobby.LobbyWebSocketService
import at.aau.serg.android.core.network.mapper.toDomain
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.state.LoadState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shared.models.lobby.request.CreateLobbyRequest

class LobbyCreateViewModel(
    private val userStore: ProtoStore<User>,
    private val api: LobbyAPI = LobbyAPI(
        RetrofitProvider.retrofit.create(LobbyService::class.java)
    )
    ) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyCreateUiState())
    val uiState: StateFlow<LobbyCreateUiState> = _uiState

    private val _effects = MutableSharedFlow<LobbyCreateEffect>()
    val effects = _effects.asSharedFlow()

    private fun createLobby() = viewModelScope.launch {
        val user = userStore.data.first()
        val state = _uiState.value

        _uiState.update {
            it.copy(loadState = LoadState.Loading)
        }

        val lobby = api.createLobby(
            user.uid,
            CreateLobbyRequest(
                displayName = user.displayName,
                maxPlayers = state.maxPlayers,
                isPrivate = state.isPrivate,
                allowGuests = true
            )
        ).toDomain()

        _uiState.update {
            it.copy(
                loadState = LoadState.Success
            )
        }

        _effects.emit(
            LobbyCreateEffect.NavigateToWaitingRoom(lobby.lobbyId)
        )
    }

    fun onEvent(event: LobbyCreateEvent) {
        when (event) {

            is LobbyCreateEvent.SetMaxPlayers ->
                _uiState.update { it.copy(maxPlayers = event.value) }

            is LobbyCreateEvent.SetIsPrivate ->
                _uiState.update { it.copy(isPrivate = event.value) }

            LobbyCreateEvent.CreateLobby -> {
                createLobby()
            }
        }
    }
}
