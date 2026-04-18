package at.aau.serg.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
            .background(bgGradient)
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

            // APPEARANCE SECTION
            SectionLabel(text = "Appearance", color = secondaryText)

            SettingsCard(cardBg = cardBg, cardBorder = cardBorder) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIcon(
                            icon = Icons.Filled.DarkMode,
                            iconBg = if (darkMode) Color(0xFF1E3A5F) else Color(0xFFEFF6FF),
                            iconTint = Color(0xFF4FC3F7)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Dark Mode", color = primaryText, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Switch app theme", color = secondaryText, fontSize = 12.sp)
                        }
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { ThemeState.isDarkMode.value = it },
                        modifier = Modifier.testTag("settings_darkmode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6C63FF)
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ACCOUNT SECTION
            SectionLabel(text = "Account", color = secondaryText)

            SettingsCard(cardBg = cardBg, cardBorder = cardBorder) {
                Column {
                    SettingsRow(
                        icon = Icons.Filled.Person,
                        iconBg = if (darkMode) Color(0xFF2D1B69) else Color(0xFFF5F3FF),
                        iconTint = Color(0xFF9B8FFF),
                        title = "Change Username",
                        subtitle = "Update your display name",
                        titleColor = primaryText,
                        subtitleColor = secondaryText,
                        onClick = onChangeUsername
                    )

                    Divider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Filled.Logout,
                        iconBg = if (darkMode) Color(0xFF3D1515) else Color(0xFFFFF1F1),
                        iconTint = Color(0xFFEF4444),
                        title = "Logout",
                        subtitle = "Sign out and clear saved data",
                        titleColor = Color(0xFFEF4444),
                        subtitleColor = secondaryText,
                        onClick = {
                            UserPrefs.clear(context)
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    cardBg: Color,
    cardBorder: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, iconBg: Color, iconTint: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingsIcon(icon = icon, iconBg = iconBg, iconTint = iconTint)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, color = titleColor, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text(subtitle, color = subtitleColor, fontSize = 12.sp)
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = subtitleColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
