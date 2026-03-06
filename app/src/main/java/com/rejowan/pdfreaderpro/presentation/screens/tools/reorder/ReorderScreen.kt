package com.rejowan.pdfreaderpro.presentation.screens.tools.reorder

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import java.io.File

// Accent colors
private val AccentIndigo = Color(0xFF7986CB)  // Reorder theme color
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentGreen = Color(0xFF81C784)
private val AccentRed = Color(0xFFEF5350)
private val AccentBlue = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: ReorderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.setSourceFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reorder Pages")
                        state.sourceFile?.let { file ->
                            Text(
                                "${file.pageCount} pages${if (state.hasChanges) " • Modified" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.hasChanges) AccentIndigo else MaterialTheme.colorScheme.onSurfaceVariant
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
                    if (state.hasChanges) {
                        IconButton(onClick = { viewModel.resetOrder() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset order",
                                tint = AccentRed
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            when {
                state.sourceFile == null && state.result == null -> {
                    EmptyState(
                        onSelectFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                state.result != null -> {
                    SuccessState(
                        result = state.result!!,
                        onOpenInApp = { navController.navigateToReader(state.result!!.outputPath) },
                        onOpenWith = {
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
                        onShare = {
                            val file = File(state.result!!.outputPath)
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
                        },
                        onReorderMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentIndigo)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading pages...")
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Instruction hint
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = AccentIndigo.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AccentIndigo
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Long press and drag pages to reorder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentIndigo
                                )
                            }
                        }

                        // Reorderable page grid
                        val gridState = rememberLazyGridState()
                        val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
                            viewModel.movePage(from.index, to.index)
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                items = state.pages,
                                key = { _, page -> page.originalIndex }
                            ) { index, page ->
                                ReorderableItem(reorderableState, key = page.originalIndex) { isDragging ->
                                    PageThumbnailItem(
                                        page = page,
                                        currentPosition = index + 1,
                                        isDragging = isDragging,
                                        hasChanged = page.originalIndex != index,
                                        modifier = Modifier.longPressDraggableHandle(),
                                        onTap = { focusManager.clearFocus() }
                                    )
                                }
                            }
                        }

                        // Bottom section
                        ReorderBottomSection(
                            outputFileName = state.outputFileName,
                            onFileNameChange = { viewModel.setOutputFileName(it) },
                            overwriteOriginal = state.overwriteOriginal,
                            onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                            isProcessing = state.isProcessing,
                            progress = state.progress,
                            canSave = state.sourceFile != null && state.hasChanges,
                            error = state.error,
                            onSave = { viewModel.reorder() },
                            onClearError = { viewModel.clearError() }
                        )
                    }
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
private fun EmptyState(onSelectFile: () -> Unit) {
    // Floating animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "empty state float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-floatOffset).dp)
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AccentIndigo.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SwapVert,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentIndigo
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Reorder PDF Pages",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Drag and drop pages to rearrange their order in the PDF",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDF")
        }
    }
}

@Composable
private fun PageThumbnailItem(
    page: PageItem,
    currentPosition: Int,
    isDragging: Boolean,
    hasChanged: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .then(
                if (isDragging) {
                    Modifier
                        .scale(1.05f)
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .then(
                if (hasChanged && !isDragging) {
                    Modifier.border(2.dp, AccentIndigo, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(onClick = onTap),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail
            if (page.thumbnail != null) {
                Image(
                    bitmap = page.thumbnail.asImageBitmap(),
                    contentDescription = "Page ${page.pageNumber}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Drag handle indicator at top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Position badge (current position)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .background(
                        color = if (hasChanged) AccentIndigo else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "$currentPosition",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (hasChanged) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Original page number (if different)
            if (hasChanged) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "was ${page.pageNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderBottomSection(
    outputFileName: String,
    onFileNameChange: (String) -> Unit,
    overwriteOriginal: Boolean,
    onOverwriteChange: (Boolean) -> Unit,
    isProcessing: Boolean,
    progress: Float,
    canSave: Boolean,
    error: String?,
    onSave: () -> Unit,
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

        // Overwrite original checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOverwriteChange(!overwriteOriginal) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = overwriteOriginal,
                onCheckedChange = onOverwriteChange,
                colors = CheckboxDefaults.colors(checkedColor = AccentIndigo)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Overwrite original file", style = MaterialTheme.typography.bodyMedium)
        }

        // Output filename
        AnimatedVisibility(visible = !overwriteOriginal) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = outputFileName,
                    onValueChange = onFileNameChange,
                    label = { Text("Output file name") },
                    suffix = { Text(".pdf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = canSave && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.SwapVert, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isProcessing) "Saving..." else "Save Reordered PDF")
        }
    }
}

@Composable
private fun SuccessState(
    result: ReorderResult,
    onOpenInApp: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onReorderMore: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Success icon
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Pages Reordered!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Your PDF has been saved with the new page order",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // File info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AccentIndigo
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        File(result.outputPath).name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${result.pageCount} pages • ${formatFileSize(result.fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenInApp,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open", maxLines = 1)
            }
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onReorderMore,
                modifier = Modifier.weight(1f)
            ) {
                Text("New File", maxLines = 1)
            }
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f)
            ) {
                Text("Done", maxLines = 1)
            }
        }
    }
}

@Composable
private fun ProcessingOverlay(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(56.dp),
                    color = AccentIndigo,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Saving...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentIndigo
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
