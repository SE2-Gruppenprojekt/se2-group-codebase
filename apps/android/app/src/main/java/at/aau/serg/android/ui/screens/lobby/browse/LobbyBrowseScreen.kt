package at.aau.serg.android.ui.screens.lobby.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.browse.components.LobbyBrowseCard
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.appColors
import at.aau.serg.android.ui.util.ErrorUiMapper

@Composable
fun LobbyBrowseScreen(
    viewModel: LobbyBrowseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LobbyBrowseScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun LobbyBrowseScreenContent(
    uiState: LobbyBrowseUiState,
    onEvent: (LobbyBrowseEvent) -> Unit
) {
    val c = appColors()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(c.screen.bgTop, c.screen.bgBottom)
    )

    val enteredLobbyId = uiState.lobbyIdInput.trim().uppercase()

    Column(
        modifier = Modifier
            .testTag(LobbyBrowseTestTags.SCREEN)
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        TopBar(
            subtitle = "Available Lobbies",
            onBack = { onEvent(LobbyBrowseEvent.OnBack) },
            onSettings = { onEvent(LobbyBrowseEvent.OnSettings) },
            backButtonModifier = Modifier.testTag(LobbyBrowseTestTags.BACK_BUTTON),
            titleModifier = Modifier.testTag(LobbyBrowseTestTags.TITLE),
            subtitleModifier = Modifier.testTag(LobbyBrowseTestTags.SUBTITLE),
            settingsButtonModifier = Modifier.testTag(LobbyBrowseTestTags.SETTINGS_BUTTON)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // direct join row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.lobbyIdInput,
                onValueChange = { onEvent(LobbyBrowseEvent.OnLobbyIdChanged(it)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag(LobbyBrowseTestTags.LOBBY_ID_INPUT),
                singleLine = true,
                label = { Text("Lobby ID", color = c.screen.secondaryText) },
                placeholder = { Text("Enter lobby ID to join...", color = c.screen.secondaryText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Tag,
                        contentDescription = null,
                        tint = c.screen.secondaryText
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = c.screen.primaryText,
                    unfocusedTextColor = c.screen.primaryText,
                    focusedContainerColor = c.screen.card,
                    unfocusedContainerColor = c.screen.card,
                    focusedBorderColor = c.screen.cardBorder,
                    unfocusedBorderColor = c.screen.cardBorder
                )
            )

            Button(
                onClick = { onEvent(LobbyBrowseEvent.OnJoinLobby(enteredLobbyId)) },
                enabled = enteredLobbyId.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.screen.actionButton,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag(LobbyBrowseTestTags.JOIN_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Join", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // list header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Open Games",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
            )
            Text(
                text = "${uiState.lobbies.size} available",
                style = MaterialTheme.typography.labelMedium,
                color = c.screen.secondaryText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.loadState == LoadState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPurple)
            }
        }

        when (val state = uiState.loadState) {
            is LoadState.Error -> {
                Text(
                    text = ErrorUiMapper.toMessage(state.error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            else -> Unit
        }

        // lobby list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag(LobbyBrowseTestTags.LOBBY_LIST),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.lobbies) { lobby ->
                LobbyBrowseCard(
                    lobby = lobby,
                    cardColor = c.screen.card,
                    primaryText = c.screen.primaryText,
                    secondaryText = c.screen.secondaryText,
                    onJoinLobby = { onEvent(LobbyBrowseEvent.OnJoinLobby(it)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // create button
        Button(
            onClick = { onEvent(LobbyBrowseEvent.OnCreateNewLobby) },
            modifier = Modifier
                .testTag(LobbyBrowseTestTags.CREATE_BUTTON)
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentPurple,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = AccentPurple,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create New Lobby",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
