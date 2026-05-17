package at.aau.serg.android.ui.screens.lobby.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.android.core.datastore.ProtoStore
import at.aau.serg.android.core.network.RetrofitProvider
import at.aau.serg.android.core.network.lobby.LobbyAPI
import at.aau.serg.android.core.network.mapper.NetworkErrorMapper
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
    private val api: LobbyAPI = RetrofitProvider.retrofit.create(LobbyAPI::class.java)
    ) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyCreateUiState())
    val uiState: StateFlow<LobbyCreateUiState> = _uiState

    private val _effects = MutableSharedFlow<LobbyCreateEffect>()
    val effects = _effects.asSharedFlow()

    private fun createLobby() = viewModelScope.launch {
        _uiState.update {
            it.copy(loadState = LoadState.Loading)
        }

        val user = userStore.data.first()
        val state = _uiState.value

        try {
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
                it.copy(loadState = LoadState.Success)
            }

            _effects.emit(
                LobbyCreateEffect.NavigateToWaitingRoom(lobby.lobbyId)
            )

        } catch (e: Throwable) {
            val appError = NetworkErrorMapper.map(e)

            _uiState.update {
                it.copy(loadState = LoadState.Error(appError))
            }
        }
    }

    fun onEvent(event: LobbyCreateEvent) {
        when (event) {
            LobbyCreateEvent.OnSettings -> {
                viewModelScope.launch {
                    _effects.emit(LobbyCreateEffect.NavigateToSettings)
                }
            }

            LobbyCreateEvent.OnBack -> {
                viewModelScope.launch {
                    _effects.emit(LobbyCreateEffect.NavigateBack)
                }
            }

            LobbyCreateEvent.CreateLobby -> {
                createLobby()
            }

            is LobbyCreateEvent.SetMaxPlayers -> {
                _uiState.update {
                    it.copy(maxPlayers = event.value)
                }
            }

            is LobbyCreateEvent.SetIsPrivate -> {
                _uiState.update {
                    it.copy(isPrivate = event.value)
                }
            }

            is LobbyCreateEvent.SetQuickMode -> {
                _uiState.update {
                    it.copy(quickMode = event.value)
                }
            }

            is LobbyCreateEvent.SetRequireInitialMeld -> {
                _uiState.update {
                    it.copy(requireInitialMeld = event.value)
                }
            }

            is LobbyCreateEvent.ChangeTurnTimer -> {
                _uiState.update {
                    it.copy(
                        turnTimer = (it.turnTimer + event.delta)
                            .coerceAtLeast(0)
                    )
                }
            }

            is LobbyCreateEvent.ChangeStartingTiles -> {
                _uiState.update {
                    it.copy(
                        startingTiles = (it.startingTiles + event.delta)
                            .coerceAtLeast(0)
                    )
                }
            }

            is LobbyCreateEvent.ChangeWinScore -> {
                _uiState.update {
                    it.copy(
                        winScore = (it.winScore + event.delta)
                            .coerceAtLeast(0)
                    )
                }
            }
        }
    }
}
