package com.duchastel.simon.photocategorizer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import com.duchastel.simon.photocategorizer.photoswiper.PhotoSwiperScreen
import com.duchastel.simon.photocategorizer.ui.theme.PhotoCategorizerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject @Dropbox lateinit var authProvider: AuthProvider
    @Inject @Dropbox lateinit var fileManager: FileManager

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        authProvider.processIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authProvider.processIntent(intent)

        enableEdgeToEdge()
        setContent {
            PhotoCategorizerTheme {
                Scaffold(modifier = Modifier.fillMaxSize().padding(16.dp)) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        PhotoSwiperScreen()
                    }
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, this::class.java)
        }
    }
}
