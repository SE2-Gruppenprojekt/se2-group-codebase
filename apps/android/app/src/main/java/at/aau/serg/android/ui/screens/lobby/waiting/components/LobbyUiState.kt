package at.aau.serg.android.ui.screens.lobby.waiting.components

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

object LobbyUiState {
    val lobbyName = mutableStateOf("Waiting Room")
    val maxPlayers = mutableIntStateOf(8)
    val turnTimer = mutableIntStateOf(60)
    val startingCards = mutableIntStateOf(7)
    val stackEnabled = mutableStateOf(true)
    val roomCode = mutableStateOf("")
}
