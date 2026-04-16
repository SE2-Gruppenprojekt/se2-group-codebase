package at.aau.serg.android.ui.screens.username

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.viewmodel.UsernameViewModel

@Composable
fun UsernameScreen(
    viewModel: UsernameViewModel,
    onContinue: () -> Unit = {}
) {
    val username = viewModel.username.collectAsState().value
    val error = viewModel.usernameError.collectAsState().value
    val loadState = viewModel.loadState.collectAsState().value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Choose Username",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Username") },
            isError = error != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.submit(context) {
                    onContinue()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && error == null && loadState !is LoadState.Loading
        ) {
            Text("Continue")
        }

        if (loadState is LoadState.Loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
