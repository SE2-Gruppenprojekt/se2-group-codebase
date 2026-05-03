package at.aau.serg.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.ui.theme.AccentBlue
import at.aau.serg.android.ui.theme.AccentPurple
import at.aau.serg.android.ui.theme.HomeCreateBrushEnd
import at.aau.serg.android.ui.theme.HomeCreateBrushStart
import at.aau.serg.android.ui.theme.HomeIconGradientEnd
import at.aau.serg.android.ui.theme.appColors
import at.aau.serg.android.ui.util.ErrorUiMapper

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit
) {
    val c = appColors()

    // background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(c.home.bgTop, c.home.bgMid, c.home.bgBottom)
    )
    val settingsBrush = Brush.horizontalGradient(
        colors = listOf(c.home.settingsGradientStart, c.home.settingsGradientEnd)
    )

    // root screen layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .testTag(HomeTestTags.SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopBar(
            subtitle = "Main Menu",
            onSettings = { onEvent(HomeEvent.OnSettings) },
            modifier = Modifier.padding(16.dp),
            settingsButtonModifier = Modifier.testTag(HomeTestTags.TOPBAR_SETTINGS_BUTTON)
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
                            listOf(AccentBlue, HomeIconGradientEnd)
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
                color = c.home.title
            )

            Text(
                text = "Classic Tile Game",
                style = MaterialTheme.typography.bodyLarge,
                color = c.home.subtitle
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (val state = uiState.loadState) {
                // loading state
                LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag(HomeTestTags.LOADING),
                        color = c.home.loadingIndicator
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                // error state
                is LoadState.Error -> {
                    Text(
                        text = ErrorUiMapper.toMessage(state.error),
                        color = c.home.error,
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
                onClick = { onEvent(HomeEvent.OnCreateLobby) },
                icon = { tint ->
                    Icon(Icons.Filled.Person, null, tint = tint, modifier = Modifier.size(24.dp))
                },
                containerBrush = Brush.horizontalGradient(
                    colors = listOf(HomeCreateBrushStart, HomeCreateBrushEnd)
                ),
                contentColor = c.home.buttonText,
                modifier = Modifier.testTag(HomeTestTags.ACTION_CREATE_LOBBY)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // browse lobbies
            HomeActionButton(
                text = "Browse Lobbies",
                onClick = { onEvent(HomeEvent.OnBrowseLobby) },
                icon = { tint ->
                    Icon(Icons.Filled.Groups, null, tint = tint, modifier = Modifier.size(24.dp))
                },
                containerBrush = Brush.horizontalGradient(
                    colors = listOf(AccentPurple, AccentPurple)
                ),
                contentColor = c.home.buttonText,
                modifier = Modifier.testTag(HomeTestTags.ACTION_BROWSE_LOBBY)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // settings action
            HomeActionButton(
                text = "Settings",
                onClick = { onEvent(HomeEvent.OnSettings) },
                icon = { tint ->
                    Icon(Icons.Filled.Settings, null, tint = tint, modifier = Modifier.size(22.dp))
                },
                containerBrush = settingsBrush,
                contentColor = c.home.buttonText,
                modifier = Modifier.testTag(HomeTestTags.ACTION_SETTINGS)
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        // bottom profile/info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(c.home.playerBar)
                .border(
                    width = 1.dp,
                    color = c.home.playerBarBorder,
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
                        .background(c.home.playerIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        null,
                        tint = c.home.playerName,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = uiState.user?.displayName ?: "Guest",
                        color = c.home.playerName,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(HomeTestTags.USERNAME_TEXT)
                    )
                    Text(
                        text = "#482731",
                        color = c.home.xp
                    )
                }
            }

            // level info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Level 12",
                    color = c.home.playerLevel,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "850 XP",
                    color = c.home.xp
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
