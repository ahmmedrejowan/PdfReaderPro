package com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import androidx.compose.ui.res.stringResource
import com.rejowan.pdfreaderpro.R
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors
private val AccentOrange = Color(0xFFFF9800) // Page numbers theme color
private val AccentGreen = Color(0xFF81C784)
private val AccentBlue = Color(0xFF64B5F6)

// Preset colors for page numbers
private val PresetColors = listOf(
    Color(0xFF000000), // Black
    Color(0xFF424242), // Dark Gray
    Color(0xFF757575), // Gray
    Color(0xFFEF5350), // Red
    Color(0xFF42A5F5), // Blue
    Color(0xFF66BB6A), // Green
    Color(0xFFAB47BC), // Purple
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageNumbersScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: PageNumbersViewModel = koinViewModel()
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
                        Text(stringResource(R.string.tool_add_page_numbers))
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
                    EmptyState(
                        onSelectFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                state.result != null -> {
                    val result = requireNotNull(state.result)
                    SuccessState(
                        result = result,
                        onOpenInApp = { navController.navigateToReader(result.outputPath) },
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
                        onNumberMore = { viewModel.reset() },
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
                            Text(stringResource(R.string.loading_pdf))
                        }
                    }
                }
                else -> {
                    PageNumbersContent(
                        state = state,
                        onPositionChange = { viewModel.setPosition(it) },
                        onFormatChange = { viewModel.setFormat(it) },
                        onFontSizeChange = { viewModel.setFontSize(it) },
                        onColorChange = { viewModel.setTextColor(it) },
                        onStartNumberChange = { viewModel.setStartNumber(it) },
                        onPrefixChange = { viewModel.setCustomPrefix(it) },
                        onSuffixChange = { viewModel.setCustomSuffix(it) },
                        onMarginXChange = { viewModel.setMarginX(it) },
                        onMarginYChange = { viewModel.setMarginY(it) },
                        onPageSelectionChange = { viewModel.setPageSelection(it) },
                        onCustomPagesChange = { viewModel.setCustomPages(it) },
                        onSkipFirstNChange = { viewModel.setSkipFirstN(it) },
                        onOutputFileNameChange = { viewModel.setOutputFileName(it) },
                        onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                        onApply = { viewModel.applyPageNumbers() },
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
                .background(AccentOrange.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.FormatListNumbered,
                contentDescription = stringResource(R.string.cd_add_page_numbers),
                modifier = Modifier.size(40.dp),
                tint = AccentOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Add Page Numbers",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Insert page numbers to your PDF document",
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
            Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.cd_select_files))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.select_pdf))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageNumbersContent(
    state: PageNumbersState,
    onPositionChange: (NumberPosition) -> Unit,
    onFormatChange: (NumberFormat) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit,
    onStartNumberChange: (Int) -> Unit,
    onPrefixChange: (String) -> Unit,
    onSuffixChange: (String) -> Unit,
    onMarginXChange: (Float) -> Unit,
    onMarginYChange: (Float) -> Unit,
    onPageSelectionChange: (PageSelection) -> Unit,
    onCustomPagesChange: (String) -> Unit,
    onSkipFirstNChange: (Int) -> Unit,
    onOutputFileNameChange: (String) -> Unit,
    onOverwriteChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onClearError: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Preview card on top
        PageNumberPreview(
            format = state.format,
            fontSize = state.fontSize,
            textColor = state.textColor,
            startNumber = state.startNumber,
            customPrefix = state.customPrefix,
            customSuffix = state.customSuffix,
            position = state.position
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Position selector (compact)
        SectionLabel("POSITION")
        Spacer(modifier = Modifier.height(8.dp))
        CompactPositionSelector(
            selectedPosition = state.position,
            onPositionChange = onPositionChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Format selector
        SectionLabel("FORMAT")
        Spacer(modifier = Modifier.height(8.dp))
        FormatSelector(
            selectedFormat = state.format,
            onFormatChange = onFormatChange
        )

        // Custom prefix/suffix fields
        AnimatedVisibility(visible = state.format == NumberFormat.CUSTOM) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.customPrefix,
                        onValueChange = onPrefixChange,
                        label = { Text(stringResource(R.string.prefix_label)) },
                        placeholder = { Text(stringResource(R.string.prefix_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.customSuffix,
                        onValueChange = onSuffixChange,
                        label = { Text(stringResource(R.string.suffix_label)) },
                        placeholder = { Text(stringResource(R.string.suffix_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance settings
        SectionLabel("APPEARANCE")
        Spacer(modifier = Modifier.height(8.dp))

        // Font size
        SettingSlider(
            label = "Font Size",
            value = state.fontSize,
            valueRange = 8f..72f,
            valueLabel = "${state.fontSize.toInt()}pt",
            onValueChange = onFontSizeChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Color picker
        var showColorPicker by remember { mutableStateOf(false) }

        Text(
            "Color",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetColors.forEach { color ->
                ColorCircle(
                    color = color,
                    isSelected = color == state.textColor,
                    onClick = { onColorChange(color) }
                )
            }
            // Custom color picker button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                            colors = listOf(
                                Color.Red, Color.Yellow, Color.Green,
                                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                            )
                        )
                    )
                    .clickable { showColorPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }

        // Color picker sheet
        if (showColorPicker) {
            ColorPickerSheet(
                currentColor = state.textColor,
                onColorSelected = { color ->
                    onColorChange(color)
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.startNumber.toString(),
                onValueChange = { it.toIntOrNull()?.let(onStartNumberChange) },
                label = { Text(stringResource(R.string.start_number)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Margins
        SectionLabel("MARGINS")
        Spacer(modifier = Modifier.height(8.dp))

        SettingSlider(
            label = "Horizontal Margin",
            value = state.marginX,
            valueRange = 0f..200f,
            valueLabel = "${state.marginX.toInt()}pt",
            onValueChange = onMarginXChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingSlider(
            label = "Vertical Margin",
            value = state.marginY,
            valueRange = 0f..200f,
            valueLabel = "${state.marginY.toInt()}pt",
            onValueChange = onMarginYChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Page selection
        SectionLabel("APPLY TO PAGES")
        Spacer(modifier = Modifier.height(8.dp))
        PageSelector(
            selectedSelection = state.pageSelection,
            customPages = state.customPages,
            skipFirstN = state.skipFirstN,
            totalPages = state.sourceFile?.pageCount ?: 0,
            onSelectionChange = onPageSelectionChange,
            onCustomPagesChange = onCustomPagesChange,
            onSkipFirstNChange = onSkipFirstNChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Error message
        AnimatedVisibility(visible = state.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEF5350).copy(alpha = 0.1f)
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
                        color = Color(0xFFEF5350),
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
                            tint = Color(0xFFEF5350)
                        )
                    }
                }
            }
        }

        // Overwrite original checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOverwriteChange(!state.overwriteOriginal) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.overwriteOriginal,
                onCheckedChange = onOverwriteChange,
                colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Overwrite original file", style = MaterialTheme.typography.bodyMedium)
        }

        // Output filename
        AnimatedVisibility(visible = !state.overwriteOriginal) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.outputFileName,
                    onValueChange = onOutputFileNameChange,
                    label = { Text(stringResource(R.string.output_filename_label)) },
                    suffix = { Text(stringResource(R.string.pdf_extension)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apply button
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing
        ) {
            Icon(Icons.Default.FormatListNumbered, contentDescription = stringResource(R.string.cd_add_page_numbers))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.tool_add_page_numbers))
        }

        Spacer(modifier = Modifier.height(80.dp))
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
private fun CompactPositionSelector(
    selectedPosition: NumberPosition,
    onPositionChange: (NumberPosition) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumberPosition.entries.forEach { position ->
            FilterChip(
                selected = selectedPosition == position,
                onClick = { onPositionChange(position) },
                label = {
                    Text(
                        when (position) {
                            NumberPosition.TOP_LEFT -> "Top Left"
                            NumberPosition.TOP_CENTER -> "Top Center"
                            NumberPosition.TOP_RIGHT -> "Top Right"
                            NumberPosition.BOTTOM_LEFT -> "Bottom Left"
                            NumberPosition.BOTTOM_CENTER -> "Bottom Center"
                            NumberPosition.BOTTOM_RIGHT -> "Bottom Right"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                    selectedLabelColor = AccentOrange
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatSelector(
    selectedFormat: NumberFormat,
    onFormatChange: (NumberFormat) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "${selectedFormat.label} (${selectedFormat.example})",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NumberFormat.entries.forEach { format ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(format.label, fontWeight = FontWeight.Medium)
                            Text(
                                format.example,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onFormatChange(format)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                valueLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = AccentOrange
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AccentOrange,
                activeTrackColor = AccentOrange
            )
        )
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, AccentOrange, CircleShape)
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.cd_checkbox),
                modifier = Modifier.size(18.dp),
                tint = if (color == Color.Black || color == Color(0xFF424242)) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun PageNumberPreview(
    format: NumberFormat,
    fontSize: Float,
    textColor: Color,
    startNumber: Int,
    customPrefix: String,
    customSuffix: String,
    position: NumberPosition
) {
    val previewText = when (format) {
        NumberFormat.NUMBER_ONLY -> "$startNumber"
        NumberFormat.PAGE_X -> "Page $startNumber"
        NumberFormat.X_OF_Y -> "$startNumber of 10"
        NumberFormat.DASH_X_DASH -> "- $startNumber -"
        NumberFormat.CUSTOM -> "$customPrefix$startNumber$customSuffix"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview box simulating page with number at position
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                // Mock content lines
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (index == 3) 0.5f else 0.8f + (index % 2) * 0.1f)
                                .height(3.dp)
                                .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(1.dp))
                        )
                    }
                }

                // Page number at position
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = when (position) {
                        NumberPosition.TOP_LEFT -> Alignment.TopStart
                        NumberPosition.TOP_CENTER -> Alignment.TopCenter
                        NumberPosition.TOP_RIGHT -> Alignment.TopEnd
                        NumberPosition.BOTTOM_LEFT -> Alignment.BottomStart
                        NumberPosition.BOTTOM_CENTER -> Alignment.BottomCenter
                        NumberPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                    }
                ) {
                    Text(
                        text = previewText,
                        fontSize = (fontSize * 0.35f).coerceIn(8f, 18f).sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${format.label} • ${fontSize.toInt()}pt • ${position.name.replace("_", " ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerSheet(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val focusManager = LocalFocusManager.current

    var red by remember { mutableStateOf((currentColor.red * 255).toInt()) }
    var green by remember { mutableStateOf((currentColor.green * 255).toInt()) }
    var blue by remember { mutableStateOf((currentColor.blue * 255).toInt()) }
    var hexInput by remember { mutableStateOf(String.format("%02X%02X%02X", red, green, blue)) }

    var redText by remember { mutableStateOf(red.toString()) }
    var greenText by remember { mutableStateOf(green.toString()) }
    var blueText by remember { mutableStateOf(blue.toString()) }

    val isHexValid = hexInput.length == 6 && hexInput.all { it in '0'..'9' || it in 'A'..'F' }
    val isRedValid = redText.toIntOrNull()?.let { it in 0..255 } ?: false
    val isGreenValid = greenText.toIntOrNull()?.let { it in 0..255 } ?: false
    val isBlueValid = blueText.toIntOrNull()?.let { it in 0..255 } ?: false

    val selectedColor = Color(red, green, blue)

    fun updateHexFromRgb() {
        hexInput = String.format("%02X%02X%02X", red, green, blue)
    }

    fun updateRgbFromHex(hex: String) {
        if (hex.length == 6 && hex.all { it in '0'..'9' || it in 'A'..'F' }) {
            try {
                val colorInt = hex.toLong(16).toInt()
                red = (colorInt shr 16) and 0xFF
                green = (colorInt shr 8) and 0xFF
                blue = colorInt and 0xFF
                redText = red.toString()
                greenText = green.toString()
                blueText = blue.toString()
            } catch (_: Exception) { }
        }
    }

    @Composable
    fun ColorPickerContent(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentOrange.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FormatListNumbered,
                            contentDescription = stringResource(R.string.cd_add_page_numbers),
                            modifier = Modifier.size(20.dp),
                            tint = AccentOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Pick Color",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Page number color",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color preview with hex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedColor)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                    )
                    Column {
                        Text(
                            "#${String.format("%02X%02X%02X", red, green, blue)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange
                        )
                        Text(
                            "RGB($red, $green, $blue)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hex input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it in '0'..'9' || it.uppercaseChar() in 'A'..'F' }
                            .take(6).uppercase()
                        hexInput = filtered
                        updateRgbFromHex(filtered)
                    },
                    label = { Text(stringResource(R.string.hex_label)) },
                    prefix = { Text(stringResource(R.string.hex_prefix)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = hexInput.isNotEmpty() && !isHexValid,
                    supportingText = if (hexInput.isNotEmpty() && !isHexValid) {
                        { Text(stringResource(R.string.hex_help)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // RGB inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = redText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }.take(3)
                            redText = filtered
                            filtered.toIntOrNull()?.coerceIn(0, 255)?.let {
                                red = it
                                updateHexFromRgb()
                            }
                        },
                        label = { Text(stringResource(R.string.color_r)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isError = redText.isNotEmpty() && !isRedValid,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = greenText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }.take(3)
                            greenText = filtered
                            filtered.toIntOrNull()?.coerceIn(0, 255)?.let {
                                green = it
                                updateHexFromRgb()
                            }
                        },
                        label = { Text(stringResource(R.string.color_g)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isError = greenText.isNotEmpty() && !isGreenValid,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = blueText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }.take(3)
                            blueText = filtered
                            filtered.toIntOrNull()?.coerceIn(0, 255)?.let {
                                blue = it
                                updateHexFromRgb()
                            }
                        },
                        label = { Text(stringResource(R.string.color_b)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isError = blueText.isNotEmpty() && !isBlueValid,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RGB Sliders
                ColorSlider(label = "R", value = red.toFloat(), color = Color.Red, onValueChange = {
                    red = it.toInt(); redText = red.toString(); updateHexFromRgb()
                })
                Spacer(modifier = Modifier.height(4.dp))
                ColorSlider(label = "G", value = green.toFloat(), color = Color.Green, onValueChange = {
                    green = it.toInt(); greenText = green.toString(); updateHexFromRgb()
                })
                Spacer(modifier = Modifier.height(4.dp))
                ColorSlider(label = "B", value = blue.toFloat(), color = Color.Blue, onValueChange = {
                    blue = it.toInt(); blueText = blue.toString(); updateHexFromRgb()
                })

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(onClick = { onColorSelected(selectedColor) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.select_action))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (isLandscape) {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() }
                )
                Card(
                    modifier = Modifier.width(320.dp).fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ColorPickerContent(modifier = Modifier.fillMaxSize())
                }
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ColorPickerContent()
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(24.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
        Text(
            value.toInt().toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PageSelector(
    selectedSelection: PageSelection,
    customPages: String,
    skipFirstN: Int,
    totalPages: Int,
    onSelectionChange: (PageSelection) -> Unit,
    onCustomPagesChange: (String) -> Unit,
    onSkipFirstNChange: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSelection == PageSelection.ALL,
                onClick = { onSelectionChange(PageSelection.ALL) },
                label = { Text("All ($totalPages)", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                    selectedLabelColor = AccentOrange
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.ODD,
                onClick = { onSelectionChange(PageSelection.ODD) },
                label = { Text("Odd", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                    selectedLabelColor = AccentOrange
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.EVEN,
                onClick = { onSelectionChange(PageSelection.EVEN) },
                label = { Text("Even", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                    selectedLabelColor = AccentOrange
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.SKIP_FIRST,
                onClick = { onSelectionChange(PageSelection.SKIP_FIRST) },
                label = { Text("Skip First", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                    selectedLabelColor = AccentOrange
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.CUSTOM,
                onClick = { onSelectionChange(PageSelection.CUSTOM) },
                label = { Text("Custom", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                    selectedLabelColor = AccentOrange
                )
            )
        }

        AnimatedVisibility(visible = selectedSelection == PageSelection.SKIP_FIRST) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = skipFirstN.toString(),
                    onValueChange = { it.toIntOrNull()?.let(onSkipFirstNChange) },
                    label = { Text(stringResource(R.string.skip_first_pages)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(visible = selectedSelection == PageSelection.CUSTOM) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customPages,
                    onValueChange = onCustomPagesChange,
                    label = { Text(stringResource(R.string.page_numbers_label)) },
                    placeholder = { Text(stringResource(R.string.page_numbers_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SuccessState(
    result: PageNumbersResult,
    onOpenInApp: () -> Unit,
    onShare: () -> Unit,
    onNumberMore: () -> Unit,
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
                contentDescription = stringResource(R.string.cd_success),
                modifier = Modifier.size(48.dp),
                tint = AccentGreen
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Page Numbers Added!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Numbered ${result.numberedPages} of ${result.pageCount} pages",
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
                    contentDescription = stringResource(R.string.cd_pdf_file),
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
                        "${result.pageCount} pages - ${formatFileSize(result.fileSize)}",
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
                    contentDescription = stringResource(R.string.cd_open),
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
                    contentDescription = stringResource(R.string.cd_share),
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
                onClick = onNumberMore,
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
                    "Adding Numbers...",
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
