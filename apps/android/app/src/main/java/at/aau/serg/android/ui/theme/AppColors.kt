package at.aau.serg.android.ui.theme

import androidx.compose.ui.graphics.Color

data class ScreenColors(
    val bgTop: Color,
    val bgBottom: Color,
    val card: Color,
    val cardBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val actionButton: Color,
    val disabledButton: Color,
    val selectedBox: Color,
    val selectedBorder: Color
)

data class HomeColors(
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val title: Color,
    val subtitle: Color,
    val error: Color,
    val settingsGradientStart: Color,
    val settingsGradientEnd: Color,
    val playerBar: Color,
    val playerBarBorder: Color,
    val playerIconBg: Color,
    val playerName: Color,
    val playerLevel: Color,
    val xp: Color,
    val loadingIndicator: Color,
    val buttonText: Color
)

data class SettingsColors(
    val background: Color,
    val card: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val divider: Color,
    val iconBlueBg: Color,
    val iconBlueTint: Color,
    val iconPurpleBg: Color,
    val iconRedBg: Color,
    val chevron: Color
)

data class GameColors(
    val background: Color,
    val surface: Color,
    val boardBorder: Color,
    val iconBtnBg: Color,
    val iconBtnTint: Color,
    val endTurnButton: Color
)

data class WaitingColors(
    val activePlayerBg: Color,
    val activePlayerBorder: Color,
    val selfPlayerBg: Color,
    val selfPlayerBorder: Color,
    val placeholderBg: Color,
    val placeholderBorder: Color,
    val placeholderPrimaryText: Color,
    val placeholderSecondaryText: Color
)

data class AppColors(
    val screen: ScreenColors,
    val home: HomeColors,
    val settings: SettingsColors,
    val game: GameColors,
    val auth: AuthColors,
    val waiting: WaitingColors
)

