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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import at.aau.serg.android.datastore.proto.User
import at.aau.serg.android.ui.screens.auth.AuthViewModel
import at.aau.serg.android.ui.screens.home.HomeViewModel
import at.aau.serg.android.ui.theme.AuthButtonGradientStart
import at.aau.serg.android.ui.theme.AuthDarkBackground
import at.aau.serg.android.ui.theme.AuthDarkCard
import at.aau.serg.android.ui.theme.AuthDarkPrimaryText
import at.aau.serg.android.ui.theme.AuthDarkSecondaryText
import at.aau.serg.android.ui.theme.AuthLightBackground
import at.aau.serg.android.ui.theme.AuthLightCard
import at.aau.serg.android.ui.theme.AuthLightPrimaryText
import at.aau.serg.android.ui.theme.AuthLightSecondaryText
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onChangeUsername: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
) {
    val darkMode = isDarkMode

    val background   = if (darkMode) AuthDarkBackground    else AuthLightBackground
    val card         = if (darkMode) AuthDarkCard          else AuthLightCard
    val primaryText  = if (darkMode) AuthDarkPrimaryText   else AuthLightPrimaryText
    val secondaryText = if (darkMode) AuthDarkSecondaryText else AuthLightSecondaryText
    val divider      = if (darkMode) Color(0xFF1E2A40)     else Color(0xFFE5E7EB)
    val iconBlueBg   = if (darkMode) Color(0xFF1A2D55)     else Color(0xFFDEEAFF)
    val iconBlueTint = if (darkMode) Color(0xFF60A5FA)     else Color(0xFF3B82F6)
    val iconPurpleBg = if (darkMode) Color(0xFF241A55)     else Color(0xFFF0EBFF)
    val iconPurpleTint = Color(0xFF8B5CF6)
    val iconRedBg    = if (darkMode) Color(0xFF3A1A1A)     else Color(0xFFFFEBEB)
    val iconRedTint  = Color(0xFFEF4444)
    val chevron      = if (darkMode) Color(0xFF4A5568)     else Color(0xFFCBD5E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {

        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Settings",
                color = primaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.height(28.dp))

        // --- APPEARANCE section ---
        Text(
            text = "APPEARANCE",
            color = secondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(card)
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
                        .background(iconBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = null,
                        tint = iconBlueTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Mode",
                        color = primaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Switch app theme",
                        color = secondaryText,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AuthButtonGradientStart,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = secondaryText.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("settings_darkmode_switch")
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- ACCOUNT section ---
        Text(
            text = "ACCOUNT",
            color = secondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(card)
        ) {
            // Change Username row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onChangeUsername)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconPurpleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = iconPurpleTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change Username",
                        color = primaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Update your display name",
                        color = secondaryText,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = chevron,
                    modifier = Modifier.size(14.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = divider
            )

            // Logout row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = iconRedTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logout",
                        color = iconRedTint,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Sign out and clear saved data",
                        color = secondaryText,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = chevron,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
