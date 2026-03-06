package com.rejowan.pdfreaderpro.presentation.screens.tools.watermark

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                        onApply = { viewModel.applyWatermark() },
                        onReset = { viewModel.reset() },
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
    onOverwriteChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top section: Preview on left, buttons on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact preview
            CompactWatermarkPreview(
                watermarkType = state.watermarkType,
                text = state.watermarkText,
                fontSize = state.fontSize,
                textColor = state.textColor,
                textOpacity = state.textOpacity,
                textRotation = state.textRotation,
                imageUri = state.imageUri,
                imageScale = state.imageScale,
                imageOpacity = state.imageOpacity,
                position = state.position,
                modifier = Modifier.weight(2f)
            )

            // Action buttons
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isProcessing
                ) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply")
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable customization section
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
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
                    colors = CheckboxDefaults.colors(checkedColor = AccentCyan)
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
                        label = { Text("Output file name") },
                        suffix = { Text(".pdf") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WatermarkTypeTabs(
    selectedType: WatermarkType,
    onTypeChange: (WatermarkType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Text option
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (selectedType == WatermarkType.TEXT)
                        AccentCyan.copy(alpha = 0.15f)
                    else Color.Transparent
                )
                .clickable { onTypeChange(WatermarkType.TEXT) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TextFields,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (selectedType == WatermarkType.TEXT)
                        AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Text",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedType == WatermarkType.TEXT)
                        AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Image option
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (selectedType == WatermarkType.IMAGE)
                        AccentCyan.copy(alpha = 0.15f)
                    else Color.Transparent
                )
                .clickable { onTypeChange(WatermarkType.IMAGE) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (selectedType == WatermarkType.IMAGE)
                        AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Image",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedType == WatermarkType.IMAGE)
                        AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    isSelected = color == textColor,
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
                currentColor = textColor,
                onColorSelected = { color ->
                    onColorChange(color)
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
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

    // Text field values (to allow intermediate typing states)
    var redText by remember { mutableStateOf(red.toString()) }
    var greenText by remember { mutableStateOf(green.toString()) }
    var blueText by remember { mutableStateOf(blue.toString()) }

    // Validation states
    val isHexValid = hexInput.length == 6 && hexInput.all { it in '0'..'9' || it in 'A'..'F' }
    val isRedValid = redText.toIntOrNull()?.let { it in 0..255 } ?: false
    val isGreenValid = greenText.toIntOrNull()?.let { it in 0..255 } ?: false
    val isBlueValid = blueText.toIntOrNull()?.let { it in 0..255 } ?: false

    val selectedColor = Color(red, green, blue)

    // Update hex when RGB changes
    fun updateHexFromRgb() {
        hexInput = String.format("%02X%02X%02X", red, green, blue)
    }

    // Update RGB from hex
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
                            .background(AccentCyan.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = AccentCyan
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
                            "Custom watermark color",
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
                            color = AccentCyan
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
                    label = { Text("Hex") },
                    prefix = { Text("#") },
                    singleLine = true,
                    isError = hexInput.isNotEmpty() && !isHexValid,
                    supportingText = if (hexInput.isNotEmpty() && !isHexValid) {
                        { Text("Enter 6 hex characters (0-9, A-F)") }
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
                        label = { Text("R") },
                        singleLine = true,
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
                        label = { Text("G") },
                        singleLine = true,
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
                        label = { Text("B") },
                        singleLine = true,
                        isError = blueText.isNotEmpty() && !isBlueValid,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RGB Sliders
                ColorSlider(
                    label = "R",
                    value = red.toFloat(),
                    color = Color.Red,
                    onValueChange = {
                        red = it.toInt()
                        redText = red.toString()
                        updateHexFromRgb()
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                ColorSlider(
                    label = "G",
                    value = green.toFloat(),
                    color = Color.Green,
                    onValueChange = {
                        green = it.toInt()
                        greenText = green.toString()
                        updateHexFromRgb()
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                ColorSlider(
                    label = "B",
                    value = blue.toFloat(),
                    color = Color.Blue,
                    onValueChange = {
                        blue = it.toInt()
                        blueText = blue.toString()
                        updateHexFromRgb()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onColorSelected(selectedColor) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (isLandscape) {
        // Side panel for landscape
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Scrim on the left
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() }
                )
                // Panel on the right
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    ColorPickerContent(modifier = Modifier.fillMaxSize())
                }
            }
        }
    } else {
        // Bottom sheet for portrait
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
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
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
private fun CompactWatermarkPreview(
    watermarkType: WatermarkType,
    text: String,
    fontSize: Float,
    textColor: Color,
    textOpacity: Float,
    textRotation: Float,
    imageUri: android.net.Uri?,
    imageScale: Float,
    imageOpacity: Float,
    position: WatermarkPosition,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Compact mock PDF page
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                // Simplified mock content lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(8) { index ->
                        val widthFraction = when (index) {
                            0 -> 0.5f
                            7 -> 0.3f
                            else -> 0.7f + (index % 2) * 0.15f
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(widthFraction.coerceAtMost(1f))
                                .height(3.dp)
                                .background(
                                    Color.Gray.copy(alpha = 0.12f),
                                    RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }

                // Watermark overlay
                if (position == WatermarkPosition.TILED) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(2) {
                                    WatermarkElement(
                                        watermarkType = watermarkType,
                                        text = text,
                                        fontSize = fontSize * 0.08f,
                                        textColor = textColor,
                                        textOpacity = textOpacity,
                                        textRotation = textRotation,
                                        imageUri = imageUri,
                                        imageScale = imageScale * 0.2f,
                                        imageOpacity = imageOpacity,
                                        isTiled = true
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = when (position) {
                            WatermarkPosition.CENTER -> Alignment.Center
                            WatermarkPosition.TOP_LEFT -> Alignment.TopStart
                            WatermarkPosition.TOP_CENTER -> Alignment.TopCenter
                            WatermarkPosition.TOP_RIGHT -> Alignment.TopEnd
                            WatermarkPosition.BOTTOM_LEFT -> Alignment.BottomStart
                            WatermarkPosition.BOTTOM_CENTER -> Alignment.BottomCenter
                            WatermarkPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                            WatermarkPosition.TILED -> Alignment.Center
                        }
                    ) {
                        WatermarkElement(
                            watermarkType = watermarkType,
                            text = text,
                            fontSize = fontSize * 0.1f,
                            textColor = textColor,
                            textOpacity = textOpacity,
                            textRotation = textRotation,
                            imageUri = imageUri,
                            imageScale = imageScale * 0.25f,
                            imageOpacity = imageOpacity,
                            isTiled = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatermarkElement(
    watermarkType: WatermarkType,
    text: String,
    fontSize: Float,
    textColor: Color,
    textOpacity: Float,
    textRotation: Float,
    imageUri: android.net.Uri?,
    imageScale: Float,
    imageOpacity: Float,
    isTiled: Boolean
) {
    when (watermarkType) {
        WatermarkType.TEXT -> {
            val displayText = text.ifEmpty { "Sample" }
            Text(
                text = displayText,
                fontSize = fontSize.coerceIn(5f, if (isTiled) 10f else 16f).sp,
                color = textColor.copy(alpha = (textOpacity / 100f).coerceIn(0.1f, 1f)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.rotate(textRotation),
                maxLines = 1
            )
        }
        WatermarkType.IMAGE -> {
            if (imageUri != null) {
                val size = (imageScale * 1f).coerceIn(12f, if (isTiled) 24f else 50f)
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Watermark preview",
                    modifier = Modifier
                        .size(size.dp)
                        .graphicsLayer { alpha = (imageOpacity / 100f).coerceIn(0.1f, 1f) },
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (isTiled) 16.dp else 28.dp)
                        .graphicsLayer { alpha = 0.3f },
                    tint = AccentCyan
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
