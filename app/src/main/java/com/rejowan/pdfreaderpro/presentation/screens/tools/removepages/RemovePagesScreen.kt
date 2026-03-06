package com.rejowan.pdfreaderpro.presentation.screens.tools.removepages

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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors
private val AccentRed = Color(0xFFEF5350)  // Remove pages theme color
private val AccentGreen = Color(0xFF81C784)
private val AccentBlue = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePagesScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: RemovePagesViewModel = koinViewModel()
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
                        Text("Remove Pages")
                        state.sourceFile?.let { file ->
                            val selectedCount = file.pages.count { it.isSelected }
                            if (selectedCount > 0) {
                                Text(
                                    "$selectedCount of ${file.pageCount} selected for removal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentRed
                                )
                            } else {
                                Text(
                                    "${file.pageCount} pages",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                        onRemoveMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentRed)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading pages...")
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Scrollable content
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Selection header
                            item(span = { GridItemSpan(2) }) {
                                SelectionHeader(
                                    selectedCount = state.sourceFile?.pages?.count { it.isSelected } ?: 0,
                                    totalCount = state.sourceFile?.pageCount ?: 0,
                                    onSelectAll = { viewModel.selectAllPages() },
                                    onClear = { viewModel.deselectAllPages() },
                                    onSelectOdd = { viewModel.selectOddPages() },
                                    onSelectEven = { viewModel.selectEvenPages() },
                                    onSelectRange = { start, end -> viewModel.selectRange(start, end) },
                                    onSelectBefore = { page -> viewModel.selectBeforePage(page) },
                                    onSelectAfter = { page -> viewModel.selectAfterPage(page) },
                                    onSelectFirstN = { n -> viewModel.selectFirstN(n) },
                                    onSelectLastN = { n -> viewModel.selectLastN(n) }
                                )
                            }

                            // Page thumbnails
                            items(state.sourceFile?.pages ?: emptyList()) { page ->
                                PageThumbnailItem(
                                    page = page,
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.togglePageSelection(page.pageNumber)
                                    }
                                )
                            }
                        }

                        // Bottom section
                        RemoveBottomSection(
                            outputFileName = state.outputFileName,
                            onFileNameChange = { viewModel.setOutputFileName(it) },
                            overwriteOriginal = state.overwriteOriginal,
                            onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                            isProcessing = state.isProcessing,
                            progress = state.progress,
                            selectedCount = state.sourceFile?.pages?.count { it.isSelected } ?: 0,
                            totalCount = state.sourceFile?.pageCount ?: 0,
                            error = state.error,
                            onRemove = { viewModel.removePages() },
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
                .background(AccentRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DeleteSweep,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentRed
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Remove Pages",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Select and delete unwanted pages from your PDF",
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
private fun SelectionHeader(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onSelectOdd: () -> Unit,
    onSelectEven: () -> Unit,
    onSelectRange: (Int, Int) -> Unit,
    onSelectBefore: (Int) -> Unit,
    onSelectAfter: (Int) -> Unit,
    onSelectFirstN: (Int) -> Unit,
    onSelectLastN: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRangeDialog by remember { mutableStateOf(false) }
    var showBeforeAfterDialog by remember { mutableStateOf(false) }
    var showFirstLastDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "TAP TO SELECT",
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
                        color = AccentRed
                    )
                }
            }

            if (selectedCount > 0) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onClear),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Selected pages will be removed from the PDF",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick selection chips - Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickSelectChip(
                label = "All",
                onClick = onSelectAll,
                modifier = Modifier.weight(1f)
            )
            QuickSelectChip(
                label = "Odd",
                onClick = onSelectOdd,
                modifier = Modifier.weight(1f)
            )
            QuickSelectChip(
                label = "Even",
                onClick = onSelectEven,
                modifier = Modifier.weight(1f)
            )
            QuickSelectChip(
                label = "Range",
                onClick = { showRangeDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick selection chips - Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickSelectChip(
                label = "First N",
                onClick = {
                    dialogMode = "first"
                    showFirstLastDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickSelectChip(
                label = "Last N",
                onClick = {
                    dialogMode = "last"
                    showFirstLastDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickSelectChip(
                label = "Before",
                onClick = {
                    dialogMode = "before"
                    showBeforeAfterDialog = true
                },
                modifier = Modifier.weight(1f)
            )
            QuickSelectChip(
                label = "After",
                onClick = {
                    dialogMode = "after"
                    showBeforeAfterDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Range Dialog
    if (showRangeDialog) {
        RangeInputDialog(
            totalPages = totalCount,
            onDismiss = { showRangeDialog = false },
            onConfirm = { start, end ->
                onSelectRange(start, end)
                showRangeDialog = false
            }
        )
    }

    // Before/After Dialog
    if (showBeforeAfterDialog) {
        PageInputDialog(
            title = if (dialogMode == "before") "Select Before Page" else "Select After Page",
            hint = if (dialogMode == "before") "Pages before this will be selected" else "Pages after this will be selected",
            totalPages = totalCount,
            onDismiss = { showBeforeAfterDialog = false },
            onConfirm = { page ->
                if (dialogMode == "before") onSelectBefore(page) else onSelectAfter(page)
                showBeforeAfterDialog = false
            }
        )
    }

    // First/Last N Dialog
    if (showFirstLastDialog) {
        PageInputDialog(
            title = if (dialogMode == "first") "Select First N Pages" else "Select Last N Pages",
            hint = if (dialogMode == "first") "First N pages will be selected" else "Last N pages will be selected",
            totalPages = totalCount,
            label = "Number of pages",
            onDismiss = { showFirstLastDialog = false },
            onConfirm = { n ->
                if (dialogMode == "first") onSelectFirstN(n) else onSelectLastN(n)
                showFirstLastDialog = false
            }
        )
    }
}

@Composable
private fun QuickSelectChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = AccentRed.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = AccentRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RangeInputDialog(
    totalPages: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var startPage by remember { mutableStateOf("1") }
    var endPage by remember { mutableStateOf(totalPages.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Page Range") },
        text = {
            Column {
                Text(
                    "Select pages from start to end (inclusive)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = startPage,
                        onValueChange = { startPage = it.filter { c -> c.isDigit() } },
                        label = { Text("From") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Text("to", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = endPage,
                        onValueChange = { endPage = it.filter { c -> c.isDigit() } },
                        label = { Text("To") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = AccentRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val start = startPage.toIntOrNull() ?: 0
                val end = endPage.toIntOrNull() ?: 0
                when {
                    start < 1 || start > totalPages -> error = "Start page must be 1-$totalPages"
                    end < 1 || end > totalPages -> error = "End page must be 1-$totalPages"
                    start > end -> error = "Start must be less than end"
                    else -> onConfirm(start, end)
                }
            }) {
                Text("Select")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PageInputDialog(
    title: String,
    hint: String,
    totalPages: Int,
    label: String = "Page number",
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var pageInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it.filter { c -> c.isDigit() } },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = AccentRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val page = pageInput.toIntOrNull() ?: 0
                when {
                    page < 1 || page > totalPages -> error = "Value must be 1-$totalPages"
                    else -> onConfirm(page)
                }
            }) {
                Text("Select")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PageThumbnailItem(
    page: PageInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (page.isSelected) {
                    Modifier.border(2.dp, AccentRed, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (page.isSelected) {
                AccentRed.copy(alpha = 0.05f)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (page.isSelected) {
                                Modifier.background(AccentRed.copy(alpha = 0.1f))
                            } else Modifier
                        ),
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

            // Selection indicator (delete icon)
            if (page.isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(AccentRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoveBottomSection(
    outputFileName: String,
    onFileNameChange: (String) -> Unit,
    overwriteOriginal: Boolean,
    onOverwriteChange: (Boolean) -> Unit,
    isProcessing: Boolean,
    progress: Float,
    selectedCount: Int,
    totalCount: Int,
    error: String?,
    onRemove: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Warning if trying to remove all
        if (selectedCount > 0 && selectedCount >= totalCount) {
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
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AccentRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cannot remove all pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentRed
                    )
                }
            }
        }

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
                colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
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

        // Remove button
        Button(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedCount > 0 && selectedCount < totalCount && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                if (isProcessing) "Removing..." else "Remove $selectedCount Page${if (selectedCount != 1) "s" else ""}"
            )
        }
    }
}

@Composable
private fun SuccessState(
    result: RemovePagesResult,
    onOpenInApp: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onRemoveMore: () -> Unit,
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
            "Pages Removed!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "${result.removedPages} page${if (result.removedPages > 1) "s" else ""} removed",
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
                    tint = AccentBlue
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
                        "${result.newPageCount} pages • ${formatFileSize(result.fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Page count change
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AccentRed.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${result.originalPageCount} pages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "→",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentRed
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "${result.newPageCount} pages",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AccentRed
                )
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
                onClick = onRemoveMore,
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
                    color = AccentRed,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Removing...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentRed
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
