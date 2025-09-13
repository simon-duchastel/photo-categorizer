package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * A generic Snackbar component that automatically dismisses after a specified duration.
 * 
 * This component encapsulates both the timing logic (LaunchedEffect) and the Snackbar display,
 * making it reusable across the application for showing temporary messages.
 *
 * @param message The text message to display in the snackbar
 * @param isVisible Whether the snackbar should be visible
 * @param onDismiss Callback invoked when the snackbar should be dismissed
 * @param duration Duration in milliseconds before auto-dismissal (default: 3000ms)
 * @param modifier Modifier to be applied to the snackbar
 */
@Composable
fun AutoDismissSnackbar(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    duration: Long = 3000L,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        LaunchedEffect(isVisible) {
            delay(duration)
            onDismiss()
        }
        
        Snackbar(
            modifier = modifier
        ) {
            Text(message)
        }
    }
}