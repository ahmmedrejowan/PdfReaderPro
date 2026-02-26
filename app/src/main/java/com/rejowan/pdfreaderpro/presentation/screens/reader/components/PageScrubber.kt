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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val AccentPurple = Color(0xFF9575CD)
private val AccentPurpleLight = Color(0xFFB39DDB)
private val SurfaceDark = Color(0xFF1C1C1E)

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

    // Progress calculation (0-based page index)
    val progress = if (totalPages > 1) currentPage.toFloat() / (totalPages - 1) else 0f

    // Track height for drag calculations
    var trackHeightPx by remember { mutableFloatStateOf(0f) }

    // Drag state
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }

    // Animate progress when not dragging
    val animatedProgress by animateFloatAsState(
        targetValue = if (isDragging) dragProgress else progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scrubber progress"
    )

    val surfaceColor = if (isDarkMode) SurfaceDark.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.95f)
    val trackColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isDarkMode) Color.White else Color.Black

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
        Box(
            modifier = Modifier
                .padding(end = 8.dp, top = 80.dp, bottom = 120.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Main scrubber container
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = AccentPurple.copy(alpha = 0.1f),
                        spotColor = AccentPurple.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current page label
                    Text(
                        text = "${currentPage + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = AccentPurple
                    )

                    // Vertical track
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .width(28.dp)
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
                                    onDragCancel = {
                                        isDragging = false
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val newY = change.position.y
                                        dragProgress = (newY / trackHeightPx).coerceIn(0f, 1f)
                                    }
                                )
                            }
                    ) {
                        // Track background
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(trackColor)
                        )

                        // Progress fill (from top)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(4.dp)
                                .fillMaxHeight(animatedProgress)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            AccentPurple,
                                            AccentPurpleLight
                                        )
                                    )
                                )
                        )

                        // Draggable thumb
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset {
                                    IntOffset(
                                        0,
                                        ((trackHeightPx * animatedProgress) - with(density) { 10.dp.toPx() })
                                            .coerceAtLeast(0f)
                                            .roundToInt()
                                    )
                                }
                        ) {
                            // Thumb with page preview when dragging
                            if (isDragging) {
                                // Show page preview bubble
                                Surface(
                                    modifier = Modifier
                                        .offset(x = (-40).dp)
                                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentPurple
                                ) {
                                    Text(
                                        text = "${(dragProgress * (totalPages - 1)).roundToInt() + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Thumb circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .shadow(
                                        elevation = if (isDragging) 8.dp else 4.dp,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (isDragging) AccentPurple else Color.White
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Inner dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDragging) Color.White else AccentPurple
                                        )
                                )
                            }
                        }
                    }

                    // Total pages label
                    Text(
                        text = "$totalPages",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
