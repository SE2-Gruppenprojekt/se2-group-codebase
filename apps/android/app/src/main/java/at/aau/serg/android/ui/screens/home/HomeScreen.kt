package at.aau.serg.android.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.state.LoadState

@Composable
fun HomeScreen(
    state: LoadState,
    modifier: Modifier = Modifier,
    onCreateLobby: () -> Unit,
    onBrowseLobbies: () -> Unit,
    onShowLeaderboard: () -> Unit,
    onSettings: () -> Unit,
    onWaitingRoom: () -> Unit
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090F22),
            Color(0xFF121A31),
            Color(0xFF0E1429)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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
            color = Color(0xFFEAEFFF)
        )

        Text(
            text = "Classic Tile Game",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.78f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (state is LoadState.Loading) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (state is LoadState.Error) {
            Text(
                text = state.message,
                color = Color(0xFFFF8F8F),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        HomeActionButton(
            text = "Create Lobby",
            onClick = onCreateLobby,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            },
            containerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF4B68FF), Color(0xFF4B3FD4))
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        HomeActionButton(
            text = "Browse Lobbies",
            onClick = onBrowseLobbies,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            },
            containerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF9D3CFF), Color(0xFF7D23D7))
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        HomeActionButton(
            text = "Waiting Room",
            onClick = onWaitingRoom,
            icon = {
                Icon(
                    imageVector = Icons.Filled.ViewInAr,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            },
            containerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF4C59E8), Color(0xFF3154C8))
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            text = "Settings",
            onClick = onSettings,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            },
            containerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF2D3951), Color(0xFF253046))
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        HomeActionButton(
            text = "Leaderboard",
            onClick = onShowLeaderboard,
            icon = {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            },
            containerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF33435B), Color(0xFF273347))
            )
        )
    }
}

@Composable
private fun HomeActionButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    containerBrush: Brush
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = ButtonDefaults.ContentPadding
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
                icon()
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
