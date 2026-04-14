package at.aau.serg.android.ui.screens.waiting

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.lobby.LobbyUiState
import at.aau.serg.android.ui.screens.lobby.LobbyViewModel
import at.aau.serg.android.ui.screens.waiting.components.*
import at.aau.serg.android.ui.theme.ThemeState
import kotlin.random.Random

@Composable
fun WaitingRoomScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onGameStarted: () -> Unit = {},
    lobbyId: String? = null,
    viewModel: LobbyViewModel? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()


    val darkMode = ThemeState.isDarkMode.value

    val fetchedLobbyState = viewModel?.lobby?.collectAsState()
    val fetchedLobby = fetchedLobbyState?.value

    // observe WebSocket-State
    val isDeleted by viewModel?.isDeleted?.collectAsState() ?: remember { mutableStateOf(false) }
    val matchId by viewModel?.matchId?.collectAsState() ?: remember { mutableStateOf(null) }


    LaunchedEffect(lobbyId) {
        if (lobbyId != null && viewModel != null) {
            viewModel.loadLobby(lobbyId) // REST: immediate initial state
            viewModel.connectWebSocket(lobbyId) // WebSocket: live updates
        }
    }

    // Lobby deleted → go back
    LaunchedEffect(isDeleted) {
        if (isDeleted) onBack()
    }

    // game started → navigate on
    LaunchedEffect(matchId) {
        if (matchId != null) onGameStarted()
    }

    // shared lobby state
    val fallbackLobbyName by LobbyUiState.lobbyName
    val fallbackMaxPlayers by LobbyUiState.maxPlayers
    // Lobby state
    val lobbyName by LobbyUiState.lobbyName
    val maxPlayers by LobbyUiState.maxPlayers
    val turnTimer by LobbyUiState.turnTimer
    val startingCards by LobbyUiState.startingCards
    val stackEnabled by LobbyUiState.stackEnabled

    if (LobbyUiState.roomCode.value.isBlank() && lobbyId == null) {
        LobbyUiState.roomCode.value = generateRoomCode()
    }
    val roomCode by LobbyUiState.roomCode

    val players = fetchedLobby?.players ?: emptyList()
    val joinedCount = if (fetchedLobby != null) players.size else 2
    val isLoading = lobbyId != null && fetchedLobby == null

    // Colors (minimal, safe)
    val gradientTop = if (darkMode) Color(0xFF0F172A) else Color(0xFFF5F7FB)
    val gradientBottom = if (darkMode) Color(0xFF111827) else Color(0xFFEAEFFF)

    val activePlayerBg = if (darkMode) Color(0xFF1F356A) else Color(0xFFEAF1FF)
    val activePlayerBorder = if (darkMode) Color(0xFF3E73E8) else Color(0xFF4C84FF)

    val secondPlayerBg = if (darkMode) Color(0xFF1E3A2D) else Color(0xFFEAFBF1)
    val secondPlayerBorder = if (darkMode) Color(0xFF2BC46D) else Color(0xFF20C76F)

    val waitingBg = if (darkMode) Color(0xFF1F2937) else Color.White

    val primaryText = if (darkMode) Color.White else Color.Black
    val secondaryText = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradientTop, gradientBottom)))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // HEADER
        WaitingRoomHeader(
            lobbyName = if (fetchedLobby != null) "Waiting Room" else lobbyName,
            onBack = onBack,
            onSettings = onSettings
        )

        Spacer(Modifier.height(12.dp))

        // ROOM INFO
        RoomInfoCard(
            roomCode = roomCode,
            joinedCount = joinedCount,
            maxPlayers = maxPlayers,
            onCopy = {
                Toast.makeText(context, "Room Code copied", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(Modifier.height(16.dp))

        // PLAYERS
        if (isLoading) {
            Spacer(Modifier.height(20.dp))
        } else {

            players.forEachIndexed { index, player ->
                PlayerItem(
                    name = player.displayName,
                    subtitle = if (player.isReady) "Ready" else "Not ready",
                    isHost = player.userId == fetchedLobby?.hostUserId,
                    isJoined = true,
                    isPlaceholder = false,
                    borderColor = if (index == 1) secondPlayerBorder else activePlayerBorder,
                    backgroundColor = if (index == 1) secondPlayerBg else activePlayerBg,
                    primaryTextColor = primaryText,
                    secondaryTextColor = secondaryText
                )
            }
        }

        val placeholderCount = (maxPlayers - joinedCount).coerceAtLeast(0)

        repeat(placeholderCount) {
            PlayerItem(
                name = "Waiting for player...",
                subtitle = "",
                isPlaceholder = true,
                borderColor = Color.Gray,
                backgroundColor = waitingBg,
                primaryTextColor = secondaryText,
                secondaryTextColor = secondaryText
            )
        }

        Spacer(Modifier.height(18.dp))

        // SETTINGS
        GameSettingsPanel(
            turnTimer = turnTimer,
            startingCards = startingCards,
            stackEnabled = stackEnabled,

            onTurnMinus = {
                if (LobbyUiState.turnTimer.intValue > 10)
                    LobbyUiState.turnTimer.intValue -= 10
            },
            onTurnPlus = {
                LobbyUiState.turnTimer.intValue += 10
            },

            onCardsMinus = {
                if (LobbyUiState.startingCards.intValue > 1)
                    LobbyUiState.startingCards.intValue -= 1
            },
            onCardsPlus = {
                LobbyUiState.startingCards.intValue += 1
            },

            onStackToggle = {
                LobbyUiState.stackEnabled.value = it
            }
        )

        Spacer(Modifier.height(16.dp))

        // ACTIONS
        WaitingRoomActions(
            onStart = { },
            onInvite = { }
        )

        Spacer(Modifier.height(24.dp))
    }
}

private fun generateRoomCode(length: Int = 6): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}
