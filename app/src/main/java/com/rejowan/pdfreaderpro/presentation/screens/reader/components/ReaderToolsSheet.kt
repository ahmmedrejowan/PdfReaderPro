package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.presentation.screens.tools.PdfTool
import com.rejowan.pdfreaderpro.presentation.screens.tools.ToolCategory
import com.rejowan.pdfreaderpro.presentation.screens.tools.pdfTools

/**
 * The PDF tools that can work on the open document, from the reader's
 * More Options. Picking one opens that tool with this file already chosen.
 *
 * @param onToolClick Called with the tool's id, as used on the Tools screen.
 */
@Composable
fun ReaderToolsSheet(
    isPasswordProtected: Boolean,
    onToolClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Signing has its own place in the reader, and Images to PDF does not take
    // a PDF. A protected file can be unlocked but not locked again, and the
    // other way round for an open one.
    val tools = pdfTools.filter { tool ->
        tool.isEnabled && when (tool.id) {
            "sign_pdf", "img_to_pdf" -> false
            "lock_pdf" -> !isPasswordProtected
            "unlock_pdf" -> isPasswordProtected
            else -> true
        }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ToolsSideSheet(tools, onToolClick, onDismiss)
    } else {
        ToolsBottomSheet(tools, onToolClick, onDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolsBottomSheet(
    tools: List<PdfTool>,
    onToolClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        ToolsSheetContent(
            tools = tools,
            onToolClick = onToolClick,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun ToolsSideSheet(
    tools: List<PdfTool>,
    onToolClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Back closes the panel the same way tapping outside it does.
    BackHandler { onDismiss() }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

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
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    }
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
                    .width(320.dp),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                ToolsSheetContent(
                    tools = tools,
                    onToolClick = onToolClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = systemBarsPadding.calculateTopPadding(),
                            bottom = systemBarsPadding.calculateBottomPadding()
                        )
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun ToolsSheetContent(
    tools: List<PdfTool>,
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ToolsSheetHeader()

        ToolCategory.entries.forEach { category ->
            val inCategory = tools.filter { it.category == category }
            if (inCategory.isEmpty()) return@forEach

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(category.titleRes),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            )
            inCategory.forEach { tool ->
                Spacer(modifier = Modifier.height(6.dp))
                ToolRow(
                    tool = tool,
                    accentColor = category.accentColor,
                    onClick = { onToolClick(tool.id) }
                )
            }
        }
    }
}

@Composable
private fun ToolsSheetHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Build,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = stringResource(R.string.pdf_tools),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.reader_pdf_tools_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ToolRow(
    tool: PdfTool,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(7.dp)
                        .size(18.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(tool.nameRes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(tool.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
