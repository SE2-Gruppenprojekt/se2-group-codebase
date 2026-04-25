package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.screens.lobby.create.LobbyCreateViewModel
import at.aau.serg.android.ui.theme.ThemeState
import shared.models.lobby.domain.Lobby


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

    val players = uiState.lobby?.players ?: emptyList()
    val joinedCount = uiState.lobby?.players?.size
    val maxPlayers = uiState.lobby?.settings?.maxPlayers ?: 0
    val roomCode = uiState.lobby?.lobbyId?.take(6)?.uppercase() ?: "------"

    val gradientTop = if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF5F7FB)
    val gradientBottom = if (darkMode) MaterialTheme.colorScheme.surface else Color(0xFFEAEFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradientTop, gradientBottom)))
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Room Code: $roomCode",
                modifier = Modifier.testTag(LobbyWaitingTestTags.ROOM_CODE)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Players: $joinedCount / $maxPlayers",
                modifier = Modifier.testTag(LobbyWaitingTestTags.PLAYER_LIST)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Turn Timer: ${uiState.turnTimer}",
                modifier = Modifier.testTag(LobbyWaitingTestTags.TURN_TIMER_VALUE)
            )

            Row {
                Button(
                    onClick = { onEvent(LobbyWaitingEvent.OnTurnTimerDecrease) },
                    modifier = Modifier.testTag(LobbyWaitingTestTags.TURN_TIMER_MINUS)
                ) {
                    Text("-")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { onEvent(LobbyWaitingEvent.OnTurnTimerIncrease) },
                    modifier = Modifier.testTag(LobbyWaitingTestTags.TURN_TIMER_PLUS)
                ) {
                    Text("+")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Starting Cards: ${uiState.startingCards}",
                modifier = Modifier.testTag(LobbyWaitingTestTags.STARTING_CARDS_VALUE)
            )

            Row {
                Button(
                    onClick = { onEvent(LobbyWaitingEvent.OnStartingCardsDecrease) },
                    modifier = Modifier.testTag(LobbyWaitingTestTags.STARTING_CARDS_MINUS)
                ) {
                    Text("-")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { onEvent(LobbyWaitingEvent.OnStartingCardsIncrease) },
                    modifier = Modifier.testTag(LobbyWaitingTestTags.STARTING_CARDS_PLUS)
                ) {
                    Text("+")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text(
                    text = "Stack Enabled: ${uiState.stackEnabled}",
                    modifier = Modifier.testTag(LobbyWaitingTestTags.SETTINGS_SECTION)
                )

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = uiState.stackEnabled,
                    onCheckedChange = {
                        onEvent(LobbyWaitingEvent.OnStackToggle(it))
                    },
                    modifier = Modifier.testTag(LobbyWaitingTestTags.STACK_SWITCH)
                )
            }
        }

        if (uiState.lobby?.hostUserId == uiState.user?.uid) {
            Button(
                onClick = { onEvent(LobbyWaitingEvent.onMatchStart) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
                    .testTag("waiting_start_game_button")
            ) {
                Text(
                    text = "Start Game",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

}
