package com.rejowan.pdfreaderpro.presentation.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameDialog
import com.rejowan.pdfreaderpro.util.FileOperations
import com.rejowan.pdfreaderpro.presentation.components.SortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.ViewModeToggle
import com.rejowan.pdfreaderpro.presentation.navigation.Settings
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToFolderDetail
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToSearch
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.FavoritesTab
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.FilesTab
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.FoldersTab
import com.rejowan.pdfreaderpro.presentation.screens.home.tabs.RecentTab
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

enum class HomeTab(val title: String) {
    RECENT("Recent"),
    FILES("All Files"),
    FAVORITES("Favorites"),
    FOLDERS("Folders")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { HomeTab.entries.size }
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

    // Dialog states
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForDialog by remember { mutableStateOf<PdfFile?>(null) }

    Scaffold(
        topBar = {
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
                    IconButton(onClick = { navController.navigate(Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                HomeTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(tab.title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (HomeTab.entries[page]) {
                    HomeTab.RECENT -> RecentTab(
                        recentFiles = recentFiles,
                        isLoading = isLoading,
                        onFileClick = { recent ->
                            navController.navigateToReader(recent.path, recent.lastPage)
                        },
                        onFileOptionsClick = { recent ->
                            // Find the PdfFile from allFiles for the options sheet
                            allFiles.find { it.path == recent.path }?.let { pdf ->
                                selectedFile = pdf
                                scope.launch {
                                    selectedFileFavorite = viewModel.isFavorite(pdf.path)
                                }
                            }
                        },
                        onBrowseClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(HomeTab.FILES.ordinal)
                            }
                        }
                    )

                    HomeTab.FILES -> FilesTab(
                        files = allFiles,
                        viewMode = viewMode,
                        isLoading = isLoading,
                        onFileClick = { pdf ->
                            navController.navigateToReader(pdf.path)
                        },
                        onFileOptionsClick = { pdf ->
                            selectedFile = pdf
                            scope.launch {
                                selectedFileFavorite = viewModel.isFavorite(pdf.path)
                            }
                        },
                        onRefresh = { viewModel.refresh() }
                    )

                    HomeTab.FAVORITES -> FavoritesTab(
                        favorites = favoriteFiles,
                        viewMode = viewMode,
                        isLoading = isLoading,
                        onFileClick = { pdf ->
                            navController.navigateToReader(pdf.path)
                        },
                        onFileOptionsClick = { pdf ->
                            selectedFile = pdf
                            selectedFileFavorite = true
                        },
                        onBrowseClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(HomeTab.FILES.ordinal)
                            }
                        }
                    )

                    HomeTab.FOLDERS -> FoldersTab(
                        folders = folders,
                        isLoading = isLoading,
                        onFolderClick = { folder ->
                            navController.navigateToFolderDetail(folder.path, folder.name)
                        },
                        onRefresh = { viewModel.refresh() }
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
