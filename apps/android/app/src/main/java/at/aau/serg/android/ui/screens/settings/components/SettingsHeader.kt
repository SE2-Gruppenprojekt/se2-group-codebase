package at.aau.serg.android.ui.screens.settings.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun SettingsHeader() {
    Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineMedium
    )
}
