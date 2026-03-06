package com.rejowan.pdfreaderpro.presentation.screens.tools.pdftoimage

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Image
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
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors
private val AccentPurple = Color(0xFF9575CD) // PDF to Image theme
private val AccentGreen = Color(0xFF81C784)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentRed = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToImageScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: PdfToImageViewModel = koinViewModel()
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
                        Text("PDF to Images")
                        state.sourceFile?.let { file ->
                            Text(
                                file.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                    EmptyState(
                        onSelectFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                state.result != null -> {
                    SuccessState(
                        result = state.result!!,
                        onShareAll = {
                            val files = state.result!!.imagePaths.map { File(it) }
                            val uris = files.map { file ->
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "image/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Images"))
                        },
                        onShareSingle = { path ->
                            val file = File(path)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
                        },
                        onViewSingle = { path ->
                            val file = File(path)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(viewIntent, "View Image"))
                        },
                        onExportMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentPurple)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading PDF...")
                        }
                    }
                }
                else -> {
                    ExportContent(
                        state = state,
                        onFormatChange = { viewModel.setImageFormat(it) },
                        onPageSelectionChange = { viewModel.setPageSelection(it) },
                        onCustomPagesChange = { viewModel.setCustomPages(it) },
                        onExport = { viewModel.exportImages() },
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
                .background(AccentPurple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Photo,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "PDF to Images",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Export PDF pages as image files",
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
private fun ExportContent(
    state: PdfToImageState,
    onFormatChange: (ImageFormat) -> Unit,
    onPageSelectionChange: (PageSelection) -> Unit,
    onCustomPagesChange: (String) -> Unit,
    onExport: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Source file preview
        state.sourceFile?.let { sourceFile ->
            SourceFileCard(sourceFile = sourceFile)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Image format selection
        SectionLabel("OUTPUT FORMAT")
        Spacer(modifier = Modifier.height(8.dp))
        FormatSelector(
            selectedFormat = state.imageFormat,
            onFormatChange = onFormatChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Page selection
        SectionLabel("PAGES TO EXPORT")
        Spacer(modifier = Modifier.height(8.dp))
        PageSelector(
            selectedSelection = state.pageSelection,
            customPages = state.customPages,
            totalPages = state.sourceFile?.pageCount ?: 0,
            onSelectionChange = onPageSelectionChange,
            onCustomPagesChange = onCustomPagesChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AccentPurple.copy(alpha = 0.1f)
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
                    Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AccentPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                val pageCount = when (state.pageSelection) {
                    PageSelection.ALL -> state.sourceFile?.pageCount ?: 0
                    PageSelection.CUSTOM -> {
                        val parsed = parseCustomPages(state.customPages, state.sourceFile?.pageCount ?: 0)
                        parsed.size
                    }
                }
                Text(
                    "Will create $pageCount ${state.imageFormat.extension.uppercase()} image${if (pageCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error message
        AnimatedVisibility(visible = state.error != null) {
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
                        state.error ?: "",
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

        // Export button
        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Photo, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Images")
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SourceFileCard(sourceFile: SourceFile) {
    Card(
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
            // Preview thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (sourceFile.previewBitmap != null) {
                    Image(
                        bitmap = sourceFile.previewBitmap.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = AccentPurple
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sourceFile.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${sourceFile.pageCount} pages - ${formatFileSize(sourceFile.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
        ),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FormatSelector(
    selectedFormat: ImageFormat,
    onFormatChange: (ImageFormat) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ImageFormat.entries.forEach { format ->
            FormatCard(
                format = format,
                isSelected = selectedFormat == format,
                onClick = { onFormatChange(format) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FormatCard(
    format: ImageFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, AccentPurple, RoundedCornerShape(12.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                AccentPurple.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                format.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) AccentPurple else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                when (format) {
                    ImageFormat.PNG -> "Lossless quality"
                    ImageFormat.JPG -> "Smaller file size"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PageSelector(
    selectedSelection: PageSelection,
    customPages: String,
    totalPages: Int,
    onSelectionChange: (PageSelection) -> Unit,
    onCustomPagesChange: (String) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSelection == PageSelection.ALL,
                onClick = { onSelectionChange(PageSelection.ALL) },
                label = { Text("All Pages ($totalPages)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPurple.copy(alpha = 0.2f),
                    selectedLabelColor = AccentPurple
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.CUSTOM,
                onClick = { onSelectionChange(PageSelection.CUSTOM) },
                label = { Text("Custom") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPurple.copy(alpha = 0.2f),
                    selectedLabelColor = AccentPurple
                )
            )
        }

        AnimatedVisibility(visible = selectedSelection == PageSelection.CUSTOM) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customPages,
                    onValueChange = onCustomPagesChange,
                    label = { Text("Page numbers") },
                    placeholder = { Text("e.g., 1-5, 8, 10-12") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SuccessState(
    result: PdfToImageResult,
    onShareAll: () -> Unit,
    onShareSingle: (String) -> Unit,
    onViewSingle: (String) -> Unit,
    onExportMore: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = AccentGreen
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Images Exported!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${result.imageCount} ${result.format} image${if (result.imageCount > 1) "s" else ""} created",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Output location info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AccentPurple.copy(alpha = 0.1f)
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
                    Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AccentPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    result.outputDir.replace("/storage/emulated/0/", ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentPurple,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Share all button
        if (result.imageCount > 1) {
            OutlinedButton(
                onClick = onShareAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share All Images")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Image list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(result.imagePaths) { index, path ->
                ImageResultItem(
                    path = path,
                    index = index + 1,
                    onView = { onViewSingle(path) },
                    onShare = { onShareSingle(path) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onExportMore,
                modifier = Modifier.weight(1f)
            ) {
                Text("New Export", maxLines = 1)
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
private fun ImageResultItem(
    path: String,
    index: Int,
    onView: () -> Unit,
    onShare: () -> Unit
) {
    val file = File(path)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AccentPurple.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$index",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentPurple,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatFileSize(file.length()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onView,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "View",
                    modifier = Modifier.size(18.dp),
                    tint = AccentGreen
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(18.dp),
                    tint = AccentBlue
                )
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
                    color = AccentPurple,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Exporting...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentPurple
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

private fun parseCustomPages(input: String, totalPages: Int): List<Int> {
    val pages = mutableSetOf<Int>()
    val parts = input.split(",")

    for (part in parts) {
        val trimmed = part.trim()
        if (trimmed.contains("-")) {
            val range = trimmed.split("-")
            if (range.size == 2) {
                val start = range[0].trim().toIntOrNull() ?: continue
                val end = range[1].trim().toIntOrNull() ?: continue
                for (i in start..end) {
                    if (i in 1..totalPages) pages.add(i)
                }
            }
        } else {
            val page = trimmed.toIntOrNull()
            if (page != null && page in 1..totalPages) {
                pages.add(page)
            }
        }
    }

    return pages.sorted()
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
