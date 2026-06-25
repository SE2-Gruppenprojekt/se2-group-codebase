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
import androidx.compose.material3.MaterialTheme
import at.aau.serg.android.ui.theme.appColors
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
    secondaryTextColor: Color
) {
    val w = MaterialTheme.appColors.waiting

    if (fetchedLobby != null) {
        players.forEachIndexed { index, player ->
            val isSelf = player.userId == localId
            val canToggleReady = isSelf && !isLoading
            PlayerItem(
                name = player.displayName,
                subtitle = if (player.isReady) "Ready" else "Not ready",
                isHost = player.userId == fetchedLobby.hostUserId,
                isReady = player.isReady,
                isPlaceholder = false,
                borderColor = if (isSelf) w.selfPlayerBorder else w.activePlayerBorder,
                backgroundColor = if (isSelf) w.selfPlayerBg else w.activePlayerBg,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                onClick = if (canToggleReady) {
                    { onEvent(LobbyWaitingEvent.ToggleReadyState(player.userId)) }
                } else {
                    null
                },
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
            borderColor = w.placeholderBorder,
            backgroundColor = w.placeholderBg,
            primaryTextColor = w.placeholderPrimaryText,
            secondaryTextColor = w.placeholderSecondaryText
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
}
