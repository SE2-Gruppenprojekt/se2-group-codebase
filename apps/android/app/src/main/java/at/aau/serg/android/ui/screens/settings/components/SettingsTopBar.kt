package at.aau.serg.android.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.components.BackButton

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        BackButton(onBack)

        Spacer(Modifier.width(8.dp))

        SettingsHeader()
    }
}
