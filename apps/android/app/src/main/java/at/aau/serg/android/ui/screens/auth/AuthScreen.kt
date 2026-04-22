package at.aau.serg.android.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.android.ui.components.BackButton
import at.aau.serg.android.ui.theme.AuthButtonGradientEnd
import at.aau.serg.android.ui.theme.AuthButtonGradientStart
import at.aau.serg.android.ui.theme.AuthDarkBackground
import at.aau.serg.android.ui.theme.AuthDarkBorderBlue
import at.aau.serg.android.ui.theme.AuthDarkBorderGreen
import at.aau.serg.android.ui.theme.AuthDarkBorderPurple
import at.aau.serg.android.ui.theme.AuthDarkCard
import at.aau.serg.android.ui.theme.AuthDarkCardBlue
import at.aau.serg.android.ui.theme.AuthDarkCardGreen
import at.aau.serg.android.ui.theme.AuthDarkCardPurple
import at.aau.serg.android.ui.theme.AuthDarkInput
import at.aau.serg.android.ui.theme.AuthDarkPrimaryText
import at.aau.serg.android.ui.theme.AuthDarkSecondaryText
import at.aau.serg.android.ui.theme.AuthGradientEnd
import at.aau.serg.android.ui.theme.AuthGradientStart
import at.aau.serg.android.ui.theme.AuthLightBackground
import at.aau.serg.android.ui.theme.AuthLightBorderBlue
import at.aau.serg.android.ui.theme.AuthLightBorderGreen
import at.aau.serg.android.ui.theme.AuthLightBorderPurple
import at.aau.serg.android.ui.theme.AuthLightCard
import at.aau.serg.android.ui.theme.AuthLightCardBlue
import at.aau.serg.android.ui.theme.AuthLightCardGreen
import at.aau.serg.android.ui.theme.AuthLightCardPurple
import at.aau.serg.android.ui.theme.AuthLightInput
import at.aau.serg.android.ui.theme.AuthLightPrimaryText
import at.aau.serg.android.ui.theme.AuthLightSecondaryText
import at.aau.serg.android.ui.theme.ThemeState
import at.aau.serg.android.ui.theme.AuthColors
import at.aau.serg.android.ui.screens.auth.components.SuggestionChip


private fun authColors(darkMode: Boolean): AuthColors = if (darkMode) {
    AuthColors(
        background = AuthDarkBackground,
        card = AuthDarkCard,
        input = AuthDarkInput,
        primaryText = AuthDarkPrimaryText,
        secondaryText = AuthDarkSecondaryText,
        cardBlue = AuthDarkCardBlue,
        borderBlue = AuthDarkBorderBlue,
        cardPurple = AuthDarkCardPurple,
        borderPurple = AuthDarkBorderPurple,
        cardGreen = AuthDarkCardGreen,
        borderGreen = AuthDarkBorderGreen,
    )
} else {
    AuthColors(
        background = AuthLightBackground,
        card = AuthLightCard,
        input = AuthLightInput,
        primaryText = AuthLightPrimaryText,
        secondaryText = AuthLightSecondaryText,
        cardBlue = AuthLightCardBlue,
        borderBlue = AuthLightBorderBlue,
        cardPurple = AuthLightCardPurple,
        borderPurple = AuthLightBorderPurple,
        cardGreen = AuthLightCardGreen,
        borderGreen = AuthLightBorderGreen,
    )
}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    val username = uiState.username
    val validation = uiState.validation

    val isValid = validation.isValid
    val darkMode = ThemeState.isDarkMode.value
    val colors = authColors(darkMode)

    val titleGradient = Brush.horizontalGradient(listOf(AuthGradientStart, AuthGradientEnd))
    val buttonGradient = Brush.horizontalGradient(listOf(AuthButtonGradientStart, AuthButtonGradientEnd))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag(AuthTestTags.SCREEN)
    ) {
        // scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Back button (only when navigated from within the app) ---
            if (onBack != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    BackButton(
                        onBack = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .background(colors.card),
                        tint = colors.primaryText
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // --- Header ---
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            brush = titleGradient,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    ) { append("RUMMIKUB") }
                },
                textAlign = TextAlign.Center
            )
            Text(
                text = "Welcome to the Game",
                color = colors.secondaryText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // --- Avatar ---
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(AuthGradientStart, AuthGradientEnd)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = AuthGradientStart,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // --- Section heading ---
            Text(
                text = "Create Your Profile",
                color = colors.primaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose a unique username to get started",
                color = colors.secondaryText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // --- Username label ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = AuthGradientStart,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Username",
                    color = colors.primaryText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // --- Username text field ---
            OutlinedTextField(
                value = username,
                onValueChange = viewModel::onUsernameChanged,
                placeholder = {
                    Text("Enter your username", color = colors.secondaryText)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                },
                isError = !isValid && username.isNotBlank(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = colors.primaryText,
                    unfocusedTextColor = colors.primaryText,
                    focusedContainerColor = colors.input,
                    unfocusedContainerColor = colors.input,
                    disabledContainerColor = colors.input,
                    cursorColor = AuthGradientStart,
                    focusedIndicatorColor = AuthGradientStart,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = colors.input,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorTextColor = colors.primaryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AuthTestTags.INPUT)
            )

            if (!isValid && username.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthTestTags.ERROR_TEXT)
                ) {
                    validation.violations.forEach {
                        Text(
                            text = it.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ℹ  3-16 characters, letters and numbers only",
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            // --- Quick Suggestions ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.LightbulbCircle,
                    contentDescription = null,
                    tint = Color(0xFFFFD93D),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Quick Suggestions",
                    color = colors.primaryText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            val suggestions = listOf(
                "Player7429" to Icons.Filled.SportsEsports,
                "Gamer456" to Icons.Filled.Star,
                "TempUser123" to Icons.Filled.WorkspacePremium,
                "BestGamerEver" to Icons.Filled.LocalFireDepartment,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { (label, icon) ->
                            SuggestionChip(
                                label = label,
                                icon = icon,
                                colors = colors,
                                onClick = { viewModel.onUsernameChanged(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // --- Continue button + footer pinned at bottom ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isValid) Modifier.background(buttonGradient)
                        else Modifier.background(colors.secondaryText.copy(alpha = 0.3f))
                    )
                    .clickable(enabled = isValid) { viewModel.submit(onContinue) }
                    .testTag(AuthTestTags.CONTINUE_BUTTON),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("By continuing, you agree to our ")
                    withStyle(SpanStyle(color = AuthGradientStart)) { append("Terms of Service") }
                },
                color = colors.secondaryText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
