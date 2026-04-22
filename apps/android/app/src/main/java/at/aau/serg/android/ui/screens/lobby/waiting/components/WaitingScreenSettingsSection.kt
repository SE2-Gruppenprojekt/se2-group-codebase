package at.aau.serg.android.ui.screens.lobby.waiting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WaitingScreenSettingsSection(
    turnTimer: MutableIntState,
    startingCards: MutableIntState,
    stackEnabled: MutableState<Boolean>,

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
            value = "${turnTimer.intValue}s",
            onMinus = onTurnTimerMinus,
            onPlus = onTurnTimerPlus,
            cardColor = cardColor,
            primaryTextColor = primaryTextColor,
            buttonColor = buttonColor
        )

        SettingRow(
            title = "Starting Cards",
            value = "${startingCards.intValue}",
            onMinus = onStartingCardsMinus,
            onPlus = onStartingCardsPlus,
            cardColor = cardColor,
            primaryTextColor = primaryTextColor,
            buttonColor = buttonColor
        )
    }
}

@Composable
fun SettingRow(
    title: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    cardColor: Color,
    primaryTextColor: Color,
    buttonColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                color = primaryTextColor,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onMinus,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.width(10.dp))

                // value
                Text(
                    text = value,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(10.dp))

                // plus button
                Button(
                    onClick = onPlus,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
