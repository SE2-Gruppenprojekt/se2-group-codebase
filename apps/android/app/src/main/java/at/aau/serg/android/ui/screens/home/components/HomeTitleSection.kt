package at.aau.serg.android.ui.screens.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.aau.serg.android.ui.theme.AccentBlue
import at.aau.serg.android.ui.theme.HomeIconGradientEnd

@Composable
fun HomeTitleSection(
    titleColor: Color,
    subtitleColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(AccentBlue, HomeIconGradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ViewInAr, contentDescription = null, tint = Color.White)
        }

        Spacer(Modifier.height(22.dp))

        Text("RUMMIKUB", color = titleColor, fontWeight = FontWeight.Black)
        Text("Classic Tile Game", color = subtitleColor)
    }
}
