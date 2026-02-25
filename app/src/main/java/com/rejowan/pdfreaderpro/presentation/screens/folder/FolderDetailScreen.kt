package com.rejowan.pdfreaderpro.presentation.screens.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.presentation.components.EmptyState
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfGridItem
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem
import com.rejowan.pdfreaderpro.presentation.components.SortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.ViewModeToggle
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameDialog
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import com.rejowan.pdfreaderpro.util.FileOperations
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    navController: NavController,
    folderPath: String,
    folderName: String,
    viewModel: FolderDetailViewModel = koinViewModel()
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
                    viewModel.loadFilesForFolder(folderPath)
                }
                hasPermission = newPermissionState
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val files by viewModel.files.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFavorite by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForDialog by remember { mutableStateOf<PdfFile?>(null) }

    // Load files for this folder
    androidx.compose.runtime.LaunchedEffect(folderPath) {
        viewModel.loadFilesForFolder(folderPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    ViewModeToggle(
                        currentMode = viewMode,
                        onModeChange = { viewModel.setViewMode(it) }
                    )
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
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
            when {
                !hasPermission -> {
                    PermissionRequiredState(
                        onGrantClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
                isLoading -> {
                    LoadingState()
                }
                files.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.Description,
                        title = "No PDFs",
                        message = "This folder doesn't contain any PDF files."
                    )
                }
                viewMode == ViewMode.LIST -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = files,
                            key = { it.id }
                        ) { file ->
                            PdfListItem(
                                pdfFile = file,
                                onClick = {
                                    navController.navigateToReader(file.path)
                                },
                                onOptionsClick = {
                                    selectedFile = file
                                    scope.launch {
                                        selectedFileFavorite = viewModel.isFavorite(file.path)
                                    }
                                }
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(
                            items = files,
                            key = { it.id }
                        ) { file ->
                            PdfGridItem(
                                pdfFile = file,
                                onClick = {
                                    navController.navigateToReader(file.path)
                                },
                                onOptionsClick = {
                                    selectedFile = file
                                    scope.launch {
                                        selectedFileFavorite = viewModel.isFavorite(file.path)
                                    }
                                }
                            )
                        }
                    }
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
                    viewModel.loadFilesForFolder(folderPath)
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
                    viewModel.loadFilesForFolder(folderPath)
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
