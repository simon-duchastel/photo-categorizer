package com.duchastel.simon.photocategorizer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchastel.simon.photocategorizer.auth.AuthManager
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import com.duchastel.simon.photocategorizer.navigation.AppNavigation
import com.duchastel.simon.photocategorizer.ui.theme.PhotoCategorizerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject @Dropbox lateinit var authManager: AuthManager
    @Inject @Dropbox lateinit var fileManager: FileManager

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        authManager.processIntent(intent)
    }

    private fun createIntent(): PendingIntent {
        val intent = Intent(this, this::class.java)
        return PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_MUTABLE,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager.processIntent(intent)

        println("TODO - ON CREATE")

        enableEdgeToEdge()
        setContent {
            PhotoCategorizerTheme {
                Scaffold(modifier = Modifier.fillMaxSize().padding(16.dp)) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
