package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ErrorState
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PageJumpDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PasswordDialog
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PdfPageView
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderBottomBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.ReaderTopBar
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.SearchOverlay
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.SettingsPanel
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.TableOfContentsSheet
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.state.collectAsState()
    val currentPageBitmap by viewModel.currentPageBitmap.collectAsState()

    // Handle full screen mode
    val activity = context as? Activity
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

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReaderEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ReaderEvent.NavigateToPage -> {
                    // Handled internally
                }
                is ReaderEvent.DocumentClosed -> {
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

    // Pager state for page navigation
    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.totalPages.coerceAtLeast(1) }
    )

    // Sync pager with state
    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentPage && !pagerState.isScrollInProgress) {
            viewModel.onAction(ReaderAction.GoToPage(pagerState.currentPage))
        }
    }

    // Password dialog
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
        return
    }

    // Loading state
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    state.error?.let { error ->
        ErrorState(
            message = error,
            onRetry = { /* Retry logic */ },
            onBack = { navController.popBackStack() }
        )
        return
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
                    onPageJumpClick = { viewModel.onAction(ReaderAction.ShowPageJumpDialog) }
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
            // PDF Page Display
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }

            if (state.scrollDirection == ScrollDirection.HORIZONTAL) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    PdfPageView(
                        pageIndex = page,
                        viewModel = viewModel,
                        colorMode = state.colorMode,
                        zoom = state.zoom,
                        searchResults = state.searchResultsOnCurrentPage,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { viewModel.onAction(ReaderAction.ToggleToolbar) },
                                    onDoubleTap = {
                                        if (state.zoom > 1f) {
                                            viewModel.onAction(ReaderAction.ResetZoom)
                                        } else {
                                            viewModel.onAction(ReaderAction.SetZoom(2f))
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (state.zoom * gestureZoom)
                                        .coerceIn(state.minZoom, state.maxZoom)
                                    viewModel.onAction(ReaderAction.SetZoom(newZoom))
                                }
                            }
                    )
                }
            } else {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    PdfPageView(
                        pageIndex = page,
                        viewModel = viewModel,
                        colorMode = state.colorMode,
                        zoom = state.zoom,
                        searchResults = state.searchResultsOnCurrentPage,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { viewModel.onAction(ReaderAction.ToggleToolbar) },
                                    onDoubleTap = {
                                        if (state.zoom > 1f) {
                                            viewModel.onAction(ReaderAction.ResetZoom)
                                        } else {
                                            viewModel.onAction(ReaderAction.SetZoom(2f))
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (state.zoom * gestureZoom)
                                        .coerceIn(state.minZoom, state.maxZoom)
                                    viewModel.onAction(ReaderAction.SetZoom(newZoom))
                                }
                            }
                    )
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
                    resultCount = state.searchResults.size,
                    currentIndex = state.currentSearchIndex,
                    onQueryChange = { viewModel.onAction(ReaderAction.Search(it)) },
                    onPreviousResult = { viewModel.onAction(ReaderAction.PreviousSearchResult) },
                    onNextResult = { viewModel.onAction(ReaderAction.NextSearchResult) },
                    onClose = { viewModel.onAction(ReaderAction.ClearSearch) }
                )
            }
        }
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

    // Table of contents sheet
    if (state.isTableOfContentsVisible) {
        TableOfContentsSheet(
            items = state.tableOfContents,
            currentPage = state.currentPage,
            onItemClick = { item ->
                viewModel.onAction(ReaderAction.GoToPage(item.page))
                viewModel.onAction(ReaderAction.HideTableOfContents)
            },
            onDismiss = { viewModel.onAction(ReaderAction.HideTableOfContents) }
        )
    }

    // Settings panel
    if (state.isSettingsPanelVisible) {
        SettingsPanel(
            colorMode = state.colorMode,
            brightness = state.brightness,
            scrollDirection = state.scrollDirection,
            keepScreenOn = state.keepScreenOn,
            onColorModeChange = { viewModel.onAction(ReaderAction.SetColorMode(it)) },
            onBrightnessChange = { viewModel.onAction(ReaderAction.SetBrightness(it)) },
            onScrollDirectionChange = { viewModel.onAction(ReaderAction.SetScrollDirection(it)) },
            onKeepScreenOnChange = { viewModel.onAction(ReaderAction.SetKeepScreenOn(it)) },
            onFullScreenClick = { viewModel.onAction(ReaderAction.ToggleFullScreen) },
            onDismiss = { viewModel.onAction(ReaderAction.HideSettingsPanel) }
        )
    }
}
