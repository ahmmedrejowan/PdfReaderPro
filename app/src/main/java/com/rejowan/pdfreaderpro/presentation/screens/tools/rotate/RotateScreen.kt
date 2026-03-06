package com.rejowan.pdfreaderpro.presentation.screens.tools.rotate

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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import java.io.File

// Accent colors
private val AccentOrange = Color(0xFFFF9800)  // Rotate theme color
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentGreen = Color(0xFF81C784)
private val AccentRed = Color(0xFFEF5350)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentPurple = Color(0xFF9575CD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotateScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: RotateViewModel = koinViewModel()
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
                        Text("Rotate Pages")
                        state.sourceFile?.let { file ->
                            val selectedCount = file.pages.count { it.isSelected }
                            val selectionText = when (state.selectionMode) {
                                PageSelectionMode.ALL_PAGES -> "All ${file.pageCount} pages"
                                PageSelectionMode.SELECTED_PAGES -> "$selectedCount of ${file.pageCount} selected"
                            }
                            Text(
                                selectionText,
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
                    EmptyState(
                        onSelectFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                state.result != null -> {
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
                        onRotateMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentOrange)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading pages...")
                        }
                    }
                }
                else -> {
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

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Scrollable content
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Rotation options (spans 2 columns)
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        alpha = item1Alpha
                                        scaleX = item1Scale
                                        scaleY = item1Scale
                                    }
                                ) {
                                    RotationOptionsSection(
                                        selectedAngle = state.rotationAngle,
                                        onAngleSelected = {
                                            focusManager.clearFocus()
                                            viewModel.setRotationAngle(it)
                                        }
                                    )
                                }
                            }

                            // Quick selection chips (spans 2 columns)
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        alpha = item2Alpha
                                        scaleX = item2Scale
                                        scaleY = item2Scale
                                    }
                                ) {
                                    QuickSelectionSection(
                                        onQuickSelect = {
                                            focusManager.clearFocus()
                                            viewModel.applyQuickSelection(it)
                                        },
                                        selectedCount = state.sourceFile?.pages?.count { it.isSelected } ?: 0,
                                        totalCount = state.sourceFile?.pageCount ?: 0,
                                        onClear = {
                                            focusManager.clearFocus()
                                            viewModel.deselectAllPages()
                                        }
                                    )
                                }
                            }

                            // Page thumbnails
                            items(state.sourceFile?.pages ?: emptyList()) { page ->
                                PageThumbnailItem(
                                    page = page,
                                    rotationAngle = state.rotationAngle,
                                    isAllSelected = state.selectionMode == PageSelectionMode.ALL_PAGES,
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.togglePageSelection(page.pageNumber)
                                    }
                                )
                            }
                        }

                        // Bottom section
                        RotateBottomSection(
                            outputFileName = state.outputFileName,
                            onFileNameChange = { viewModel.setOutputFileName(it) },
                            overwriteOriginal = state.overwriteOriginal,
                            onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                            isProcessing = state.isProcessing,
                            progress = state.progress,
                            canRotate = state.sourceFile != null,
                            error = state.error,
                            onRotate = { viewModel.rotate() },
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
                .background(AccentOrange.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Rotate PDF Pages",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Select a PDF file to rotate its pages by 90°, 180°, or 270°",
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
private fun RotationOptionsSection(
    selectedAngle: RotationAngle,
    onAngleSelected: (RotationAngle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "ROTATION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RotationAngle.entries.forEach { angle ->
                RotationChip(
                    angle = angle,
                    isSelected = angle == selectedAngle,
                    onClick = { onAngleSelected(angle) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RotationChip(
    angle: RotationAngle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, AccentOrange, RoundedCornerShape(10.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            AccentOrange.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(angle.degrees.toFloat() - 90f),
                tint = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                angle.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickSelectionSection(
    onQuickSelect: (QuickSelection) -> Unit,
    selectedCount: Int,
    totalCount: Int,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        // Header row with title, count, and clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "SELECT PAGES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                if (selectedCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "($selectedCount/$totalCount)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentOrange
                    )
                }
            }

            if (selectedCount > 0) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onClear),
                    shape = RoundedCornerShape(6.dp),
                    color = AccentRed.copy(alpha = 0.1f)
                ) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentRed,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick selection chips - single row with wrap
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickSelectionChip(
                label = "All",
                onClick = { onQuickSelect(QuickSelection.ALL) },
                modifier = Modifier.weight(1f)
            )
            QuickSelectionChip(
                label = "Odd",
                onClick = { onQuickSelect(QuickSelection.ODD) },
                modifier = Modifier.weight(1f)
            )
            QuickSelectionChip(
                label = "Even",
                onClick = { onQuickSelect(QuickSelection.EVEN) },
                modifier = Modifier.weight(1f)
            )
            QuickSelectionChip(
                label = "1st ½",
                onClick = { onQuickSelect(QuickSelection.FIRST_HALF) },
                modifier = Modifier.weight(1f)
            )
            QuickSelectionChip(
                label = "2nd ½",
                onClick = { onQuickSelect(QuickSelection.SECOND_HALF) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickSelectionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageThumbnailItem(
    page: PageInfo,
    rotationAngle: RotationAngle,
    isAllSelected: Boolean,
    onClick: () -> Unit
) {
    val isSelected = isAllSelected || page.isSelected

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, AccentOrange, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail with rotation preview
            if (page.thumbnail != null) {
                Image(
                    bitmap = page.thumbnail.asImageBitmap(),
                    contentDescription = "Page ${page.pageNumber}",
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(if (isSelected) rotationAngle.degrees.toFloat() else 0f),
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

            // Page number badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "${page.pageNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(AccentOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RotateBottomSection(
    outputFileName: String,
    onFileNameChange: (String) -> Unit,
    overwriteOriginal: Boolean,
    onOverwriteChange: (Boolean) -> Unit,
    isProcessing: Boolean,
    progress: Float,
    canRotate: Boolean,
    error: String?,
    onRotate: () -> Unit,
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
                colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
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

        // Rotate button
        Button(
            onClick = onRotate,
            modifier = Modifier.fillMaxWidth(),
            enabled = canRotate && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isProcessing) "Rotating..." else "Rotate Pages")
        }
    }
}

@Composable
private fun SuccessState(
    result: RotateResult,
    onOpenInApp: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onRotateMore: () -> Unit,
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
            "Rotation Complete!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "${result.rotatedPages} page${if (result.rotatedPages > 1) "s" else ""} rotated",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = AccentOrange
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
                onClick = onRotateMore,
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
                    color = AccentOrange,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Rotating...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentOrange
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
