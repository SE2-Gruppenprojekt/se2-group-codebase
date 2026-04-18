package at.aau.serg.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.theme.ThemeState
import at.aau.serg.android.util.UserPrefs

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangeUsername: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val darkMode = ThemeState.isDarkMode.value

    val bgGradient = if (darkMode)
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF121A31), Color(0xFF0E1429)))
    else
        Brush.verticalGradient(listOf(Color(0xFFF6F8FD), Color(0xFFEFF3FF), Color(0xFFE7ECFA)))

    val primaryText = if (darkMode) Color.White else Color(0xFF1D2750)
    val secondaryText = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF4D5A78)
    val cardBg = if (darkMode) Color(0xFF1E293B) else Color.White
    val cardBorder = if (darkMode) Color(0xFF334155) else Color(0xFFD5DDEA)
    val dividerColor = if (darkMode) Color(0xFF1E293B) else Color(0xFFEEF1F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryText
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
