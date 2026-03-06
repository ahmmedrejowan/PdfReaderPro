package com.rejowan.pdfreaderpro.presentation.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.presentation.components.AnimatedBottomNav
import com.rejowan.pdfreaderpro.presentation.components.AnimatedSelectionActionBar
import com.rejowan.pdfreaderpro.presentation.components.CollapsingHomeHeader
import com.rejowan.pdfreaderpro.presentation.components.CompactTabRow
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.FolderOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.SortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.StatsSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.ExitConfirmSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameSheet
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToFolderDetail
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToMergeTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToSearch
import com.rejowan.pdfreaderpro.presentation.screens.folders.FoldersScreen
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.FavoritesTab
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.FilesTab
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.RecentTab
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsScreenContent
import com.rejowan.pdfreaderpro.presentation.screens.tools.ToolsScreen
import com.rejowan.pdfreaderpro.util.FileOperations
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// Bottom Navigation Items
enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    FOLDERS("Folders", Icons.Filled.Folder, Icons.Outlined.Folder),
    TOOLS("Tools", Icons.Outlined.Build, Icons.Outlined.Build),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

// Home Sub-tabs
enum class HomeSubTab(val title: String) {
    RECENT("Recent"),
    FAVORITES("Favorites"),
    ALL("All Files")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission state that updates on lifecycle resume
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else true
        )
    }

    // Track if this is the first resume (initial launch)
    var isFirstResume by remember { mutableStateOf(true) }

    // Re-check permission and do silent refresh on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else true

                if (newPermissionState && !hasPermission) {
                    // Permission was just granted, refresh data
                    viewModel.refresh()
                } else if (newPermissionState && !isFirstResume) {
                    // App resumed from background with permission, do silent refresh
                    viewModel.silentRefresh()
                }
                hasPermission = newPermissionState
                isFirstResume = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val openPermissionSettings = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    var selectedNavItem by rememberSaveable { mutableIntStateOf(0) }
    val homeSubTabPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { HomeSubTab.entries.size }
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val folderSortOption by viewModel.folderSortOption.collectAsState()
    val folderSearchQuery by viewModel.folderSearchQuery.collectAsState()

    val allFiles by viewModel.allFiles.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val favoriteFiles by viewModel.favoriteFiles.collectAsState()
    val folders by viewModel.folders.collectAsState()

    // Selection mode state
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPaths by viewModel.selectedPaths.collectAsState()

    // Clear selected items when switching tabs (but keep selection mode active)
    LaunchedEffect(homeSubTabPagerState.currentPage) {
        if (isSelectionMode && selectedPaths.isNotEmpty()) {
            viewModel.clearSelectedItems()
        }
    }

    var showSortSheet by remember { mutableStateOf(false) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFavorite by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showExitConfirmSheet by remember { mutableStateOf(false) }
    var fileForDialog by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFromRecent by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<PdfFolder?>(null) }

    // Bottom bar visibility + Header collapse with smooth scroll tracking
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var headerScrollOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val maxHeaderScroll = with(density) { 50.dp.toPx() } // Search bar height

    val bottomBarHeight = 120.dp
    val bottomBarOffset by animateDpAsState(
        targetValue = if (isBottomBarVisible) 0.dp else bottomBarHeight,
        animationSpec = tween(durationMillis = 300),
        label = "bottomBarOffset"
    )

    // Calculate collapse progress (0 = expanded, 1 = collapsed)
    val headerCollapseProgress = (headerScrollOffset / maxHeaderScroll).coerceIn(0f, 1f)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // Update header scroll offset (smooth tracking)
                val newOffset = (headerScrollOffset - delta).coerceIn(0f, maxHeaderScroll)
                headerScrollOffset = newOffset

                // Bottom bar visibility (threshold-based)
                if (delta > 10) {
                    isBottomBarVisible = true
                } else if (delta < -10) {
                    isBottomBarVisible = false
                }

                return Offset.Zero
            }
        }
    }

    // Handle back press: exit selection mode first, then navigate to Home, then show exit confirmation
    BackHandler(enabled = true) {
        when {
            isSelectionMode -> {
                // Exit selection mode first
                viewModel.exitSelectionMode()
            }
            selectedNavItem != 0 -> {
                // If not on Home tab, navigate to Home first
                selectedNavItem = 0
            }
            else -> {
                // If already on Home, show exit confirmation
                showExitConfirmSheet = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        Scaffold(
            topBar = {
                when (BottomNavItem.entries[selectedNavItem]) {
                    BottomNavItem.HOME -> {
                        // No TopAppBar for HOME - using WelcomeHeader instead
                    }
                    BottomNavItem.FOLDERS -> {
                        TopAppBar(
                            title = { Text("Folders") },
                            actions = {
                                IconButton(onClick = { navController.navigateToSearch() }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        )
                    }
                    BottomNavItem.TOOLS -> {
                        TopAppBar(title = { Text("PDF Tools") })
                    }
                    BottomNavItem.SETTINGS -> {
                        TopAppBar(title = { Text("Settings") })
                    }
                }
            },
            // Content bleeds behind the bottom bar for the cutout effect
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            // Only apply top padding - content goes behind bottom nav
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                when (BottomNavItem.entries[selectedNavItem]) {
                    BottomNavItem.HOME -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Collapsing header (Welcome + Search)
                            CollapsingHomeHeader(
                                collapseProgress = headerCollapseProgress,
                                onSearchClick = { navController.navigateToSearch() },
                                onSortClick = { showSortSheet = true },
                                onStatsClick = { showStatsSheet = true }
                            )

                            // Compact tabs with view mode toggle
                            CompactTabRow(
                                tabs = HomeSubTab.entries.map { it.title },
                                selectedIndex = homeSubTabPagerState.currentPage,
                                onTabSelected = { index ->
                                    scope.launch {
                                        homeSubTabPagerState.animateScrollToPage(index)
                                    }
                                },
                                isGridView = viewMode == com.rejowan.pdfreaderpro.domain.model.ViewMode.GRID,
                                onViewModeToggle = {
                                    viewModel.setViewMode(
                                        if (viewMode == com.rejowan.pdfreaderpro.domain.model.ViewMode.GRID)
                                            com.rejowan.pdfreaderpro.domain.model.ViewMode.LIST
                                        else
                                            com.rejowan.pdfreaderpro.domain.model.ViewMode.GRID
                                    )
                                }
                            )

                            HorizontalPager(
                                state = homeSubTabPagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (HomeSubTab.entries[page]) {
                                    HomeSubTab.RECENT -> RecentTab(
                                        recentFiles = recentFiles,
                                        viewMode = viewMode,
                                        isLoading = isLoading,
                                        isRefreshing = isRefreshing,
                                        hasPermission = hasPermission,
                                        onFileClick = { recent ->
                                            navController.navigateToReader(recent.path, recent.lastPage)
                                        },
                                        onFileOptionsClick = { recent ->
                                            allFiles.find { it.path == recent.path }?.let { pdf ->
                                                selectedFile = pdf
                                                selectedFileFromRecent = true
                                                scope.launch {
                                                    selectedFileFavorite = viewModel.isFavorite(pdf.path)
                                                }
                                            }
                                        },
                                        onBrowseClick = {
                                            scope.launch {
                                                homeSubTabPagerState.animateScrollToPage(HomeSubTab.ALL.ordinal)
                                            }
                                        },
                                        onRefresh = { viewModel.refresh() },
                                        onGrantPermissionClick = openPermissionSettings,
                                        isSelectionMode = isSelectionMode,
                                        selectedPaths = selectedPaths,
                                        onSelectionToggle = { recent ->
                                            viewModel.toggleSelection(recent.path)
                                        },
                                        onLongClick = { recent ->
                                            if (!isSelectionMode) {
                                                viewModel.enterSelectionMode(recent.path)
                                            }
                                        }
                                    )

                                    HomeSubTab.FAVORITES -> FavoritesTab(
                                        favorites = favoriteFiles,
                                        viewMode = viewMode,
                                        isLoading = isLoading,
                                        isRefreshing = isRefreshing,
                                        hasPermission = hasPermission,
                                        onFileClick = { pdf ->
                                            navController.navigateToReader(pdf.path)
                                        },
                                        onFileOptionsClick = { pdf ->
                                            selectedFile = pdf
                                            selectedFileFavorite = true
                                            selectedFileFromRecent = false
                                        },
                                        onBrowseClick = {
                                            scope.launch {
                                                homeSubTabPagerState.animateScrollToPage(HomeSubTab.ALL.ordinal)
                                            }
                                        },
                                        onRefresh = { viewModel.refresh() },
                                        onGrantPermissionClick = openPermissionSettings,
                                        isSelectionMode = isSelectionMode,
                                        selectedPaths = selectedPaths,
                                        onSelectionToggle = { pdf ->
                                            viewModel.toggleSelection(pdf.path)
                                        },
                                        onLongClick = { pdf ->
                                            if (!isSelectionMode) {
                                                viewModel.enterSelectionMode(pdf.path)
                                            }
                                        }
                                    )

                                    HomeSubTab.ALL -> FilesTab(
                                        files = allFiles,
                                        viewMode = viewMode,
                                        isLoading = isLoading,
                                        isRefreshing = isRefreshing,
                                        hasPermission = hasPermission,
                                        onFileClick = { pdf ->
                                            navController.navigateToReader(pdf.path)
                                        },
                                        onFileOptionsClick = { pdf ->
                                            selectedFile = pdf
                                            selectedFileFromRecent = false
                                            scope.launch {
                                                selectedFileFavorite = viewModel.isFavorite(pdf.path)
                                            }
                                        },
                                        onRefresh = { viewModel.refresh() },
                                        onGrantPermissionClick = openPermissionSettings,
                                        isSelectionMode = isSelectionMode,
                                        selectedPaths = selectedPaths,
                                        onSelectionToggle = { pdf ->
                                            viewModel.toggleSelection(pdf.path)
                                        },
                                        onLongClick = { pdf ->
                                            if (!isSelectionMode) {
                                                viewModel.enterSelectionMode(pdf.path)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    BottomNavItem.FOLDERS -> {
                        FoldersScreen(
                            folders = folders,
                            isLoading = isLoading,
                            isRefreshing = isRefreshing,
                            hasPermission = hasPermission,
                            currentSort = folderSortOption,
                            searchQuery = folderSearchQuery,
                            onFolderClick = { folder ->
                                navController.navigateToFolderDetail(folder.path, folder.name)
                            },
                            onFolderLongClick = { folder ->
                                selectedFolder = folder
                            },
                            onRefresh = { viewModel.refresh() },
                            onGrantPermissionClick = openPermissionSettings,
                            onSortSelected = { viewModel.setFolderSortOption(it) },
                            onSearchQueryChange = { viewModel.setFolderSearchQuery(it) }
                        )
                    }

                    BottomNavItem.TOOLS -> {
                        ToolsScreen(navController = navController)
                    }

                    BottomNavItem.SETTINGS -> {
                        SettingsScreenContent(
                            onBackClick = null // No back button in bottom nav
                        )
                    }
                }
            }
        }

        // Bottom: Selection action bar OR Bottom navigation
        if (isSelectionMode && selectedNavItem == 0) {
            // Selection action bar at bottom when in selection mode
            AnimatedSelectionActionBar(
                visible = true,
                selectedCount = selectedPaths.size,
                totalCount = when (homeSubTabPagerState.currentPage) {
                    HomeSubTab.RECENT.ordinal -> recentFiles.size
                    HomeSubTab.FAVORITES.ordinal -> favoriteFiles.size
                    else -> allFiles.size
                },
                onClose = { viewModel.exitSelectionMode() },
                onSelectAll = {
                    val paths = when (homeSubTabPagerState.currentPage) {
                        HomeSubTab.RECENT.ordinal -> recentFiles.map { it.path }
                        HomeSubTab.FAVORITES.ordinal -> favoriteFiles.map { it.path }
                        else -> allFiles.map { it.path }
                    }
                    viewModel.selectAll(paths)
                },
                onMerge = {
                    val paths = selectedPaths.toList()
                    viewModel.exitSelectionMode()
                    navController.navigateToMergeTool(paths)
                },
                onShare = {
                    val paths = selectedPaths.toList()
                    FileOperations.shareMultiplePdfs(context, paths)
                    viewModel.exitSelectionMode()
                },
                onDelete = {
                    showBatchDeleteConfirm = true
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            // Bottom navigation bar when not in selection mode
            AnimatedBottomNav(
                selectedIndex = selectedNavItem,
                onItemClick = { index ->
                    selectedNavItem = index
                    isBottomBarVisible = true  // Show bottom bar when switching tabs
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = bottomBarOffset)
            )
        }
    }

    // Sort Options Sheet
    if (showSortSheet) {
        SortOptionsSheet(
            currentSort = sortOption,
            onSortSelected = { viewModel.setSortOption(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    // Stats Sheet
    if (showStatsSheet) {
        StatsSheet(
            totalPdfs = allFiles.size,
            totalSize = allFiles.sumOf { it.size },
            favoritesCount = favoriteFiles.size,
            recentCount = recentFiles.size,
            onDismiss = { showStatsSheet = false }
        )
    }

    // File Options Sheet
    selectedFile?.let { file ->
        FileOptionsSheet(
            pdfFile = file,
            isFavorite = selectedFileFavorite,
            onDismiss = { selectedFile = null },
            onFavoriteClick = { viewModel.toggleFavorite(file) },
            onShareClick = {
                FileOperations.sharePdf(context, file.path)
            },
            onRenameClick = {
                fileForDialog = file
                showRenameDialog = true
            },
            onInfoClick = {
                fileForDialog = file
                showInfoDialog = true
            },
            onDeleteClick = {
                fileForDialog = file
                showDeleteDialog = true
            },
            onRemoveFromRecentsClick = if (selectedFileFromRecent) {
                { viewModel.removeFromRecent(file.path) }
            } else null
        )
    }

    // Folder Options Sheet
    selectedFolder?.let { folder ->
        FolderOptionsSheet(
            folder = folder,
            onDismiss = { selectedFolder = null },
            onOpenClick = {
                navController.navigateToFolderDetail(folder.path, folder.name)
            }
        )
    }

    // Rename Sheet
    if (showRenameDialog) {
        fileForDialog?.let { file ->
            RenameSheet(
                pdfFile = file,
                onDismiss = {
                    showRenameDialog = false
                    fileForDialog = null
                },
                onRename = { newName ->
                    viewModel.renameFile(file.path, newName) { /* success handled internally */ }
                }
            )
        }
    }

    // Delete Confirmation Sheet
    if (showDeleteDialog) {
        fileForDialog?.let { file ->
            DeleteConfirmSheet(
                pdfFile = file,
                onDismiss = {
                    showDeleteDialog = false
                    fileForDialog = null
                },
                onConfirm = {
                    viewModel.deleteFile(file.path) { /* success handled internally */ }
                }
            )
        }
    }

    // File Info Dialog
    if (showInfoDialog) {
        fileForDialog?.let { file ->
            FileInfoDialog(
                pdfFile = file,
                onDismiss = {
                    showInfoDialog = false
                    fileForDialog = null
                }
            )
        }
    }

    // Exit Confirmation Sheet
    if (showExitConfirmSheet) {
        val activity = context as? Activity
        ExitConfirmSheet(
            onDismiss = { showExitConfirmSheet = false },
            onConfirmExit = {
                showExitConfirmSheet = false
                activity?.finishAffinity()
            }
        )
    }

    // Batch Delete Confirmation Sheet
    if (showBatchDeleteConfirm) {
        BatchDeleteConfirmSheet(
            count = selectedPaths.size,
            onDismiss = { showBatchDeleteConfirm = false },
            onConfirm = {
                viewModel.deleteSelectedFiles { success, fail ->
                    // Could show a snackbar here with results
                }
                showBatchDeleteConfirm = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchDeleteConfirmSheet(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFEF5350)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Delete $count files?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This action cannot be undone. The files will be permanently deleted from your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350)
                    )
                ) {
                    Text("Delete")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
