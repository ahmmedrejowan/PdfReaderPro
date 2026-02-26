package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val AccentPurple = Color(0xFF9575CD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageScrubber(
    currentPage: Int,
    totalPages: Int,
    isVisible: Boolean,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val density = LocalDensity.current

    val progress = if (totalPages > 1) currentPage.toFloat() / (totalPages - 1) else 0f

    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    var showJumpToPageSheet by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isDragging) dragProgress else progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scrubber progress"
    )

    val displayPage = if (isDragging) {
        (dragProgress * (totalPages - 1)).roundToInt() + 1
    } else {
        currentPage + 1
    }

    val trackColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
    val progressColor = AccentPurple
    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f)

    AnimatedVisibility(
        visible = isVisible && totalPages > 1,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(end = 6.dp, top = 100.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Track area with droplet
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(64.dp)
                    .onSizeChanged { trackHeightPx = it.height.toFloat() }
                    .pointerInput(totalPages) {
                        detectTapGestures { offset ->
                            val newProgress = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                            val newPage = (newProgress * (totalPages - 1)).roundToInt()
                            onPageChange(newPage)
                        }
                    }
                    .pointerInput(totalPages) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val newProgress = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                                dragProgress = newProgress
                                val newPage = (newProgress * (totalPages - 1)).roundToInt()
                                onPageChange(newPage)
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                val newProgress = (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                                val oldPage = (dragProgress * (totalPages - 1)).roundToInt()
                                val newPage = (newProgress * (totalPages - 1)).roundToInt()
                                dragProgress = newProgress
                                if (newPage != oldPage) {
                                    onPageChange(newPage)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.TopEnd
            ) {
                // Droplet thumb height
                val thumbHeight = 36.dp
                val thumbHeightPx = with(density) { thumbHeight.toPx() }
                val trackPadding = thumbHeight / 2  // Track is shorter so tip aligns at edges

                // Track background (shorter, so tip reaches both ends)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(vertical = trackPadding)
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(trackColor)
                )

                // Progress fill (within the shorter track)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(vertical = trackPadding)
                        .width(4.dp)
                        .fillMaxHeight(animatedProgress.coerceIn(0.001f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(progressColor)
                )

                // Droplet thumb - tip aligns with the shortened track (no clamping needed)
                Box(
                    modifier = Modifier
                        .offset {
                            // Map progress 0→1 to droplet position 0→(trackHeight-thumbHeight)
                            // This makes tip align with shortened track: thumbHeight/2 → trackHeight-thumbHeight/2
                            val yOffset = (animatedProgress * (trackHeightPx - thumbHeightPx)).roundToInt()
                            IntOffset(0, yOffset)
                        }
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp)
                ) {
                    DropletThumb(
                        pageNumber = displayPage,
                        isDragging = isDragging,
                        onClick = { showJumpToPageSheet = true }
                    )
                }
            }

            // Total pages at bottom
            Text(
                text = "$totalPages",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp, end = 0.dp),
                textAlign = TextAlign.End
            )
        }
    }

    // Jump to Page Bottom Sheet
    if (showJumpToPageSheet) {
        JumpToPageBottomSheet(
            currentPage = currentPage + 1,
            totalPages = totalPages,
            onDismiss = { showJumpToPageSheet = false },
            onPageSelected = { page ->
                onPageChange(page - 1) // Convert to 0-indexed
                showJumpToPageSheet = false
            }
        )
    }
}

// Droplet shape pointing RIGHT (toward the track)
// Rounded bulb on left, pointed tip on right
private val DropletShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val bulbRadius = height / 2

    // Start at the pointed tip (right side, middle)
    moveTo(width, height / 2)

    // Curve from tip to top of bulb
    quadraticTo(
        x1 = width * 0.5f,
        y1 = 0f,
        x2 = bulbRadius,
        y2 = 0f
    )

    // Arc for the left bulb (top to bottom)
    arcTo(
        rect = androidx.compose.ui.geometry.Rect(
            left = 0f,
            top = 0f,
            right = bulbRadius * 2,
            bottom = height
        ),
        startAngleDegrees = -90f,
        sweepAngleDegrees = -180f,
        forceMoveTo = false
    )

    // Curve from bottom of bulb back to tip
    quadraticTo(
        x1 = width * 0.5f,
        y1 = height,
        x2 = width,
        y2 = height / 2
    )

    close()
}

@Composable
private fun DropletThumb(
    pageNumber: Int,
    isDragging: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDragging) AccentPurple else AccentPurple.copy(alpha = 0.95f)

    Surface(
        modifier = modifier
            .width(56.dp)
            .height(36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isDragging,
                onClick = onClick
            ),
        shape = DropletShape,
        color = bgColor,
        shadowElevation = if (isDragging) 8.dp else 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(end = 8.dp, start = 2.dp)
        ) {
            Text(
                text = "$pageNumber",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JumpToPageBottomSheet(
    currentPage: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var pageInput by remember {
        val text = currentPage.toString()
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isValidPage = pageInput.text.toIntOrNull()?.let { it in 1..totalPages } ?: false

    fun submitPage() {
        pageInput.text.toIntOrNull()?.let { page ->
            if (page in 1..totalPages) {
                keyboardController?.hide()
                onPageSelected(page)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Numbers,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Jump to Page",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter a page number (1 - $totalPages)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input field
            OutlinedTextField(
                value = pageInput,
                onValueChange = { input ->
                    if (input.text.all { it.isDigit() } || input.text.isEmpty()) {
                        pageInput = input
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Page number",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = { submitPage() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                isError = pageInput.text.isNotEmpty() && !isValidPage,
                shape = RoundedCornerShape(12.dp)
            )

            // Error text - always takes space to prevent UI jump
            val showError = pageInput.text.isNotEmpty() && !isValidPage
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enter a number between 1 and $totalPages",
                style = MaterialTheme.typography.labelSmall,
                color = if (showError) MaterialTheme.colorScheme.error else Color.Transparent
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { submitPage() },
                    modifier = Modifier.weight(1f),
                    enabled = isValidPage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}
