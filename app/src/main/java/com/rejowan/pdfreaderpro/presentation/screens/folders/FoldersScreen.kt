package com.rejowan.pdfreaderpro.presentation.screens.folders

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.FolderSortOption
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.presentation.components.EmptyFoldersState
import com.rejowan.pdfreaderpro.presentation.components.FolderItem
import com.rejowan.pdfreaderpro.presentation.components.FolderSortOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState

private val FolderAmber = Color(0xFFFFB74D)
private val SoftBlue = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    folders: List<PdfFolder>,
    isLoading: Boolean,
    hasPermission: Boolean,
    currentSort: FolderSortOption,
    searchQuery: String,
    onFolderClick: (PdfFolder) -> Unit,
    onFolderLongClick: (PdfFolder) -> Unit,
    onRefresh: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    onSortSelected: (FolderSortOption) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortSheet by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            !hasPermission -> {
                PermissionRequiredState(onGrantClick = onGrantPermissionClick)
            }
            isLoading && folders.isEmpty() -> {
                LoadingState()
            }
            folders.isEmpty() && searchQuery.isBlank() -> {
                EmptyFoldersState(onScanClick = onRefresh)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusManager.clearFocus()
                        },
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Header with stats
                    item {
                        FoldersHeader(
                            folderCount = folders.size,
                            totalPdfCount = folders.sumOf { it.pdfCount },
                            isSearchExpanded = isSearchExpanded,
                            searchQuery = searchQuery,
                            searchFocusRequester = searchFocusRequester,
                            onSearchToggle = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) {
                                    focusManager.clearFocus()
                                }
                            },
                            onSearchQueryChange = onSearchQueryChange,
                            onSortClick = { showSortSheet = true }
                        )
                    }

                    // Empty search state
                    if (folders.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No folders found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Try a different search term",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Folder items
                    itemsIndexed(
                        items = folders,
                        key = { _, folder -> folder.path }
                    ) { index, folder ->
                        FolderItem(
                            folder = folder,
                            onClick = { onFolderClick(folder) },
                            onLongClick = { onFolderLongClick(folder) },
                            animationDelay = index * 30
                        )
                    }
                }
            }
        }
    }

    // Sort Options Sheet
    if (showSortSheet) {
        FolderSortOptionsSheet(
            currentSort = currentSort,
            onSortSelected = onSortSelected,
            onDismiss = { showSortSheet = false }
        )
    }
}

@Composable
private fun FoldersHeader(
    folderCount: Int,
    totalPdfCount: Int,
    isSearchExpanded: Boolean,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatChip(
                icon = Icons.Outlined.Folder,
                value = folderCount.toString(),
                label = "Folders",
                accentColor = FolderAmber,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                icon = Icons.Outlined.Description,
                value = totalPdfCount.toString(),
                label = "PDFs",
                accentColor = SoftBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar or controls row - fixed height to prevent layout shift
        val headerRowHeight = 44.dp

        if (isSearchExpanded) {
            SearchBar(
                query = searchQuery,
                focusRequester = searchFocusRequester,
                onQueryChange = onSearchQueryChange,
                onClose = {
                    onSearchQueryChange("")
                    onSearchToggle()
                },
                modifier = Modifier.height(headerRowHeight)
            )
        } else {
            // Section label with controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerRowHeight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Folders",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )

                // Search and Sort buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        onClick = onSearchToggle
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search folders",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    ControlButton(
                        onClick = onSortClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Sort,
                            contentDescription = "Sort folders",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-focus when search bar appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search folders...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close search",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        label = "control button tint"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        contentColor = tint
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = accentColor
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
