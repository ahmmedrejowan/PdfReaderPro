package com.rejowan.pdfreaderpro.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.rejowan.pdfreaderpro.presentation.components.AnimatedBottomNav
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.SortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.ViewModeToggle
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameDialog
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

    // Re-check permission when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else true

                if (newPermissionState && !hasPermission) {
                    // Permission was just granted, refresh data
                    viewModel.refresh()
                }
                hasPermission = newPermissionState
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

    var selectedNavItem by remember { mutableIntStateOf(0) }
    val homeSubTabPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { HomeSubTab.entries.size }
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    val allFiles by viewModel.allFiles.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val favoriteFiles by viewModel.favoriteFiles.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFavorite by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForDialog by remember { mutableStateOf<PdfFile?>(null) }

    Scaffold(
        topBar = {
            when (BottomNavItem.entries[selectedNavItem]) {
                BottomNavItem.HOME -> {
                    TopAppBar(
                        title = { Text("PDF Reader Pro") },
                        actions = {
                            ViewModeToggle(
                                currentMode = viewMode,
                                onModeChange = { viewModel.setViewMode(it) }
                            )
                            IconButton(onClick = { showSortSheet = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                            }
                            IconButton(onClick = { navController.navigateToSearch() }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    )
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
        bottomBar = {
            AnimatedBottomNav(
                selectedIndex = selectedNavItem,
                onItemClick = { index -> selectedNavItem = index }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (BottomNavItem.entries[selectedNavItem]) {
                BottomNavItem.HOME -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-tabs for Home
                        SecondaryTabRow(selectedTabIndex = homeSubTabPagerState.currentPage) {
                            HomeSubTab.entries.forEachIndexed { index, tab ->
                                Tab(
                                    selected = homeSubTabPagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            homeSubTabPagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(tab.title) }
                                )
                            }
                        }

                        HorizontalPager(
                            state = homeSubTabPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (HomeSubTab.entries[page]) {
                                HomeSubTab.RECENT -> RecentTab(
                                    recentFiles = recentFiles,
                                    isLoading = isLoading,
                                    hasPermission = hasPermission,
                                    onFileClick = { recent ->
                                        navController.navigateToReader(recent.path, recent.lastPage)
                                    },
                                    onFileOptionsClick = { recent ->
                                        allFiles.find { it.path == recent.path }?.let { pdf ->
                                            selectedFile = pdf
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
                                    },
                                    onBrowseClick = {
                                        scope.launch {
                                            homeSubTabPagerState.animateScrollToPage(HomeSubTab.ALL.ordinal)
                                        }
                                    },
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
                        onFolderClick = { folder ->
                            navController.navigateToFolderDetail(folder.path, folder.name)
                        },
                        onRefresh = { viewModel.refresh() },
                        onGrantPermissionClick = openPermissionSettings
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

    // Sort Options Sheet
    if (showSortSheet) {
        SortOptionsSheet(
            currentSort = sortOption,
            onSortSelected = { viewModel.setSortOption(it) },
            onDismiss = { showSortSheet = false }
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
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog) {
        fileForDialog?.let { file ->
            RenameDialog(
                pdfFile = file,
                onDismiss = {
                    showRenameDialog = false
                    fileForDialog = null
                },
                onRename = { newName ->
                    FileOperations.renameFile(file.path, newName)
                    viewModel.refresh()
                }
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        fileForDialog?.let { file ->
            DeleteConfirmDialog(
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
}
