package at.aau.serg.android.ui.screens.lobby.waiting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.waiting.components.LobbyUiState.lobbyName
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenPlayerSection
import at.aau.serg.android.ui.screens.lobby.waiting.components.WaitingScreenRoomCard
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.appColors
import shared.models.lobby.domain.LobbyStatus

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
    val c = appColors()
    val context = LocalContext.current

    val players = uiState.lobby?.players ?: emptyList()
    val joinedCount = players.size
    val maxPlayers = uiState.lobby?.settings?.maxPlayers ?: 0
    val roomCode = uiState.lobby?.lobbyId?.take(6)?.uppercase() ?: "------"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(c.screen.bgTop, c.screen.bgBottom)))
            .testTag(LobbyWaitingTestTags.SCREEN)
    ) {

        // scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp)
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
                primaryTextColor = c.screen.primaryText,
                secondaryTextColor = c.screen.secondaryText,
                cardColor = c.screen.card
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
                primaryTextColor = c.screen.primaryText,
                secondaryTextColor = c.screen.secondaryText,
            )

            Spacer(Modifier.height(20.dp))

            uiState.lobby?.settings?.let { settings ->
                Text(
                    text = "GAME INFO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = c.screen.secondaryText,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = c.screen.card),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        GameInfoRow(
                            icon = Icons.Filled.Person,
                            label = "Max Players",
                            value = settings.maxPlayers.toString(),
                            textColor = c.screen.primaryText,
                            secondaryColor = c.screen.secondaryText
                        )
                        HorizontalDivider(color = c.screen.secondaryText.copy(alpha = 0.1f))
                        GameInfoRow(
                            icon = if (settings.isPrivate) Icons.Filled.Lock else Icons.Filled.Public,
                            label = "Lobby",
                            value = if (settings.isPrivate) "Private" else "Public",
                            textColor = c.screen.primaryText,
                            secondaryColor = c.screen.secondaryText
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

        }

        // --- start button (just for host) ---
        if (uiState.lobby?.hostUserId == uiState.user?.uid) {
            val canStart = uiState.lobby?.status == LobbyStatus.OPEN
            Button(
                onClick = { onEvent(LobbyWaitingEvent.onMatchStart) },
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .height(58.dp)
                    .testTag(LobbyWaitingTestTags.START_BUTTON),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
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

@Composable
private fun GameInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    secondaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = textColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, color = secondaryColor, style = MaterialTheme.typography.bodyMedium)
    }
}
