package at.aau.serg.android.ui.screens.lobby.create.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateLobbyActions(
    lobbyName: String,
    onCreate: (String) -> Unit,
    onBack: () -> Unit
) {
    Column {
        Button(
            onClick = { onCreate(lobbyName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
