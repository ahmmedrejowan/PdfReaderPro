package com.rejowan.pdfreaderpro.presentation

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.presentation.navigation.Home
import com.rejowan.pdfreaderpro.presentation.navigation.Onboarding
import com.rejowan.pdfreaderpro.presentation.navigation.PdfReaderNavGraph
import com.rejowan.pdfreaderpro.presentation.theme.PdfReaderProTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val preferencesRepository: PreferencesRepository by inject()

    private var isReady by mutableStateOf(false)
    private var startDestination: Any by mutableStateOf(Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash visible while loading preferences
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Determine start destination
        lifecycleScope.launch {
            val prefs = preferencesRepository.preferences.first()
            val hasPermission = Environment.isExternalStorageManager()

            startDestination = if (prefs.hasCompletedOnboarding && hasPermission) {
                Home
            } else {
                Onboarding
            }
            isReady = true
        }

        setContent {
            PdfReaderProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isReady) {
                        PdfReaderNavGraph(startDestination = startDestination)
                    }
                }
            }
        }
    }
}
