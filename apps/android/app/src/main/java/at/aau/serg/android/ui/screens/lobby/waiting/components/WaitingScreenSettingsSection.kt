package at.aau.serg.android.ui.screens.lobby.waiting.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun WaitingScreenSettingsSection(
    turnTimer: Int,
    startingCards: Int,
    stackEnabled: Boolean,

    onTurnTimerMinus: () -> Unit,
    onTurnTimerPlus: () -> Unit,
    onStartingCardsMinus: () -> Unit,
    onStartingCardsPlus: () -> Unit,
    onStackToggle: (Boolean) -> Unit,

    cardColor: Color,
    primaryTextColor: Color,
    buttonColor: Color
) {
    Column {

        SettingRow(
            title = "Turn Timer",
            value = "${turnTimer}s",
            onMinus = onTurnTimerMinus,
            onPlus = onTurnTimerPlus,
            cardColor = cardColor,
            primaryTextColor = primaryTextColor,
            buttonColor = buttonColor
        )

        SettingRow(
            title = "Starting Cards",
            value = "$startingCards",
            onMinus = onStartingCardsMinus,
            onPlus = onStartingCardsPlus,
            cardColor = cardColor,
            primaryTextColor = primaryTextColor,
            buttonColor = buttonColor
        )
    }
}
