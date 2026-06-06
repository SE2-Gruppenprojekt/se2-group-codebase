package at.aau.serg.android.ui.screens.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.core.errors.ApiRuleViolation
import at.aau.serg.android.ui.theme.NotReadyRed

@Composable
fun BoardSetValidationMessage(
    violations: List<ApiRuleViolation>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        violations.forEach { violation ->
            Text(
                text = violation.message,
                color = NotReadyRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
