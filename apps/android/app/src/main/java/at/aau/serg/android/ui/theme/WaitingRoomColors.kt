package at.aau.serg.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class WaitingRoomColors(
    val gradientTop: Color,
    val gradientBottom: Color,

    val card: Color,
    val primaryText: Color,
    val secondaryText: Color,

    val activePlayerBackground: Color,
    val activePlayerBorder: Color,

    val secondPlayerBackground: Color,
    val secondPlayerBorder: Color,

    val waitingBackground: Color,

    val inviteButton: Color,

    val settingsBackground: Color


)

@Composable
fun waitingRoomColors(darkMode: Boolean): WaitingRoomColors {
    return if (darkMode) {
        WaitingRoomColors(
            gradientTop = MaterialTheme.colorScheme.background,
            gradientBottom = MaterialTheme.colorScheme.surface,

            card = MaterialTheme.colorScheme.surface,
            primaryText = MaterialTheme.colorScheme.onSurface,
            secondaryText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),

            activePlayerBackground = Color(0xFF1F356A),
            activePlayerBorder = Color(0xFF3E73E8),

            secondPlayerBackground = Color(0xFF1E3A2D),
            secondPlayerBorder = Color(0xFF2BC46D),

            waitingBackground = MaterialTheme.colorScheme.surface,

            inviteButton = Color(0xFF2A3552),

            settingsBackground = Color.White.copy(alpha = 0.06f)
        )
    } else {
        WaitingRoomColors(
            gradientTop = Color(0xFFF5F7FB),
            gradientBottom = Color(0xFFEAEFFF),

            card = Color.White,
            primaryText = Color.Black,
            secondaryText = Color(0xFF6B7280),

            activePlayerBackground = Color(0xFFEAF1FF),
            activePlayerBorder = Color(0xFF4C84FF),

            secondPlayerBackground = Color(0xFFEAFBF1),
            secondPlayerBorder = Color(0xFF20C76F),

            waitingBackground = Color.White,

            inviteButton = Color(0xFF2F3A57),

            settingsBackground = Color.Black.copy(alpha = 0.04f)
        )
    }
}
