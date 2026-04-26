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
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.browse.components.LobbyBrowseCard
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun LobbyBrowseScreen(
    viewModel: LobbyBrowseViewModel = viewModel()
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
    val darkMode = ThemeState.isDarkMode.value

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF5F7FB),
            if (darkMode) MaterialTheme.colorScheme.surface else Color(0xFFEAEFFF)
        )
    )

    val cardColor = MaterialTheme.colorScheme.surface
    val borderColor = if (darkMode) Color(0xFF394766) else Color(0xFFD8DEF0)
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = if (darkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    } else {
        Color(0xFF6B7280)
    }

    val accentPurple = Color(0xFF9D3CFF)
    val actionButtonColor = if (darkMode) Color(0xFF2A3552) else Color(0xFF2F3A57)

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
                label = { Text("Lobby ID", color = secondaryText) },
                placeholder = { Text("Enter lobby ID to join...", color = secondaryText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Tag,
                        contentDescription = null,
                        tint = secondaryText
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor
                )
            )

            Button(
                onClick = { onEvent(LobbyBrowseEvent.OnJoinLobby(enteredLobbyId)) },
                enabled = enteredLobbyId.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionButtonColor,
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
                color = secondaryText
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
                CircularProgressIndicator(color = accentPurple)
            }
        }

        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
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
                    cardColor = cardColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
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
                containerColor = accentPurple,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = accentPurple,
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
