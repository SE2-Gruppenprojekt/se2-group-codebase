package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.lobby.main.LobbyViewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.waiting.components.*
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState.lobbyName
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState.stackEnabled
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState.startingCards
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState.turnTimer
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun WaitingRoomScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onStartGame: () -> Unit = {},
    lobbyId: String? = null,
    userId: String = "",
    viewModel: LobbyViewModel
) {
    val scrollState = rememberScrollState()
    val darkMode = ThemeState.isDarkMode.value
    val context = LocalContext.current

    val lobby by viewModel.lobby.collectAsState()
    val isWebSocketConnected by viewModel.isWebSocketConnected.collectAsState()

    LaunchedEffect(lobbyId) {
        lobbyId?.let { viewModel.loadLobby(it) }
    }

    val players = lobby?.players ?: emptyList()
    val joinedCount = players.size
    val maxPlayers = lobby?.settings?.maxPlayers ?: 0
    val roomCode = lobby?.lobbyId?.take(6)?.uppercase() ?: "------"

    val isLoading = lobby == null

    val gradientTop = if (darkMode) {
        MaterialTheme.colorScheme.background
    } else Color(0xFFF5F7FB)

    val gradientBottom = if (darkMode) {
        MaterialTheme.colorScheme.surface
    } else Color(0xFFEAEFFF)

    val cardColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val buttonColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(gradientTop, gradientBottom))
            )
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        TopBar(
            subtitle = lobbyName.value,
            onBack = onBack,
            onSettings = onSettings
        )

        Spacer(Modifier.height(12.dp))

        WaitingScreenRoomCard(
            roomCode = roomCode,
            joinedCount = joinedCount,
            maxPlayers = maxPlayers,
            context = context,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            cardColor = cardColor
        )

        Spacer(Modifier.height(16.dp))

        WaitingScreenPlayerSection(
            isLoading = isLoading,
            players = players,
            fetchedLobby = lobby,
            maxPlayers = maxPlayers,
            joinedCount = joinedCount,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            darkMode = darkMode
        )

        Spacer(Modifier.height(18.dp))

        WaitingScreenSettingsSection(
            turnTimer = turnTimer,
            startingCards = startingCards,
            stackEnabled = stackEnabled,

            onTurnTimerMinus = {
                if (LobbyUiState.turnTimer.intValue > 10) {
                    LobbyUiState.turnTimer.intValue -= 10
                }
            },
            onTurnTimerPlus = {
                LobbyUiState.turnTimer.intValue += 10
            },

            onStartingCardsMinus = {
                if (LobbyUiState.startingCards.intValue > 1) {
                    LobbyUiState.startingCards.intValue -= 1
                }
            },
            onStartingCardsPlus = {
                LobbyUiState.startingCards.intValue += 1
            },

            onStackToggle = {
                LobbyUiState.stackEnabled.value = it
            },

            cardColor = cardColor,
            primaryTextColor = primaryTextColor,
            buttonColor = buttonColor
        )

        if (lobby?.hostUserId == userId) {
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStartGame,
                enabled = isWebSocketConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9D3CFF),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Start Game",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
