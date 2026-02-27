package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTableOfContents(
    items: List<OutlineItem>,
    currentPage: Int,
    onItemClick: (OutlineItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Auto-scroll to current page section
    LaunchedEffect(items, currentPage) {
        val currentIndex = items.indexOfFirst { it.page == currentPage }
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
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
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentPurple.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp),
                        tint = AccentPurple
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Table of Contents",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (items.isNotEmpty()) {
                        Text(
                            text = "${items.size} ${if (items.size == 1) "section" else "sections"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                EmptyTocState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    itemsIndexed(items) { index, item ->
                        TocItem(
                            item = item,
                            isCurrentSection = item.page == currentPage,
                            onClick = { onItemClick(item) },
                            animationDelay = (index * 20).coerceAtMost(200)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TocItem(
    item: OutlineItem,
    isCurrentSection: Boolean,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "toc item scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrentSection) AccentPurple.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "toc item background"
    )

    val indicatorColor by animateColorAsState(
        targetValue = if (isCurrentSection) AccentPurple else MaterialTheme.colorScheme.outlineVariant,
        label = "indicator color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isCurrentSection) AccentPurple else MaterialTheme.colorScheme.onSurface,
        label = "toc text color"
    )

    val startPadding = 24.dp + (item.level * 20).dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AccentPurple),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level indicator line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (item.level == 0) 28.dp else 20.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(indicatorColor)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Title and chevron
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentSection || item.level == 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = if (item.level == 0) 16.sp else 15.sp
                    ),
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Page number chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrentSection) AccentPurple else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.page + 1}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isCurrentSection) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isCurrentSection) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTocState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AccentBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Contents Available",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This PDF doesn't have a table of contents",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
