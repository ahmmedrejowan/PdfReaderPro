package com.rejowan.pdfreaderpro.presentation.screens.tools.compress

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import androidx.compose.ui.res.stringResource
import com.rejowan.pdfreaderpro.R
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors - consistent with app design system
private val AccentPurple = Color(0xFF9575CD)   // Compress theme color
private val AccentTeal = Color(0xFF4DB6AC)     // Preview/Change
private val AccentGreen = Color(0xFF81C784)    // Success
private val AccentRed = Color(0xFFEF5350)      // PDF icon
private val AccentBlue = Color(0xFF64B5F6)     // Info
private val AccentAmber = Color(0xFFFFB74D)    // Warning/Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: CompressViewModel = koinViewModel()
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
                        Text(stringResource(R.string.tool_compress_pdf))
                        state.sourceFile?.let { file ->
                            Text(
                                "${file.pageCount} pages • ${formatFileSize(file.size)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                    val result = requireNotNull(state.result)
                    SuccessState(
                        result = result,
                        onOpenInApp = { navController.navigateToReader(result.outputPath) },
                        onOpenWith = {
                            val file = File(result.outputPath)
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
                            val file = File(result.outputPath)
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
                        onCompressMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                else -> {
                    // Compress options
                    // Staggered entry animation states
                    var item1Visible by remember { mutableStateOf(false) }
                    var item2Visible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        item1Visible = true
                        delay(40)
                        item2Visible = true
                    }

                    val item1Alpha by animateFloatAsState(
                        targetValue = if (item1Visible) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "item1 alpha"
                    )
                    val item1Scale by animateFloatAsState(
                        targetValue = if (item1Visible) 1f else 0.95f,
                        animationSpec = tween(durationMillis = 300),
                        label = "item1 scale"
                    )
                    val item2Alpha by animateFloatAsState(
                        targetValue = if (item2Visible) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "item2 alpha"
                    )
                    val item2Scale by animateFloatAsState(
                        targetValue = if (item2Visible) 1f else 0.95f,
                        animationSpec = tween(durationMillis = 300),
                        label = "item2 scale"
                    )

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
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            alpha = item1Alpha
                                            scaleX = item1Scale
                                            scaleY = item1Scale
                                        }
                                ) {
                                    SourceFileCard(
                                        sourceFile = requireNotNull(state.sourceFile),
                                        onPreview = {
                                            state.sourceFile?.path?.let { path ->
                                                navController.navigateToReader(path)
                                            }
                                        },
                                        onChangeFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                                    )
                                }
                            }

                            // Compression level selection with size estimate
                            item {
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            alpha = item2Alpha
                                            scaleX = item2Scale
                                            scaleY = item2Scale
                                        }
                                ) {
                                    CompressionLevelSection(
                                        selectedLevel = state.compressionLevel,
                                        onLevelSelected = { viewModel.setCompressionLevel(it) },
                                        sourceFile = requireNotNull(state.sourceFile)
                                    )
                                }
                            }
                        }

                        // Bottom section
                        CompressBottomSection(
                            outputFileName = state.outputFileName,
                            onFileNameChange = { viewModel.setOutputFileName(it) },
                            overwriteOriginal = state.overwriteOriginal,
                            onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                            isProcessing = state.isProcessing,
                            progress = state.progress,
                            canCompress = state.sourceFile != null,
                            error = state.error,
                            onCompress = { viewModel.compress() },
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
                .background(AccentPurple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Compress,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Compress PDF File",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Select a PDF file to reduce its file size while maintaining quality",
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
            Text(stringResource(R.string.select_pdf))
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top section: Thumbnail + File info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentRed.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (sourceFile.thumbnail != null) {
                        Image(
                            bitmap = sourceFile.thumbnail.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = AccentRed
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // File info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sourceFile.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${sourceFile.pageCount} pages • ${formatFileSize(sourceFile.size)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom section: Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preview button
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Outlined.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AccentTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.preview))
                }

                // Change file button
                OutlinedButton(
                    onClick = onChangeFile,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.change_file))
                }
            }
        }
    }
}

@Composable
private fun CompressionLevelSection(
    selectedLevel: CompressionLevel,
    onLevelSelected: (CompressionLevel) -> Unit,
    sourceFile: SourceFile
) {
    Column {
        Text(
            "COMPRESSION LEVEL",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompressionLevel.entries.forEach { level ->
                CompressionLevelItem(
                    level = level,
                    isSelected = level == selectedLevel,
                    onClick = { onLevelSelected(level) }
                )
            }
        }

        // Size estimate pill
        Spacer(modifier = Modifier.height(20.dp))
        SizeEstimatePill(
            sourceFile = sourceFile,
            compressionLevel = selectedLevel
        )
    }
}

@Composable
private fun CompressionLevelItem(
    level: CompressionLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = when (level) {
        CompressionLevel.LOW -> AccentGreen
        CompressionLevel.MEDIUM -> AccentBlue
        CompressionLevel.HIGH -> AccentAmber
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, accentColor, RoundedCornerShape(12.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            accentColor.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Default.Compress,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    level.label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = accentColor
                )
            }
        }
    }
}

@Composable
private fun SizeEstimatePill(
    sourceFile: SourceFile,
    compressionLevel: CompressionLevel
) {
    val estimate = sourceFile.compressionEstimate
    val originalSize = sourceFile.size

    // Get estimated size based on compression level
    val estimatedSize = when {
        estimate != null -> when (compressionLevel) {
            CompressionLevel.LOW -> estimate.estimatedSizeLow
            CompressionLevel.MEDIUM -> estimate.estimatedSizeMedium
            CompressionLevel.HIGH -> estimate.estimatedSizeHigh
        }
        // Fallback if no analysis available
        else -> when (compressionLevel) {
            CompressionLevel.LOW -> (originalSize * 0.90f).toLong()
            CompressionLevel.MEDIUM -> (originalSize * 0.70f).toLong()
            CompressionLevel.HIGH -> (originalSize * 0.50f).toLong()
        }
    }

    val savedSize = originalSize - estimatedSize
    val reductionPercent = if (originalSize > 0) {
        ((savedSize.toFloat() / originalSize) * 100).toInt()
    } else 0

    // Determine color based on potential
    val (pillColor, contentInfo) = when {
        estimate?.isAlreadyOptimized == true -> AccentAmber to "Already optimized"
        reductionPercent >= 30 -> AccentGreen to null
        reductionPercent >= 15 -> AccentBlue to null
        else -> AccentAmber to "Limited potential"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = pillColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main estimate row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Compress,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = pillColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "~${formatFileSize(estimatedSize)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = pillColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "(save ${formatFileSize(savedSize)} • $reductionPercent%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Content info if applicable
            if (contentInfo != null || estimate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val infoText = contentInfo ?: when {
                    estimate?.hasImages == true -> "Contains images • Good compression potential"
                    else -> "Mostly text content"
                }
                Text(
                    infoText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun CompressBottomSection(
    outputFileName: String,
    onFileNameChange: (String) -> Unit,
    overwriteOriginal: Boolean,
    onOverwriteChange: (Boolean) -> Unit,
    isProcessing: Boolean,
    progress: Float,
    canCompress: Boolean,
    error: String?,
    onCompress: () -> Unit,
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
                            contentDescription = stringResource(R.string.cd_dismiss),
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
                colors = CheckboxDefaults.colors(checkedColor = AccentTeal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Overwrite original file",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Output filename (only show when not overwriting)
        AnimatedVisibility(visible = !overwriteOriginal) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = outputFileName,
                    onValueChange = onFileNameChange,
                    label = { Text(stringResource(R.string.output_filename_label)) },
                    suffix = { Text(stringResource(R.string.pdf_extension)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compress button
        Button(
            onClick = onCompress,
            modifier = Modifier.fillMaxWidth(),
            enabled = canCompress && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.Compress, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isProcessing) "Compressing..." else "Compress PDF")
        }
    }
}

@Composable
private fun SuccessState(
    result: CompressResult,
    onOpenInApp: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onCompressMore: () -> Unit,
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
            "Compression Complete!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Size comparison card
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
                // Original size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Original Size",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatFileSize(result.originalSize),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compressed size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Compressed Size",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatFileSize(result.compressedSize),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AccentGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reduction percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Size Reduced",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "%.1f%%".format(result.reductionPercentage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (result.reductionPercentage > 0) AccentGreen else AccentAmber
                        )
                        if (result.savedBytes > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "(${formatFileSize(result.savedBytes)} saved)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // File info
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
                    tint = AccentPurple
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
                        "${result.pageCount} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons - Open In App & Open With
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
                Text(stringResource(R.string.open_in_app))
            }
            OutlinedButton(
                onClick = onOpenWith,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.open_with))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Share button
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.share))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCompressMore,
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
                    color = AccentPurple,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Compressing...",
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

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
