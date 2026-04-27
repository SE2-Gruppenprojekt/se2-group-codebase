package at.aau.serg.android.ui.screens.lobby.waiting

import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import at.aau.serg.android.ui.theme.ThemeState
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseScreenContent
import at.aau.serg.android.ui.screens.lobby.browse.LobbyBrowseViewModel


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
    onEvent: (LobbyWaitingEvent) -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val darkMode = ThemeState.isDarkMode.value

    val lobby = uiState.lobby

    val players = lobby?.players ?: emptyList()
    val joinedCount = players.size
    val maxPlayers = lobby?.settings?.maxPlayers ?: 4
    val roomCode = lobby?.lobbyId?.take(6)?.uppercase() ?: "------"

    val gradientTop = if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF5F7FB)
    val gradientBottom = if (darkMode) MaterialTheme.colorScheme.surface else Color(0xFFEAEFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradientTop, gradientBottom)))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // 🔹 TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onEvent(LobbyWaitingEvent.OnBack) },
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }

            Text("Waiting Room")

            IconButton(
                onClick = { onEvent(LobbyWaitingEvent.OnSettings) },
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 ROOM CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Room Code")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            roomCode,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("room_code")
                        )

                        IconButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Room Code", roomCode)
                                )
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                        }
                    }
                }

                Text(
                    "$joinedCount / $maxPlayers",
                    modifier = Modifier.testTag("player_count")
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 PLAYER LIST
        players.forEach {
            Text(it.displayName)
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 SETTINGS

        Text(
            "Turn Timer: ${uiState.turnTimer}",
            modifier = Modifier.testTag("turn_timer")
        )

        Row {
            Button(
                onClick = { onEvent(LobbyWaitingEvent.OnTurnTimerDecrease) },
                modifier = Modifier.testTag("timer_minus")
            ) { Text("-") }

            Button(
                onClick = { onEvent(LobbyWaitingEvent.OnTurnTimerIncrease) },
                modifier = Modifier.testTag("timer_plus")
            ) { Text("+") }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Starting Cards: ${uiState.startingCards}",
            modifier = Modifier.testTag("starting_cards")
        )

        Row {
            Button(
                onClick = { onEvent(LobbyWaitingEvent.OnStartingCardsDecrease) },
                modifier = Modifier.testTag("cards_minus")
            ) { Text("-") }

            Button(
                onClick = { onEvent(LobbyWaitingEvent.OnStartingCardsIncrease) },
                modifier = Modifier.testTag("cards_plus")
            ) { Text("+") }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Stack")

            Switch(
                checked = uiState.stackEnabled,
                onCheckedChange = {
                    onEvent(LobbyWaitingEvent.OnStackToggle(it))
                },
                modifier = Modifier.testTag("stack_switch")
            )
        }

        Spacer(Modifier.height(24.dp))

        // 🔥 START BUTTON (MIT TEST TAG!)
        if (lobby?.hostUserId == uiState.user?.uid) {
            Button(
                onClick = { onEvent(LobbyWaitingEvent.onMatchStart) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(LobbyWaitingTestTags.START_BUTTON)
            ) {
                Text("Start Game")
            }
        }
    }
}
