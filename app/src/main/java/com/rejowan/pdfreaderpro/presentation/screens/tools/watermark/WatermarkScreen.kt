package com.rejowan.pdfreaderpro.presentation.screens.tools.watermark

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import coil.compose.AsyncImage
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors
private val AccentCyan = Color(0xFF00BCD4) // Watermark theme color
private val AccentGreen = Color(0xFF81C784)
private val AccentBlue = Color(0xFF64B5F6)

// Preset colors for text watermark
private val PresetColors = listOf(
    Color(0xFF808080), // Gray
    Color(0xFFEF5350), // Red
    Color(0xFF42A5F5), // Blue
    Color(0xFF66BB6A), // Green
    Color(0xFFFFCA28), // Amber
    Color(0xFFAB47BC), // Purple
    Color(0xFF26A69A), // Teal
    Color(0xFF000000), // Black
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: WatermarkViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.setSourceFile(it) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.setWatermarkImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Watermark")
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
                        onWatermarkMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentCyan)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading PDF...")
                        }
                    }
                }
                else -> {
                    WatermarkContent(
                        state = state,
                        onWatermarkTypeChange = { viewModel.setWatermarkType(it) },
                        onTextChange = { viewModel.setWatermarkText(it) },
                        onFontSizeChange = { viewModel.setFontSize(it) },
                        onTextColorChange = { viewModel.setTextColor(it) },
                        onTextOpacityChange = { viewModel.setTextOpacity(it) },
                        onTextRotationChange = { viewModel.setTextRotation(it) },
                        onSelectImage = { imagePickerLauncher.launch(arrayOf("image/*")) },
                        onImageScaleChange = { viewModel.setImageScale(it) },
                        onImageOpacityChange = { viewModel.setImageOpacity(it) },
                        onPositionChange = { viewModel.setPosition(it) },
                        onPageSelectionChange = { viewModel.setPageSelection(it) },
                        onCustomPagesChange = { viewModel.setCustomPages(it) },
                        onOutputFileNameChange = { viewModel.setOutputFileName(it) },
                        onApply = { viewModel.applyWatermark() },
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
                .background(AccentCyan.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.WaterDrop,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentCyan
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Add Watermark",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Add text or image watermark to your PDF pages",
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
private fun WatermarkContent(
    state: WatermarkState,
    onWatermarkTypeChange: (WatermarkType) -> Unit,
    onTextChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onTextOpacityChange: (Float) -> Unit,
    onTextRotationChange: (Float) -> Unit,
    onSelectImage: () -> Unit,
    onImageScaleChange: (Float) -> Unit,
    onImageOpacityChange: (Float) -> Unit,
    onPositionChange: (WatermarkPosition) -> Unit,
    onPageSelectionChange: (PageSelection) -> Unit,
    onCustomPagesChange: (String) -> Unit,
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
        // Watermark Type Tabs
        WatermarkTypeTabs(
            selectedType = state.watermarkType,
            onTypeChange = onWatermarkTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type-specific settings
        when (state.watermarkType) {
            WatermarkType.TEXT -> {
                TextWatermarkSettings(
                    text = state.watermarkText,
                    fontSize = state.fontSize,
                    textColor = state.textColor,
                    opacity = state.textOpacity,
                    rotation = state.textRotation,
                    onTextChange = onTextChange,
                    onFontSizeChange = onFontSizeChange,
                    onColorChange = onTextColorChange,
                    onOpacityChange = onTextOpacityChange,
                    onRotationChange = onTextRotationChange
                )
            }
            WatermarkType.IMAGE -> {
                ImageWatermarkSettings(
                    imagePath = state.imagePath,
                    imageUri = state.imageUri,
                    scale = state.imageScale,
                    opacity = state.imageOpacity,
                    onSelectImage = onSelectImage,
                    onScaleChange = onImageScaleChange,
                    onOpacityChange = onImageOpacityChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Position selection
        PositionSelector(
            selectedPosition = state.position,
            onPositionChange = onPositionChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Page selection
        PageSelector(
            selectedSelection = state.pageSelection,
            customPages = state.customPages,
            totalPages = state.sourceFile?.pageCount ?: 0,
            onSelectionChange = onPageSelectionChange,
            onCustomPagesChange = onCustomPagesChange
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            Icon(Icons.Default.WaterDrop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Watermark")
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun WatermarkTypeTabs(
    selectedType: WatermarkType,
    onTypeChange: (WatermarkType) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        TabRow(
            selectedTabIndex = if (selectedType == WatermarkType.TEXT) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = AccentCyan,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[if (selectedType == WatermarkType.TEXT) 0 else 1])
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 24.dp)
                        .background(AccentCyan, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                )
            }
        ) {
            Tab(
                selected = selectedType == WatermarkType.TEXT,
                onClick = { onTypeChange(WatermarkType.TEXT) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Text")
                    }
                },
                selectedContentColor = AccentCyan,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Tab(
                selected = selectedType == WatermarkType.IMAGE,
                onClick = { onTypeChange(WatermarkType.IMAGE) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Image")
                    }
                },
                selectedContentColor = AccentCyan,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextWatermarkSettings(
    text: String,
    fontSize: Float,
    textColor: Color,
    opacity: Float,
    rotation: Float,
    onTextChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit
) {
    Column {
        // Text input
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Watermark text") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Font size slider
        SettingSlider(
            label = "Font Size",
            value = fontSize,
            valueRange = 12f..200f,
            valueLabel = "${fontSize.toInt()}pt",
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
                    isSelected = color == textColor,
                    onClick = { onColorChange(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opacity slider
        SettingSlider(
            label = "Opacity",
            value = opacity,
            valueRange = 1f..100f,
            valueLabel = "${opacity.toInt()}%",
            onValueChange = onOpacityChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rotation slider
        SettingSlider(
            label = "Rotation",
            value = rotation,
            valueRange = -180f..180f,
            valueLabel = "${rotation.toInt()}°",
            onValueChange = onRotationChange
        )
    }
}

@Composable
private fun ImageWatermarkSettings(
    imagePath: String?,
    imageUri: android.net.Uri?,
    scale: Float,
    opacity: Float,
    onSelectImage: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit
) {
    Column {
        // Image picker
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .clickable(onClick = onSelectImage),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Watermark image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = AccentCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap to select image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scale slider
        SettingSlider(
            label = "Scale",
            value = scale,
            valueRange = 1f..100f,
            valueLabel = "${scale.toInt()}%",
            onValueChange = onScaleChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Opacity slider
        SettingSlider(
            label = "Opacity",
            value = opacity,
            valueRange = 1f..100f,
            valueLabel = "${opacity.toInt()}%",
            onValueChange = onOpacityChange
        )
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
                color = AccentCyan
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan
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
                    Modifier.border(3.dp, AccentCyan, CircleShape)
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
                tint = if (color == Color.Black || color == Color(0xFF808080)) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun PositionSelector(
    selectedPosition: WatermarkPosition,
    onPositionChange: (WatermarkPosition) -> Unit
) {
    Column {
        Text(
            "POSITION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // First row: corners and center
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WatermarkPosition.entries.forEach { position ->
                FilterChip(
                    selected = selectedPosition == position,
                    onClick = { onPositionChange(position) },
                    label = { Text(position.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AccentCyan
                    )
                )
            }
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
        Text(
            "APPLY TO PAGES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSelection == PageSelection.ALL,
                onClick = { onSelectionChange(PageSelection.ALL) },
                label = { Text("All ($totalPages)", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AccentCyan
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.ODD,
                onClick = { onSelectionChange(PageSelection.ODD) },
                label = { Text("Odd", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AccentCyan
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.EVEN,
                onClick = { onSelectionChange(PageSelection.EVEN) },
                label = { Text("Even", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AccentCyan
                )
            )
            FilterChip(
                selected = selectedSelection == PageSelection.CUSTOM,
                onClick = { onSelectionChange(PageSelection.CUSTOM) },
                label = { Text("Custom", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AccentCyan
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
    result: WatermarkResult,
    onOpenInApp: () -> Unit,
    onShare: () -> Unit,
    onWatermarkMore: () -> Unit,
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
            "Watermark Added!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Applied to ${result.pageCount} pages",
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
                onClick = onWatermarkMore,
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
                    color = AccentCyan,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Adding Watermark...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentCyan
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
