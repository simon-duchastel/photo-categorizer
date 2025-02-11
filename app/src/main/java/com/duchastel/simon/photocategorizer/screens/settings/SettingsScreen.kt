package com.duchastel.simon.photocategorizer.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(onLogoutClicked = viewModel::onLogoutClicked)
}


@Composable
private fun SettingsContent(
    onLogoutClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Settings",
            modifier = Modifier.padding(top = 32.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.padding(bottom = 32.dp),
            onClick = onLogoutClicked,
        ) { Text("Logout") }
    }
}