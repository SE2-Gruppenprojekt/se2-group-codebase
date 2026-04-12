package at.aau.serg.android.ui.screens.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BackButton(onBack: () -> Unit) {

    OutlinedButton(
        onClick = onBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Back")
    }
}
