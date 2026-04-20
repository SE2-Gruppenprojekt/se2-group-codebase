package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.lobby.main.LobbyViewModel
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenPlayerSection
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenRoomCard
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenSettingsSection
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenTopBar
import at.aau.serg.android.ui.theme.ThemeState
import kotlin.random.Random

@Composable
fun WaitingRoomScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    lobbyId: String? = null,
    viewModel: LobbyViewModel? = null
) {
    val scrollState = rememberScrollState()
    val darkMode = ThemeState.isDarkMode.value

    val fetchedLobbyState = viewModel?.lobby?.collectAsState()
    val fetchedLobby = fetchedLobbyState?.value

    LaunchedEffect(lobbyId) {
        if (lobbyId != null && viewModel != null) {
            viewModel.loadLobby(lobbyId)
        }
    }

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
    val joinedCount = players.size
    val isLoading = lobbyId != null && fetchedLobby == null

    val gradientTop = if (darkMode) {
        MaterialTheme.colorScheme.background
    } else {
        Color(0xFFF5F7FB)
    }

    val gradientBottom = if (darkMode) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFEAEFFF)
    }

    val cardColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(gradientTop, gradientBottom))
            )
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {


        WaitingScreenTopBar(
            onBack = onBack,
            onSettings = onSettings,
            lobbyName = lobbyName
        )

        Spacer(Modifier.height(12.dp))

        WaitingScreenRoomCard(
            roomCode = roomCode,
            joinedCount = joinedCount,
            maxPlayers = maxPlayers,
            context = LocalContext.current,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            cardColor = cardColor
        )

        Spacer(Modifier.height(16.dp))

        WaitingScreenPlayerSection(
            isLoading = isLoading,
            players = players,
            fetchedLobby = fetchedLobby,
            maxPlayers = maxPlayers,
            joinedCount = joinedCount,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            darkMode = darkMode
        )

        Spacer(Modifier.height(18.dp))


        val buttonColor = MaterialTheme.colorScheme.primary
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

        Spacer(Modifier.height(24.dp))
    }
}

fun generateRoomCode(length: Int = 6): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
}
