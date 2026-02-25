package com.rejowan.pdfreaderpro.presentation

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.presentation.components.PermissionBottomSheet
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
    private var showPermissionSheet by mutableStateOf(false)
    private var hasCompletedOnboarding by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash visible while loading preferences
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Determine start destination
        lifecycleScope.launch {
            val prefs = preferencesRepository.preferences.first()
            hasCompletedOnboarding = prefs.hasCompletedOnboarding
            val hasPermission = Environment.isExternalStorageManager()

            startDestination = if (hasCompletedOnboarding && hasPermission) {
                Home
            } else if (hasCompletedOnboarding && !hasPermission) {
                // Onboarding done but permission revoked - go to Home but show sheet
                showPermissionSheet = true
                Home
            } else {
                Onboarding
            }
            isReady = true
        }

        // Re-check permission when returning from settings
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (isReady && hasCompletedOnboarding) {
                    val hasPermission = Environment.isExternalStorageManager()
                    if (hasPermission) {
                        showPermissionSheet = false
                    }
                }
            }
        }

        setContent {
            PdfReaderProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isReady) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            PdfReaderNavGraph(startDestination = startDestination)

                            if (showPermissionSheet) {
                                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                                LaunchedEffect(Unit) {
                                    sheetState.show()
                                }

                                PermissionBottomSheet(
                                    sheetState = sheetState,
                                    onDismiss = { showPermissionSheet = false },
                                    onGrantClick = { /* Settings will open, permission checked on resume */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
