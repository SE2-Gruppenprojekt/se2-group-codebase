package at.aau.serg.android.ui.screens.lobby.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.screens.lobby.create.components.LargeSelectableBox
import at.aau.serg.android.ui.screens.lobby.create.components.NumericSettingRow
import at.aau.serg.android.ui.screens.lobby.create.components.SectionTitle
import at.aau.serg.android.ui.screens.lobby.create.components.SelectableBox
import at.aau.serg.android.ui.screens.lobby.create.components.ToggleSettingRow
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.AccentGreen
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.appColors

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
    val c = appColors()
    val context = LocalContext.current

    val roomCode = remember { "XK7P2M" }

    Column(
        modifier = Modifier
            .testTag(LobbyCreateTestTags.SCREEN)
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(c.screen.bgTop, c.screen.bgBottom)
                )
            )
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

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier
                .testTag(LobbyCreateTestTags.ROOM_CODE_CARD)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = c.screen.card
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "#  ROOM CODE",
                        color = c.screen.secondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roomCode,
                            modifier = Modifier.testTag(LobbyCreateTestTags.ROOM_CODE_TEXT),
                            style = MaterialTheme.typography.titleMedium,
                            color = c.screen.primaryText,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                val clipboardManager =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Room Code", roomCode)
                                clipboardManager.setPrimaryClip(clip)

                                Toast.makeText(
                                    context,
                                    "Room code copied",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .testTag(LobbyCreateTestTags.COPY_ROOM_CODE_BUTTON)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Room Code",
                                tint = c.screen.primaryText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Groups, null, tint = AccentPurple.copy(alpha = 0.61f)) },
            title = "Maximum Players"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(2, 4, 6, 8).forEach { count ->

                SelectableBox(
                    text = count.toString(),
                    selected = uiState.maxPlayers == count,
                    onClick = {
                        onEvent(LobbyCreateEvent.SetMaxPlayers(count))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("${LobbyCreateTestTags.MaxPlayers.OPTION_PREFIX}_$count"),
                    cardColor = c.screen.card,
                    selectedColor = c.screen.selectedBox,
                    borderColor = c.screen.cardBorder,
                    selectedBorder = c.screen.selectedBorder,
                    textColor = c.screen.primaryText,
                    selectedTextColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Lock, null, tint = AccentPurple.copy(alpha = 0.62f)) },
            title = "Privacy"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LargeSelectableBox(
                title = "Public",
                icon = { tint -> Icon(Icons.Filled.Public, null, tint = tint) },
                selected = !uiState.isPrivate,
                onClick = {
                    onEvent(LobbyCreateEvent.SetIsPrivate(false))
                },
                modifier = Modifier
                    .testTag(LobbyCreateTestTags.PRIVACY_PUBLIC)
                    .weight(1f)
                    .height(72.dp),
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
                onClick = {
                    onEvent(LobbyCreateEvent.SetIsPrivate(true))
                },
                modifier = Modifier
                    .testTag(LobbyCreateTestTags.PRIVACY_PRIVATE)
                    .weight(1f)
                    .height(72.dp),
                cardColor = c.screen.card,
                selectedColor = c.screen.selectedBox,
                borderColor = c.screen.cardBorder,
                selectedBorder = c.screen.selectedBorder,
                textColor = c.screen.primaryText,
                selectedTextColor = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Timer, null, tint = AccentPurple) },
            title = "Game Settings"
        )

        Spacer(modifier = Modifier.height(8.dp))

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Timer, null, tint = c.screen.primaryText) },
            title = "Turn Timer",
            value = "${uiState.turnTimer}s",
            onMinus = {
                onEvent(LobbyCreateEvent.ChangeTurnTimer(-10))
            },
            onPlus = {
                onEvent(LobbyCreateEvent.ChangeTurnTimer(10))
            },
            modifier = Modifier
                .testTag(LobbyCreateTestTags.TURN_TIMER_ROW)
                .padding(bottom = 4.dp),
            cardColor = c.screen.card,
            textColor = c.screen.primaryText,
            buttonColor = c.screen.actionButton,
            valueTag = LobbyCreateTestTags.TURN_TIMER_VALUE,
            minusTag = LobbyCreateTestTags.TURN_TIMER_MINUS,
            plusTag = LobbyCreateTestTags.TURN_TIMER_PLUS,
        )

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Groups, null, tint = c.screen.primaryText) },
            title = "Starting Tiles",
            value = uiState.startingTiles.toString(),
            onMinus = {
                onEvent(LobbyCreateEvent.ChangeStartingTiles(-10))
            },
            onPlus = {
                onEvent(LobbyCreateEvent.ChangeStartingTiles(10))
            },
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

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Star, null, tint = c.screen.primaryText) },
            title = "Win Score",
            value = uiState.winScore.toString(),
            onMinus = {
                onEvent(LobbyCreateEvent.ChangeWinScore(-100))
            },
            onPlus = {
                onEvent(LobbyCreateEvent.ChangeWinScore(100))
            },
            modifier = Modifier
                .testTag(LobbyCreateTestTags.WIN_SCORE_ROW)
                .padding(bottom = 4.dp),
            cardColor = c.screen.card,
            textColor = c.screen.primaryText,
            buttonColor = c.screen.actionButton,
            valueTag = LobbyCreateTestTags.WIN_SCORE_VALUE,
            minusTag = LobbyCreateTestTags.WIN_SCORE_MINUS,
            plusTag = LobbyCreateTestTags.WIN_SCORE_PLUS,
        )

        ToggleSettingRow(
            icon = { Icon(Icons.Filled.Speed, null, tint = c.screen.primaryText) },
            title = "Quick Mode",
            checked = uiState.quickMode,
            onCheckedChange = { onEvent(LobbyCreateEvent.SetQuickMode(it)) },
            modifier = Modifier.padding(bottom = 4.dp),
            cardColor = c.screen.card,
            textColor = c.screen.primaryText,
            switchColor = c.screen.actionButton,
            switchTestTag = LobbyCreateTestTags.QUICK_MODE_TOGGLE
        )

        ToggleSettingRow(
            icon = { Icon(Icons.Filled.Visibility, null, tint = c.screen.primaryText) },
            title = "Require Initial Meld",
            checked = uiState.requireInitialMeld,
            onCheckedChange = { onEvent(LobbyCreateEvent.SetRequireInitialMeld(it)) },
            modifier = Modifier.padding(bottom = 4.dp),
            cardColor = c.screen.card,
            textColor = c.screen.primaryText,
            switchColor = c.screen.actionButton,
            switchTestTag = LobbyCreateTestTags.REQUIRE_INITIAL_MELD_TOGGLE
        )

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(20.dp))
    }
}
