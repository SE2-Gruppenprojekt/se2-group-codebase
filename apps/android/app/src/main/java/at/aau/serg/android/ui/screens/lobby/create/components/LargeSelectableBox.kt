package at.aau.serg.android.ui.screens.lobby.create.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LargeSelectableBox(
    title: String,
    icon: @Composable (Color) -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color,
    selectedColor: Color,
    borderColor: Color,
    selectedBorder: Color,
    textColor: Color,
    selectedTextColor: Color
) {
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (selected) selectedBorder else borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) selectedColor else cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val contentColor = if (selected) selectedTextColor else textColor
            Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(contentColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
