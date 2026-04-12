package at.aau.serg.android.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.screens.settings.components.BackButton
import at.aau.serg.android.ui.screens.settings.components.DarkModeToggle
import at.aau.serg.android.ui.screens.settings.components.SettingsHeader
import at.aau.serg.android.ui.theme.ThemeState
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        SettingsHeader()

        Spacer(Modifier.height(16.dp))

        DarkModeToggle()

        Spacer(Modifier.height(16.dp))

        BackButton(onBack)
    }
}
