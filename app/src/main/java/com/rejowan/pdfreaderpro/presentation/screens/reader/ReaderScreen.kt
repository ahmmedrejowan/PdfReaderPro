package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.rejowan.pdfreaderpro.presentation.components.pdf.print.DefaultPdfPrintAdapter
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.DeleteConfirmDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.EnhancedTableOfContents
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ErrorState
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.FloatingControlBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.FloatingSearchBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageJumpSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageScrubber
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PasswordDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PdfInfoDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderSidebar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderTopBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ViewModeSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageSpreadMode
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ZoomSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ZoomPreset
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.DisplaySheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.BookmarksSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.MoreOptionsSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.AutoScrollSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.AutoScrollOverlay
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

    // Track scrolling activity for showing page scrubber in immersive mode
    var isScrolling by remember { mutableStateOf(false) }
    var lastScrollTime by remember { mutableStateOf(0L) }

    // Show scrubber temporarily when page changes in immersive mode
    LaunchedEffect(state.currentPage) {
        if (!state.isToolbarVisible || state.isFullScreen) {
            isScrolling = true
            lastScrollTime = System.currentTimeMillis()
        }
    }

    // Auto-hide scrubber after 2 seconds of no scrolling
    LaunchedEffect(lastScrollTime) {
        if (isScrolling && lastScrollTime > 0) {
            kotlinx.coroutines.delay(2000)
            if (System.currentTimeMillis() - lastScrollTime >= 2000) {
                isScrolling = false
            }
        }
    }

    // Calculate content padding for PDF viewer (in pixels)
    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    val topPaddingPx = statusBarHeightPx + with(density) { 6.dp.roundToPx() }
    val bottomPaddingPx = navBarHeightPx + with(density) { 6.dp.roundToPx() }

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

    // Handle screen orientation
    DisposableEffect(state.screenOrientation) {
        activity?.let {
            it.requestedOrientation = when (state.screenOrientation) {
                ScreenOrientation.AUTO -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    // Handle brightness
    DisposableEffect(state.brightness) {
        activity?.let {
            val layoutParams = it.window.attributes
            layoutParams.screenBrightness = state.brightness
            it.window.attributes = layoutParams
        }

        onDispose {
            // Reset to system brightness (-1)
            activity?.let {
                val layoutParams = it.window.attributes
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                it.window.attributes = layoutParams
            }
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

                    // Set up print adapter
                    pdfPrintAdapter = DefaultPdfPrintAdapter(ctx).also {
                        it.defaultFileName = viewModel.pdfPath.substringAfterLast("/")
                    }

                    onReady {
                        ui.toolbarEnabled = false
                        ui.isSideBarOpen = false
                        setContentPadding(topPaddingPx, bottomPaddingPx)
                        loadFromFile(viewModel.pdfPath)
                    }

                    viewModel.setPdfViewer(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
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
                        viewModel.printDocument()
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

                // Page scrubber on right edge (shows with toolbar OR when scrolling in immersive mode)
                val showScrubber = (state.isToolbarVisible && !state.isSearchActive && !state.isFullScreen) || isScrolling
                PageScrubber(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    isVisible = showScrubber,
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
                        isBookmarked = state.isCurrentPageBookmarked,
                        onTocClick = { viewModel.onAction(ReaderAction.ShowTableOfContents) },
                        onViewClick = { viewModel.onAction(ReaderAction.ShowViewModeSheet) },
                        onZoomClick = { viewModel.onAction(ReaderAction.ShowZoomSheet) },
                        onDisplayClick = { viewModel.onAction(ReaderAction.ShowDisplaySheet) },
                        onBookmarkClick = { viewModel.onAction(ReaderAction.TogglePageBookmark) },
                        onMoreClick = { viewModel.onAction(ReaderAction.ShowMoreOptionsSheet) },
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

    // Table of Contents & Attachments sheet - using enhanced version
    if (state.isTableOfContentsVisible) {
        EnhancedTableOfContents(
            items = state.outline,
            attachments = state.attachments,
            currentPage = state.currentPage,
            onItemClick = { item ->
                viewModel.navigateToOutlineItem(item)
                viewModel.onAction(ReaderAction.HideTableOfContents)
            },
            onAttachmentClick = { attachment ->
                viewModel.onAction(ReaderAction.DownloadAttachment(attachment))
                viewModel.onAction(ReaderAction.HideTableOfContents)
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideTableOfContents) }
        )
    }

    // Page jump sheet
    if (state.isPageJumpDialogVisible) {
        PageJumpSheet(
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

    // View Mode Sheet
    if (state.isViewModeSheetVisible) {
        ViewModeSheet(
            currentScrollDirection = state.scrollDirection,
            currentSpreadMode = when (state.spreadMode) {
                SpreadMode.NONE -> PageSpreadMode.NONE
                SpreadMode.ODD -> PageSpreadMode.ODD
                SpreadMode.EVEN -> PageSpreadMode.EVEN
            },
            isSnapEnabled = state.isSnapEnabled,
            onScrollDirectionChange = { direction ->
                viewModel.onAction(ReaderAction.SetScrollDirection(direction))
            },
            onSpreadModeChange = { mode ->
                val spreadMode = when (mode) {
                    PageSpreadMode.NONE -> SpreadMode.NONE
                    PageSpreadMode.ODD -> SpreadMode.ODD
                    PageSpreadMode.EVEN -> SpreadMode.EVEN
                }
                viewModel.onAction(ReaderAction.SetSpreadMode(spreadMode))
            },
            onSnapToggle = { enabled ->
                viewModel.onAction(ReaderAction.SetSnapEnabled(enabled))
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideViewModeSheet) }
        )
    }

    // Zoom Sheet
    if (state.isZoomSheetVisible) {
        ZoomSheet(
            currentZoom = state.zoom,
            currentOrientation = state.screenOrientation,
            onZoomIn = { viewModel.onAction(ReaderAction.ZoomIn) },
            onZoomOut = { viewModel.onAction(ReaderAction.ZoomOut) },
            onZoomPreset = { preset ->
                when (preset) {
                    ZoomPreset.FIT_PAGE -> viewModel.onAction(ReaderAction.ZoomFitPage)
                    ZoomPreset.FIT_WIDTH -> viewModel.onAction(ReaderAction.ZoomFitWidth)
                    ZoomPreset.ACTUAL_SIZE -> viewModel.onAction(ReaderAction.ZoomActualSize)
                }
            },
            onOrientationChange = { orientation ->
                viewModel.onAction(ReaderAction.SetScreenOrientation(orientation))
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideZoomSheet) }
        )
    }

    // Display Sheet
    if (state.isDisplaySheetVisible) {
        DisplaySheet(
            brightness = state.brightness,
            keepScreenOn = state.keepScreenOn,
            currentTheme = state.readingTheme,
            onBrightnessChange = { viewModel.onAction(ReaderAction.SetBrightness(it)) },
            onKeepScreenOnChange = { viewModel.onAction(ReaderAction.SetKeepScreenOn(it)) },
            onThemeChange = { viewModel.onAction(ReaderAction.SetReadingTheme(it)) },
            onDismiss = { viewModel.onAction(ReaderAction.HideDisplaySheet) }
        )
    }

    // Bookmarks Sheet
    if (state.isBookmarksSheetVisible) {
        BookmarksSheet(
            bookmarks = state.bookmarks,
            currentPage = state.currentPage,
            onBookmarkClick = { bookmark ->
                viewModel.onAction(ReaderAction.GoToBookmark(bookmark))
            },
            onDeleteBookmark = { bookmark ->
                viewModel.onAction(ReaderAction.DeleteBookmark(bookmark))
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideBookmarksSheet) }
        )
    }

    // More Options Sheet
    if (state.isMoreOptionsSheetVisible) {
        MoreOptionsSheet(
            onBookmarksClick = {
                viewModel.onAction(ReaderAction.ShowBookmarksSheet)
            },
            onAutoScrollClick = {
                viewModel.onAction(ReaderAction.ShowAutoScrollSheet)
            },
            onGoToPageClick = {
                viewModel.onAction(ReaderAction.ShowPageJumpDialog)
            },
            onShareClick = {
                viewModel.onAction(ReaderAction.ShareDocument)
            },
            onPrintClick = {
                viewModel.printDocument()
            },
            onDocumentInfoClick = {
                viewModel.onAction(ReaderAction.ShowInfoDialog)
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideMoreOptionsSheet) }
        )
    }

    // Auto-Scroll Sheet
    if (state.isAutoScrollSheetVisible) {
        AutoScrollSheet(
            currentSpeed = state.autoScrollSpeed,
            onStartAutoScroll = { speed ->
                viewModel.onAction(ReaderAction.StartAutoScroll(speed))
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideAutoScrollSheet) }
        )
    }

    // Auto-Scroll Overlay (shown when auto-scrolling)
    Box(modifier = Modifier.fillMaxSize()) {
        AutoScrollOverlay(
            isVisible = state.isAutoScrollActive,
            isPaused = state.isAutoScrollPaused,
            currentSpeed = state.autoScrollSpeed,
            onTogglePause = { viewModel.onAction(ReaderAction.ToggleAutoScrollPause) },
            onStop = { viewModel.onAction(ReaderAction.StopAutoScroll) },
            onSpeedChange = { speed -> viewModel.onAction(ReaderAction.SetAutoScrollSpeed(speed)) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp)
        )
    }
}
