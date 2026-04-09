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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val onlineCardColor = if (darkMode) {
        Color(0xFF183C30)
    } else {
        Color(0xFFDFF7E8)
    }

    val onlineTextColor = if (darkMode) {
        Color(0xFF8CFFB1)
    } else {
        Color(0xFF1E6B57)
    }

    val hostBackground = if (darkMode) {
        Color(0xFF1C2A45)
    } else {
        Color(0xFFEFF4FF)
    }

    val playerBackground = if (darkMode) {
        Color(0xFF1B3326)
    } else {
        Color(0xFFEAFBF1)
    }

    val waitingBackground = if (darkMode) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.White
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column {
                    Text(
                        text = "RUMMIKUB",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Waiting Room",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = onlineCardColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "● Online",
                        color = onlineTextColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Room Code",
                        color = secondaryTextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roomCode,
                            style = MaterialTheme.typography.headlineMedium,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
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
                                tint = primaryTextColor
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Players",
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "4/8",
                        color = primaryTextColor,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Players Ready",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "2 joined",
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PlayerItem(
            name = "You",
            subtitle = "HOST • Level 24",
            borderColor = Color(0xFF3C7CFF),
            backgroundColor = hostBackground,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )

        PlayerItem(
            name = "Alex",
            subtitle = "Level 18",
            borderColor = Color(0xFF20C76F),
            backgroundColor = playerBackground,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )

        PlayerItem(
            name = "Waiting for player...",
            subtitle = "",
            borderColor = if (darkMode) Color(0xFF3A3F4B) else Color(0xFFD1D5DB),
            backgroundColor = waitingBackground,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )

        PlayerItem(
            name = "Waiting for player...",
            subtitle = "",
            borderColor = if (darkMode) Color(0xFF3A3F4B) else Color(0xFFD1D5DB),
            backgroundColor = waitingBackground,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Game Settings",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingRow(
            title = "Turn Timer",
            value = "${turnTimer}s",
            onMinus = { if (turnTimer > 10) turnTimer -= 10 },
            onPlus = { turnTimer += 10 },
            cardColor = cardColor,
            primaryTextColor = primaryTextColor
        )

        SettingRow(
            title = "Starting Cards",
            value = "$startingCards",
            onMinus = { if (startingCards > 1) startingCards -= 1 },
            onPlus = { startingCards += 1 },
            cardColor = cardColor,
            primaryTextColor = primaryTextColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stack +2/+4",
                    color = primaryTextColor,
                    style = MaterialTheme.typography.titleMedium
                )

                Switch(
                    checked = stackEnabled,
                    onCheckedChange = { stackEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Game")
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invite")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PlayerItem(
    name: String,
    subtitle: String,
    borderColor: Color,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                color = primaryTextColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
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
    primaryTextColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = primaryTextColor,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onMinus,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("-")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = value,
                    color = primaryTextColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onPlus,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("+")
                }
            }
        }
    }
}

private fun generateRoomCode(length: Int = 6): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..length)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}
