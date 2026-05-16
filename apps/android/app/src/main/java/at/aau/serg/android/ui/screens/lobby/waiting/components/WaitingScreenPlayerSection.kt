package at.aau.serg.android.ui.screens.lobby.waiting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import at.aau.serg.android.ui.screens.lobby.waiting.LobbyWaitingEvent
import at.aau.serg.android.ui.screens.lobby.waiting.LobbyWaitingTestTags
import shared.models.lobby.domain.Lobby
import shared.models.lobby.domain.LobbyPlayer


@Composable
fun WaitingScreenPlayerSection(
    onEvent: (LobbyWaitingEvent) -> Unit,
    localId: String = "",
    isLoading: Boolean,
    players: List<LobbyPlayer>,
    fetchedLobby: Lobby?,
    maxPlayers: Int,
    joinedCount: Int,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    darkMode: Boolean
) {

    val activePlayerBackground = if (darkMode) {
        Color(0xFF1F356A)
    } else {
        Color(0xFFEAF1FF)
    }

    val activePlayerBorder = if (darkMode) {
        Color(0xFF3E73E8)
    } else {
        Color(0xFF4C84FF)
    }

    val secondPlayerBackground = if (darkMode) {
        Color(0xFF1E3A2D)
    } else {
        Color(0xFFEAFBF1)
    }

    val secondPlayerBorder = if (darkMode) {
        Color(0xFF2BC46D)
    } else {
        Color(0xFF20C76F)
    }

    val waitingBackground = if (darkMode) {
        Color(0xFF1E1E1E)
    } else {
        Color.White
    }

    if (fetchedLobby != null) {
        players.forEachIndexed { index, player ->
            val isSelf = player.userId == localId
            PlayerItem(
                name = player.displayName,
                subtitle = if (player.isReady) "Ready" else "Not ready",
                isHost = player.userId == fetchedLobby.hostUserId,
                isReady = player.isReady,
                isPlaceholder = false,
                borderColor = if (isSelf) secondPlayerBorder else activePlayerBorder,
                backgroundColor = if (isSelf) secondPlayerBackground else activePlayerBackground,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                onClick = if (isSelf) { { onEvent(LobbyWaitingEvent.ToggleReadyState(player.userId)) } } else null,
                testTag = LobbyWaitingTestTags.Players.ready_tag(player.userId)
            )
        }
    } else if (isLoading) {
        Text(
            text = "Loading players...",
            color = secondaryTextColor,
            modifier = Modifier.height(40.dp)
        )
        return
    }

    val placeholderCount = if (fetchedLobby != null) {
        (maxPlayers - players.size).coerceAtLeast(0)
    } else {
        (maxPlayers - 2).coerceAtLeast(0)
    }

    repeat(placeholderCount) {
        PlayerItem(
            name = "Waiting for player...",
            subtitle = "",
            isPlaceholder = true,
            borderColor = if (darkMode) Color(0xFF3A3F4B) else Color(0xFFD1D5DB),
            backgroundColor = waitingBackground,
            primaryTextColor = if (darkMode) Color(0xFF727887) else Color(0xFF9AA3B2),
            secondaryTextColor = if (darkMode) Color(0xFF5E6573) else Color(0xFFB2BAC8)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
}
