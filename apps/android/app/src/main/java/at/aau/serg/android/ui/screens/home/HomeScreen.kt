package at.aau.serg.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.ThemeState
import at.aau.serg.android.ui.util.ErrorUiMapper

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onBrowseFancyLobbies: () -> Unit,
    onShowLeaderboard: () -> Unit,
    onSettings: () -> Unit,
    onNewLobbyScreen: () -> Unit
) {
    val darkMode = ThemeState.isDarkMode.value

    // background gradient for light and dark mode
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF6F8FD),
            if (darkMode) Color(0xFF121A31) else Color(0xFFEFF3FF),
            if (darkMode) Color(0xFF0E1429) else Color(0xFFE7ECFA)
        )
    )

    // title color
    val titleColor = if (darkMode) Color(0xFFEAEFFF) else Color(0xFF1D2750)

    // subtitle color
    val subtitleColor = if (darkMode) {
        Color.White.copy(alpha = 0.78f)
    } else {
        Color(0xFF4D5A78)
    }

    // error text color
    val errorColor = if (darkMode) {
        Color(0xFFFF8F8F)
    } else {
        Color(0xFFC74141)
    }

    // settings button gradient
    val settingsBrush = Brush.horizontalGradient(
        colors = if (darkMode) {
            listOf(Color(0xFF2D3951), Color(0xFF253046))
        } else {
            listOf(Color(0xFFD7DEEA), Color(0xFFC9D2E2))
        }
    )

    // leaderboard button gradient
    val leaderboardBrush = Brush.horizontalGradient(
        colors = if (darkMode) {
            listOf(Color(0xFF33435B), Color(0xFF273347))
        } else {
            listOf(Color(0xFFDCE3EF), Color(0xFFCED7E7))
        }
    )

    // shared ui colors for neutral buttons and bottom profile bar
    val neutralButtonContentColor = if (darkMode) Color.White else Color(0xFF23314C)

    val playerBarBackground = if (darkMode) Color(0xFF151D34) else Color(0xFFF3F6FC)
    val playerBarBorder = if (darkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFD5DDEA)
    val playerIconBackground = if (darkMode) Color(0xFF27324A) else Color(0xFFE3E9F4)
    val playerNameColor = if (darkMode) Color.White else Color(0xFF1E2847)
    val playerLevelColor = if (darkMode) Color(0xFFFFD93D) else Color(0xFFC08A00)
    val xpColor = if (darkMode) Color(0xFF9AA6C0) else Color(0xFF6A7692)

    val uiState by viewModel.uiState.collectAsState()
    val username = uiState.username

    // root screen layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .testTag(HomeTestTags.SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopBar(
            subtitle = "Main Menu",
            onSettings = onSettings,
            modifier = Modifier.padding(16.dp),
            settingsButtonModifier = Modifier.testTag(HomeTestTags.SETTINGS_BUTTON)
        )

        // main content section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(18.dp))

            // app icon card
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4F8DFF), Color(0xFF9B42FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ViewInAr,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "RUMMIKUB",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = titleColor
            )

            Text(
                text = "Classic Tile Game",
                style = MaterialTheme.typography.bodyLarge,
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (val state = uiState.loadState) {
                // loading state
                LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag(HomeTestTags.LOADING),
                        color = if (darkMode) Color.White else Color(0xFF9D3CFF)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                // error state
                is LoadState.Error -> {
                    Text(
                        text = ErrorUiMapper.toMessage(state.error),
                        color = errorColor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(HomeTestTags.ERROR_TEXT)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                else -> Unit // includes Idle + Success
            }


            // create lobby screen
            HomeActionButton(
                text = "Create a Lobby",
                onClick = onNewLobbyScreen,
                icon = { tint ->
                    Icon(Icons.Filled.Person, null, tint = tint, modifier = Modifier.size(24.dp))
                },
                containerBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xE24B68FF), Color(0xD74B3FD4))
                ),
                modifier = Modifier.testTag(HomeTestTags.CREATE_LOBBY_BUTTON)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // browse lobbies
            HomeActionButton(
                text = "Browse Lobbies",
                onClick = onBrowseFancyLobbies,
                icon = { tint ->
                    Icon(Icons.Filled.Groups, null, tint = tint, modifier = Modifier.size(24.dp))
                },
                containerBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF9D3CFF), Color(0xFF9D3CFF))
                ),
                modifier = Modifier.testTag(HomeTestTags.BROWSE_LOBBIES_BUTTON)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // settings action
            HomeActionButton(
                text = "Settings",
                onClick = onSettings,
                icon = { tint ->
                    Icon(Icons.Filled.Settings, null, tint = tint, modifier = Modifier.size(22.dp))
                },
                containerBrush = settingsBrush,
                modifier = Modifier.testTag(HomeTestTags.SETTINGS_LIST_BUTTON),
                contentColor = neutralButtonContentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // leaderboard action
            HomeActionButton(
                text = "Leaderboard",
                onClick = onShowLeaderboard,
                icon = { tint ->
                    Icon(Icons.Filled.EmojiEvents, null, tint = tint, modifier = Modifier.size(22.dp))
                },
                containerBrush = leaderboardBrush,
                modifier = Modifier.testTag(HomeTestTags.LEADERBOARD_BUTTON),
                contentColor = neutralButtonContentColor
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        // bottom profile/info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(playerBarBackground)
                .border(
                    width = 1.dp,
                    color = playerBarBorder,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // player info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(playerIconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        null,
                        tint = playerNameColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = username,
                        color = playerNameColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(HomeTestTags.USERNAME_TEXT)
                    )
                    Text(
                        text = "#482731",
                        color = xpColor
                    )
                }
            }

            // level info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Level 12",
                    color = playerLevelColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "850 XP",
                    color = xpColor
                )
            }
        }
    }
}

@Composable
private fun HomeActionButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    containerBrush: Brush,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(containerBrush),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon(contentColor)
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
