package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.selected
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.data.mapper.asOpaqueSwatch
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.SelectionAnchor
import com.rejowan.pdfreaderpro.presentation.screens.reader.HighlightColors

private val SWATCH_SIZE = 22.dp
private val BAR_CORNER = 14.dp

/**
 * Gap between the bottom of the selection and the bar.
 *
 * Wide enough to clear the system's selection handles, which hang below the text
 * and otherwise collide with the bar.
 */
private val ANCHOR_GAP = 30.dp

/**
 * The app's own action bar for a text selection, sitting below the selection.
 *
 * The system selection menu keeps Copy, Translate and the rest and stays where it
 * is, above the selection. This adds the app's own actions underneath, so
 * highlighting is one tap rather than buried in the system menu's overflow.
 *
 * Colours are shown inline rather than behind a "Highlight" button, so creating a
 * highlight is a single tap. The separate colour picker is still used for editing an
 * existing highlight, where the delete action also lives.
 *
 * @param anchor Selection bounds in CSS pixels relative to the viewer's top-left,
 * which map 1:1 to dp for a full-width WebView.
 * @param viewerHeight Height of the viewer in dp, used to flip the bar above the
 * selection when there is no room below.
 */
@Composable
fun SelectionActionBar(
    isVisible: Boolean,
    anchor: SelectionAnchor?,
    viewerWidthDp: Float,
    viewerHeightDp: Float,
    onHighlight: (Int) -> Unit,
    modifier: Modifier = Modifier,
    /** Ticked swatch when editing an existing highlight, null when creating one. */
    selectedColor: Int? = null,
    /** Shown only when editing an existing highlight. */
    onDelete: (() -> Unit)? = null
) {
    if (anchor == null) return

    val density = LocalDensity.current
    var barSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    val barWidthDp = with(density) { barSize.width.toDp().value }
    val barHeightDp = with(density) { barSize.height.toDp().value }

    // Centre on the selection, then keep the whole bar on screen.
    val desiredX = anchor.x + anchor.w / 2f - barWidthDp / 2f
    val clampedX = desiredX.coerceIn(8f, (viewerWidthDp - barWidthDp - 8f).coerceAtLeast(8f))

    // Below the selection, unless that would run off the bottom, in which case above.
    val belowY = anchor.y + anchor.h + ANCHOR_GAP.value
    val fitsBelow = belowY + barHeightDp <= viewerHeightDp - 8f
    val resolvedY = if (fitsBelow) belowY else (anchor.y - barHeightDp - ANCHOR_GAP.value)
    val clampedY = resolvedY.coerceIn(8f, (viewerHeightDp - barHeightDp - 8f).coerceAtLeast(8f))

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier.offset {
            IntOffset(
                x = with(density) { clampedX.dp.roundToPx() },
                y = with(density) { clampedY.dp.roundToPx() }
            )
        }
    ) {
        Surface(
            shape = RoundedCornerShape(BAR_CORNER),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.onSizeChanged { barSize = it }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FormatColorFill,
                    contentDescription = stringResource(R.string.highlight),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(SWATCH_SIZE)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                HighlightColors.ALL.forEach { color ->
                    val description = stringResource(colorNameFor(color))
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(SWATCH_SIZE)
                            .clip(CircleShape)
                            .background(Color(color.asOpaqueSwatch()))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape
                            )
                            .clickable { onHighlight(color) }
                            .semantics {
                                contentDescription = description
                                selected = isSelected
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                // The palette is light by design, so a dark tick
                                // stays legible on every swatch.
                                tint = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(SWATCH_SIZE)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Box(
                        modifier = Modifier
                            .size(SWATCH_SIZE)
                            .clip(CircleShape)
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_highlight),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

internal fun colorNameFor(color: Int): Int = when (color) {
    HighlightColors.YELLOW -> R.string.highlight_color_yellow
    HighlightColors.GREEN -> R.string.highlight_color_green
    HighlightColors.BLUE -> R.string.highlight_color_blue
    HighlightColors.PINK -> R.string.highlight_color_pink
    HighlightColors.RED -> R.string.highlight_color_red
    else -> R.string.highlight_color
}
