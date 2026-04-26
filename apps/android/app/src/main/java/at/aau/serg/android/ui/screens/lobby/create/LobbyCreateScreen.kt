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
import at.aau.serg.android.ui.theme.ThemeState

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
    val darkMode = ThemeState.isDarkMode.value
    val context = LocalContext.current

    // align dark mode with waiting room styling
    val bgTop = if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF5F7FB)
    val bgBottom = if (darkMode) MaterialTheme.colorScheme.surface else Color(0xFFEAEFFF)
    val cardColor = MaterialTheme.colorScheme.surface
    val cardBorder = if (darkMode) Color(0xFF394766) else Color(0xFFD8DEF0)
    val primaryText = MaterialTheme.colorScheme.onSurface

    // improved selected colors for dark mode
    val selectedColor = if (darkMode) Color(0xFF7C3AED) else Color(0xFF2F3A57)
    val selectedBorder = if (darkMode) Color(0xFFB794F4) else Color(0xFF47536F)
    val selectedContentColor = Color.White

    val actionGreen = Color(0xFF22C55E)
    val settingButtonColor = if (darkMode) Color(0xFF2A3552) else Color(0xFF2F3A57)
    val secondaryText = if (darkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    } else {
        Color(0xFF6B7280)
    }
    val roomCode = remember { "XK7P2M" }

    Column(
        modifier = Modifier
            .testTag(LobbyCreateTestTags.SCREEN)
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgTop, bgBottom)
                )
            )
            .verticalScroll(rememberScrollState()) // allow smaller screens to scroll
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
                containerColor = cardColor
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
                        color = secondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roomCode,
                            modifier = Modifier.testTag(LobbyCreateTestTags.ROOM_CODE_TEXT),
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryText,
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
                                tint = primaryText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Groups, null, tint = Color(0x9C9D3CFF)) },
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
                    cardColor = cardColor,
                    selectedColor = selectedColor,
                    borderColor = cardBorder,
                    selectedBorder = selectedBorder,
                    textColor = primaryText,
                    selectedTextColor = selectedContentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Lock, null, tint = Color(0x9E9D3CFF)) },
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
                cardColor = cardColor,
                selectedColor = selectedColor,
                borderColor = cardBorder,
                selectedBorder = selectedBorder,
                textColor = primaryText,
                selectedTextColor = selectedContentColor
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
                cardColor = cardColor,
                selectedColor = selectedColor,
                borderColor = cardBorder,
                selectedBorder = selectedBorder,
                textColor = primaryText,
                selectedTextColor = selectedContentColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Timer, null, tint = Color(0xFF9D3CFF)) },
            title = "Game Settings"
        )

        Spacer(modifier = Modifier.height(8.dp))

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Timer, null, tint = primaryText) },
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
            cardColor = cardColor,
            textColor = primaryText,
            buttonColor = settingButtonColor,
            valueTag = LobbyCreateTestTags.TURN_TIMER_VALUE,
            minusTag = LobbyCreateTestTags.TURN_TIMER_MINUS,
            plusTag = LobbyCreateTestTags.TURN_TIMER_PLUS,
        )

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Groups, null, tint = primaryText) },
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
            cardColor = cardColor,
            textColor = primaryText,
            buttonColor = settingButtonColor,
            valueTag = LobbyCreateTestTags.STARTING_TILES_VALUE,
            minusTag = LobbyCreateTestTags.STARTING_TILES_MINUS,
            plusTag = LobbyCreateTestTags.STARTING_TILES_PLUS,
        )

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Star, null, tint = primaryText) },
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
            cardColor = cardColor,
            textColor = primaryText,
            buttonColor = settingButtonColor,
            valueTag = LobbyCreateTestTags.WIN_SCORE_VALUE,
            minusTag = LobbyCreateTestTags.WIN_SCORE_MINUS,
            plusTag = LobbyCreateTestTags.WIN_SCORE_PLUS,
        )

        ToggleSettingRow(
            icon = { Icon(Icons.Filled.Speed, null, tint = primaryText) },
            title = "Quick Mode",
            checked = uiState.quickMode,
            onCheckedChange = { onEvent(LobbyCreateEvent.SetQuickMode(it)) },
            modifier = Modifier.padding(bottom = 4.dp),
            cardColor = cardColor,
            textColor = primaryText,
            switchColor = settingButtonColor,
            switchTestTag = LobbyCreateTestTags.QUICK_MODE_TOGGLE
        )

        ToggleSettingRow(
            icon = { Icon(Icons.Filled.Visibility, null, tint = primaryText) },
            title = "Require Initial Meld",
            checked = uiState.requireInitialMeld,
            onCheckedChange = { onEvent(LobbyCreateEvent.SetRequireInitialMeld(it)) },
            modifier = Modifier.padding(bottom = 4.dp),
            cardColor = cardColor,
            textColor = primaryText,
            switchColor = settingButtonColor,
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
                containerColor = Color(0xFF9D3CFF),
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
