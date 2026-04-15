package at.aau.serg.android.ui.screens.createlobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.createlobby.components.CreateLobbyActions
import at.aau.serg.android.ui.screens.createlobby.components.CreateLobbyHeader
import at.aau.serg.android.ui.screens.createlobby.components.LobbyNameField

@Composable
fun CreateLobbyScreen(
    onBack: () -> Unit,
    onCreate: (String) -> Unit
) {
    var lobbyName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        CreateLobbyHeader()

        LobbyNameField(
            value = lobbyName,
            onValueChange = { lobbyName = it }
        )

        CreateLobbyActions(
            lobbyName = lobbyName,
            onCreate = onCreate,
            onBack = onBack
        )
    }
}
