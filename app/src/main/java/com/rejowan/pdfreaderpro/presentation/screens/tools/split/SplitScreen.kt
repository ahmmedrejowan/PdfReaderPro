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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Visibility
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors - consistent with app design system
private val AccentAmber = Color(0xFFFFB74D)     // Split theme color
private val AccentTeal = Color(0xFF4DB6AC)      // Preview
private val AccentGreen = Color(0xFF81C784)     // Success
private val AccentRed = Color(0xFFEF5350)       // PDF icon
private val AccentBlue = Color(0xFF64B5F6)      // Info/General

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: SplitViewModel = koinViewModel()
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
                        Text("Split PDF")
                        state.sourceFile?.let { file ->
                            Text(
                                "${file.pageCount} pages • ${state.splitMode.toDisplayName()}",
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
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
                        onViewFile = { filePath ->
                            navController.navigateToReader(filePath)
                        },
                        onShareFile = { filePath ->
                            val file = File(filePath)
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
                                    onPreview = {
                                        state.sourceFile?.path?.let { path ->
                                            navController.navigateToReader(path)
                                        }
                                    },
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
                .background(AccentAmber.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.CallSplit,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentAmber
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
    onPreview: () -> Unit,
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

            // Preview button
            IconButton(
                onClick = onPreview,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Visibility,
                        contentDescription = "Preview",
                        modifier = Modifier.size(18.dp),
                        tint = AccentTeal
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

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

        // Mode selection with descriptions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First row: Multiple PDF outputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SplitModeChip(
                    label = "By Ranges",
                    description = "Multiple PDFs",
                    selected = selectedMode == SplitMode.BY_RANGES,
                    onClick = { onModeSelected(SplitMode.BY_RANGES) },
                    modifier = Modifier.weight(1f)
                )
                SplitModeChip(
                    label = "Every N Pages",
                    description = "Auto split",
                    selected = selectedMode == SplitMode.EVERY_N_PAGES,
                    onClick = { onModeSelected(SplitMode.EVERY_N_PAGES) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Second row: Single page / extraction modes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SplitModeChip(
                    label = "Single Pages",
                    description = "1 page = 1 PDF",
                    selected = selectedMode == SplitMode.INTO_PAGES,
                    onClick = { onModeSelected(SplitMode.INTO_PAGES) },
                    modifier = Modifier.weight(1f)
                )
                SplitModeChip(
                    label = "Extract Pages",
                    description = "Single PDF",
                    selected = selectedMode == SplitMode.SPECIFIC_PAGES,
                    onClick = { onModeSelected(SplitMode.SPECIFIC_PAGES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SplitModeChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) AccentAmber else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) AccentAmber.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier.height(56.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentAmber.copy(alpha = 0.15f),
            selectedLabelColor = AccentAmber,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                val rangeCount = countRanges(state.rangesInput)
                val hasError = state.rangesError != null
                Column {
                    OutlinedTextField(
                        value = state.rangesInput,
                        onValueChange = onRangesChange,
                        label = { Text("Page Ranges") },
                        placeholder = { Text("e.g., 1-5, 6-10, 11-15") },
                        supportingText = {
                            state.rangesError?.let { error ->
                                Text(error, color = AccentRed)
                            } ?: Text("Each range creates a separate PDF. Pages: 1-${state.sourceFile?.pageCount ?: 0}")
                        },
                        isError = hasError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (rangeCount > 0 && !hasError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AccentAmber.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.CallSplit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AccentAmber
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Will create $rangeCount PDF file${if (rangeCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            SplitMode.EVERY_N_PAGES -> {
                val maxPages = state.sourceFile?.pageCount ?: 1
                val sliderMax = maxPages.coerceAtLeast(1).toFloat()
                Column {
                    Text(
                        "Split every ${state.everyNPages} page${if (state.everyNPages > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = state.everyNPages.toFloat().coerceIn(1f, sliderMax),
                        onValueChange = { onEveryNChange(it.toInt()) },
                        valueRange = 1f..sliderMax,
                        steps = (sliderMax.toInt() - 2).coerceAtLeast(0),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Will create ${calculateParts(maxPages, state.everyNPages)} file${if (calculateParts(maxPages, state.everyNPages) > 1) "s" else ""} (max: $maxPages pages)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SplitMode.INTO_PAGES -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentAmber.copy(alpha = 0.1f)
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
                            tint = AccentAmber
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
                val pageCount = countExtractedPages(state.specificPagesInput, state.sourceFile?.pageCount ?: 0)
                val hasError = state.specificPagesError != null
                Column {
                    OutlinedTextField(
                        value = state.specificPagesInput,
                        onValueChange = onSpecificPagesChange,
                        label = { Text("Pages to Extract") },
                        placeholder = { Text("e.g., 1, 3, 5-8, 12") },
                        supportingText = {
                            state.specificPagesError?.let { error ->
                                Text(error, color = AccentRed)
                            } ?: Text("Creates a single PDF with selected pages. Pages: 1-${state.sourceFile?.pageCount ?: 0}")
                        },
                        isError = hasError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    if (pageCount > 0 && !hasError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AccentBlue.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AccentBlue
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Will create 1 PDF with $pageCount page${if (pageCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
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
                    color = AccentAmber
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
                    color = AccentAmber
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
    onViewFile: (String) -> Unit,
    onShareFile: (String) -> Unit,
    onSplitMore: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Header section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Split Complete!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                "${result.createdFiles.size} files created",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Files list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(result.createdFiles) { filePath ->
                SplitResultFileItem(
                    filePath = filePath,
                    onView = { onViewFile(filePath) },
                    onShare = { onShareFile(filePath) }
                )
            }
        }

        // Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSplitMore,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Split Another PDF")
            }

            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun SplitResultFileItem(
    filePath: String,
    onView: () -> Unit,
    onShare: () -> Unit
) {
    val file = File(filePath)

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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AccentRed
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                    formatFileSize(file.length()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // View button
            IconButton(
                onClick = onView,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Visibility,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp),
                        tint = AccentTeal
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Share button
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp),
                        tint = AccentBlue
                    )
                }
            }
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

private fun countRanges(rangesInput: String): Int {
    if (rangesInput.isBlank()) return 0
    return rangesInput
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .count { range ->
            // Validate range format: "N" or "N-M"
            val parts = range.split("-")
            when (parts.size) {
                1 -> parts[0].toIntOrNull() != null
                2 -> parts[0].toIntOrNull() != null && parts[1].toIntOrNull() != null
                else -> false
            }
        }
}

private fun SplitMode.toDisplayName(): String = when (this) {
    SplitMode.BY_RANGES -> "By Ranges"
    SplitMode.EVERY_N_PAGES -> "Every N Pages"
    SplitMode.INTO_PAGES -> "Single Pages"
    SplitMode.SPECIFIC_PAGES -> "Extract"
}

private fun countExtractedPages(input: String, maxPages: Int): Int {
    if (input.isBlank() || maxPages == 0) return 0
    val pages = mutableSetOf<Int>()

    input.split(",").forEach { part ->
        val trimmed = part.trim()
        if (trimmed.contains("-")) {
            // Range: "5-8"
            val parts = trimmed.split("-").map { it.trim().toIntOrNull() }
            if (parts.size == 2) {
                val start = parts[0]
                val end = parts[1]
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

    return pages.size
}
