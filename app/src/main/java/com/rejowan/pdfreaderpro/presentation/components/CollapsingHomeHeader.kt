package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun getGreetingEmoji(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "☀️"
        hour < 17 -> "🌤️"
        hour < 21 -> "🌙"
        else -> "✨"
    }
}

// Search bar height for collapse calculation
val SEARCH_BAR_HEIGHT = 50.dp // 10dp vertical padding + ~20dp content + 4dp top + 6dp bottom + buffer

/**
 * Collapsing header for Home screen - Welcome header stays fixed, search bar collapses.
 *
 * @param collapseProgress 0f = fully expanded (search bar visible), 1f = fully collapsed (search bar hidden)
 * @param onSearchClick Called when search is clicked
 * @param onSortClick Called when sort button is clicked
 * @param onStatsClick Called when stats button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingHomeHeader(
    collapseProgress: Float,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = getGreeting()
    val emoji = getGreetingEmoji()

    // Clamp progress between 0 and 1
    val progress = collapseProgress.coerceIn(0f, 1f)

    // Search bar collapses: height shrinks and alpha fades
    val searchBarHeight = SEARCH_BAR_HEIGHT * (1f - progress)
    val searchBarAlpha = (1f - progress * 1.5f).coerceIn(0f, 1f)

    // Search icon in toolbar appears as search bar disappears
    val searchIconAlpha = ((progress - 0.3f) * 2f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Welcome header - stays fixed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greeting $emoji",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Welcome to your PDF library",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Row {
                // Search icon - appears when search bar is collapsed
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.graphicsLayer { alpha = searchIconAlpha }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = { PlainTooltip { Text("Sort files") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = onSortClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Sort,
                            contentDescription = "Sort files",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = { PlainTooltip { Text("Library statistics") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = onStatsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Library statistics",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Search bar - collapses as user scrolls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(searchBarHeight)
                .graphicsLayer { alpha = searchBarAlpha }
        ) {
            if (progress < 0.95f) { // Don't render when fully collapsed
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSearchClick),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search PDFs...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
