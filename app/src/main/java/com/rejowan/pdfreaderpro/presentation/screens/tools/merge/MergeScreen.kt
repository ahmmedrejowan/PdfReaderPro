package com.rejowan.pdfreaderpro.presentation.screens.tools.merge

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Visibility
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors - consistent with app design system
private val AccentPurple = Color(0xFF9575CD)    // Primary actions (merge)
private val AccentBlue = Color(0xFF64B5F6)      // General/Info actions
private val AccentTeal = Color(0xFF4DB6AC)      // Preview/View
private val AccentAmber = Color(0xFFFFB74D)     // Range/Pages selection
private val AccentGreen = Color(0xFF81C784)     // Success
private val AccentRed = Color(0xFFEF5350)       // PDF icon, Remove

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeScreen(
    navController: NavController,
    initialFiles: List<String> = emptyList(),
    viewModel: MergeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Page selection sheet state
    var selectedFileForPageSelection by remember { mutableStateOf<MergeFile?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.addFiles(uris)
    }

    // Page Selection Sheet (hybrid: bottom sheet in portrait, side panel in landscape)
    if (selectedFileForPageSelection != null) {
        PageSelectionSheet(
            file = selectedFileForPageSelection!!,
            onDismiss = { selectedFileForPageSelection = null },
            onSelectionChanged = { selection ->
                viewModel.updatePageSelection(selectedFileForPageSelection!!, selection)
                selectedFileForPageSelection = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Merge PDFs")
                        if (state.selectedFiles.isNotEmpty()) {
                            Text(
                                "${state.selectedFiles.size} files selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show add button in top bar when files are selected
                    if (state.selectedFiles.isNotEmpty() && !state.isProcessing && state.result == null) {
                        IconButton(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add more PDFs",
                                tint = AccentBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.selectedFiles.isEmpty() && state.result == null) {
                // Empty state
                EmptyState(
                    onAddFiles = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                )
            } else if (state.result != null) {
                // Success state
                SuccessState(
                    result = state.result!!,
                    onOpenInApp = {
                        // Open in app's reader screen
                        navController.navigateToReader(state.result!!.outputPath)
                    },
                    onOpenWith = {
                        // Open with external app via ACTION_VIEW
                        val file = File(state.result!!.outputPath)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Open with"))
                    },
                    onMergeMore = {
                        viewModel.reset()
                    },
                    onDone = {
                        navController.popBackStack()
                    }
                )
            } else {
                // File list and merge options
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // File list with drag-to-reorder
                    val lazyListState = rememberLazyListState()
                    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                        viewModel.moveFile(from.index, to.index)
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            count = state.selectedFiles.size,
                            key = { index -> state.selectedFiles[index].path }
                        ) { index ->
                            val file = state.selectedFiles[index]
                            ReorderableItem(reorderableLazyListState, key = file.path) { isDragging ->
                                MergeFileItem(
                                    file = file,
                                    index = index + 1,
                                    isDragging = isDragging,
                                    dragHandleModifier = Modifier.draggableHandle(),
                                    longPressDragModifier = Modifier.longPressDraggableHandle(),
                                    onPreview = { navController.navigateToReader(file.path) },
                                    onSelectPages = { selectedFileForPageSelection = file },
                                    onRemove = { viewModel.removeFile(file) }
                                )
                            }
                        }

                        // Add more files card at the end of the list
                        item {
                            AddMoreFilesCard(
                                onAddFiles = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                            )
                        }
                    }

                    // Bottom section: filename input and merge button
                    MergeBottomSection(
                        outputFileName = state.outputFileName,
                        onFileNameChange = { viewModel.setOutputFileName(it) },
                        isProcessing = state.isProcessing,
                        progress = state.progress,
                        canMerge = state.selectedFiles.size >= 2,
                        error = state.error,
                        onMerge = { viewModel.merge() },
                        onClearError = { viewModel.clearError() }
                    )
                }
            }

            // Processing overlay
            AnimatedVisibility(
                visible = state.isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ProcessingOverlay(progress = state.progress)
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddFiles: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AccentBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.CallMerge,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Merge PDF Files",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Select multiple PDF files to combine them into a single document",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddFiles,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDFs")
        }
    }
}

@Composable
private fun MergeFileItem(
    file: MergeFile,
    index: Int,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    longPressDragModifier: Modifier = Modifier,
    onPreview: () -> Unit,
    onSelectPages: () -> Unit,
    onRemove: () -> Unit
) {
    val selectedPageCount = file.pageSelection.getSelectedCount(file.pageCount)
    val isPartialSelection = file.pageSelection !is PageSelection.All

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(longPressDragModifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top row: Drag handle, thumbnail, file info, remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle - directly draggable
                Box(
                    modifier = dragHandleModifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isDragging) AccentPurple.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        modifier = Modifier.size(20.dp),
                        tint = if (isDragging) {
                            AccentPurple
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Index badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$index",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // PDF Thumbnail
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (file.thumbnail != null) {
                        Image(
                            bitmap = file.thumbnail.asImageBitmap(),
                            contentDescription = "PDF preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = AccentRed
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // File info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${formatFileSize(file.size)} • ${file.pageCount} pages",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Remove button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentRed.copy(alpha = 0.08f))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp),
                        tint = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: Action chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preview chip
                ActionChip(
                    icon = Icons.Outlined.Visibility,
                    label = "Preview",
                    color = AccentTeal,
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                )

                // Pages chip
                ActionChip(
                    icon = Icons.Default.Pages,
                    label = if (isPartialSelection) {
                        "$selectedPageCount/${file.pageCount} pages"
                    } else {
                        "All ${file.pageCount} pages"
                    },
                    color = AccentAmber,
                    isHighlighted = isPartialSelection,
                    onClick = onSelectPages,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (isHighlighted) 0.15f else 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AddMoreFilesCard(
    onAddFiles: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = AccentTeal.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onAddFiles),
        color = AccentTeal.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentTeal.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp),
                    tint = AccentTeal
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Add more files",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MergeBottomSection(
    outputFileName: String,
    onFileNameChange: (String) -> Unit,
    isProcessing: Boolean,
    progress: Float,
    canMerge: Boolean,
    error: String?,
    onMerge: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Error message
        AnimatedVisibility(visible = error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentRed,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onClearError,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(16.dp),
                            tint = AccentRed
                        )
                    }
                }
            }
        }

        // Output filename input
        OutlinedTextField(
            value = outputFileName,
            onValueChange = onFileNameChange,
            label = { Text("Output filename") },
            suffix = { Text(".pdf") },
            singleLine = true,
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Output location info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Saved to: Documents/PdfReaderPro/",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Merge button
        Button(
            onClick = onMerge,
            enabled = canMerge && !isProcessing,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.CallMerge, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Merge PDFs")
        }
    }
}

@Composable
private fun ProcessingOverlay(progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(48.dp),
                    color = AccentBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Merging PDFs...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SuccessState(
    result: MergeResult,
    onOpenInApp: () -> Unit,
    onOpenWith: () -> Unit,
    onMergeMore: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn() + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AccentGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Merge Complete!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Result info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AccentRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        File(result.outputPath).name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${result.pageCount} pages • ${formatFileSize(result.fileSize)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    result.outputPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Open in App button (primary)
        Button(
            onClick = onOpenInApp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open in App", style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Open With button (secondary)
        OutlinedButton(
            onClick = onOpenWith,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open With...", style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Merge More button
        OutlinedButton(
            onClick = onMergeMore,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Merge More Files", style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Done button
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Done", style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}

// ============================================================================
// Page Selection Sheet - Hybrid Pattern
// ============================================================================

private enum class SelectionMode {
    ALL, RANGE, CUSTOM
}

@Composable
private fun PageSelectionSheet(
    file: MergeFile,
    onDismiss: () -> Unit,
    onSelectionChanged: (PageSelection) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        PageSelectionSidePanel(
            file = file,
            onDismiss = onDismiss,
            onSelectionChanged = onSelectionChanged
        )
    } else {
        PageSelectionBottomSheet(
            file = file,
            onDismiss = onDismiss,
            onSelectionChanged = onSelectionChanged
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageSelectionBottomSheet(
    file: MergeFile,
    onDismiss: () -> Unit,
    onSelectionChanged: (PageSelection) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        PageSelectionContent(
            file = file,
            onDismiss = onDismiss,
            onSelectionChanged = onSelectionChanged,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PageSelectionSidePanel(
    file: MergeFile,
    onDismiss: () -> Unit,
    onSelectionChanged: (PageSelection) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    }
            )
        }

        // Side panel
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                PageSelectionContent(
                    file = file,
                    onDismiss = onDismiss,
                    onSelectionChanged = onSelectionChanged,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = systemBarsPadding.calculateTopPadding(),
                            bottom = systemBarsPadding.calculateBottomPadding()
                        )
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun PageSelectionContent(
    file: MergeFile,
    onDismiss: () -> Unit,
    onSelectionChanged: (PageSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine initial mode from current selection
    val initialMode = when (file.pageSelection) {
        is PageSelection.All -> SelectionMode.ALL
        is PageSelection.Range -> SelectionMode.RANGE
        is PageSelection.Custom -> SelectionMode.CUSTOM
    }

    var selectionMode by remember { mutableStateOf(initialMode) }

    // Range inputs
    var rangeStart by remember {
        mutableStateOf(
            when (val sel = file.pageSelection) {
                is PageSelection.Range -> sel.start.toString()
                else -> "1"
            }
        )
    }
    var rangeEnd by remember {
        mutableStateOf(
            when (val sel = file.pageSelection) {
                is PageSelection.Range -> sel.end.toString()
                else -> file.pageCount.toString()
            }
        )
    }

    // Custom pages input
    var customPages by remember {
        mutableStateOf(
            when (val sel = file.pageSelection) {
                is PageSelection.Custom -> sel.pages.joinToString(", ")
                else -> ""
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentAmber.copy(alpha = 0.12f)
            ) {
                Icon(
                    Icons.Default.Pages,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = AccentAmber
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Select Pages",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    file.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total pages info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AccentRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Total: ${file.pageCount} pages",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selection mode chips
        Text(
            "Selection Mode",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectionMode == SelectionMode.ALL,
                onClick = { selectionMode = SelectionMode.ALL },
                label = { Text("All") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentAmber.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            FilterChip(
                selected = selectionMode == SelectionMode.RANGE,
                onClick = { selectionMode = SelectionMode.RANGE },
                label = { Text("Range") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentAmber.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            FilterChip(
                selected = selectionMode == SelectionMode.CUSTOM,
                onClick = { selectionMode = SelectionMode.CUSTOM },
                label = { Text("Custom") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentAmber.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode-specific options
        when (selectionMode) {
            SelectionMode.ALL -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = AccentGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "All ${file.pageCount} pages will be included",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            SelectionMode.RANGE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rangeStart,
                        onValueChange = { rangeStart = it.filter { c -> c.isDigit() } },
                        label = { Text("From") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rangeEnd,
                        onValueChange = { rangeEnd = it.filter { c -> c.isDigit() } },
                        label = { Text("To") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Pages $rangeStart to $rangeEnd will be included",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            SelectionMode.CUSTOM -> {
                OutlinedTextField(
                    value = customPages,
                    onValueChange = { customPages = it },
                    label = { Text("Page numbers") },
                    placeholder = { Text("e.g., 1, 3, 5-8, 12") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    supportingText = {
                        Text("Enter page numbers or ranges separated by commas")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    val selection = when (selectionMode) {
                        SelectionMode.ALL -> PageSelection.All
                        SelectionMode.RANGE -> {
                            val start = rangeStart.toIntOrNull()?.coerceIn(1, file.pageCount) ?: 1
                            val end = rangeEnd.toIntOrNull()?.coerceIn(start, file.pageCount) ?: file.pageCount
                            PageSelection.Range(start, end)
                        }
                        SelectionMode.CUSTOM -> {
                            val pages = parseCustomPages(customPages, file.pageCount)
                            if (pages.isEmpty()) PageSelection.All else PageSelection.Custom(pages)
                        }
                    }
                    onSelectionChanged(selection)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun parseCustomPages(input: String, maxPages: Int): List<Int> {
    val pages = mutableSetOf<Int>()

    input.split(",").forEach { part ->
        val trimmed = part.trim()
        if (trimmed.contains("-")) {
            // Range: "5-8"
            val rangeParts = trimmed.split("-")
            if (rangeParts.size == 2) {
                val start = rangeParts[0].trim().toIntOrNull()
                val end = rangeParts[1].trim().toIntOrNull()
                if (start != null && end != null) {
                    for (i in start..end) {
                        if (i in 1..maxPages) pages.add(i)
                    }
                }
            }
        } else {
            // Single page: "3"
            trimmed.toIntOrNull()?.let { page ->
                if (page in 1..maxPages) pages.add(page)
            }
        }
    }

    return pages.sorted()
}
