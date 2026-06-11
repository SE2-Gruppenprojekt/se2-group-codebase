package at.aau.serg.android.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.components.TopBar
import at.aau.serg.android.ui.theme.appColors

@Composable
fun RulesScreen(
    onBack: () -> Unit = {}
) {
    RulesScreenContent(onBack = onBack)
}

@Composable
fun RulesScreenContent(
    onBack: () -> Unit
) {
    val c = appColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.settings.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag(RulesTestTags.SCREEN)
    ) {

        TopBar(
            subtitle = "Game Rules",
            onBack = onBack,
            backButtonModifier = Modifier.testTag(RulesTestTags.BACK_BUTTON)
        )

        Text(
            text = "TODO: Rummikub rules content",
            style = MaterialTheme.typography.bodyLarge,
            color = c.settings.primaryText,
            modifier = Modifier
                .padding(top = 18.dp)
                .testTag(RulesTestTags.CONTENT)
        )
    }
}
