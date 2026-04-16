package at.aau.serg.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.ThemeState
import androidx.compose.ui.platform.LocalContext
import at.aau.serg.android.util.UserPrefs


@Composable
fun HomeScreen(
    state: LoadState,
    modifier: Modifier = Modifier,
    onCreateLobby: () -> Unit,
    onBrowseFancyLobbies: () -> Unit,
    onShowLeaderboard: () -> Unit,
    onSettings: () -> Unit,
    onWaitingRoom: () -> Unit,
    onNewLobbyScreen: () -> Unit
) {
    // read current theme mode
    val darkMode = ThemeState.isDarkMode.value

    // background gradient for light and dark mode
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            if (darkMode) MaterialTheme.colorScheme.background else Color(0xFFF6F8FD),
            if (darkMode) Color(0xFF121A31) else Color(0xFFEFF3FF),
            if (darkMode) Color(0xFF0E1429) else Color(0xFFE7ECFA)
        )
    )

    val titleColor = if (darkMode) {
        Color(0xFFEAEFFF)
    } else {
        Color(0xFF1D2750)
    }

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

    val context = LocalContext.current
    val username = UserPrefs.getUsername(context) ?: "Guest"

    // root screen layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RUMMIKUB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Main Menu",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                )
            }

            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

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
                            colors = listOf(
                                Color(0xFF4F8DFF),
                                Color(0xFF9B42FF)
                            )
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

            // loading state
            if (state is LoadState.Loading) {
                CircularProgressIndicator(color = if (darkMode) Color.White else Color(0xFF456EFF))
                Spacer(modifier = Modifier.height(14.dp))
            }

            // error state
            if (state is LoadState.Error) {
                Text(
                    text = state.message,
                    color = errorColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // create lobby screen
            HomeActionButton(
                text = "Create a Lobby",
                onClick = onNewLobbyScreen,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                containerBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xE24B68FF), Color(0xD74B3FD4))
                ),
                modifier = Modifier.testTag("home_create_lobby_button")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // browse lobbies
            HomeActionButton(
                text = "Browse Lobbies",
                onClick = onBrowseFancyLobbies,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                containerBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF9D3CFF), Color(0xFF9D3CFF))
                ),
                modifier = Modifier.testTag("home_browse_lobbies_button")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // settings action
            HomeActionButton(
                text = "Settings",
                onClick = onSettings,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                containerBrush = settingsBrush,
                contentColor = neutralButtonContentColor,
                modifier = Modifier.testTag("home_settings_list_button")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // leaderboard action
            HomeActionButton(
                text = "Leaderboard",
                onClick = onShowLeaderboard,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                containerBrush = leaderboardBrush,
                contentColor = neutralButtonContentColor,
                modifier = Modifier.testTag("home_leaderboard_button")
            )

            // push bottom profile bar down
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
            // left side player identity
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(playerIconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (darkMode) Color.White.copy(alpha = 0.92f) else Color(0xFF58657F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = username,
                        color = playerNameColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "#482731",
                        color = xpColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // right side level and xp
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Level 12",
                    color = playerLevelColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "850 XP",
                    color = xpColor,
                    style = MaterialTheme.typography.bodyMedium
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
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
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

