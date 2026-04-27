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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

        Spacer(Modifier.height(8.dp))

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
                    text = "Stack +2/+4",
                    color = primaryTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = stackEnabled,
                    onCheckedChange = onStackToggle,
                    modifier = Modifier.scale(0.78f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = buttonColor,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.9f),
                        uncheckedTrackColor = buttonColor.copy(alpha = 0.55f),
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    )
                )
            }
        }
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
