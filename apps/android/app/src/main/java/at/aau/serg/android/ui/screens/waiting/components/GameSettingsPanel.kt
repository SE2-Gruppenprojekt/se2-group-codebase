package at.aau.serg.android.ui.screens.waiting.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameSettingsPanel(
    turnTimer: Int,
    startingCards: Int,
    stackEnabled: Boolean,
    onTurnMinus: () -> Unit,
    onTurnPlus: () -> Unit,
    onCardsMinus: () -> Unit,
    onCardsPlus: () -> Unit,
    onStackToggle: (Boolean) -> Unit
) {
    Column {

        Text("Game Settings")

        Spacer(modifier = Modifier.height(8.dp))

        SettingRow("Turn Timer", "$turnTimer", onTurnMinus, onTurnPlus)
        SettingRow("Starting Cards", "$startingCards", onCardsMinus, onCardsPlus)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stack +2/+4")

            Switch(
                checked = stackEnabled,
                onCheckedChange = onStackToggle
            )
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(title)

        Row(verticalAlignment = Alignment.CenterVertically) {

            Button(onClick = onMinus) {
                Text("-")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(value)

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onPlus) {
                Text("+")
            }
        }
    }
}


