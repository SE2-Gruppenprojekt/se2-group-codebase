package at.aau.serg.android.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalContextProvider = staticCompositionLocalOf<Context> {
    error("No context provided")
}

@Composable
fun ProvideLocalContext(content: @Composable () -> Unit) {
    val context = LocalContext.current
    androidx.compose.runtime.CompositionLocalProvider(
        LocalContextProvider provides context,
        content = content
    )
}
