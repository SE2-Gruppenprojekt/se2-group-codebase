package at.aau.serg.android.ui.screens.browselobbies

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.theme.ThemeState
import androidx.compose.material.icons.automirrored.filled.ArrowBack

data class LobbyBrowseItem(
    val lobbyId: String,
    val hostId: String,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val turnTimerSeconds: Int,
    val startingCards: Int,
    val isOpen: Boolean,
    val accentColor: Color
)

@Composable
fun BrowsingLobbiesScreen(
    lobbies: List<LobbyBrowseItem>,
    onJoinLobby: (String) -> Unit,
    onCreateNewLobby: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit
) {
    val darkMode = ThemeState.isDarkMode.value
    var searchQuery by remember { mutableStateOf("") }

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
    val selectedCardColor = Color(0xF2B670FC)
    val actionButtonColor = if (darkMode) Color(0xFF2A3552) else Color(0xFF2F3A57)

    val onlineCardColor = cardColor
    val onlineTextColor = secondaryText

    val filteredLobbies = lobbies.filter {
        searchQuery.isBlank() ||
            it.lobbyId.contains(searchQuery, ignoreCase = true) ||
            it.hostId.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        // header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "RUMMIKUB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Available Lobbies",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = onlineCardColor
                ) {
                    Text(
                        text = "● Online",
                        color = onlineTextColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontWeight = FontWeight.SemiBold
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

        Spacer(modifier = Modifier.height(18.dp))

        // search row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text(
                        "Search lobbies...",
                        color = secondaryText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = secondaryText
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
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
                onClick = {
                    filteredLobbies.firstOrNull { it.isOpen }?.let { onJoinLobby(it.lobbyId) }
                },
                enabled = filteredLobbies.any { it.isOpen },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionButtonColor,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Join",
                    fontWeight = FontWeight.Bold
                )
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
                text = "${filteredLobbies.size} available",
                style = MaterialTheme.typography.labelMedium,
                color = secondaryText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // lobby list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredLobbies) { lobby ->
                LobbyBrowseCard(
                    lobby = lobby,
                    cardColor = cardColor,
                    borderColor = borderColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    selectedCardColor = selectedCardColor,
                    actionButtonColor = actionButtonColor,
                    onJoinLobby = onJoinLobby
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // create button
        Button(
            onClick = onCreateNewLobby,
            modifier = Modifier
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
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

@Composable
private fun LobbyBrowseCard(
    lobby: LobbyBrowseItem,
    cardColor: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    selectedCardColor: Color,
    actionButtonColor: Color,
    onJoinLobby: (String) -> Unit
) {
    val statusColor = if (lobby.isOpen) Color.White else secondaryText
    val buttonColor = if (lobby.isOpen) selectedCardColor else actionButtonColor.copy(alpha = 0.55f)
    val buttonText = if (lobby.isOpen) "Join" else "Full"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Lobby ${lobby.lobbyId}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeChip(
                        text = if (lobby.currentPlayers >= lobby.maxPlayers) "FULL" else "OPEN",
                        backgroundColor = if (lobby.isOpen) {
                            selectedCardColor
                        } else {
                            actionButtonColor.copy(alpha = 0.3f)
                        },
                        textColor = statusColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Host: ${lobby.hostId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lobby.turnTimerSeconds}s",
                        color = secondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Filled.Style,
                        contentDescription = null,
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lobby.startingCards} cards",
                        color = secondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        tint = primaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lobby.currentPlayers}/${lobby.maxPlayers}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (lobby.isOpen) "OPEN" else "FULL",
                    color = if (lobby.isOpen) selectedCardColor else secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onJoinLobby(lobby.lobbyId) },
                    enabled = lobby.isOpen,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF555A6E),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
