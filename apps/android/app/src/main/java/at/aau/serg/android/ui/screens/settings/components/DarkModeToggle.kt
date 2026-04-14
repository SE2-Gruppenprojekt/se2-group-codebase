package at.aau.serg.android.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import at.aau.serg.android.ui.theme.ThemeState

@Composable
fun DarkModeToggle() {

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Dark Mode")

        Switch(
            checked = ThemeState.isDarkMode.value,
            onCheckedChange = {
                ThemeState.isDarkMode.value = it
            }
        )
    }
}
