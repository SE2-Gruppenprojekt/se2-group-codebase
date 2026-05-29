package at.aau.serg.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

@Composable
fun appColors(): AppColors {
    val darkMode = ThemeState.isDarkMode.value
    return AppColors(
        screen = ScreenColors(
            bgTop = if (darkMode) MaterialTheme.colorScheme.background else LightScreenBgTop,
            bgBottom = if (darkMode) MaterialTheme.colorScheme.surface else LightScreenBgBottom,
            card = MaterialTheme.colorScheme.surface,
            cardBorder = if (darkMode) CardBorderDark else CardBorderLight,
            primaryText = MaterialTheme.colorScheme.onSurface,
            secondaryText = if (darkMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else AuthLightSecondaryText,
            actionButton = if (darkMode) ActionButtonDark else ActionButtonLight,
            disabledButton = if (darkMode) DisabledButtonDark else DisabledButtonLight,
            selectedBox = if (darkMode) LobbySelectedDark else ActionButtonLight,
            selectedBorder = if (darkMode) LobbySelectedBorderDark else LobbySelectedBorderLight
        ),
        home = HomeColors(
            bgTop = if (darkMode) MaterialTheme.colorScheme.background else HomeLightBgTop,
            bgMid = if (darkMode) HomeDarkGradientMid else HomeLightGradientMid,
            bgBottom = if (darkMode) HomeDarkGradientBottom else HomeLightGradientBottom,
            title = if (darkMode) HomeDarkTitle else HomeLightTitle,
            subtitle = if (darkMode) Color.White.copy(alpha = 0.78f) else HomeLightSubtitle,
            error = if (darkMode) HomeDarkError else HomeLightError,
            settingsGradientStart = if (darkMode) HomeDarkSettingsGradientStart else HomeLightSettingsGradientStart,
            settingsGradientEnd = if (darkMode) HomeDarkSettingsGradientEnd else HomeLightSettingsGradientEnd,
            playerBar = if (darkMode) HomeDarkPlayerBar else HomeLightPlayerBar,
            playerBarBorder = if (darkMode) Color.White.copy(alpha = 0.05f) else HomeLightPlayerBarBorder,
            playerIconBg = if (darkMode) HomeDarkPlayerIcon else HomeLightPlayerIcon,
            playerName = if (darkMode) Color.White else HomeLightPlayerName,
            playerLevel = if (darkMode) AccentYellow else HomeLightPlayerLevel,
            xp = if (darkMode) AuthDarkSecondaryText else HomeLightXp,
            loadingIndicator = if (darkMode) Color.White else AccentPurple,
            buttonText = if (darkMode) Color.White else Color.Black
        ),
        game = GameColors(
            background = if (darkMode) GameDarkBackground else GameLightBackground,
            surface = if (darkMode) GameDarkSurface else Color.White,
            boardBorder = if (darkMode) Color.White.copy(alpha = 0.2f) else GameLightBoardBorder,
            iconBtnBg = if (darkMode) GameDarkIconBtnBg else GameLightIconBtnBg,
            iconBtnTint = if (darkMode) Color.White else GameLightIconBtnTint,
            endTurnButton = AccentPurple
        ),
        settings = SettingsColors(
            background = if (darkMode) AuthDarkBackground else AuthLightBackground,
            card = if (darkMode) AuthDarkCard else AuthLightCard,
            primaryText = if (darkMode) AuthDarkPrimaryText else AuthLightPrimaryText,
            secondaryText = if (darkMode) AuthDarkSecondaryText else AuthLightSecondaryText,
            divider = if (darkMode) SettingsDividerDark else SettingsDividerLight,
            iconBlueBg = if (darkMode) AuthDarkCardBlue else SettingsIconBlueBgLight,
            iconBlueTint = if (darkMode) SettingsIconBlueTintDark else SettingsIconBlueTintLight,
            iconPurpleBg = if (darkMode) AuthDarkCardPurple else AuthLightCardPurple,
            iconRedBg = if (darkMode) SettingsIconRedBgDark else SettingsIconRedBgLight,
            chevron = if (darkMode) SettingsChevronDark else SettingsChevronLight
        ),
        auth = AuthColors(
            background = if (darkMode) AuthDarkBackground else AuthLightBackground,
            card = if (darkMode) AuthDarkCard else AuthLightCard,
            input = if (darkMode) AuthDarkInput else AuthLightInput,
            primaryText = if (darkMode) AuthDarkPrimaryText else AuthLightPrimaryText,
            secondaryText = if (darkMode) AuthDarkSecondaryText else AuthLightSecondaryText,
            cardBlue = if (darkMode) AuthDarkCardBlue else AuthLightCardBlue,
            borderBlue = if (darkMode) AuthDarkBorderBlue else AuthLightBorderBlue,
            cardPurple = if (darkMode) AuthDarkCardPurple else AuthLightCardPurple,
            borderPurple = if (darkMode) AuthDarkBorderPurple else AuthLightBorderPurple,
            cardGreen = if (darkMode) AuthDarkCardGreen else AuthLightCardGreen,
            borderGreen = if (darkMode) AuthDarkBorderGreen else AuthLightBorderGreen
        ),
        waiting = WaitingColors(
            activePlayerBg = if (darkMode) WaitingActivePlayerBgDark else WaitingActivePlayerBgLight,
            activePlayerBorder = if (darkMode) AuthDarkBorderBlue else WaitingActivePlayerBorderLight,
            selfPlayerBg = if (darkMode) WaitingSecondPlayerBgDark else WaitingSecondPlayerBgLight,
            selfPlayerBorder = if (darkMode) WaitingSecondPlayerBorderDark else WaitingSecondPlayerBorderLight,
            placeholderBg = if (darkMode) WaitingBgDark else Color.White,
            placeholderBorder = if (darkMode) WaitingPlaceholderBorderDark else WaitingPlaceholderBorderLight,
            placeholderPrimaryText = if (darkMode) WaitingPlaceholderPrimaryDark else WaitingPlaceholderPrimaryLight,
            placeholderSecondaryText = if (darkMode) WaitingPlaceholderSecondaryDark else WaitingPlaceholderSecondaryLight
        )
    )
}
