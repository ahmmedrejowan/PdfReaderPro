package com.rejowan.pdfreaderpro.presentation.screens.folder

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.presentation.components.AnimatedSelectionActionBar
import com.rejowan.pdfreaderpro.presentation.components.EmptyState
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfGridItem
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState
import com.rejowan.pdfreaderpro.presentation.components.SortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameSheet
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToMergeTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import com.rejowan.pdfreaderpro.util.FileOperations
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val FolderAmber = Color(0xFFFFB74D)
private val FolderAmberDark = Color(0xFFF57C00)
private val SoftBlue = Color(0xFF64B5F6)

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

    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else true
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else true

                if (newPermissionState && !hasPermission) {
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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    // Selection mode state
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPaths by viewModel.selectedPaths.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFavorite by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForDialog by remember { mutableStateOf<PdfFile?>(null) }

    LaunchedEffect(folderPath) {
        viewModel.loadFilesForFolder(folderPath)
    }

    // Handle back press: exit selection mode first, then navigate back
    BackHandler(enabled = isSelectionMode) {
        viewModel.exitSelectionMode()
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = folderName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshFolder() },
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
                isLoading && files.isEmpty() -> {
                    LoadingState()
                }
                files.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.Description,
                        title = stringResource(R.string.folder_empty),
                        message = stringResource(R.string.no_pdf_in_folder),
                        accentColor = SoftBlue
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                    // Folder header with info and controls
                    FolderDetailHeader(
                        fileCount = files.size,
                        totalSize = files.sumOf { it.size },
                        isGridView = viewMode == ViewMode.GRID,
                        onViewModeToggle = {
                            viewModel.setViewMode(
                                if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                            )
                        },
                        onSortClick = { showSortSheet = true }
                    )

                    // File list/grid
                    val bottomPadding = if (isSelectionMode) 160.dp else 16.dp
                    if (viewMode == ViewMode.LIST) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = bottomPadding)
                        ) {
                            itemsIndexed(
                                items = files,
                                key = { _, file -> file.id }
                            ) { index, file ->
                                PdfListItem(
                                    pdfFile = file,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(file.path)
                                        } else {
                                            navController.navigateToReader(file.path)
                                        }
                                    },
                                    onOptionsClick = {
                                        selectedFile = file
                                        scope.launch {
                                            selectedFileFavorite = viewModel.isFavorite(file.path)
                                        }
                                    },
                                    animationDelay = index * 30,
                                    modifier = Modifier.animateItem(),
                                    isSelectionMode = isSelectionMode,
                                    isSelected = file.path in selectedPaths,
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            viewModel.enterSelectionMode(file.path)
                                        }
                                    },
                                    onSelectionToggle = { viewModel.toggleSelection(file.path) }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = bottomPadding)
                        ) {
                            itemsIndexed(
                                items = files,
                                key = { _, file -> file.id }
                            ) { index, file ->
                                PdfGridItem(
                                    pdfFile = file,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(file.path)
                                        } else {
                                            navController.navigateToReader(file.path)
                                        }
                                    },
                                    onOptionsClick = {
                                        selectedFile = file
                                        scope.launch {
                                            selectedFileFavorite = viewModel.isFavorite(file.path)
                                        }
                                    },
                                    animationDelay = index * 30,
                                    modifier = Modifier.animateItem(),
                                    isSelectionMode = isSelectionMode,
                                    isSelected = file.path in selectedPaths,
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            viewModel.enterSelectionMode(file.path)
                                        }
                                    },
                                    onSelectionToggle = { viewModel.toggleSelection(file.path) }
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }

        // Selection action bar at bottom
        AnimatedSelectionActionBar(
            visible = isSelectionMode,
            selectedCount = selectedPaths.size,
            totalCount = files.size,
            onClose = { viewModel.exitSelectionMode() },
            onSelectAll = { viewModel.selectAll() },
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

    // Batch Delete Confirmation Sheet
    if (showBatchDeleteConfirm) {
        BatchDeleteConfirmSheet(
            count = selectedPaths.size,
            onDismiss = { showBatchDeleteConfirm = false },
            onConfirm = {
                viewModel.deleteSelectedFiles { _, _ -> }
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
                text = stringResource(R.string.delete_files_count, count),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.delete_files_warning),
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

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderDetailHeader(
    fileCount: Int,
    totalSize: Long,
    isGridView: Boolean,
    onViewModeToggle: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File count and size chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File count chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftBlue.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SoftBlue
                    )
                    Text(
                        text = "$fileCount PDF${if (fileCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Total size chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = FolderAmber.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = FolderAmber
                    )
                    Text(
                        text = formatSize(totalSize),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // View mode and sort controls with tooltips
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // List view
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text("List view") } },
                state = rememberTooltipState()
            ) {
                ViewModeButton(
                    isSelected = !isGridView,
                    onClick = { if (isGridView) onViewModeToggle() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ViewList,
                        contentDescription = stringResource(R.string.cd_list_view),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // Grid view
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text("Grid view") } },
                state = rememberTooltipState()
            ) {
                ViewModeButton(
                    isSelected = isGridView,
                    onClick = { if (!isGridView) onViewModeToggle() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = stringResource(R.string.cd_grid_view),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // Sort
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text("Sort files") } },
                state = rememberTooltipState()
            ) {
                ViewModeButton(
                    isSelected = false,
                    onClick = onSortClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = stringResource(R.string.cd_sort),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewModeButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.surfaceContainerHighest
        else
            Color.Transparent,
        label = "view mode background"
    )
    val tint by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        label = "view mode tint"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        contentColor = tint
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}
