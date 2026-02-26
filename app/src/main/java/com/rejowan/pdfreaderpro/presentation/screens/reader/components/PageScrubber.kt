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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val AccentPurple = Color(0xFF9575CD)

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
                                dragProgress = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                val newPage = (dragProgress * (totalPages - 1)).roundToInt()
                                onPageChange(newPage)
                                isDragging = false
                            },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                dragProgress = (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                            }
                        )
                    },
                contentAlignment = Alignment.TopEnd
            ) {
                // Track background
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(trackColor)
                )

                // Progress fill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(4.dp)
                        .fillMaxHeight(animatedProgress.coerceIn(0.001f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(progressColor)
                )

                // Droplet thumb
                val thumbHeight = 32.dp
                val thumbHeightPx = with(density) { thumbHeight.toPx() }

                Box(
                    modifier = Modifier
                        .offset {
                            val yOffset = (trackHeightPx * animatedProgress - thumbHeightPx / 2)
                                .coerceIn(0f, trackHeightPx - thumbHeightPx)
                                .roundToInt()
                            IntOffset(0, yOffset)
                        }
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp)  // Space from edge so droplet isn't clipped
                ) {
                    DropletThumb(
                        pageNumber = displayPage,
                        isDragging = isDragging
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
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDragging) AccentPurple else AccentPurple.copy(alpha = 0.95f)

    Surface(
        modifier = modifier
            .width(48.dp)
            .height(32.dp),
        shape = DropletShape,
        color = bgColor,
        shadowElevation = if (isDragging) 8.dp else 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(end = 6.dp) // Offset text toward the bulb
        ) {
            Text(
                text = "$pageNumber",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
