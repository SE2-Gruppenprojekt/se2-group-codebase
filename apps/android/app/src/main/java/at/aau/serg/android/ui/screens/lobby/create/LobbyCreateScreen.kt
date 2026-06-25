package at.aau.serg.android.ui.screens.lobby.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.create.components.LargeSelectableBox
import at.aau.serg.android.ui.screens.lobby.create.components.NumericSettingRow
import at.aau.serg.android.ui.screens.lobby.create.components.SectionTitle
import at.aau.serg.android.ui.screens.lobby.create.components.SelectableBox
import at.aau.serg.android.ui.screens.lobby.create.components.ToggleSettingRow
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.appColors
import at.aau.serg.android.ui.util.ErrorUiMapper

@Composable
fun LobbyCreateScreen(
    viewModel: LobbyCreateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LobbyCreateScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun LobbyCreateScreenContent(
    uiState: LobbyCreateUiState,
    onEvent: (LobbyCreateEvent) -> Unit
) {
    val c = MaterialTheme.appColors

    Column(
        modifier = Modifier
            .testTag(LobbyCreateTestTags.SCREEN)
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(c.screen.bgTop, c.screen.bgBottom)
                )
            )
    ) {

        // scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TopBar(
                subtitle = "Create New Lobby",
                onBack = { onEvent(LobbyCreateEvent.OnBack) },
                onSettings = { onEvent(LobbyCreateEvent.OnSettings) },
                backButtonModifier = Modifier.testTag(LobbyCreateTestTags.BACK_BUTTON),
                titleModifier = Modifier.testTag(LobbyCreateTestTags.TITLE),
                subtitleModifier = Modifier.testTag(LobbyCreateTestTags.SUBTITLE),
                settingsButtonModifier = Modifier.testTag(LobbyCreateTestTags.SETTINGS_BUTTON)
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(
                icon = { Icon(Icons.Filled.Groups, null, tint = AccentPurple.copy(alpha = 0.61f)) },
                title = "Maximum Players"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(2, 3, 4).forEach { count ->
                    SelectableBox(
                        text = count.toString(),
                        selected = uiState.maxPlayers == count,
                        onClick = { onEvent(LobbyCreateEvent.SetMaxPlayers(count)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("${LobbyCreateTestTags.MaxPlayers.OPTION_PREFIX}_$count"),
                        cardColor = c.screen.card,
                        selectedColor = c.screen.selectedBox,
                        borderColor = c.screen.cardBorder,
                        selectedBorder = c.screen.selectedBorder,
                        textColor = c.screen.primaryText,
                        selectedTextColor = Color.White,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(
                icon = { Icon(Icons.Filled.Lock, null, tint = AccentPurple.copy(alpha = 0.62f)) },
                title = "Privacy"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LargeSelectableBox(
                    title = "Public",
                    icon = { tint -> Icon(Icons.Filled.Public, null, tint = tint) },
                    selected = !uiState.isPrivate,
                    onClick = { onEvent(LobbyCreateEvent.SetIsPrivate(false)) },
                    modifier = Modifier
                        .testTag(LobbyCreateTestTags.PRIVACY_PUBLIC)
                        .weight(1f)
                        .height(110.dp),
                    cardColor = c.screen.card,
                    selectedColor = c.screen.selectedBox,
                    borderColor = c.screen.cardBorder,
                    selectedBorder = c.screen.selectedBorder,
                    textColor = c.screen.primaryText,
                    selectedTextColor = Color.White
                )

                LargeSelectableBox(
                    title = "Private",
                    icon = { tint -> Icon(Icons.Filled.Lock, null, tint = tint) },
                    selected = uiState.isPrivate,
                    onClick = { onEvent(LobbyCreateEvent.SetIsPrivate(true)) },
                    modifier = Modifier
                        .testTag(LobbyCreateTestTags.PRIVACY_PRIVATE)
                        .weight(1f)
                        .height(110.dp),
                    cardColor = c.screen.card,
                    selectedColor = c.screen.selectedBox,
                    borderColor = c.screen.cardBorder,
                    selectedBorder = c.screen.selectedBorder,
                    textColor = c.screen.primaryText,
                    selectedTextColor = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(
                icon = { Icon(Icons.Filled.Timer, null, tint = AccentPurple) },
                title = "Game Settings"
            )

            Spacer(modifier = Modifier.height(12.dp))

            NumericSettingRow(
                icon = { Icon(Icons.Filled.Groups, null, tint = c.screen.primaryText) },
                title = "Starting Tiles",
                value = uiState.startingTiles.toString(),
                onMinus = { onEvent(LobbyCreateEvent.ChangeStartingTiles(-1)) },
                onPlus = { onEvent(LobbyCreateEvent.ChangeStartingTiles(1)) },
                modifier = Modifier
                    .testTag(LobbyCreateTestTags.STARTING_TILES_ROW)
                    .padding(bottom = 4.dp),
                cardColor = c.screen.card,
                textColor = c.screen.primaryText,
                buttonColor = c.screen.actionButton,
                valueTag = LobbyCreateTestTags.STARTING_TILES_VALUE,
                minusTag = LobbyCreateTestTags.STARTING_TILES_MINUS,
                plusTag = LobbyCreateTestTags.STARTING_TILES_PLUS,
            )

            Spacer(modifier = Modifier.height(16.dp))

        }

        // error
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (uiState.loadState is LoadState.Error) {
                Text(
                    text = ErrorUiMapper.toMessage((uiState.loadState as LoadState.Error).error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    onEvent(LobbyCreateEvent.CreateLobby)
                          },
                modifier = Modifier
                    .testTag(LobbyCreateTestTags.CREATE_BUTTON)
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.loadState != LoadState.Loading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (uiState.loadState == LoadState.Loading) "Loading" else "Create Lobby",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(LobbyCreateTestTags.CREATE_BUTTON_TEXT)
                )
                if (uiState.loadState != LoadState.Loading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
