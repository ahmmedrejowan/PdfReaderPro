package com.rejowan.pdfreaderpro.presentation.screens.search

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import com.rejowan.pdfreaderpro.R
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.presentation.components.EmptySearchState
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem
import com.rejowan.pdfreaderpro.presentation.components.dialogs.DeleteConfirmSheet
import com.rejowan.pdfreaderpro.presentation.components.dialogs.FileInfoDialog
import com.rejowan.pdfreaderpro.presentation.components.dialogs.RenameSheet
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import com.rejowan.pdfreaderpro.util.FileOperations
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// Accent colors
private val SoftPurple = Color(0xFF9575CD)
private val SoftBlue = Color(0xFF64B5F6)
private val SoftTeal = Color(0xFF4DB6AC)
private val SoftAmber = Color(0xFFFFB74D)
private val SoftPink = Color(0xFFF06292)

// Recent searches preferences
private const val RECENT_SEARCHES_PREFS = "recent_searches"
private const val RECENT_SEARCHES_KEY = "searches"
private const val MAX_RECENT_SEARCHES = 8

@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: SearchViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Recent searches state
    val recentSearches = remember { mutableStateListOf<String>() }

    var selectedFile by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFavorite by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForDialog by remember { mutableStateOf<PdfFile?>(null) }

    // Load recent searches
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(RECENT_SEARCHES_PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(RECENT_SEARCHES_KEY, emptySet()) ?: emptySet()
        recentSearches.clear()
        recentSearches.addAll(saved.take(MAX_RECENT_SEARCHES))

        if (initialQuery.isNotBlank()) {
            viewModel.setSearchQuery(initialQuery)
        }
        focusRequester.requestFocus()
    }

    // Save search to recent when query changes and has results
    LaunchedEffect(searchResults) {
        if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
            val prefs = context.getSharedPreferences(RECENT_SEARCHES_PREFS, Context.MODE_PRIVATE)
            val updated = (listOf(searchQuery) + recentSearches.filter { it != searchQuery })
                .take(MAX_RECENT_SEARCHES)
            prefs.edit().putStringSet(RECENT_SEARCHES_KEY, updated.toSet()).apply()
            recentSearches.clear()
            recentSearches.addAll(updated)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        // Search Header
        SearchHeader(
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onClear = { viewModel.clearSearch() },
            onBack = { navController.popBackStack() },
            onSearch = { focusManager.clearFocus() },
            focusRequester = focusRequester
        )

        // Content
        when {
            isSearching -> {
                LoadingState()
            }
            searchQuery.isBlank() -> {
                // Show recent searches and tips
                SearchIdleContent(
                    recentSearches = recentSearches,
                    onRecentClick = { query ->
                        viewModel.setSearchQuery(query)
                    },
                    onClearRecent = { query ->
                        recentSearches.remove(query)
                        val prefs = context.getSharedPreferences(RECENT_SEARCHES_PREFS, Context.MODE_PRIVATE)
                        prefs.edit().putStringSet(RECENT_SEARCHES_KEY, recentSearches.toSet()).apply()
                    },
                    onClearAllRecent = {
                        recentSearches.clear()
                        val prefs = context.getSharedPreferences(RECENT_SEARCHES_PREFS, Context.MODE_PRIVATE)
                        prefs.edit().remove(RECENT_SEARCHES_KEY).apply()
                    }
                )
            }
            searchResults.isEmpty() -> {
                EmptySearchState(query = searchQuery)
            }
            else -> {
                // Results
                SearchResultsContent(
                    query = searchQuery,
                    results = searchResults,
                    onFileClick = { file ->
                        navController.navigateToReader(file.path)
                    },
                    onFileOptionsClick = { file ->
                        selectedFile = file
                        scope.launch {
                            selectedFileFavorite = viewModel.isFavorite(file.path)
                        }
                    }
                )
            }
        }
    }

    // File Options Sheet
    selectedFile?.let { file ->
        FileOptionsSheet(
            pdfFile = file,
            isFavorite = selectedFileFavorite,
            onDismiss = { selectedFile = null },
            onFavoriteClick = { viewModel.toggleFavorite(file) },
            onShareClick = {
                FileOperations.sharePdf(context, file.path)
            },
            onRenameClick = {
                fileForDialog = file
                showRenameDialog = true
            },
            onInfoClick = {
                fileForDialog = file
                showInfoDialog = true
            },
            onDeleteClick = {
                fileForDialog = file
                showDeleteDialog = true
            }
        )
    }

    // Rename Sheet
    if (showRenameDialog) {
        fileForDialog?.let { file ->
            RenameSheet(
                pdfFile = file,
                onDismiss = {
                    showRenameDialog = false
                    fileForDialog = null
                },
                onRename = { newName ->
                    viewModel.renameFile(file.path, newName) { /* success handled internally */ }
                }
            )
        }
    }

    // Delete Confirmation Sheet
    if (showDeleteDialog) {
        fileForDialog?.let { file ->
            DeleteConfirmSheet(
                pdfFile = file,
                onDismiss = {
                    showDeleteDialog = false
                    fileForDialog = null
                },
                onConfirm = {
                    viewModel.deleteFile(file.path) { /* success handled internally */ }
                }
            )
        }
    }

    // File Info Dialog
    if (showInfoDialog) {
        fileForDialog?.let { file ->
            FileInfoDialog(
                pdfFile = file,
                onDismiss = {
                    showInfoDialog = false
                    fileForDialog = null
                }
            )
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Search field
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.cd_decorative),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_your_pdfs_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearch() })
                        )
                    }

                    // Clear button
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(tween(150)) + expandVertically(),
                        exit = fadeOut(tween(100)) + shrinkVertically()
                    ) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onClear),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = stringResource(R.string.clear),
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val COLLAPSED_RECENT_COUNT = 3

@Composable
private fun SearchIdleContent(
    recentSearches: List<String>,
    onRecentClick: (String) -> Unit,
    onClearRecent: (String) -> Unit,
    onClearAllRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecentExpanded by remember { mutableStateOf(false) }
    val displayedSearches = if (isRecentExpanded) recentSearches else recentSearches.take(COLLAPSED_RECENT_COUNT)
    val hasMore = recentSearches.size > COLLAPSED_RECENT_COUNT

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Recent searches section
        if (recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = stringResource(R.string.cd_decorative),
                            modifier = Modifier.size(18.dp),
                            tint = SoftPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.recent_searches),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.clear_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftPurple,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onClearAllRecent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            itemsIndexed(
                items = displayedSearches,
                key = { _, query -> "recent_$query" }
            ) { index, query ->
                RecentSearchItem(
                    query = query,
                    onClick = { onRecentClick(query) },
                    onRemove = { onClearRecent(query) },
                    animationDelay = index * 30
                )
            }

            // Show more/less button
            if (hasMore) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isRecentExpanded = !isRecentExpanded }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRecentExpanded) stringResource(R.string.show_less) else stringResource(R.string.show_all_count, recentSearches.size),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = SoftPurple
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Search tips section
        item {
            SearchTipsSection()
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(animationDelay == 0) }

    LaunchedEffect(Unit) {
        if (animationDelay > 0) {
            kotlinx.coroutines.delay(animationDelay.toLong())
            isVisible = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "recent item scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier
                    .padding(8.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = query,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.remove),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SearchTipsSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.TipsAndUpdates,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier.size(18.dp),
                tint = SoftAmber
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.search_tips),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SearchTipCard(
            icon = Icons.Outlined.Description,
            title = stringResource(R.string.tip_search_filename_title),
            description = stringResource(R.string.tip_search_filename_desc),
            accentColor = SoftBlue,
            animationDelay = 0
        )

        Spacer(modifier = Modifier.height(10.dp))

        SearchTipCard(
            icon = Icons.Outlined.Folder,
            title = stringResource(R.string.tip_search_folder_title),
            description = stringResource(R.string.tip_search_folder_desc),
            accentColor = SoftTeal,
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(10.dp))

        SearchTipCard(
            icon = Icons.Outlined.Lightbulb,
            title = stringResource(R.string.tip_partial_matches_title),
            description = stringResource(R.string.tip_partial_matches_desc),
            accentColor = SoftPink,
            animationDelay = 100
        )
    }
}

@Composable
private fun SearchTipCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "tip scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    query: String,
    results: List<PdfFile>,
    onFileClick: (PdfFile) -> Unit,
    onFileOptionsClick: (PdfFile) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    focusManager.clearFocus()
                })
            },
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Results header
        item {
            SearchResultsHeader(
                query = query,
                resultCount = results.size
            )
        }

        // Results list with staggered animation
        itemsIndexed(
            items = results,
            key = { _, file -> file.id }
        ) { index, file ->
            SearchResultItem(
                pdfFile = file,
                query = query,
                onClick = { onFileClick(file) },
                onOptionsClick = { onFileOptionsClick(file) },
                animationDelay = index * 30,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun SearchResultsHeader(
    query: String,
    resultCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.results_for, query),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (resultCount == 1) stringResource(R.string.file_found, resultCount) else stringResource(R.string.files_found, resultCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // Result count badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SoftBlue.copy(alpha = 0.12f)
        ) {
            Text(
                text = resultCount.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SoftBlue,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultItem(
    pdfFile: PdfFile,
    query: String,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(animationDelay == 0) }

    LaunchedEffect(Unit) {
        if (animationDelay > 0) {
            kotlinx.coroutines.delay(animationDelay.toLong())
            isVisible = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "item scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onOptionsClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF Thumbnail
            com.rejowan.pdfreaderpro.presentation.components.PdfThumbnail(
                size = 52.dp,
                pdfPath = pdfFile.path,
                pageCount = pdfFile.pageCount.takeIf { it > 0 }
            )

            Spacer(modifier = Modifier.width(14.dp))

            // File info with highlighted name
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Highlighted file name
                Text(
                    text = buildHighlightedText(pdfFile.displayName, query),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Size and date row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = com.rejowan.pdfreaderpro.util.FormattingUtils.formattedFileSize(pdfFile.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = com.rejowan.pdfreaderpro.util.FormattingUtils.formattedDate(pdfFile.dateModified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Folder row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = stringResource(R.string.cd_decorative),
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF9E9E9E).copy(alpha = 0.7f)
                    )
                    Text(
                        text = pdfFile.folderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Options button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = onOptionsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.options),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun buildHighlightedText(text: String, query: String) = buildAnnotatedString {
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var startIndex = 0

    while (true) {
        val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
        if (matchIndex == -1) {
            // No more matches, append the rest
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(text.substring(startIndex))
            }
            break
        }

        // Append text before match
        if (matchIndex > startIndex) {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(text.substring(startIndex, matchIndex))
            }
        }

        // Append highlighted match
        withStyle(
            SpanStyle(
                color = SoftPurple,
                fontWeight = FontWeight.Bold
            )
        ) {
            append(text.substring(matchIndex, matchIndex + query.length))
        }

        startIndex = matchIndex + query.length
    }
}
