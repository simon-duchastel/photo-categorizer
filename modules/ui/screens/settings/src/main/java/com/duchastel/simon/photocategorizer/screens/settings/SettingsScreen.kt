package com.duchastel.simon.photocategorizer.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import com.duchastel.simon.photocategorizer.ui.components.ValidatedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(
        state = state,
        onBackendTypeChanged = viewModel::onBackendTypeChanged,
        onCameraRollPathChanged = viewModel::onCameraRollPathChanged,
        onDestinationFolderPathChanged = viewModel::onDestinationFolderPathChanged,
        onArchiveFolderPathChanged = viewModel::onArchiveFolderPathChanged,
        onSaveClicked = viewModel::onSaveClicked,
        onResetToDefaultsClicked = viewModel::onResetToDefaultsClicked,
        onLogoutClicked = viewModel::onLogoutClicked,
        onSuccessMessageShown = viewModel::onSuccessMessageShown,
        onErrorMessageShown = viewModel::onErrorMessageShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsViewModel.State,
    onBackendTypeChanged: (BackendType) -> Unit,
    onCameraRollPathChanged: (String) -> Unit,
    onDestinationFolderPathChanged: (String) -> Unit,
    onArchiveFolderPathChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onResetToDefaultsClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onSuccessMessageShown: () -> Unit,
    onErrorMessageShown: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading settings...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            // Backend Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cloud Storage Backend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = state.userSettings.backendType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Backend Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            BackendType.entries.forEach { backendType ->
                                DropdownMenuItem(
                                    text = { Text(backendType.displayName) },
                                    onClick = {
                                        onBackendTypeChanged(backendType)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Folder Configuration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Folder Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Camera Roll Path
                    ValidatedTextField(
                        value = state.userSettings.cameraRollPath,
                        onValueChange = onCameraRollPathChanged,
                        label = "Camera Roll Location",
                        supportingText = "Source folder containing photos to categorize",
                        errorMessage = state.cameraRollPathError
                    )

                    // Destination Folder Path
                    ValidatedTextField(
                        value = state.userSettings.destinationFolderPath,
                        onValueChange = onDestinationFolderPathChanged,
                        label = "Destination Folder",
                        supportingText = "Target folder for right swipe categorization",
                        errorMessage = state.destinationFolderPathError
                    )

                    // Archive Folder Path
                    ValidatedTextField(
                        value = state.userSettings.archiveFolderPath,
                        onValueChange = onArchiveFolderPathChanged,
                        label = "Archive Folder",
                        supportingText = "Target folder for up swipe archiving",
                        errorMessage = state.archiveFolderPathError
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onResetToDefaultsClicked,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset to Defaults")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onSaveClicked,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(if (state.isSaving) "Saving..." else "Save Settings")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogoutClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }

        // Success/Error Messages
        if (state.showSuccessMessage) {
            LaunchedEffect(state.showSuccessMessage) {
                delay(3000) // Show snackbar for 3 seconds
                onSuccessMessageShown()
            }
            
            Snackbar(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Settings saved successfully!")
            }
        }

        if (state.showErrorMessage) {
            LaunchedEffect(state.showErrorMessage) {
                delay(3000) // Show snackbar for 3 seconds
                onErrorMessageShown()
            }
            
            Snackbar(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Error saving settings. Please try again.")
            }
        }
    }
}