package at.aau.serg.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

internal val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

val LocalAppColors = compositionLocalOf { buildAppColors(darkMode = false, colorScheme = LightColorScheme) }

val MaterialTheme.appColors: AppColors
    @Composable @ReadOnlyComposable get() = LocalAppColors.current

@Composable
fun TempappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = buildAppColors(darkTheme, colorScheme)

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

internal fun buildAppColors(darkMode: Boolean, colorScheme: ColorScheme): AppColors = AppColors(
    screen = ScreenColors(
        bgTop = if (darkMode) colorScheme.background else LightScreenBgTop,
        bgBottom = if (darkMode) colorScheme.surface else LightScreenBgBottom,
        card = colorScheme.surface,
        cardBorder = if (darkMode) CardBorderDark else CardBorderLight,
        primaryText = colorScheme.onSurface,
        secondaryText = if (darkMode) colorScheme.onSurface.copy(alpha = 0.7f) else AuthLightSecondaryText,
        actionButton = if (darkMode) ActionButtonDark else ActionButtonLight,
        disabledButton = if (darkMode) DisabledButtonDark else DisabledButtonLight,
        selectedBox = if (darkMode) LobbySelectedDark else ActionButtonLight,
        selectedBorder = if (darkMode) LobbySelectedBorderDark else LobbySelectedBorderLight
    ),
    home = HomeColors(
        bgTop = if (darkMode) colorScheme.background else HomeLightBgTop,
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