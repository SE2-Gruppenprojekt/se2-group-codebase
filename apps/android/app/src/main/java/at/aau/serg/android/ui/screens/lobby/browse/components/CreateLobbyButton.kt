package at.aau.serg.android.ui.screens.lobby.browse.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import at.aau.serg.android.ui.theme.AccentPurple

@Composable
fun CreateLobbyButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentPurple,
            contentColor = Color.White
        )
    ) {
        Text("Create New Lobby")
    }
}
