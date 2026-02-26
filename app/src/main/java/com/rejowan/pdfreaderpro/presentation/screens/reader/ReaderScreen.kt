package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.print.PrintManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.DeleteConfirmDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.EnhancedTableOfContents
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ErrorState
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.FloatingControlBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.FloatingSearchBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageJumpDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageScrubber
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PasswordDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PdfInfoDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderSidebar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderTopBar
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
    val isDarkMode = isSystemInDarkTheme()

    val state by viewModel.state.collectAsState()

    val activity = context as? Activity

    // Background color for PdfViewer
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val backgroundColorArgb = backgroundColor.toArgb()

    // Track if user is bookmarked (placeholder - can be connected to actual bookmark logic)
    var isBookmarked by remember { mutableStateOf(false) }

    // Permanent padding for PDF viewer to avoid content being cut off by UI elements
    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    val navBarHeight = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    // Top padding: status bar + top bar space
    val topPadding = statusBarHeight + 64.dp

    // Bottom padding: floating control bar + nav bar + extra spacing
    val bottomPadding = navBarHeight + 80.dp

    // Handle immersive mode - hide system bars when toolbar is hidden or in full screen
    val shouldBeImmersive = !state.isToolbarVisible || state.isFullScreen
    DisposableEffect(shouldBeImmersive) {
        activity?.let {
            val window = it.window
            val controller = WindowCompat.getInsetsController(window, view)

            if (shouldBeImmersive) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // PDF Viewer
        AndroidView(
            factory = { ctx ->
                PdfViewer(ctx).apply {
                    setBackgroundColor(backgroundColorArgb)

                    onReady {
                        ui.toolbarEnabled = false
                        ui.isSideBarOpen = false
                        loadFromFile(viewModel.pdfPath)
                    }

                    viewModel.setPdfViewer(this)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding),
            update = { pdfViewer ->
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
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF9575CD)
                )
            }
        }

        // Main UI overlay
        if (!state.isLoading && state.error == null) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top bar (auto-hide)
                ReaderTopBar(
                    title = state.documentTitle ?: "PDF Reader",
                    isVisible = state.isToolbarVisible && !state.isSearchActive && !state.isFullScreen,
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = { viewModel.onAction(ReaderAction.ToggleSearch) },
                    onShareClick = { viewModel.onAction(ReaderAction.ShareDocument) },
                    onPrintClick = {
                        // Print functionality
                        val printManager = context.getSystemService(PrintManager::class.java)
                        // You can implement print adapter here
                    },
                    onInfoClick = { viewModel.onAction(ReaderAction.ShowInfoDialog) },
                    onFullScreenClick = { viewModel.onAction(ReaderAction.ToggleFullScreen) },
                    onDeleteClick = { viewModel.onAction(ReaderAction.ShowDeleteDialog) },
                    isDarkMode = isDarkMode,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Search bar at top (replaces top bar when active)
                AnimatedVisibility(
                    visible = state.isSearchActive,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut()
                ) {
                    FloatingSearchBar(
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
                        },
                        isDarkMode = isDarkMode
                    )
                }

                // Page scrubber on right edge (auto-hides with toolbar)
                PageScrubber(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    isVisible = state.isToolbarVisible && !state.isSearchActive && !state.isFullScreen,
                    onPageChange = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                    isDarkMode = isDarkMode,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                // Floating control bar at bottom
                AnimatedVisibility(
                    visible = state.isToolbarVisible && !state.isFullScreen,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 16.dp),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut()
                ) {
                    FloatingControlBar(
                        currentZoom = state.zoom,
                        scrollDirection = state.scrollDirection,
                        isBookmarked = isBookmarked,
                        onZoomIn = { viewModel.onAction(ReaderAction.ZoomIn) },
                        onZoomOut = { viewModel.onAction(ReaderAction.ZoomOut) },
                        onResetZoom = { viewModel.onAction(ReaderAction.ResetZoom) },
                        onScrollDirectionToggle = {
                            viewModel.onAction(
                                ReaderAction.SetScrollDirection(
                                    if (state.scrollDirection == ScrollDirection.VERTICAL)
                                        ScrollDirection.HORIZONTAL
                                    else
                                        ScrollDirection.VERTICAL
                                )
                            )
                        },
                        onTocClick = { viewModel.onAction(ReaderAction.ShowTableOfContents) },
                        onBookmarkClick = { isBookmarked = !isBookmarked },
                        onSettingsClick = { viewModel.onAction(ReaderAction.ShowSettingsPanel) },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }

        // Sidebar (settings panel)
        ReaderSidebar(
            isOpen = state.isSettingsPanelVisible,
            currentPage = state.currentPage + 1,
            totalPages = state.totalPages,
            brightness = state.brightness,
            keepScreenOn = state.keepScreenOn,
            isRotationLocked = state.isRotationLocked,
            onBrightnessChange = { viewModel.onAction(ReaderAction.SetBrightness(it)) },
            onKeepScreenOnChange = { viewModel.onAction(ReaderAction.SetKeepScreenOn(it)) },
            onRotationLockChange = { viewModel.onAction(ReaderAction.ToggleRotationLock) },
            onDismiss = { viewModel.onAction(ReaderAction.HideSettingsPanel) },
            isDarkMode = isDarkMode
        )

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )
    }

    // Table of Contents sheet - using enhanced version
    if (state.isTableOfContentsVisible) {
        EnhancedTableOfContents(
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
