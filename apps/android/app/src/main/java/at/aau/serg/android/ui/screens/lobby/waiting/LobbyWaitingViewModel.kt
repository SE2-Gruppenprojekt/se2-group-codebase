package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import at.aau.serg.android.ui.screens.lobby.main.LobbyViewModel

class LobbyWaitingViewModel(
    private val lobbyViewModel: LobbyViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyWaitingUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LobbyWaitingEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeLobby()
        observeMatchStart()
        observeLobbyDeleted()
    }

    private fun observeLobby() {
        viewModelScope.launch {
            lobbyViewModel.lobby.collect { lobby ->
                _uiState.update { current ->
                    current.copy(
                        lobbyName = lobby?.lobbyId ?: "",
                        players = lobby?.players ?: emptyList(),
                        isLoading = lobby == null
                    )
                }
            }
        }
    }

    private fun observeMatchStart() {
        viewModelScope.launch {
            lobbyViewModel.matchId.collect { matchId ->
                if (matchId != null) {
                    _effect.emit(
                        LobbyWaitingEffect.NavigateToMatch(matchId)
                    )
                }
            }
        }
    }

    private fun observeLobbyDeleted() {
        viewModelScope.launch {
            lobbyViewModel.isDeleted.collect { deleted ->
                if (deleted) {
                    _effect.emit(LobbyWaitingEffect.NavigateBack)
                }
            }
        }
    }

    fun onEvent(event: LobbyWaitingEvent) {
        when (event) {

            is LobbyWaitingEvent.OnLoadLobby -> {
                lobbyViewModel.loadLobby(event.lobbyId)
                lobbyViewModel.connectWebSocket(event.lobbyId)
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
                // optional
            }
        }
    }
}
