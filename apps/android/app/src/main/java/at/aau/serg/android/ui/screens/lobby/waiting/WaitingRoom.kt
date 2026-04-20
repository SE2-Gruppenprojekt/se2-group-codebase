package at.aau.serg.android.ui.screens.lobby.waiting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.lobby.LobbyUiState
import at.aau.serg.android.ui.screens.lobby.main.LobbyViewModel
import at.aau.serg.android.ui.theme.ThemeState
import kotlin.random.Random

@Composable
fun WaitingRoomScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    lobbyId: String? = null,
    viewModel: LobbyViewModel? = null
) {
    // context for clipboard and toast
    val context = LocalContext.current

    // scroll support for smaller screens
    val scrollState = rememberScrollState()

    // current theme mode
    val darkMode = ThemeState.isDarkMode.value

    val fetchedLobbyState = viewModel?.lobby?.collectAsState()
    val fetchedLobby = fetchedLobbyState?.value

    LaunchedEffect(lobbyId) {
        if (lobbyId != null && viewModel != null) {
            viewModel.loadLobby(lobbyId)
        }
    }

    // shared lobby state
    val fallbackLobbyName by LobbyUiState.lobbyName
    val fallbackMaxPlayers by LobbyUiState.maxPlayers
    val turnTimer by LobbyUiState.turnTimer
    val startingCards by LobbyUiState.startingCards
    val stackEnabled by LobbyUiState.stackEnabled

    // generate room code once if missing
    if (LobbyUiState.roomCode.value.isBlank() && lobbyId == null) {
        LobbyUiState.roomCode.value = generateRoomCode()
    }
    val fallbackRoomCode by LobbyUiState.roomCode

    val lobbyName = if (fetchedLobby != null) "Waiting Room" else fallbackLobbyName
    val maxPlayers = fetchedLobby?.settings?.maxPlayers ?: fallbackMaxPlayers
    val roomCode = fetchedLobby?.lobbyId?.take(6)?.uppercase() ?: fallbackRoomCode
    val players = fetchedLobby?.players ?: emptyList()
    val joinedCount = if (fetchedLobby != null) players.size else 2
    val isLobbyLoading = lobbyId != null && fetchedLobby == null

    // background gradient colors
    val gradientTop = if (darkMode) {
        MaterialTheme.colorScheme.background
    } else {
        Color(0xFFF5F7FB)
    }

    val gradientBottom = if (darkMode) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFEAEFFF)
    }

    // text and card colors
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (darkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    } else {
        Color(0xFF6B7280)
    }

    // first player highlight
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

    // second player highlight
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

    // placeholder player colors
    val waitingBackground = if (darkMode) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.White
    }

    // action button color
    val inviteButtonColor = if (darkMode) {
        Color(0xFF2A3552)
    } else {
        Color(0xFF2F3A57)
    }

    // settings group background
    val settingsSectionBackground = if (darkMode) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.04f)
    }

    // root content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom)
                )
            )
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = "RUMMIKUB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = lobbyName,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
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

        Spacer(modifier = Modifier.height(10.dp))

        // room info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Room Code",
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roomCode,
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // copy room code
                        IconButton(
                            modifier = Modifier.size(28.dp),
                            onClick = {
                                val clipboardManager =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Room Code", roomCode)
                                clipboardManager.setPrimaryClip(clip)

                                Toast.makeText(
                                    context,
                                    "Room Code copied",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Room Code",
                                tint = primaryTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Players",
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$joinedCount/$maxPlayers",
                        color = primaryTextColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // player list header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Players Ready",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$joinedCount joined",
                color = secondaryTextColor,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isLobbyLoading) {
            Text(
                text = "Loading players...",
                color = secondaryTextColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else if (fetchedLobby != null) {
            players.forEachIndexed { index, player ->
                PlayerItem(
                    name = player.displayName,
                    subtitle = if (player.isReady) "Ready" else "Not ready",
                    isHost = player.userId == fetchedLobby.hostUserId,
                    isJoined = true,
                    isPlaceholder = false,
                    borderColor = if (index == 1) secondPlayerBorder else activePlayerBorder,
                    backgroundColor = if (index == 1) secondPlayerBackground else activePlayerBackground,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        } else {
            PlayerItem(
                name = "You",
                subtitle = "Level 24",
                isHost = true,
                isJoined = true,
                isPlaceholder = false,
                borderColor = activePlayerBorder,
                backgroundColor = activePlayerBackground,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor
            )

            PlayerItem(
                name = "Alex",
                subtitle = "Level 18",
                isHost = false,
                isJoined = true,
                isPlaceholder = false,
                borderColor = secondPlayerBorder,
                backgroundColor = secondPlayerBackground,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor
            )
        }

        val placeholderCount = if (isLobbyLoading) {
            0
        } else if (fetchedLobby != null) {
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

        Spacer(modifier = Modifier.height(18.dp))

        // settings panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = settingsSectionBackground,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Game Settings",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // turn timer control
            SettingRow(
                title = "Turn Timer",
                value = "${turnTimer}s",
                onMinus = {
                    if (LobbyUiState.turnTimer.intValue > 10) {
                        LobbyUiState.turnTimer.intValue -= 10
                    }
                },
                onPlus = {
                    LobbyUiState.turnTimer.intValue += 10
                },
                cardColor = cardColor,
                primaryTextColor = primaryTextColor,
                buttonColor = inviteButtonColor
            )

            // starting cards control
            SettingRow(
                title = "Starting Cards",
                value = "$startingCards",
                onMinus = {
                    if (LobbyUiState.startingCards.intValue > 1) {
                        LobbyUiState.startingCards.intValue -= 1
                    }
                },
                onPlus = {
                    LobbyUiState.startingCards.intValue += 1
                },
                cardColor = cardColor,
                primaryTextColor = primaryTextColor,
                buttonColor = inviteButtonColor
            )

            // stack toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stack +2/+4",
                        color = primaryTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Switch(
                        checked = stackEnabled,
                        onCheckedChange = { LobbyUiState.stackEnabled.value = it },
                        modifier = Modifier.scale(0.78f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = inviteButtonColor,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.9f),
                            uncheckedTrackColor = inviteButtonColor.copy(alpha = 0.55f),
                            uncheckedBorderColor = Color.Transparent,
                            checkedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9D3CFF),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Start Game",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = inviteButtonColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Invite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PlayerItem(
    name: String,
    subtitle: String,
    isHost: Boolean = false,
    isJoined: Boolean = false,
    isPlaceholder: Boolean = false,
    borderColor: Color,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    // avatar background depends on placeholder state
    val avatarBackground = if (isPlaceholder) {
        secondaryTextColor.copy(alpha = 0.16f)
    } else {
        Color(0xFF4F8DFF)
    }

    // avatar icon tint
    val avatarIconTint = if (isPlaceholder) {
        secondaryTextColor
    } else {
        Color.White
    }

    // player row card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = avatarBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = avatarIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // player text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        color = primaryTextColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // host indicator
                    if (isHost) {
                        HostBadge()
                    }
                }

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // joined checkmark
            if (isJoined) {
                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = Color(0xFF2DBE60),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HostBadge() {
    // small host label
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF4F8DFF).copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "HOST",
            color = Color(0xFF4F8DFF),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingRow(
    title: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    cardColor: Color,
    primaryTextColor: Color,
    buttonColor: Color
) {
    // reusable numeric settings row
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = primaryTextColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            // minus/value/plus controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onMinus,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "-",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = value,
                    color = primaryTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onPlus,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "+",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun generateRoomCode(length: Int = 6): String {
    // exclude confusing characters
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}
