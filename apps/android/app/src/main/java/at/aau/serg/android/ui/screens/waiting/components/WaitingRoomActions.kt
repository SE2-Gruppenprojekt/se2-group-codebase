package at.aau.serg.android.ui.screens.waiting.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaitingRoomActions(
    onStart: () -> Unit,
    onInvite: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStart,
            modifier = Modifier.weight(1f)
        ) {
            Text("Start Game")
        }

        Button(
            onClick = onInvite,
            modifier = Modifier.weight(1f)
        ) {
            Text("Invite")
        }
    }
}
