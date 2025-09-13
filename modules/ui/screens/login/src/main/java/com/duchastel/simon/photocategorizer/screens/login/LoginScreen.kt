package com.duchastel.simon.photocategorizer.screens.login

import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.duchastel.simon.photocategorizer.ui.components.LoadingButton
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val redirectIntent = remember(context) {
        PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ context.packageManager.getLaunchIntentForPackage(context.packageName),
            /* flags = */ PendingIntent.FLAG_MUTABLE,
        )
    }

    LoginContent(
        loginInProgress = state.loginInProgress,
        onLoginClicked = { viewModel.login(redirectIntent) },
    )
}

@Composable
private fun LoginContent(
    loginInProgress: Boolean,
    onLoginClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LoadingButton(
            text = "Login",
            isLoading = loginInProgress,
            onClick = onLoginClicked,
        )
    }
}

// Previews

@Preview
@Composable
private fun LoginPreview() {
    LoginContent(
        loginInProgress = false,
        onLoginClicked = { },
    )
}

@Preview
@Composable
private fun LoginPreviewLoading() {
    LoginContent(
        loginInProgress = true,
        onLoginClicked = { },
    )
}
