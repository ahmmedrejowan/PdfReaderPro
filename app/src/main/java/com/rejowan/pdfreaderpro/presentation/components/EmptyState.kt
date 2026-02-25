package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Soft accent colors for different states
private val SoftBlue = Color(0xFF64B5F6)
private val SoftPurple = Color(0xFF9575CD)
private val SoftPink = Color(0xFFF06292)
private val SoftTeal = Color(0xFF4DB6AC)
private val SoftAmber = Color(0xFFFFB74D)

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    accentColor: Color = SoftPurple,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Use top bias to account for header above - content appears in upper-center
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Soft icon container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = accentColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.Medium
                )
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
        title = "No PDFs here yet",
        message = "We couldn't find any PDF files on your device. Try scanning again or add some PDFs to get started!",
        accentColor = SoftBlue,
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
        title = "Your reading journey starts here",
        message = "PDFs you open will show up here for quick access. Start exploring your files!",
        accentColor = SoftTeal,
        actionLabel = "Browse Files",
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
        title = "No favorites yet",
        message = "Tap the heart icon on any PDF to save it here for easy access later.",
        accentColor = SoftPink,
        actionLabel = "Browse Files",
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
        title = "No matches found",
        message = "We couldn't find anything for \"$query\". Try a different search term.",
        accentColor = SoftAmber,
        modifier = modifier
    )
}

@Composable
fun EmptyFoldersState(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.FolderOpen,
        title = "No folders yet",
        message = "PDF folders will appear here once your files are scanned. Let's find them!",
        accentColor = SoftPurple,
        actionLabel = "Scan for PDFs",
        onAction = onScanClick,
        modifier = modifier
    )
}

@Composable
fun PermissionRequiredState(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use top bias to account for header above - content appears in upper-center
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Soft icon container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = SoftPurple.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = SoftPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "We need your permission",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To show your PDF files here, please allow access to your storage",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onGrantClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftPurple
            ),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Allow Access",
                fontWeight = FontWeight.Medium
            )
        }
    }
}
