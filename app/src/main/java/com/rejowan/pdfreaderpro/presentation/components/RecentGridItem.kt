package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.RecentFile

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentGridItem(
    recentFile: RecentFile,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onOptionsClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column {
            // Thumbnail with options button overlay
            Box {
                PdfThumbnailGrid(
                    modifier = Modifier.fillMaxWidth(),
                    pdfPath = recentFile.path,
                    pageCount = recentFile.totalPages.takeIf { it > 0 }
                )

                // Options button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .combinedClickable(
                            onClick = onOptionsClick,
                            onLongClick = onOptionsClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // File info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // File name - fixed height for 2 lines
                Text(
                    text = recentFile.name.removeSuffix(".pdf"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    ),
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { recentFile.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${recentFile.progressPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Page info and time ago
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Page ${recentFile.lastPage + 1}/${recentFile.totalPages}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = formatTimeAgo(recentFile.lastOpened),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / 60000
    val hours = diff / 3600000
    val days = diff / 86400000

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
            sdf.format(java.util.Date(timestamp))
        }
    }
}
