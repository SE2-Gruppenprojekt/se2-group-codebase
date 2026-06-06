package at.aau.serg.android.ui.screens.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.core.errors.ApiRuleViolation
import at.aau.serg.android.ui.screens.game.GameTestTags
import at.aau.serg.android.ui.theme.NotReadyRed

@Composable
fun GlobalValidationBanner(
    summaryMessage: String?,
    violations: List<ApiRuleViolation>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NotReadyRed.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(GameTestTags.GLOBAL_VALIDATION_BANNER)
    ) {
        Text(
            text = summaryMessage ?: "Invalid move",
            fontWeight = FontWeight.Bold,
            color = NotReadyRed,
            fontSize = 13.sp
        )
        violations.forEach { violation ->
            Text(
                text = "• ${violation.message}",
                color = NotReadyRed,
                fontSize = 12.sp
            )
        }
    }
}
