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

data class AppColors(
    val screen: ScreenColors,
    val home: HomeColors,
    val settings: SettingsColors
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
        )
    )
}
