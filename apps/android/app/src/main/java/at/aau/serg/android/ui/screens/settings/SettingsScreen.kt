package at.aau.serg.android.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.general_components.BackButton
import at.aau.serg.android.ui.screens.settings.components.DarkModeToggle
import at.aau.serg.android.ui.screens.settings.components.SettingsHeader
import at.aau.serg.android.ui.screens.settings.components.SettingsTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangeUsername: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        SettingsTopBar(onBack)

        Spacer(Modifier.height(16.dp))

        DarkModeToggle()

        Button(
            onClick = {
                onChangeUsername()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Username")
        }

        Spacer(Modifier.height(16.dp))



    }
}
