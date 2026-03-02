package com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
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
    Color(0xFF26A69A), // Teal
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
                        Text("Add Page Numbers")
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
                        onOpenInApp = { navController.navigateToReader(state.result!!.outputPath) },
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
                            Text("Loading PDF...")
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
                Icons.Default.FormatListNumbered,
                contentDescription = null,
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
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDF")
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
    onApply: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Position selector
        SectionLabel("POSITION")
        Spacer(modifier = Modifier.height(8.dp))
        PositionSelector(
            selectedPosition = state.position,
            onPositionChange = onPositionChange
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                        label = { Text("Prefix") },
                        placeholder = { Text("e.g., Page ") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.customSuffix,
                        onValueChange = onSuffixChange,
                        label = { Text("Suffix") },
                        placeholder = { Text("e.g., .") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                label = { Text("Start Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFEF5350)
                        )
                    }
                }
            }
        }

        // Output filename
        OutlinedTextField(
            value = state.outputFileName,
            onValueChange = onOutputFileNameChange,
            label = { Text("Output file name") },
            suffix = { Text(".pdf") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply button
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing
        ) {
            Icon(Icons.Default.FormatListNumbered, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Page Numbers")
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
private fun PositionSelector(
    selectedPosition: NumberPosition,
    onPositionChange: (NumberPosition) -> Unit
) {
    // Visual position grid
    Card(
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
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PositionButton(
                    label = "TL",
                    isSelected = selectedPosition == NumberPosition.TOP_LEFT,
                    onClick = { onPositionChange(NumberPosition.TOP_LEFT) }
                )
                PositionButton(
                    label = "TC",
                    isSelected = selectedPosition == NumberPosition.TOP_CENTER,
                    onClick = { onPositionChange(NumberPosition.TOP_CENTER) }
                )
                PositionButton(
                    label = "TR",
                    isSelected = selectedPosition == NumberPosition.TOP_RIGHT,
                    onClick = { onPositionChange(NumberPosition.TOP_RIGHT) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Page representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Page Content",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PositionButton(
                    label = "BL",
                    isSelected = selectedPosition == NumberPosition.BOTTOM_LEFT,
                    onClick = { onPositionChange(NumberPosition.BOTTOM_LEFT) }
                )
                PositionButton(
                    label = "BC",
                    isSelected = selectedPosition == NumberPosition.BOTTOM_CENTER,
                    onClick = { onPositionChange(NumberPosition.BOTTOM_CENTER) }
                )
                PositionButton(
                    label = "BR",
                    isSelected = selectedPosition == NumberPosition.BOTTOM_RIGHT,
                    onClick = { onPositionChange(NumberPosition.BOTTOM_RIGHT) }
                )
            }
        }
    }
}

@Composable
private fun PositionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) AccentOrange.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (color == Color.Black || color == Color(0xFF424242)) Color.White else Color.Black
            )
        }
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
                    label = { Text("Skip first N pages") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                contentDescription = null,
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
