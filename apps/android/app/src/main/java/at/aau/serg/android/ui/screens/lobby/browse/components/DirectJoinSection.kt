package at.aau.serg.android.ui.screens.lobby.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun DirectJoinSection(
    lobbyIdInput: String,
    onLobbyIdChange: (String) -> Unit,
    onJoin: () -> Unit,
    enabled: Boolean
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = lobbyIdInput,
            onValueChange = onLobbyIdChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("Lobby ID") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9D3CFF).copy(alpha = 0.5f),
                unfocusedBorderColor = Color(0xFF9D3CFF).copy(alpha = 0.25f),
                focusedLabelColor = Color(0xFF9D3CFF).copy(alpha = 0.5f),
                cursorColor = Color(0xFF9D3CFF).copy(alpha = 0.5f)
            )
        )

        Button(
            onClick = onJoin,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9D3CFF),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF9D3CFF).copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Text("Join by ID")
        }
    }
}
