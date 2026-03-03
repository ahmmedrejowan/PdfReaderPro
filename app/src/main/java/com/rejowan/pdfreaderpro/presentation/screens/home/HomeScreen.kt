package com.rejowan.pdfreaderpro.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.presentation.components.AnimatedBottomNav
import com.rejowan.pdfreaderpro.presentation.components.ClickableSearchBar
import com.rejowan.pdfreaderpro.presentation.components.CompactTabRow
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.FolderOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.SortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.StatsSheet
import com.rejowan.pdfreaderpro.presentation.components.WelcomeHeader
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.ExitConfirmSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameSheet
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToFolderDetail
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
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val folderSortOption by viewModel.folderSortOption.collectAsState()
    val folderSearchQuery by viewModel.folderSearchQuery.collectAsState()

    val allFiles by viewModel.allFiles.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val favoriteFiles by viewModel.favoriteFiles.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
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

    // Handle back press: navigate to Home first, then show exit confirmation
    BackHandler(enabled = true) {
        if (selectedNavItem != 0) {
            // If not on Home tab, navigate to Home first
            selectedNavItem = 0
        } else {
            // If already on Home, show exit confirmation
            showExitConfirmSheet = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            // Welcome Header with greeting
                            WelcomeHeader(
                                onSortClick = { showSortSheet = true },
                                onStatsClick = { showStatsSheet = true }
                            )

                            // Search bar
                            ClickableSearchBar(
                                onClick = { navController.navigateToSearch() }
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
                                        onGrantPermissionClick = openPermissionSettings
                                    )

                                    HomeSubTab.FAVORITES -> FavoritesTab(
                                        favorites = favoriteFiles,
                                        viewMode = viewMode,
                                        isLoading = isLoading,
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
                                        onGrantPermissionClick = openPermissionSettings
                                    )

                                    HomeSubTab.ALL -> FilesTab(
                                        files = allFiles,
                                        viewMode = viewMode,
                                        isLoading = isLoading,
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
                                        onGrantPermissionClick = openPermissionSettings
                                    )
                                }
                            }
                        }
                    }

                    BottomNavItem.FOLDERS -> {
                        FoldersScreen(
                            folders = folders,
                            isLoading = isLoading,
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

        // Bottom navigation bar - outside Scaffold, floating on top
        AnimatedBottomNav(
            selectedIndex = selectedNavItem,
            onItemClick = { index -> selectedNavItem = index },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                    FileOperations.deleteFile(file.path)
                    viewModel.refresh()
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
}
