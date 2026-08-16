package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import java.io.File
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
import com.rejowan.pdfreaderpro.presentation.components.pdf.print.DefaultPdfPrintAdapter
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.BakeHighlightsDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.HighlightNavBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.HighlightsSheet
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
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ZoomSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ZoomPreset
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.DisplaySheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.BookmarksSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.MoreOptionsSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.AutoScrollSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.AutoScrollOverlay
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.TopBarMenuPanel
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.RemoveFavoriteSheet
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.SelectionActionBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReaderScreen(
    navController: NavController,
    path: String,
    initialPage: Int = 0,
    startSigning: Boolean = false,
    viewModel: ReaderViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isDarkMode = com.rejowan.pdfreaderpro.presentation.theme.LocalIsDarkTheme.current

    // SAF launcher for saving document
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.saveToUri(it) }
    }

    // Separate launcher from the plain save, so the suggested filename can make it
    // obvious which copy carries the highlights.
    val bakeHighlightsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.bakeHighlightsToUri(it) }
    }

    val decryptedCopyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.saveDecryptedCopyToUri(it) }
    }

    val signedCopyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.requestSignedCopy(it) }
    }

    val state by viewModel.state.collectAsState()

    // Arriving from the Sign PDF tool. The signature editor needs a loaded
    // document, so wait for the first render rather than firing on entry.
    var signingStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(startSigning, state.isLoading) {
        if (startSigning && !state.isLoading && !signingStarted) {
            signingStarted = true
            viewModel.onAction(ReaderAction.StartSigning)
        }
    }

    val activity = context as? Activity

    // Background color for PdfViewer — follows the app theme (true black in Black theme)
    val backgroundColor = MaterialTheme.colorScheme.background
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

    // Auto-hide toolbar after 5 seconds when enabled
    LaunchedEffect(state.isToolbarVisible, state.autoHideToolbar) {
        if (state.isToolbarVisible && state.autoHideToolbar && !state.isSearchActive) {
            kotlinx.coroutines.delay(5000)
            viewModel.onAction(ReaderAction.ToggleToolbar)
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
            // Reset to unspecified to respect device orientation settings
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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

    // Handle orientation changes - auto fit width when orientation changes
    val configuration = LocalConfiguration.current
    var lastOrientation by remember { mutableIntStateOf(configuration.orientation) }

    LaunchedEffect(configuration.orientation) {
        if (lastOrientation != configuration.orientation) {
            lastOrientation = configuration.orientation
            // Auto fit width when orientation changes
            viewModel.onAction(ReaderAction.ZoomFitWidth)
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
                    try {
                        val file = File(path)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to share: ${e.message}")
                    }
                }
                is ReaderEvent.SaveDocumentPicker -> {
                    saveDocumentLauncher.launch(viewModel.getDocumentFileName())
                }
                is ReaderEvent.BakeHighlightsPicker -> {
                    bakeHighlightsLauncher.launch(viewModel.getHighlightedFileName())
                }
                is ReaderEvent.SaveDecryptedCopyPicker -> {
                    decryptedCopyLauncher.launch(viewModel.getDecryptedFileName())
                }
                is ReaderEvent.SaveSignedCopyPicker -> {
                    signedCopyLauncher.launch(viewModel.getSignedFileName())
                }
                is ReaderEvent.FavoriteAdded -> {
                    snackbarHostState.showSnackbar("Added to favourites")
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

                    // Adds "Highlight" beside Copy in the text selection menu.
                    selectionMenuItems = listOf(
                        PdfViewer.SelectionMenuItem(
                            id = MENU_ID_HIGHLIGHT,
                            title = ctx.getString(R.string.highlight)
                        ) {
                            viewModel.onAction(ReaderAction.StartHighlight)
                        }
                    )

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
                val targetScrollMode = when (state.scrollMode) {
                    ScrollMode.VERTICAL -> PdfViewer.PageScrollMode.VERTICAL
                    ScrollMode.HORIZONTAL -> PdfViewer.PageScrollMode.HORIZONTAL
                }
                if (pdfViewer.isInitialized && pdfViewer.pageScrollMode != targetScrollMode) {
                    pdfViewer.pageScrollMode = targetScrollMode
                }
            },
            onRelease = { viewModel.clearPdfViewer() }
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

        // Signing bar. The viewer's own editor toolbar is hidden, so this is the
        // only way back out of signature mode, and the only way to keep the result.
        AnimatedVisibility(
            visible = state.isSigning,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    // Sit above the floating control bar rather than behind it,
                    // the same clearance the highlight nav strip uses.
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = if (state.isToolbarVisible && !state.isFullScreen) 96.dp else 24.dp
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.sign_mode_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        TextButton(
                            onClick = { viewModel.onAction(ReaderAction.CancelSigning) }
                        ) {
                            Text(stringResource(R.string.sign_cancel))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.onAction(ReaderAction.SaveSignedCopy) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.sign_save))
                        }
                    }
                }
            }
        }

        // Print preparation overlay. Rasterising every page takes tens of seconds
        // on a longer document, and until this existed the reader simply sat there.
        AnimatedVisibility(
            visible = state.printProgress != null || state.isSavingDecryptedCopy ||
                state.isSavingSignedCopy,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val progress = state.printProgress
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (progress != null) {
                        CircularProgressIndicator(
                            progress = { progress },
                            color = Color(0xFF9575CD)
                        )
                    } else {
                        CircularProgressIndicator(color = Color(0xFF9575CD))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when {
                            state.isSavingSignedCopy -> stringResource(R.string.sign_saving)
                            state.isSavingDecryptedCopy ->
                                stringResource(R.string.save_decrypted_copy)
                            progress != null && progress > 0f -> stringResource(
                                R.string.print_preparing_percent,
                                (progress * 100).toInt()
                            )
                            else -> stringResource(R.string.print_preparing)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
                    onMenuClick = { viewModel.onAction(ReaderAction.ShowTopBarMenu) },
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
                        isDarkMode = isDarkMode,
                        highlightMatchCount = state.highlightsMatchingSearch.size,
                        onHighlightMatchesClick = {
                            viewModel.onAction(
                                ReaderAction.ShowHighlightsSheet(state.searchQuery)
                            )
                        }
                    )
                }

                // Page scrubber (shows with toolbar OR when scrolling in immersive mode)
                // Vertical scrubber on right edge for vertical scroll mode
                // Horizontal scrubber above bottom bar for horizontal scroll mode
                // When the toolbar is hidden, the scroll-triggered scrubber is
                // gated by the user's "scrubber on scroll" preference.
                val showScrubber = (state.isToolbarVisible && !state.isSearchActive && !state.isFullScreen) ||
                    (isScrolling && state.scrubberOnScroll)
                val isHorizontalScroll = state.scrollMode == ScrollMode.HORIZONTAL

                // When toolbar visible: above control bar. When hidden: just above system nav
                val toolbarVisible = state.isToolbarVisible && !state.isFullScreen

                PageScrubber(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    isVisible = showScrubber,
                    onPageChange = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                    isHorizontal = isHorizontalScroll,
                    isDarkMode = isDarkMode,
                    modifier = if (isHorizontalScroll) {
                        Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = if (toolbarVisible) 80.dp else 8.dp)
                    } else {
                        Modifier.align(Alignment.TopEnd)
                    }
                )

                // The app's own selection actions, anchored under the selection. The
                // system menu keeps Copy and friends above it.
                val isEditingHighlight = state.isHighlightPickerVisible && state.editingHighlightId != null
                SelectionActionBar(
                    isVisible = isEditingHighlight || state.pendingSelection != null,
                    anchor = if (isEditingHighlight) state.editingAnchor else state.pendingSelection?.anchor,
                    viewerWidthDp = configuration.screenWidthDp.toFloat(),
                    viewerHeightDp = configuration.screenHeightDp.toFloat(),
                    onHighlight = { color ->
                        if (isEditingHighlight) {
                            viewModel.onAction(ReaderAction.ApplyHighlightColor(color))
                        } else {
                            viewModel.onAction(ReaderAction.HighlightSelection(color))
                        }
                    },
                    selectedColor = state.editingHighlight?.color,
                    onDelete = state.editingHighlightId?.let { id ->
                        { viewModel.onAction(ReaderAction.DeleteHighlight(id)) }
                    },
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Highlight navigation strip, above the control bar. Jumping to a
                // highlight brings it near the top of the view, so a strip up there
                // covered the very thing it had just navigated to.
                HighlightNavBar(
                    isVisible = state.isHighlightNavVisible && !state.isHighlightPickerVisible,
                    positionLabel = state.highlightPositionLabel,
                    onPrevious = { viewModel.onAction(ReaderAction.PreviousHighlight) },
                    onNext = { viewModel.onAction(ReaderAction.NextHighlight) },
                    onClose = { viewModel.onAction(ReaderAction.HideHighlightNav) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = if (state.isToolbarVisible && !state.isFullScreen) 96.dp else 24.dp)
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
            onAttachmentOpen = { attachment ->
                viewModel.onAction(ReaderAction.OpenAttachment(attachment))
                viewModel.onAction(ReaderAction.HideTableOfContents)
            },
            onAttachmentDownload = { attachment ->
                viewModel.onAction(ReaderAction.DownloadAttachment(attachment))
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
            currentScrollMode = state.scrollMode,
            isSnapEnabled = state.isSnapEnabled,
            lockHorizontalScroll = state.lockHorizontalScroll,
            canLockHorizontalScroll = state.canLockHorizontalScroll,
            isAutoHideToolbar = state.autoHideToolbar,
            isScrubberOnScroll = state.scrubberOnScroll,
            isTapToTurnPage = state.tapToTurnPage,
            onScrollModeChange = { mode ->
                viewModel.onAction(ReaderAction.SetScrollMode(mode))
            },
            onSnapToggle = { enabled ->
                viewModel.onAction(ReaderAction.SetSnapEnabled(enabled))
            },
            onLockHorizontalScrollToggle = { enabled ->
                viewModel.onAction(ReaderAction.SetLockHorizontalScroll(enabled))
            },
            onAutoHideToolbarToggle = { enabled ->
                viewModel.onAction(ReaderAction.SetAutoHideToolbar(enabled))
            },
            onScrubberOnScrollToggle = { enabled ->
                viewModel.onAction(ReaderAction.SetScrubberOnScroll(enabled))
            },
            onTapToTurnPageToggle = { enabled ->
                viewModel.onAction(ReaderAction.SetTapToTurnPage(enabled))
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideViewModeSheet) }
        )
    }

    // Zoom Sheet
    if (state.isZoomSheetVisible) {
        ZoomSheet(
            currentZoom = state.zoom,
            currentOrientation = state.screenOrientation,
            currentDoubleTapZoom = state.doubleTapZoom,
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
            onDoubleTapZoomChange = { zoom ->
                viewModel.onAction(ReaderAction.SetDoubleTapZoom(zoom))
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

    // Highlights Sheet
    if (state.isHighlightsSheetVisible) {
        HighlightsSheet(
            highlights = state.allHighlights,
            currentPage = state.currentPage,
            onHighlightClick = { highlight ->
                viewModel.onAction(ReaderAction.GoToHighlight(highlight.id))
            },
            onDeleteHighlight = { highlight ->
                viewModel.onAction(ReaderAction.DeleteHighlight(highlight.id))
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideHighlightsSheet) },
            initialQuery = state.highlightsSheetQuery
        )
    }

    // More Options Sheet
    if (state.isMoreOptionsSheetVisible) {
        MoreOptionsSheet(
            onHighlightsClick = {
                viewModel.onAction(ReaderAction.ShowHighlightsSheet())
            },
            onSaveWithHighlightsClick = {
                viewModel.onAction(ReaderAction.ShowBakeHighlightsDialog)
            },
            onSaveDecryptedCopyClick = {
                viewModel.onAction(ReaderAction.SaveDecryptedCopy)
            },
            onSignClick = {
                viewModel.onAction(ReaderAction.StartSigning)
            },
            isPasswordProtected = state.isPasswordProtected,
            hasHighlights = state.highlights.isNotEmpty(),
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

    // Top bar menu panel (slide from right)
    TopBarMenuPanel(
        isVisible = state.isTopBarMenuVisible,
        isFavorite = state.isFavorite,
        onInfoClick = { viewModel.onAction(ReaderAction.ShowInfoDialog) },
        onShareClick = { viewModel.onAction(ReaderAction.ShareDocument) },
        onPrintClick = { viewModel.onAction(ReaderAction.PrintDocument) },
        onOpenWithClick = { viewModel.onAction(ReaderAction.OpenWithExternal) },
        onSaveClick = { viewModel.onAction(ReaderAction.SaveDocumentWithPicker) },
        onFavoriteClick = {
            if (state.isFavorite) {
                viewModel.onAction(ReaderAction.ShowRemoveFavoriteDialog)
            } else {
                viewModel.onAction(ReaderAction.AddToFavorite)
            }
        },
        onDeleteClick = { viewModel.onAction(ReaderAction.ShowDeleteDialog) },
        onDismiss = { viewModel.onAction(ReaderAction.HideTopBarMenu) }
    )

    // Confirmation for writing highlights into a copy of the PDF
    if (state.isBakeHighlightsDialogVisible) {
        BakeHighlightsDialog(
            highlightCount = state.highlights.size,
            onConfirm = { viewModel.onAction(ReaderAction.ConfirmBakeHighlights) },
            onDismiss = { viewModel.onAction(ReaderAction.HideBakeHighlightsDialog) }
        )
    }

    // Remove favourite confirmation sheet
    if (state.isRemoveFavoriteDialogVisible) {
        RemoveFavoriteSheet(
            onConfirm = { viewModel.onAction(ReaderAction.ConfirmRemoveFavorite) },
            onDismiss = { viewModel.onAction(ReaderAction.HideRemoveFavoriteDialog) }
        )
    }
}

/**
 * Menu item id for "Highlight" in the text selection menu.
 *
 * Must be stable, since the menu is rebuilt on every prepare pass and items are
 * matched back by id.
 */
private const val MENU_ID_HIGHLIGHT = 0x48_4C_47_31
