package com.duchastel.simon.photocategorizer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.duchastel.simon.photocategorizer.auth.AuthRepository
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import com.duchastel.simon.photocategorizer.navigation.AppNavigation
import com.duchastel.simon.photocategorizer.ui.theme.PhotoCategorizerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject @Dropbox lateinit var authRepository: AuthRepository
    @Inject @Dropbox lateinit var photoRepository: PhotoRepository

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        authRepository.processIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authRepository.processIntent(intent)

        enableEdgeToEdge()
        setContent {
            PhotoCategorizerTheme {
                AppNavigation()
            }
        }
    }
}
