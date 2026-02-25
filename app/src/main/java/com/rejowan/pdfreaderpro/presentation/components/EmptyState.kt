package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
fun EmptyFilesState(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.Description,
        title = "No PDFs Found",
        message = "We couldn't find any PDF files on your device.",
        actionLabel = "Scan for PDFs",
        onAction = onScanClick,
        modifier = modifier
    )
}

@Composable
fun EmptyRecentState(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.History,
        title = "No Recent Files",
        message = "PDFs you open will appear here for quick access.",
        actionLabel = "Browse All Files",
        onAction = onBrowseClick,
        modifier = modifier
    )
}

@Composable
fun EmptyFavoritesState(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.FavoriteBorder,
        title = "No Favorites Yet",
        message = "Tap the star icon on any PDF to add it to your favorites.",
        actionLabel = "Browse All Files",
        onAction = onBrowseClick,
        modifier = modifier
    )
}

@Composable
fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.SearchOff,
        title = "No Results for \"$query\"",
        message = "Try a different search term or check the spelling.",
        modifier = modifier
    )
}
