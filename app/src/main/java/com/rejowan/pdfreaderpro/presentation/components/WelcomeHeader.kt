package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale

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

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
}

@Composable
fun WelcomeHeader(
    totalPdfs: Int,
    totalSize: Long,
    favoritesCount: Int,
    onSortClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = getGreeting()
    val emoji = getGreetingEmoji()

    // Stats section commented out for now
    // var isStatsExpanded by rememberSaveable { mutableStateOf(false) }
    // val rotationAngle by animateFloatAsState(
    //     targetValue = if (isStatsExpanded) 180f else 0f,
    //     label = "arrow rotation"
    // )

    // val softPurple = Color(0xFF9575CD)
    // val softBlue = Color(0xFF64B5F6)
    // val softPink = Color(0xFFF06292)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
    ) {
        // Greeting with stats action
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onStatsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = "Stats",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Stats Section - Commented out for now
        // Spacer(modifier = Modifier.height(16.dp))
        // AnimatedContent(
        //     targetState = isStatsExpanded,
        //     transitionSpec = { fadeIn() togetherWith fadeOut() },
        //     label = "stats toggle"
        // ) { expanded ->
        //     if (expanded) {
        //         // Expanded: Full stat cards
        //         Row(
        //             modifier = Modifier
        //                 .fillMaxWidth()
        //                 .padding(end = 8.dp),
        //             horizontalArrangement = Arrangement.spacedBy(10.dp),
        //             verticalAlignment = Alignment.CenterVertically
        //         ) {
        //             StatCard(
        //                 icon = Icons.Outlined.Folder,
        //                 value = totalPdfs.toString(),
        //                 label = "PDFs",
        //                 accentColor = softPurple,
        //                 modifier = Modifier.weight(1f)
        //             )
        //             StatCard(
        //                 icon = Icons.Outlined.Storage,
        //                 value = formatFileSize(totalSize),
        //                 label = "Size",
        //                 accentColor = softBlue,
        //                 modifier = Modifier.weight(1f)
        //             )
        //             StatCard(
        //                 icon = Icons.Outlined.FavoriteBorder,
        //                 value = favoritesCount.toString(),
        //                 label = "Favorites",
        //                 accentColor = softPink,
        //                 modifier = Modifier.weight(1f)
        //             )
        //             // Collapse arrow
        //             ExpandCollapseButton(
        //                 isExpanded = true,
        //                 rotationAngle = rotationAngle,
        //                 onClick = { isStatsExpanded = false }
        //             )
        //         }
        //     } else {
        //         // Collapsed: Compact bar with accent backgrounds
        //         Row(
        //             modifier = Modifier
        //                 .fillMaxWidth()
        //                 .padding(end = 8.dp),
        //             horizontalArrangement = Arrangement.spacedBy(10.dp),
        //             verticalAlignment = Alignment.CenterVertically
        //         ) {
        //             CompactStat(
        //                 icon = Icons.Outlined.Folder,
        //                 value = totalPdfs.toString(),
        //                 accentColor = softPurple
        //             )
        //             CompactStat(
        //                 icon = Icons.Outlined.Storage,
        //                 value = formatFileSize(totalSize),
        //                 accentColor = softBlue
        //             )
        //             CompactStat(
        //                 icon = Icons.Outlined.FavoriteBorder,
        //                 value = favoritesCount.toString(),
        //                 accentColor = softPink
        //             )
        //             Spacer(modifier = Modifier.weight(1f))
        //             // Expand arrow
        //             ExpandCollapseButton(
        //                 isExpanded = false,
        //                 rotationAngle = rotationAngle,
        //                 onClick = { isStatsExpanded = true }
        //             )
        //         }
        //     }
        // }

    }
}

@Composable
private fun ExpandCollapseButton(
    isExpanded: Boolean,
    rotationAngle: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier
                .padding(6.dp)
                .rotate(rotationAngle),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun CompactStat(
    icon: ImageVector,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = accentColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

