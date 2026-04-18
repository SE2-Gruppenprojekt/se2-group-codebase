package at.aau.serg.android.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onContinue: () -> Unit
) {

    val username by viewModel.username.collectAsState()
    val validation by viewModel.validation.collectAsState()

    val isValid = validation.isValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .testTag(AuthTestTags.SCREEN),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Choose Username",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = viewModel::onUsernameChanged,
            label = {
                Text(
                    "Username",
                    color = Color.White
                )
            },
            isError = !isValid && username.isNotBlank(),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.Gray,

                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,

                cursorColor = Color.White,
                focusedIndicatorColor = Color(0xFF60A5FA),
                unfocusedIndicatorColor = Color.Gray,

                errorCursorColor = MaterialTheme.colorScheme.error,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorTextColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AuthTestTags.INPUT)
        )

        if (!isValid && username.isNotBlank()) {
            Column(
                modifier = Modifier.testTag(AuthTestTags.ERROR_TEXT)
            ) {
                validation.violations.forEach {
                    Text(
                        text = it.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.submit(onContinue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AuthTestTags.CONTINUE_BUTTON),
            enabled = isValid
        ) {
            Text("Continue")
        }
    }
}
