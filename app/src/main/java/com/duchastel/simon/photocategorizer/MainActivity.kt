package com.duchastel.simon.photocategorizer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import com.duchastel.simon.photocategorizer.ui.theme.PhotoCategorizerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject @Dropbox lateinit var authProvider: AuthProvider
    @Inject @Dropbox lateinit var fileManager: FileManager

    private fun createIntent(): PendingIntent {
        val intent = Intent(this, this::class.java)
        return PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_MUTABLE,
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        authProvider.processIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authProvider.processIntent(intent)

        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            var timer: Int by remember { mutableIntStateOf(1) }
            var fileNames: List<String> by remember { mutableStateOf(emptyList()) }
            LaunchedEffect(scope) {
                while (timer > 0) {
                    delay(1000)
                    timer -= 1
                }
                scope.launch {
                    try {
                        fileNames = fileManager.listPhotos().map { it.name }
                    } catch (ex: Exception) {
                        println("NETWORK ERROR: $ex")
                    }
                }
            }

            PhotoCategorizerTheme {
                Scaffold(modifier = Modifier.fillMaxSize().padding(16.dp)) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .scrollable(rememberScrollState(), Orientation.Vertical)
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                        Text(text = "$timer")

                        fileNames.forEach {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(modifier = Modifier.padding(start = 16.dp), text = it)
                                Spacer(Modifier.weight(1f))
                            }
                        }

                        Button(
                            onClick = { authProvider.login(createIntent()) },
                        ) {
                            Text(text = "Login")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhotoCategorizerTheme {
        Greeting("Android")
    }
}