package at.aau.serg.android.ui.screens.waiting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import at.aau.serg.android.ui.theme.ThemeState
import androidx.compose.foundation.layout.*

@Composable
fun WaitingRoomScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    var turnTimer by remember { mutableStateOf(60) }
    var startingCards by remember { mutableStateOf(7) }
    var stackEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val roomCode = remember { generateRoomCode() }
    val scrollState = rememberScrollState()
    val darkMode = ThemeState.isDarkMode.value

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

    val cardColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (darkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    } else {
        Color(0xFF6B7280)
    }

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

    val waitingBackground = if (darkMode) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.White
    }

    val playersHeaderBackground = if (darkMode) {
        Color.White.copy(alpha = 0.04f)
    } else {
        Color(0xFF284B8F).copy(alpha = 0.05f)
    }

    val inviteButtonColor = if (darkMode) {
        Color(0xFF2A3552)
    } else {
        Color(0xFF2F3A57)
    }

    val settingsSectionBackground = if (darkMode) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.04f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom)
                )
            )
            .verticalScroll(scrollState)
            .padding(10.dp)
    ) {
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
                        text = "Waiting Room",
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
                        text = "4/8",
                        color = primaryTextColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                text = "2 joined",
                color = secondaryTextColor,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
            borderColor = activePlayerBorder,
            backgroundColor = activePlayerBackground,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )

        PlayerItem(
            name = "Waiting for player...",
            subtitle = "",
            isPlaceholder = true,
            borderColor = if (darkMode) Color(0xFF3A3F4B) else Color(0xFFD1D5DB),
            backgroundColor = waitingBackground,
            primaryTextColor = if (darkMode) Color(0xFF727887) else Color(0xFF9AA3B2),
            secondaryTextColor = if (darkMode) Color(0xFF5E6573) else Color(0xFFB2BAC8)
        )

        PlayerItem(
            name = "Waiting for player...",
            subtitle = "",
            isPlaceholder = true,
            borderColor = if (darkMode) Color(0xFF3A3F4B) else Color(0xFFD1D5DB),
            backgroundColor = waitingBackground,
            primaryTextColor = if (darkMode) Color(0xFF727887) else Color(0xFF9AA3B2),
            secondaryTextColor = if (darkMode) Color(0xFF5E6573) else Color(0xFFB2BAC8)
        )

        Spacer(modifier = Modifier.height(18.dp))

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

            SettingRow(
                title = "Turn Timer",
                value = "${turnTimer}s",
                onMinus = { if (turnTimer > 10) turnTimer -= 10 },
                onPlus = { turnTimer += 10 },
                cardColor = cardColor,
                primaryTextColor = primaryTextColor,
                buttonColor = inviteButtonColor
            )

            SettingRow(
                title = "Starting Cards",
                value = "$startingCards",
                onMinus = { if (startingCards > 1) startingCards -= 1 },
                onPlus = { startingCards += 1 },
                cardColor = cardColor,
                primaryTextColor = primaryTextColor,
                buttonColor = inviteButtonColor
            )

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
                        onCheckedChange = { stackEnabled = it },
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
                        containerColor = Color(0xFF4F8DFF),
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
    val displayName = if (isHost) "$name (Host)" else name
    val avatarBackground = if (isPlaceholder) {
        secondaryTextColor.copy(alpha = 0.16f)
    } else {
        Color(0xFF4F8DFF)
    }
    val avatarIconTint = if (isPlaceholder) {
        secondaryTextColor
    } else {
        Color.White
    }

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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayName,
                        color = primaryTextColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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

            if (isJoined) {
                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
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
    val chars = "ABCgenerateRoomCodeDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}
