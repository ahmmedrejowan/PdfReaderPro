package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.DeleteConfirmDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ErrorState
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageJumpDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PasswordDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PdfInfoDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderBottomBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderTopBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.SearchOverlay
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.SettingsPanel
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.TableOfContentsSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReaderScreen(
    navController: NavController,
    path: String,
    initialPage: Int = 0,
    viewModel: ReaderViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.state.collectAsState()

    val activity = context as? Activity

    // Background color for PdfViewer
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    // Handle full screen mode
    DisposableEffect(state.isFullScreen) {
        activity?.let {
            val window = it.window
            val controller = WindowCompat.getInsetsController(window, view)

            if (state.isFullScreen) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            activity?.let {
                val window = it.window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Handle keep screen on
    DisposableEffect(state.keepScreenOn) {
        activity?.let {
            if (state.keepScreenOn) {
                it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Handle rotation lock
    DisposableEffect(state.isRotationLocked) {
        activity?.let {
            it.requestedOrientation = if (state.isRotationLocked) {
                when (it.resources.configuration.orientation) {
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE ->
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    else ->
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReaderEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ReaderEvent.NavigateToPage -> { }
                is ReaderEvent.DocumentClosed -> {
                    navController.popBackStack()
                }
                is ReaderEvent.DocumentDeleted -> {
                    navController.popBackStack()
                }
                is ReaderEvent.ShareDocument -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, path.toUri())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                }
                is ReaderEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Password dialog - show when library requests it
    if (state.isPasswordRequired) {
        PasswordDialog(
            isError = state.isPasswordError,
            onSubmit = { password, remember ->
                viewModel.onAction(ReaderAction.SubmitPassword(password, remember))
            },
            onDismiss = {
                navController.popBackStack()
            }
        )
    }

    // Error state
    state.error?.let { error ->
        if (!state.isLoading) {
            ErrorState(
                message = error,
                onRetry = { },
                onBack = { navController.popBackStack() }
            )
            return
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = state.isToolbarVisible && !state.isFullScreen,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                ReaderTopBar(
                    title = state.documentTitle ?: "PDF Reader",
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = { viewModel.onAction(ReaderAction.ToggleSearch) },
                    onTableOfContentsClick = { viewModel.onAction(ReaderAction.ShowTableOfContents) },
                    onSettingsClick = { viewModel.onAction(ReaderAction.ShowSettingsPanel) }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.isToolbarVisible && !state.isFullScreen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                ReaderBottomBar(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    onPageChange = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                    onPageJumpClick = { viewModel.onAction(ReaderAction.ShowPageJumpDialog) },
                    onThumbnailsClick = { viewModel.onAction(ReaderAction.ShowPageThumbnails) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // PDF Viewer using AndroidView
            AndroidView(
                factory = { ctx ->
                    PdfViewer(ctx).apply {
                        // Set up the viewer
                        setBackgroundColor(backgroundColor)

                        // Configure UI settings
                        onReady {
                            ui.toolbarEnabled = false
                            ui.isSideBarOpen = false

                            // Load the PDF file
                            loadFromFile(viewModel.pdfPath)
                        }

                        // Register with ViewModel
                        viewModel.setPdfViewer(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { pdfViewer ->
                    // Update scroll mode when it changes
                    val targetScrollMode = when (state.scrollDirection) {
                        ScrollDirection.VERTICAL -> PdfViewer.PageScrollMode.VERTICAL
                        ScrollDirection.HORIZONTAL -> PdfViewer.PageScrollMode.HORIZONTAL
                    }
                    if (pdfViewer.isInitialized && pdfViewer.pageScrollMode != targetScrollMode) {
                        pdfViewer.pageScrollMode = targetScrollMode
                    }
                }
            )

            // Loading overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Search overlay
            AnimatedVisibility(
                visible = state.isSearchActive,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                SearchOverlay(
                    query = state.searchQuery,
                    isSearching = state.isSearching,
                    resultCount = state.searchResultCount,
                    currentIndex = state.currentSearchIndex,
                    onQueryChange = { viewModel.onAction(ReaderAction.Search(it)) },
                    onPreviousResult = { viewModel.onAction(ReaderAction.PreviousSearchResult) },
                    onNextResult = { viewModel.onAction(ReaderAction.NextSearchResult) },
                    onClose = {
                        viewModel.onAction(ReaderAction.ClearSearch)
                        viewModel.onAction(ReaderAction.ToggleSearch)
                    }
                )
            }
        }
    }

    // Table of Contents sheet
    if (state.isTableOfContentsVisible) {
        TableOfContentsSheet(
            items = state.outline,
            currentPage = state.currentPage,
            onItemClick = { item ->
                viewModel.onAction(ReaderAction.GoToPage(item.page))
                viewModel.onAction(ReaderAction.HideTableOfContents)
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideTableOfContents) }
        )
    }

    // Page jump dialog
    if (state.isPageJumpDialogVisible) {
        PageJumpDialog(
            currentPage = state.currentPage + 1,
            totalPages = state.totalPages,
            onPageSelected = {
                viewModel.onAction(ReaderAction.GoToPage(it - 1))
                viewModel.onAction(ReaderAction.HidePageJumpDialog)
            },
            onDismiss = { viewModel.onAction(ReaderAction.HidePageJumpDialog) }
        )
    }

    // Settings panel
    if (state.isSettingsPanelVisible) {
        SettingsPanel(
            brightness = state.brightness,
            scrollDirection = state.scrollDirection,
            keepScreenOn = state.keepScreenOn,
            isRotationLocked = state.isRotationLocked,
            onBrightnessChange = { viewModel.onAction(ReaderAction.SetBrightness(it)) },
            onScrollDirectionChange = { viewModel.onAction(ReaderAction.SetScrollDirection(it)) },
            onKeepScreenOnChange = { viewModel.onAction(ReaderAction.SetKeepScreenOn(it)) },
            onRotationLockChange = { viewModel.onAction(ReaderAction.ToggleRotationLock) },
            onFullScreenClick = { viewModel.onAction(ReaderAction.ToggleFullScreen) },
            onInfoClick = {
                viewModel.onAction(ReaderAction.HideSettingsPanel)
                viewModel.onAction(ReaderAction.ShowInfoDialog)
            },
            onDeleteClick = {
                viewModel.onAction(ReaderAction.HideSettingsPanel)
                viewModel.onAction(ReaderAction.ShowDeleteDialog)
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideSettingsPanel) }
        )
    }

    // PDF info dialog
    if (state.isInfoDialogVisible) {
        PdfInfoDialog(
            info = viewModel.getPdfInfo(),
            onDismiss = { viewModel.onAction(ReaderAction.HideInfoDialog) }
        )
    }

    // Delete confirmation dialog
    if (state.isDeleteDialogVisible) {
        DeleteConfirmDialog(
            fileName = state.documentTitle ?: "this PDF",
            onConfirm = { viewModel.onAction(ReaderAction.ConfirmDelete) },
            onDismiss = { viewModel.onAction(ReaderAction.HideDeleteDialog) }
        )
    }
}
