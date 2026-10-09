package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.data.mapper.asOpaqueSwatch
import com.rejowan.pdfreaderpro.domain.model.Highlight
import com.rejowan.pdfreaderpro.presentation.screens.reader.HighlightColors
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.pluralStringResource

private val PANEL_WIDTH = 320.dp
private val OUTER_CORNER = 20.dp
private val INNER_CORNER = 8.dp

/**
 * Lists every highlight in the document, newest page last.
 *
 * Side panel in landscape, bottom sheet in portrait, matching the other reader
 * sheets. See doc/reader-screen-ui-guide.md.
 */
@Composable
fun HighlightsSheet(
    highlights: List<Highlight>,
    currentPage: Int,
    onHighlightClick: (Highlight) -> Unit,
    onDeleteHighlight: (Highlight) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = ""
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        HighlightsSidePanel(
            highlights = highlights,
            currentPage = currentPage,
            onHighlightClick = onHighlightClick,
            onDeleteHighlight = onDeleteHighlight,
            onDismiss = onDismiss,
            initialQuery = initialQuery
        )
    } else {
        HighlightsBottomSheet(
            highlights = highlights,
            currentPage = currentPage,
            onHighlightClick = onHighlightClick,
            onDeleteHighlight = onDeleteHighlight,
            onDismiss = onDismiss,
            initialQuery = initialQuery
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HighlightsBottomSheet(
    highlights: List<Highlight>,
    currentPage: Int,
    onHighlightClick: (Highlight) -> Unit,
    onDeleteHighlight: (Highlight) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = ""
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = OUTER_CORNER, topEnd = OUTER_CORNER),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        HighlightsContent(
            highlights = highlights,
            currentPage = currentPage,
            onHighlightClick = onHighlightClick,
            onDeleteHighlight = onDeleteHighlight,
            initialQuery = initialQuery
        )
    }
}

@Composable
private fun HighlightsSidePanel(
    highlights: List<Highlight>,
    currentPage: Int,
    onHighlightClick: (Highlight) -> Unit,
    onDeleteHighlight: (Highlight) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = ""
) {
    // Back closes the panel the same way tapping outside it does.
    BackHandler { onDismiss() }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(PANEL_WIDTH),
                shape = RoundedCornerShape(topStart = OUTER_CORNER, bottomStart = OUTER_CORNER),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                HighlightsContent(
                    highlights = highlights,
                    currentPage = currentPage,
                    onHighlightClick = onHighlightClick,
                    onDeleteHighlight = onDeleteHighlight,
                    initialQuery = initialQuery,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = systemBarsPadding.calculateTopPadding(),
                            bottom = systemBarsPadding.calculateBottomPadding()
                        )
                )
            }
        }
    }
}

@Composable
private fun HighlightsContent(
    highlights: List<Highlight>,
    currentPage: Int,
    onHighlightClick: (Highlight) -> Unit,
    onDeleteHighlight: (Highlight) -> Unit,
    initialQuery: String = "",
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf(initialQuery) }
    var colorFilter by remember { mutableStateOf<Int?>(null) }

    // Filtering in memory rather than re-querying: the list is already loaded and
    // small enough that a round trip per keystroke would be wasted work.
    val visible = remember(highlights, query, colorFilter) {
        highlights.filter { highlight ->
            val matchesQuery = query.isBlank() ||
                highlight.text.contains(query, ignoreCase = true) ||
                highlight.label?.contains(query, ignoreCase = true) == true
            val matchesColor = colorFilter == null || highlight.color == colorFilter
            matchesQuery && matchesColor
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HighlightsHeader(count = highlights.size)

        if (highlights.isNotEmpty()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.search_highlights)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear_search),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(INNER_CORNER)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorFilterRow(
                selected = colorFilter,
                onSelect = { colorFilter = if (colorFilter == it) null else it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            highlights.isEmpty() -> EmptyHighlightsState()

            visible.isEmpty() -> NoMatchesState()

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(items = visible, key = { it.id }) { highlight ->
                    HighlightItem(
                        highlight = highlight,
                        isCurrentPage = highlight.pageNumber == currentPage,
                        onClick = { onHighlightClick(highlight) },
                        onDelete = { onDeleteHighlight(highlight) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HighlightsHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(INNER_CORNER),
            color = Color(HighlightColors.YELLOW).copy(alpha = 0.35f)
        ) {
            Icon(
                imageVector = Icons.Rounded.FormatColorFill,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = stringResource(R.string.highlights),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (count > 0) {
                Text(
                    text = if (count == 1) {
                        stringResource(R.string.highlight_count, count)
                    } else {
                        pluralStringResource(R.plurals.highlights_count, count, count)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ColorFilterRow(
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HighlightColors.ALL.forEach { color ->
            val isSelected = selected == color
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(color.asOpaqueSwatch()))
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightItem(
    highlight: Highlight,
    isCurrentPage: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swatch = Color(highlight.color.asOpaqueSwatch())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(INNER_CORNER))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(INNER_CORNER),
        color = if (isCurrentPage) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colour bar, so the highlight's colour is readable at a glance
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(swatch)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlight.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.highlight_page, highlight.pageNumber + 1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    if (!highlight.isEditable) {
                        Text(
                            text = " · " + stringResource(R.string.highlight_in_file),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    highlight.label?.let { label ->
                        Text(
                            text = " · $label",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (highlight.isEditable) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete_highlight),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Belongs to the file, so there is nothing here to delete.
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = stringResource(R.string.highlight_in_file),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(32.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHighlightsState(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.no_highlights),
        hint = stringResource(R.string.no_highlights_hint),
        modifier = modifier
    )
}

@Composable
private fun NoMatchesState(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.no_matching_highlights),
        hint = stringResource(R.string.no_matching_highlights_hint),
        modifier = modifier
    )
}

@Composable
private fun EmptyState(
    title: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(HighlightColors.YELLOW).copy(alpha = 0.25f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Rounded.FormatColorFill,
                    contentDescription = stringResource(R.string.cd_decorative),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
