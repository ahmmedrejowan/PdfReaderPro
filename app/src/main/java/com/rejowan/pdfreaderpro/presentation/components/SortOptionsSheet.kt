package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.SortOption
import kotlinx.coroutines.delay

// Soft accent colors matching StatsSheet
private val SoftPurple = Color(0xFF9575CD)
private val SoftBlue = Color(0xFF64B5F6)
private val SoftTeal = Color(0xFF4DB6AC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOptionsSheet(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            SortSheetHeader()

            Spacer(modifier = Modifier.height(20.dp))

            // Sort Categories
            SortCategoryCard(
                title = "Alphabetical",
                description = "Sort by file name",
                icon = Icons.Outlined.SortByAlpha,
                accentColor = SoftPurple,
                options = listOf(SortOption.NAME_ASC, SortOption.NAME_DESC),
                currentSort = currentSort,
                onSortSelected = {
                    onSortSelected(it)
                    onDismiss()
                },
                animationDelay = 0
            )

            Spacer(modifier = Modifier.height(12.dp))

            SortCategoryCard(
                title = "Date Modified",
                description = "Sort by last modified time",
                icon = Icons.Outlined.AccessTime,
                accentColor = SoftBlue,
                options = listOf(SortOption.DATE_DESC, SortOption.DATE_ASC),
                currentSort = currentSort,
                onSortSelected = {
                    onSortSelected(it)
                    onDismiss()
                },
                animationDelay = 50
            )

            Spacer(modifier = Modifier.height(12.dp))

            SortCategoryCard(
                title = "File Size",
                description = "Sort by storage size",
                icon = Icons.Outlined.DataUsage,
                accentColor = SoftTeal,
                options = listOf(SortOption.SIZE_DESC, SortOption.SIZE_ASC),
                currentSort = currentSort,
                onSortSelected = {
                    onSortSelected(it)
                    onDismiss()
                },
                animationDelay = 100
            )
        }
    }
}

@Composable
private fun SortSheetHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Sort,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "Sort Files",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose how to organize your PDFs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SortCategoryCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    options: List<SortOption>,
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val hasSelection = options.contains(currentSort)

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "card scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "card alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = if (hasSelection) {
            accentColor.copy(alpha = 0.06f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp),
                        tint = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // Selection indicator
                if (hasSelection) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Selected",
                            modifier = Modifier
                                .padding(4.dp)
                                .size(14.dp),
                            tint = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sort options row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    SortOptionChip(
                        option = option,
                        isSelected = option == currentSort,
                        accentColor = accentColor,
                        onClick = { onSortSelected(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortOptionChip(
    option: SortOption,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(200),
        label = "chip bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.4f)
        else Color.Transparent,
        animationSpec = tween(200),
        label = "chip border"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "chip content"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.98f,
        animationSpec = tween(150),
        label = "chip scale"
    )

    val (chipIcon, chipLabel) = getOptionDetails(option)

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Direction icon
            Icon(
                imageVector = chipIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Label
            Text(
                text = chipLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )

            // Selected check
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(200)) + expandVertically(),
                exit = fadeOut(tween(150)) + shrinkVertically()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(10.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun getOptionDetails(option: SortOption): Pair<ImageVector, String> {
    return when (option) {
        SortOption.NAME_ASC -> Icons.Rounded.ArrowUpward to "A → Z"
        SortOption.NAME_DESC -> Icons.Rounded.ArrowDownward to "Z → A"
        SortOption.DATE_DESC -> Icons.Rounded.ArrowDownward to "Newest"
        SortOption.DATE_ASC -> Icons.Rounded.ArrowUpward to "Oldest"
        SortOption.SIZE_DESC -> Icons.Rounded.ArrowDownward to "Largest"
        SortOption.SIZE_ASC -> Icons.Rounded.ArrowUpward to "Smallest"
    }
}
