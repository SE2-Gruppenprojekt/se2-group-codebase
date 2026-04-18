package at.aau.serg.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import shared.validation.ValidationResult


@Composable
fun UsernameInput(
    value: String,
    onValueChange: (String) -> Unit,
    validation: ValidationResult
) {
    val isError = !validation.isValid

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Username") },
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )

        if (isError) {
            Column {
                validation.violations.forEach {
                    Text(
                        text = it.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
