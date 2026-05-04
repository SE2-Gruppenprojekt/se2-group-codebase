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

            activePlayerBackground = WaitingActivePlayerBgDark,
            activePlayerBorder = AuthDarkBorderBlue,

            secondPlayerBackground = WaitingSecondPlayerBgDark,
            secondPlayerBorder = WaitingSecondPlayerBorderDark,

            waitingBackground = MaterialTheme.colorScheme.surface,

            inviteButton = ActionButtonDark,

            settingsBackground = Color.White.copy(alpha = 0.06f)
        )
    } else {
        WaitingRoomColors(
            gradientTop = LightScreenBgTop,
            gradientBottom = LightScreenBgBottom,

            card = Color.White,
            primaryText = Color.Black,
            secondaryText = AuthLightSecondaryText,

            activePlayerBackground = WaitingActivePlayerBgLight,
            activePlayerBorder = WaitingActivePlayerBorderLight,

            secondPlayerBackground = WaitingSecondPlayerBgLight,
            secondPlayerBorder = WaitingSecondPlayerBorderLight,

            waitingBackground = Color.White,

            inviteButton = ActionButtonLight,

            settingsBackground = Color.Black.copy(alpha = 0.04f)
        )
    }
}
