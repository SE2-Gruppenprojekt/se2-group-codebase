package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState.lobbyName
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenPlayerSection
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenRoomCard
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenSettingsSection
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun LobbyWaitingScreen(
    viewModel: LobbyWaitingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LobbyWaitingScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun LobbyWaitingScreenContent(
    uiState: LobbyWaitingUiState,
    onEvent: (LobbyWaitingEvent) -> Unit
) {
    val scrollState = rememberScrollState()
    val darkMode = ThemeState.isDarkMode.value
    val context = LocalContext.current

    val players = uiState.lobby?.players ?: emptyList()
    val joinedCount = players.size
    val maxPlayers = uiState.lobby?.settings?.maxPlayers ?: 0
    val roomCode = uiState.lobby?.lobbyId?.take(6)?.uppercase() ?: "------"

    val gradientTop = if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF5F7FB)
    val gradientBottom = if (darkMode) MaterialTheme.colorScheme.surface else Color(0xFFEAEFFF)

    val cardColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val buttonColor = if (darkMode) Color(0xFF2A3552) else Color(0xFF2F3A57)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradientTop, gradientBottom)))
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag(LobbyWaitingTestTags.SCREEN)
    ) {

        TopBar(
            subtitle = lobbyName.value,
            onBack = { onEvent(LobbyWaitingEvent.OnBack) },
            onSettings = { onEvent(LobbyWaitingEvent.OnSettings) }
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
            onEvent = onEvent,
            localId = uiState.user?.uid ?: "",
            isLoading = uiState.loadState == LoadState.Loading,
            players = players,
            fetchedLobby = uiState.lobby,
            maxPlayers = maxPlayers,
            joinedCount = joinedCount,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            darkMode = darkMode,
        )

        Spacer(Modifier.height(18.dp))

        WaitingScreenSettingsSection(
            turnTimer = uiState.turnTimer,
            startingCards = uiState.startingCards,
            stackEnabled = uiState.stackEnabled,
            onTurnTimerMinus = { onEvent(LobbyWaitingEvent.OnTurnTimerDecrease) },
            onTurnTimerPlus = { onEvent(LobbyWaitingEvent.OnTurnTimerIncrease) },
            onStartingCardsMinus = { onEvent(LobbyWaitingEvent.OnStartingCardsDecrease) },
            onStartingCardsPlus = { onEvent(LobbyWaitingEvent.OnStartingCardsIncrease) },
            onStackToggle = { onEvent(LobbyWaitingEvent.OnStackToggle(it)) },
            cardColor = cardColor,
            primaryTextColor = primaryTextColor,
            buttonColor = buttonColor
        )

        Spacer(Modifier.height(24.dp))

        // --- start button (just for host) ---
        if (uiState.lobby?.hostUserId == uiState.user?.uid) {
            Button(
                onClick = { onEvent(LobbyWaitingEvent.onMatchStart) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag(LobbyWaitingTestTags.START_BUTTON),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9D3CFF),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start Game",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
