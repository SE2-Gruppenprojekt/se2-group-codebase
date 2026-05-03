package at.aau.serg.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.theme.AuthButtonGradientStart
import at.aau.serg.android.ui.theme.AuthDarkBorderPurple
import at.aau.serg.android.ui.theme.SettingsIconRedTint
import at.aau.serg.android.ui.theme.ThemeState
import at.aau.serg.android.ui.theme.appColors


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    SettingsScreenContent(onEvent = viewModel::onEvent)
}

@Composable
fun SettingsScreenContent(
    onEvent: (SettingsEvent) -> Unit
) {
    val c = appColors()
    val darkMode = ThemeState.isDarkMode.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.settings.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp)
            .testTag(SettingsTestTags.SCREEN)
    ) {

        TopBar(
            subtitle = "Settings",
            onBack = { onEvent(SettingsEvent.OnBack) },
            backButtonModifier = Modifier.testTag(SettingsTestTags.BACK_BUTTON)
        )

        Spacer(Modifier.height(28.dp))

        // --- APPEARANCE section ---
        Text(
            text = "APPEARANCE",
            color = c.settings.secondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(c.settings.card)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(c.settings.iconBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = null,
                        tint = c.settings.iconBlueTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Mode",
                        color = c.settings.primaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Switch app theme",
                        color = c.settings.secondaryText,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = darkMode,
                    onCheckedChange = { onEvent(SettingsEvent.SetDarkMode(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AuthButtonGradientStart,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = c.settings.secondaryText.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag(SettingsTestTags.DARK_MODE_SWITCH)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- ACCOUNT section ---
        Text(
            text = "ACCOUNT",
            color = c.settings.secondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(c.settings.card)
        ) {
            // Change Username row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {onEvent(SettingsEvent.OnChangeUsername) })
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag(SettingsTestTags.CHANGE_USERNAME_BUTTON),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(c.settings.iconPurpleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AuthDarkBorderPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change Username",
                        color = c.settings.primaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Update your display name",
                        color = c.settings.secondaryText,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = c.settings.chevron,
                    modifier = Modifier.size(14.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = c.settings.divider
            )

            // Logout row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onEvent(SettingsEvent.OnLogout) })
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag(SettingsTestTags.LOGOUT_BUTTON),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(c.settings.iconRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = SettingsIconRedTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logout",
                        color = SettingsIconRedTint,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Sign out and clear saved data",
                        color = c.settings.secondaryText,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = c.settings.chevron,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
