package at.aau.serg.android.ui.screens.username

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.viewmodel.UsernameViewModel

private val suggestions = listOf(
    "Player_7429" to Icons.Filled.Diamond,
    "TileKing92" to Icons.Filled.Star,
    "ProGamer_X" to Icons.Filled.EmojiEvents,
    "Blaze2024" to Icons.Filled.Whatshot
)

@Composable
fun UsernameScreen(
    viewModel: UsernameViewModel,
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val username by viewModel.username.collectAsState()
    val error by viewModel.usernameError.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val context = LocalContext.current

    val isLoading = loadState is LoadState.Loading

    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF1A1040), Color(0xFF0D0D2B), Color(0xFF0A0A1F))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // BACK BUTTON
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            // HEADER
            Text(
                text = "RUMMIKUB",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF7B6EF6), Color(0xFF4FC3F7))
                    )
                )
            )
            Text(
                text = "Welcome to the Game",
                color = Color(0xFFB0B8D0),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(28.dp))

            // AVATAR
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF3D3580), Color(0xFF1E1B4B))
                        )
                    )
                    .border(2.dp, Color(0xFF6C63FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color(0xFF8B9EC7),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Create Your Profile",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a unique username to get started",
                color = Color(0xFF8892AA),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // USERNAME INPUT
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color(0xFF6C9EFF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Username",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.onUsernameChanged(it) },
                    placeholder = { Text("Enter your username", color = Color(0xFF5A6480)) },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = Color(0xFF5A6480),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color(0xFF2A2F4A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6C63FF),
                        errorBorderColor = Color(0xFFEF4444),
                        unfocusedContainerColor = Color(0xFF141830),
                        focusedContainerColor = Color(0xFF141830)
                    )
                )

                Spacer(Modifier.height(6.dp))

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF5A6480),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "3-16 characters, letters and numbers only",
                            color = Color(0xFF5A6480),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // SUGGESTIONS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Quick Suggestions",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (name, icon) ->
                            SuggestionChip(
                                name = name,
                                icon = icon,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onUsernameChanged(name) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // FEATURE CARDS
            FeatureCard(
                icon = Icons.Filled.EmojiEvents,
                iconBg = Color(0xFF1E3A5F),
                iconTint = Color(0xFF4FC3F7),
                title = "Track Your Progress",
                subtitle = "Earn achievements and climb the leaderboard"
            )
            Spacer(Modifier.height(8.dp))
            FeatureCard(
                icon = Icons.Filled.Groups,
                iconBg = Color(0xFF2D1B69),
                iconTint = Color(0xFF9B8FFF),
                title = "Play With Friends",
                subtitle = "Create private lobbies and invite players"
            )
            Spacer(Modifier.height(8.dp))
            FeatureCard(
                icon = Icons.Filled.TrendingUp,
                iconBg = Color(0xFF0F3D2A),
                iconTint = Color(0xFF4ADE80),
                title = "View Statistics",
                subtitle = "Analyze your gameplay and improve your skills"
            )

            Spacer(Modifier.height(28.dp))

            // CONTINUE BUTTON
            Button(
                onClick = {
                    viewModel.submit(context) { onContinue() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = username.isNotBlank() && error == null && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (username.isNotBlank() && error == null && !isLoading)
                                Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFFAB5BF5)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFF3A3A5C), Color(0xFF3A3A5C)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Continue",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row {
                Text(
                    text = "By continuing, you agree to our ",
                    color = Color(0xFF5A6480),
                    fontSize = 12.sp
                )
                Text(
                    text = "Terms of Service",
                    color = Color(0xFF6C9EFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    name: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141830))
            .border(1.dp, Color(0xFF2A2F4A), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6C9EFF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141830))
            .border(1.dp, Color(0xFF1E2340), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF8892AA),
                fontSize = 12.sp
            )
        }
    }
}
