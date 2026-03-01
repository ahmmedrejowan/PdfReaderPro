package com.rejowan.pdfreaderpro.presentation.screens.tools.split

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors
private val AccentOrange = Color(0xFFFF9800)
private val AccentGreen = Color(0xFF81C784)
private val AccentRed = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: SplitViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

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
                        Text("Split PDF")
                        state.sourceFile?.let { file ->
                            Text(
                                "${file.pageCount} pages",
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
            when {
                state.sourceFile == null && state.result == null -> {
                    // Empty state - no file selected
                    EmptyState(
                        onSelectFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                state.result != null -> {
                    // Success state
                    SuccessState(
                        result = state.result!!,
                        onOpenFolder = {
                            val folder = File(state.result!!.outputDir)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                folder
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "resource/folder")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback: try to open with file manager
                                val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                }
                                context.startActivity(Intent.createChooser(fallbackIntent, "Open folder"))
                            }
                        },
                        onSplitMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                else -> {
                    // Split options
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Source file card
                            item {
                                SourceFileCard(
                                    sourceFile = state.sourceFile!!,
                                    onChangeFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                                )
                            }

                            // Split mode selection
                            item {
                                SplitModeSection(
                                    selectedMode = state.splitMode,
                                    onModeSelected = { viewModel.setSplitMode(it) }
                                )
                            }

                            // Mode-specific options
                            item {
                                SplitOptionsSection(
                                    state = state,
                                    onRangesChange = { viewModel.setRangesInput(it) },
                                    onEveryNChange = { viewModel.setEveryNPages(it) },
                                    onSpecificPagesChange = { viewModel.setSpecificPagesInput(it) }
                                )
                            }
                        }

                        // Bottom section
                        SplitBottomSection(
                            outputPrefix = state.outputPrefix,
                            onPrefixChange = { viewModel.setOutputPrefix(it) },
                            isProcessing = state.isProcessing,
                            progress = state.progress,
                            canSplit = state.sourceFile != null,
                            error = state.error,
                            onSplit = { viewModel.split() },
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
                .background(AccentOrange.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.CallSplit,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Split PDF File",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Select a PDF file to split into multiple parts or extract specific pages",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun SourceFileCard(
    sourceFile: SourceFile,
    onChangeFile: () -> Unit
) {
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AccentRed
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sourceFile.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${sourceFile.pageCount} pages • ${formatFileSize(sourceFile.size)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onChangeFile,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Change")
            }
        }
    }
}

@Composable
private fun SplitModeSection(
    selectedMode: SplitMode,
    onModeSelected: (SplitMode) -> Unit
) {
    Column {
        Text(
            "Split Mode",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SplitModeChip(
                label = "By Ranges",
                selected = selectedMode == SplitMode.BY_RANGES,
                onClick = { onModeSelected(SplitMode.BY_RANGES) },
                modifier = Modifier.weight(1f)
            )
            SplitModeChip(
                label = "Every N",
                selected = selectedMode == SplitMode.EVERY_N_PAGES,
                onClick = { onModeSelected(SplitMode.EVERY_N_PAGES) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SplitModeChip(
                label = "Single Pages",
                selected = selectedMode == SplitMode.INTO_PAGES,
                onClick = { onModeSelected(SplitMode.INTO_PAGES) },
                modifier = Modifier.weight(1f)
            )
            SplitModeChip(
                label = "Extract",
                selected = selectedMode == SplitMode.SPECIFIC_PAGES,
                onClick = { onModeSelected(SplitMode.SPECIFIC_PAGES) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SplitModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier.height(40.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentOrange.copy(alpha = 0.15f),
            selectedLabelColor = AccentOrange
        )
    )
}

@Composable
private fun SplitOptionsSection(
    state: SplitState,
    onRangesChange: (String) -> Unit,
    onEveryNChange: (Int) -> Unit,
    onSpecificPagesChange: (String) -> Unit
) {
    Column {
        Text(
            "Options",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when (state.splitMode) {
            SplitMode.BY_RANGES -> {
                OutlinedTextField(
                    value = state.rangesInput,
                    onValueChange = onRangesChange,
                    label = { Text("Page Ranges") },
                    placeholder = { Text("e.g., 1-5, 6-10, 11-15") },
                    supportingText = {
                        Text("Separate ranges with commas. Max page: ${state.sourceFile?.pageCount ?: 0}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            SplitMode.EVERY_N_PAGES -> {
                Column {
                    Text(
                        "Split every ${state.everyNPages} pages",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = state.everyNPages.toFloat(),
                        onValueChange = { onEveryNChange(it.toInt()) },
                        valueRange = 1f..20f,
                        steps = 18,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Will create ${calculateParts(state.sourceFile?.pageCount ?: 0, state.everyNPages)} file(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SplitMode.INTO_PAGES -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentOrange.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = AccentOrange
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Split into individual pages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Will create ${state.sourceFile?.pageCount ?: 0} separate PDF files",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            SplitMode.SPECIFIC_PAGES -> {
                OutlinedTextField(
                    value = state.specificPagesInput,
                    onValueChange = onSpecificPagesChange,
                    label = { Text("Pages to Extract") },
                    placeholder = { Text("e.g., 1, 3, 5-8, 12") },
                    supportingText = {
                        Text("Enter page numbers or ranges. Max page: ${state.sourceFile?.pageCount ?: 0}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
        }
    }
}

@Composable
private fun SplitBottomSection(
    outputPrefix: String,
    onPrefixChange: (String) -> Unit,
    isProcessing: Boolean,
    progress: Float,
    canSplit: Boolean,
    error: String?,
    onSplit: () -> Unit,
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

        // Output prefix input
        OutlinedTextField(
            value = outputPrefix,
            onValueChange = onPrefixChange,
            label = { Text("Output prefix") },
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
                "Saved to: Documents/PdfReaderPro/split_$outputPrefix/",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Split button
        Button(
            onClick = onSplit,
            enabled = canSplit && !isProcessing,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Split PDF")
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
                    color = AccentOrange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Splitting PDF...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentOrange
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
    result: SplitResult,
    onOpenFolder: () -> Unit,
    onSplitMore: () -> Unit,
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
            "Split Complete!",
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
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AccentOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "${result.createdFiles.size} files created",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    result.outputDir,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List created files
                result.createdFiles.take(5).forEach { filePath ->
                    Text(
                        "• ${File(filePath).name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (result.createdFiles.size > 5) {
                    Text(
                        "• ... and ${result.createdFiles.size - 5} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onOpenFolder,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Open Folder")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSplitMore,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Split Another PDF")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done")
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

private fun calculateParts(totalPages: Int, everyN: Int): Int {
    if (totalPages == 0 || everyN == 0) return 0
    return (totalPages + everyN - 1) / everyN
}
