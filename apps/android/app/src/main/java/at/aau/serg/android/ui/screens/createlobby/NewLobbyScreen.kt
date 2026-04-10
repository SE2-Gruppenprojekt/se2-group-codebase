package at.aau.serg.android.ui.screens.createlobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.lobby.LobbyUiState
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun NewLobbyScreen(
    onBack: () -> Unit,
    onCreateLobby: (String) -> Unit
) {
    val darkMode = ThemeState.isDarkMode.value

    // theme-aware colors
    val bgTop = if (darkMode) Color(0xFF24103F) else Color(0xFFF4F6FF)
    val bgBottom = if (darkMode) Color(0xFF0C1430) else Color(0xFFE9EEFF)
    val cardColor = if (darkMode) Color(0xFF18213D) else MaterialTheme.colorScheme.surface
    val cardBorder = if (darkMode) Color(0xFF2A3558) else Color(0xFFD8DEF0)
    val primaryText = MaterialTheme.colorScheme.onBackground
    val selectedColor = if (darkMode) Color(0xFF2A4D92) else Color(0xFFDCE7FF)
    val selectedBorder = Color(0xFF4B8CFF)
    val actionGreen = Color(0xFF22C55E)

    // local form state
    var lobbyName by remember { mutableStateOf("Alex's Room") }
    var maxPlayers by remember { mutableIntStateOf(6) }
    var isPrivate by remember { mutableStateOf(false) }
    var turnTimer by remember { mutableIntStateOf(60) }
    var startingTiles by remember { mutableIntStateOf(14) }
    var winScore by remember { mutableIntStateOf(500) }
    var quickMode by remember { mutableStateOf(false) }
    var voiceChat by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgTop, bgBottom)
                )
            )
            .verticalScroll(rememberScrollState()) // allow smaller screens to scroll
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryText
                    )
                }

                Column {
                    Text(
                        text = "RUMMIKUB",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C8CFF)
                    )
                    Text(
                        text = "Create New Lobby",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryText
                    )
                }
            }

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Groups, null, tint = Color(0xFF7C8CFF)) },
            title = "Lobby Name"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingCard(cardColor) {
            // lobby name input
            OutlinedTextField(
                value = lobbyName,
                onValueChange = { lobbyName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    focusedBorderColor = selectedBorder,
                    unfocusedBorderColor = cardBorder,
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    cursorColor = selectedBorder
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Groups, null, tint = Color(0xFF5FE07A)) },
            title = "Maximum Players"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(2, 4, 6, 8).forEach { count ->
                // equal-width player count boxes
                SelectableBox(
                    text = count.toString(),
                    selected = maxPlayers == count,
                    onClick = { maxPlayers = count },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    cardColor = cardColor,
                    selectedColor = selectedColor,
                    borderColor = cardBorder,
                    selectedBorder = selectedBorder,
                    textColor = primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFFC084FC)) },
            title = "Privacy"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LargeSelectableBox(
                title = "Public",
                icon = { Icon(Icons.Filled.Public, null, tint = primaryText) },
                selected = !isPrivate,
                onClick = { isPrivate = false },
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                cardColor = cardColor,
                selectedColor = selectedColor,
                borderColor = cardBorder,
                selectedBorder = selectedBorder,
                textColor = primaryText
            )

            LargeSelectableBox(
                title = "Private",
                icon = { Icon(Icons.Filled.Lock, null, tint = primaryText) },
                selected = isPrivate,
                onClick = { isPrivate = true },
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                cardColor = cardColor,
                selectedColor = selectedColor,
                borderColor = cardBorder,
                selectedBorder = selectedBorder,
                textColor = primaryText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(
            icon = { Icon(Icons.Filled.Timer, null, tint = Color(0xFFFFC857)) },
            title = "Game Settings"
        )

        Spacer(modifier = Modifier.height(8.dp))

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Timer, null, tint = Color(0xFF60A5FA)) },
            title = "Turn Timer",
            value = "${turnTimer}s",
            onMinus = { if (turnTimer > 10) turnTimer -= 10 },
            onPlus = { turnTimer += 10 },
            cardColor = cardColor,
            textColor = primaryText
        )

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Groups, null, tint = Color(0xFF4ADE80)) },
            title = "Starting Tiles",
            value = startingTiles.toString(),
            onMinus = { if (startingTiles > 1) startingTiles -= 1 },
            onPlus = { startingTiles += 1 },
            cardColor = cardColor,
            textColor = primaryText
        )

        NumericSettingRow(
            icon = { Icon(Icons.Filled.Star, null, tint = Color(0xFFFACC15)) },
            title = "Win Score",
            value = winScore.toString(),
            onMinus = { if (winScore > 100) winScore -= 100 },
            onPlus = { winScore += 100 },
            cardColor = cardColor,
            textColor = primaryText
        )

        ToggleSettingRow(
            icon = { Icon(Icons.Filled.Speed, null, tint = Color(0xFFFB923C)) },
            title = "Quick Mode",
            checked = quickMode,
            onCheckedChange = { quickMode = it },
            cardColor = cardColor,
            textColor = primaryText
        )

        ToggleSettingRow(
            icon = { Icon(Icons.Filled.Mic, null, tint = Color(0xFFC084FC)) },
            title = "Voice Chat",
            checked = voiceChat,
            onCheckedChange = { voiceChat = it },
            cardColor = cardColor,
            textColor = primaryText
        )

        // moved near other toggle settings
        ToggleSettingRow(
            icon = { Icon(Icons.Filled.DarkMode, null, tint = Color(0xFF7C8CFF)) },
            title = if (darkMode) "Dark Mode" else "Light Mode",
            checked = ThemeState.isDarkMode.value,
            onCheckedChange = { ThemeState.isDarkMode.value = it },
            cardColor = cardColor,
            textColor = primaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    // store selected values for waiting room
                    LobbyUiState.lobbyName.value = lobbyName
                    LobbyUiState.maxPlayers.intValue = maxPlayers
                    LobbyUiState.turnTimer.intValue = turnTimer
                    LobbyUiState.startingCards.intValue = startingTiles
                    LobbyUiState.stackEnabled.value = quickMode
                    LobbyUiState.roomCode.value = ""

                    onCreateLobby(lobbyName)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionGreen),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Lobby")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .width(72.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SectionTitle(
    icon: @Composable () -> Unit,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SettingCard(
    cardColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SelectableBox(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color,
    selectedColor: Color,
    borderColor: Color,
    selectedBorder: Color,
    textColor: Color
) {
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (selected) selectedBorder else borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) selectedColor else cardColor
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LargeSelectableBox(
    title: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color,
    selectedColor: Color,
    borderColor: Color,
    selectedBorder: Color,
    textColor: Color
) {
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (selected) selectedBorder else borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) selectedColor else cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NumericSettingRow(
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    cardColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onMinus,
                    modifier = Modifier.size(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = value,
                    color = textColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onPlus,
                    modifier = Modifier.size(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+")
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    icon: @Composable () -> Unit,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    cardColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // simple reusable switch row
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
