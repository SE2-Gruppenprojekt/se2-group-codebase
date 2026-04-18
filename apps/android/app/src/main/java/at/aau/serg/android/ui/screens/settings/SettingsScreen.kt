package at.aau.serg.android.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.components.UsernameInput
import at.aau.serg.android.ui.screens.settings.components.DarkModeToggle
import at.aau.serg.android.ui.screens.settings.components.SettingsTopBar
import shared.validation.user.DisplayNameValidator

@Composable
fun SettingsScreen(
    user: User,
    onUsernameChange: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {

    var username by remember(user.displayName) {
        mutableStateOf(user.displayName)
    }

    val validation = remember(username) {
        DisplayNameValidator.validate(username)
    }

    val isChanged = username != user.displayName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        SettingsTopBar(onBack = onBack)

        Spacer(Modifier.height(16.dp))

        DarkModeToggle()

        Spacer(Modifier.height(24.dp))

        UsernameInput(
            value = username,
            onValueChange = {
                username = it
                onUsernameChange(it)
            },
            validation = validation
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (validation.isValid) {
                    onUsernameChange(username)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isChanged && validation.isValid
        ) {
            Text("Save Username")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
