package com.duchastel.simon.photocategorizer.screens.login

import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.duchastel.simon.photocategorizer.MainActivity

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val redirectIntent = remember(context) {
        PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ Intent(context, MainActivity::class.java),
            /* flags = */ PendingIntent.FLAG_MUTABLE,
        )
    }

    LoginContent(
        onLoginClicked = { viewModel.login(redirectIntent) },
    )
}

@Composable
private fun LoginContent(
    onLoginClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onLoginClicked,
        ) {
            Text("Login")
        }
    }
}
